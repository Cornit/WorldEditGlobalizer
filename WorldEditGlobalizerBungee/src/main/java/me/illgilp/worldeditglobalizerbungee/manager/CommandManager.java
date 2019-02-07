package me.illgilp.worldeditglobalizerbungee.manager;

import com.sk89q.intake.*;
import com.sk89q.intake.argument.Namespace;
import com.sk89q.intake.dispatcher.Dispatcher;
import com.sk89q.intake.fluent.CommandGraph;
import com.sk89q.intake.fluent.DispatcherNode;
import com.sk89q.intake.parametric.Injector;
import com.sk89q.intake.parametric.ParametricBuilder;
import com.sk89q.intake.parametric.provider.PrimitivesModule;
import com.sk89q.intake.util.auth.AuthorizationException;
import me.illgilp.worldeditglobalizerbungee.intake.parametric.WEGAuthorizer;
import me.illgilp.worldeditglobalizerbungee.intake.parametric.module.WEGModule;
import me.illgilp.worldeditglobalizerbungee.util.ComponentUtils;
import net.md_5.bungee.api.CommandSender;

import java.util.*;

public class CommandManager {

    private static CommandManager instance;

    public static CommandManager getInstance() {
        if (instance == null) instance = new CommandManager();
        return instance;
    }

    private Dispatcher dispatcher;

    private Map<String, Dispatcher> subCommandDispatchers = new HashMap<>();

    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }


    public void removeCommand(CommandCallable cc) {
        Injector injector = Intake.createInjector();
        injector.install(new PrimitivesModule());
        injector.install(new WEGModule());

        ParametricBuilder builder = new ParametricBuilder(injector);
        builder.setAuthorizer(new WEGAuthorizer());


        DispatcherNode dn = new CommandGraph().builder(builder).commands();
        if (dispatcher != null) {
            for (CommandMapping cm : getDispatcher().getCommands()) {
                if (cm.getCallable().equals(cc)) {

                } else {
                    dn.register(cm.getCallable(), cm.getAllAliases());
                }
            }
        }
        this.dispatcher = dn.graph().getDispatcher();
    }

    public void removeSubCommand(String command, CommandCallable cc) {
        Dispatcher dispatcher = subCommandDispatchers.get(command);
        Injector injector = Intake.createInjector();
        injector.install(new PrimitivesModule());
        injector.install(new WEGModule());

        ParametricBuilder builder = new ParametricBuilder(injector);
        builder.setAuthorizer(new WEGAuthorizer());


        DispatcherNode dn = new CommandGraph().builder(builder).commands();
        if (dispatcher != null) {
            for (CommandMapping cm : dispatcher.getCommands()) {
                if (cm.getCallable().equals(cc)) {

                } else {
                    dn.register(cm.getCallable(), cm.getAllAliases());
                }
            }
        }
        dispatcher = dn.graph().getDispatcher();
        subCommandDispatchers.put(command, dispatcher);
    }

    public void addCommand(Object obj) {
        Injector injector = Intake.createInjector();
        injector.install(new PrimitivesModule());
        injector.install(new WEGModule());

        ParametricBuilder builder = new ParametricBuilder(injector);
        builder.setAuthorizer(new WEGAuthorizer());


        DispatcherNode dn = new CommandGraph().builder(builder).commands();
        if (dispatcher != null) {
            for (CommandMapping cm : getDispatcher().getCommands()) {

                dn.register(cm.getCallable(), cm.getAllAliases());
            }
        }
        dn.registerMethods(obj);
        this.dispatcher = dn.graph().getDispatcher();


    }

    public void addSubCommand(String command, Object obj) {
        Dispatcher dispatcher = subCommandDispatchers.get(command);
        Injector injector = Intake.createInjector();
        injector.install(new PrimitivesModule());
        injector.install(new WEGModule());

        ParametricBuilder builder = new ParametricBuilder(injector);
        builder.setAuthorizer(new WEGAuthorizer());


        DispatcherNode dn = new CommandGraph().builder(builder).commands();
        if (dispatcher != null) {
            for (CommandMapping cm : dispatcher.getCommands()) {

                dn.register(cm.getCallable(), cm.getAllAliases());
            }
        }
        dn.registerMethods(obj);
        dispatcher = dn.graph().getDispatcher();
        subCommandDispatchers.put(command, dispatcher);
    }

    public void handleCommand(String command, CommandSender sender) {
        try {
            Namespace namespace = new Namespace();
            namespace.put(CommandSender.class, sender);
            this.dispatcher.call(command, namespace, Collections.emptyList());
        } catch (InvalidUsageException e) {
            if (!e.getCommand().getDescription().getUsage().isEmpty()) {
                handleCommand("help", sender);
            } else {
                MessageManager.sendMessage(sender, "command.usage-message", "/weg " + e.getCommand().getDescription().getHelp());
            }

        } catch (CommandException | InvocationCommandException e) {
            sender.sendMessage(ComponentUtils.addText(null, MessageManager.getInstance().getPrefix() + "§cAn error occurred while processing this command! Please inform an admin!"));
            e.printStackTrace();
        } catch (AuthorizationException e) {
            MessageManager.sendMessage(sender, "command.permissionDenied");
        }

    }

    public void handleSubCommand(String commandprefix, String command, String commandline, CommandSender sender) {
        Dispatcher dispatcher = subCommandDispatchers.get(command);
        if (commandline == null) commandline = "help";
        try {
            Namespace namespace = new Namespace();
            namespace.put(CommandSender.class, sender);
            dispatcher.call(commandline, namespace, Collections.emptyList());
        } catch (InvalidUsageException e) {
            if (!e.getCommand().getDescription().getUsage().isEmpty()) {
                handleSubCommand(commandprefix, command, "help", sender);
            } else {
                MessageManager.sendMessage(sender, "command.usage-message", "/" + commandprefix + " " + e.getCommand().getDescription().getHelp());
            }

        } catch (CommandException | InvocationCommandException e) {
            sender.sendMessage(ComponentUtils.addText(null, MessageManager.getInstance().getPrefix() + "§cAn error occurred while processing this command! Please inform an admin!"));
            e.printStackTrace();
        } catch (AuthorizationException e) {
            MessageManager.sendMessage(sender, "command.permissionDenied");
        }

    }

    public List<CommandMapping> getCommands() {
        return new ArrayList<>(getDispatcher().getCommands());
    }

    public List<CommandMapping> getSubCommands(String command) {
        Dispatcher dispatcher = subCommandDispatchers.get(command);
        if (dispatcher == null) return new ArrayList<>();
        return new ArrayList<>(dispatcher.getCommands());
    }


    public Dispatcher getDispatcher() {
        return this.dispatcher;
    }


}
