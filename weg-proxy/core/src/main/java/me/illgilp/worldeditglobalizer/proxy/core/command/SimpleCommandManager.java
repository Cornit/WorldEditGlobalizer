package me.illgilp.worldeditglobalizer.proxy.core.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import me.illgilp.worldeditglobalizer.proxy.core.api.WegProxy;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.CommandManager;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.CommandSource;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.intake.module.WegModule;
import me.illgilp.worldeditglobalizer.proxy.core.intake.CommandCallable;
import me.illgilp.worldeditglobalizer.proxy.core.intake.CommandException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.CommandMapping;
import me.illgilp.worldeditglobalizer.proxy.core.intake.Description;
import me.illgilp.worldeditglobalizer.proxy.core.intake.ImmutableDescription;
import me.illgilp.worldeditglobalizer.proxy.core.intake.Intake;
import me.illgilp.worldeditglobalizer.proxy.core.intake.InvalidUsageException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.InvocationCommandException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.CommandCancelException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.Namespace;
import me.illgilp.worldeditglobalizer.proxy.core.intake.dispatcher.Dispatcher;
import me.illgilp.worldeditglobalizer.proxy.core.intake.fluent.CommandGraph;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.Injector;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.ParametricBuilder;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.provider.PrimitivesModule;
import me.illgilp.worldeditglobalizer.proxy.core.intake.util.Preconditions;
import me.illgilp.worldeditglobalizer.proxy.core.intake.util.auth.AuthorizationException;
import net.kyori.adventure.text.Component;

public class SimpleCommandManager implements CommandManager {

    private final Dispatcher dispatcher;

    private final Map<CommandCallable, CommandNode> mappings = new HashMap<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(r ->
        new Thread(r, "WorldEditGlobalizer Command Executor Thread"));

    public SimpleCommandManager() {
        Injector injector = Intake.createInjector();
        injector.install(new PrimitivesModule());
        injector.install(new WegModule());

        ParametricBuilder builder = new ParametricBuilder(injector);
        builder.setAuthorizer((namespace, permission, allowCached) ->
            Optional.ofNullable(namespace.get(CommandSource.class))
                .orElseThrow(() -> new IllegalStateException("missing command source in namespace"))
                .hasPermission(permission, allowCached));

        this.dispatcher = new CommandGraph()
            .builder(builder)
            .commands()
            .group("weg")
            .registerMethods(new WegCommands())
            .group(new ImmutableDescription.Builder()
                    .setShortDescription(TranslationKey.COMMAND_WEG_CLIPBOARD_DESCRIPTION)
                    .build(),
                "clipboard")
            .registerMethods(new ClipboardCommands())
            .parent()
            .group(new ImmutableDescription.Builder()
                    .setShortDescription(TranslationKey.COMMAND_WEG_SCHEMATIC_DESCRIPTION)
                    .build(),
                "schematic")
            .registerMethods(new SchematicCommands())
            .parent()
            .parent()
            .graph()
            .getDispatcher();

        mappings.putAll(walkCommandTree(this.dispatcher, null));
    }

    @Override
    public void handleCommandLine(CommandSource source, String commandLine) {
        this.executorService.execute(() -> {
            try {
                dispatcher.call(commandLine, createNamespace(source), Collections.emptyList());
            } catch (CommandException e) {
                if (e.getMessageBuilder() != null) {
                    MessageHelper.builder()
                        .translation(TranslationKey.PREFIX)
                        .component(Component.space())
                        .component(e.getMessageBuilder())
                        .sendMessageTo(source);
                }
                if (e instanceof InvalidUsageException) {
                    InvalidUsageException ex = (InvalidUsageException) e;
                    generateHelp(createNamespace(source), ex.getCommand())
                        .sendMessageTo(source);
                }
            } catch (AuthorizationException e) {
                MessageHelper.builder()
                    .translation(TranslationKey.COMMAND_ERROR_NO_PERMISSION)
                    .sendMessageTo(source);
            } catch (InvocationCommandException e) {
                if (e.getCause() == CommandCancelException.INSTANCE) {
                    return;
                }
                WegProxy.getInstance().getLogger().log(Level.SEVERE, "Failed to execute command", e);
                MessageHelper.builder()
                    .translation(TranslationKey.PREFIX)
                    .component(Component.space())
                    .translation(TranslationKey.COMMAND_ERROR_INTERNAL)
                    .lazyPlaceholder("error", e.getMessageBuilder())
                    .sendMessageTo(source);
            }
        });
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String commandLine) {
        try {
            return this.dispatcher.getSuggestions(commandLine, createNamespace(source)).stream().sorted().collect(Collectors.toList());
        } catch (CommandException e) {
            WegProxy.getInstance().getLogger().log(Level.SEVERE, "exception while getting suggestions for command line '" + commandLine + "' and player '" + source + "'");
        }
        return Collections.emptyList();
    }

    public boolean shutdown() {
        this.executorService.shutdown();
        try {
            return this.executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            WegProxy.getInstance().getLogger().log(Level.WARNING, "shutdown of SimpleCommandManager interrupted", e);
            return false;
        }
    }


    public List<CommandInfo> getCommands() {
        return mappings.values().stream().map(CommandInfo::new).collect(Collectors.toList());
    }

    private Namespace createNamespace(CommandSource source) {
        Namespace namespace = new Namespace();
        namespace.put(CommandSource.class, source);
        return namespace;
    }

    private MessageHelper.Builder generateHelp(Namespace namespace, CommandCallable commandCallable) {
        CommandNode node = this.mappings.get(commandCallable);
        if (node == null) {
            WegProxy.getInstance().getLogger().log(Level.SEVERE, "could not find command mapping for command callable " + commandCallable);
            return MessageHelper.builder()
                .translation(TranslationKey.PREFIX)
                .component(Component.space())
                .translation(TranslationKey.COMMAND_ERROR_INTERNAL_GENERIC);
        }
        MessageHelper.Builder builder = MessageHelper.builder()
            .translation(TranslationKey.COMMAND_FORMAT_HELP_HEADER);
        if (node.commandCallable instanceof Dispatcher) {
            for (CommandMapping command : ((Dispatcher) node.commandCallable).getCommands()) {
                CommandNode node1 = mappings.get(command.getCallable());
                if (node1 != null) {
                    if (!node1.commandCallable.testPermission(namespace, true)) {
                        continue;
                    }
                    builder
                        .component(Component.newline())
                        .component(
                            MessageHelper.builder()
                                .translation(TranslationKey.COMMAND_FORMAT_HELP_ROW)
                                .lazyPlaceholder("command_usage", node1.getUsage())
                                .lazyPlaceholder("command_description", MessageHelper.builder()
                                    .translation(
                                        Preconditions.checkNotNull(
                                            command.getDescription().getShortDescription(),
                                            "description of command '" + node1.getUsage().buildPlain() + "' is missing"
                                        )
                                    )
                                )
                        );
                }
            }
        } else {
            if (node.commandCallable.testPermission(namespace, true)) {
                builder
                    .component(Component.newline())
                    .component(
                        MessageHelper.builder()
                            .translation(TranslationKey.COMMAND_FORMAT_HELP_ROW)
                            .lazyPlaceholder("command_usage", node.getUsage())
                            .lazyPlaceholder("command_description", MessageHelper.builder()
                                .translation(
                                    Preconditions.checkNotNull(
                                        commandCallable.getDescription().getShortDescription(),
                                        "description of command '" + node.getUsage().buildPlain() + "' is missing"
                                    )
                                )
                            )
                    );
            }

        }
        return builder;
    }

    private Map<CommandCallable, CommandNode> walkCommandTree(Dispatcher dispatcher, CommandNode node) {
        Map<CommandCallable, CommandNode> map = new HashMap<>();
        for (CommandMapping command : dispatcher.getCommands()) {
            CommandNode[] newPath = new CommandNode[
                Optional.ofNullable(node)
                    .map(commandNode -> commandNode.commandPath.length)
                    .map(i -> i + 1)
                    .orElse(0)
                ];
            if (node != null) {
                System.arraycopy(node.commandPath, 0, newPath, 0, node.commandPath.length);
                newPath[newPath.length - 1] = node;
            }
            CommandNode node1;
            map.put(command.getCallable(), node1 = new CommandNode(
                command,
                command.getCallable(),
                newPath
            ));
            if (command.getCallable() instanceof Dispatcher) {
                map.putAll(walkCommandTree((Dispatcher) command.getCallable(), node1));
            }
        }
        return map;
    }

    @AllArgsConstructor
    private class CommandNode {

        private CommandMapping mapping;
        private CommandCallable commandCallable;
        private CommandNode[] commandPath;

        private MessageHelper.Builder getUsage() {
            MessageHelper.Builder builder = MessageHelper.builder();
            builder.component(Component.text("/"));
            builder.component(Component.text(Arrays.stream(commandPath)
                .map(node -> node.mapping.getPrimaryAlias())
                .collect(Collectors.joining(" "))));
            if (commandPath.length > 0) {
                builder.component(Component.space());
            }
            builder.component(Component.text(mapping.getPrimaryAlias()));
            Optional<MessageHelper.Builder> usage = mapping.getDescription().getUsage();
            usage.ifPresent(value ->
                builder
                    .component(Component.space())
                    .component(value)
            );

            return builder;
        }
    }

    @RequiredArgsConstructor
    public class CommandInfo {

        private final CommandNode commandNode;

        public CommandCallable getCommandCallable() {
            return commandNode.commandCallable;
        }

        public CommandMapping getCommandMapping() {
            return commandNode.mapping;
        }

        public Description getDescription() {
            return commandNode.mapping.getDescription();
        }

        public MessageHelper.Builder getFullUsage() {
            return commandNode.getUsage();
        }
    }
}
