/*
 * Intake, a command processing library
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) Intake team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.illgilp.worldeditglobalizer.proxy.core.intake.parametric;

import static me.illgilp.worldeditglobalizer.proxy.core.intake.util.Preconditions.checkNotNull;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import me.illgilp.worldeditglobalizer.proxy.core.intake.CommandCallable;
import me.illgilp.worldeditglobalizer.proxy.core.intake.CommandException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.InvalidUsageException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.InvocationCommandException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.ArgumentException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.ArgumentParseException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.Arguments;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.CommandArgs;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.CommandContext;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.MessageArgumentException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.MissingArgumentException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.Namespace;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.UnusedArgumentException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.completion.NullCompleter;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.handler.ExceptionConverter;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.handler.InvokeHandler;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.handler.InvokeListener;
import me.illgilp.worldeditglobalizer.proxy.core.intake.util.auth.AuthorizationException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

/**
 * A base class for commands that use {@link ArgumentParser}.
 */
public abstract class AbstractParametricCallable implements CommandCallable {

    private final ParametricBuilder builder;
    private final ArgumentParser parser;

    private List<? extends Annotation> commandAnnotations = Collections.emptyList();
    private boolean ignoreUnusedFlags = false;
    private Set<Character> unusedFlags = Collections.emptySet();

    /**
     * Create a new instance.
     *
     * @param builder An instance of the parametric builder
     * @param parser  The argument parser
     */
    protected AbstractParametricCallable(ParametricBuilder builder, ArgumentParser parser) {
        checkNotNull(builder, "builder");
        checkNotNull(parser, "parser");

        this.builder = builder;
        this.parser = parser;
    }

    /**
     * Get the parametric builder.
     *
     * @return The parametric builder
     */
    protected ParametricBuilder getBuilder() {
        return builder;
    }

    /**
     * Get the argument parser.
     *
     * @return The argument parser
     */
    protected ArgumentParser getParser() {
        return parser;
    }

    /**
     * Get the annotations on the command.
     *
     * @return The annotations on the command
     */
    protected List<? extends Annotation> getCommandAnnotations() {
        return commandAnnotations;
    }

    /**
     * Set the annotations on the command.
     *
     * @param commandAnnotations The annotations on the command
     */
    protected void setCommandAnnotations(List<? extends Annotation> commandAnnotations) {
        this.commandAnnotations = Collections.unmodifiableList(new ArrayList<>(commandAnnotations));
    }

    /**
     * Get whether flags that are not used by any parameter should be
     * ignored so that an {@link UnusedArgumentException} is not
     * thrown.
     *
     * @return Whether unused flags should be ignored
     */
    protected boolean isIgnoreUnusedFlags() {
        return ignoreUnusedFlags;
    }

    /**
     * Set whether flags that are not used by any parameter should be
     * ignored so that an {@link UnusedArgumentException} is not
     * thrown.
     *
     * @param ignoreUnusedFlags Whether unused flags should be ignored
     */
    protected void setIgnoreUnusedFlags(boolean ignoreUnusedFlags) {
        this.ignoreUnusedFlags = ignoreUnusedFlags;
    }

    /**
     * Get a list of flags that should not cause an
     * {@link UnusedArgumentException} to be thrown if they are
     * not consumed by a parameter.
     *
     * @return List of flags that can be unconsumed
     */
    protected Set<Character> getUnusedFlags() {
        return unusedFlags;
    }

    /**
     * Set a list of flags that should not cause an
     * {@link UnusedArgumentException} to be thrown if they are
     * not consumed by a parameter.
     *
     * @param unusedFlags List of flags that can be unconsumed
     */
    protected void setUnusedFlags(Set<Character> unusedFlags) {
        this.unusedFlags = Collections.unmodifiableSet(new HashSet<>(unusedFlags));
    }

    @Override
    public final boolean call(String stringArguments, Namespace namespace, List<String> parentCommands) throws CommandException, InvocationCommandException, AuthorizationException {
        // Test permission
        if (!testPermission(namespace, false)) {
            throw new AuthorizationException();
        }

        String calledCommand = !parentCommands.isEmpty() ? parentCommands.get(parentCommands.size() - 1) : "_";
        String[] split = CommandContext.split(calledCommand + " " + stringArguments);
        CommandContext context = new CommandContext(split, parser.getValueFlags(), false, namespace);
        final CommandArgs commandArgs = Arguments.viewOf(context);
        List<InvokeHandler> handlers = new ArrayList<InvokeHandler>();

        // Provide help if -? is specified
        if (context.hasFlag('?')) {
            throw new InvalidUsageException(null, this, parentCommands, true);
        }

        for (InvokeListener listener : builder.getInvokeListeners()) {
            InvokeHandler handler = listener.createInvokeHandler();
            handlers.add(handler);
        }

        try {
            boolean invoke = true;

            // preProcess
            for (InvokeHandler handler : handlers) {
                if (!handler.preProcess(commandAnnotations, parser, commandArgs)) {
                    invoke = false;
                }
            }

            if (!invoke) {
                return true; // Abort early
            }

            final Object[] args = parser.parseArguments(commandArgs, ignoreUnusedFlags, unusedFlags, getBuilder().getAuthorizer());

            // preInvoke
            for (InvokeHandler handler : handlers) {
                if (!handler.preInvoke(commandAnnotations, parser, args, commandArgs)) {
                    invoke = false;
                }
            }

            if (!invoke) {
                return true; // Abort early
            }

            namespace.put(CommandArgs.class, commandArgs);

            // invoke
            try {
                builder.getCommandExecutor().submit(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        AbstractParametricCallable.this.call(args);
                        return null;
                    }
                }, commandArgs).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }

            // postInvoke
            for (InvokeHandler handler : handlers) {
                handler.postInvoke(commandAnnotations, parser, args, commandArgs);
            }

        } catch (AuthorizationException e) {
            throw e;
        } catch (MissingArgumentException e) {
            if (e.getParameter() != null) {
                throw new InvalidUsageException(
                    MessageHelper.builder()
                        .translation(TranslationKey.COMMAND_ERROR_PARAMETER_TOO_FEW_ARGUMENTS)
                        .lazyPlaceholder("parameter_name", e.getParameter().getName())
                    , this, parentCommands, false, e);
            } else {
                throw new InvalidUsageException(
                    MessageHelper.builder()
                        .translation(TranslationKey.COMMAND_ERROR_TOO_FEW_ARGUMENTS)
                    , this, parentCommands, false, e);
            }

        } catch (UnusedArgumentException e) {
            throw new InvalidUsageException(
                MessageHelper.builder()
                    .translation(TranslationKey.COMMAND_ERROR_TOO_MANY_ARGUMENTS)
                    .tagResolver(Placeholder.unparsed("arguments", e.getUnconsumed()))
                , this, parentCommands, false, e);

        } catch (ArgumentParseException e) {
            if (e.getParameter() != null) {
                throw new InvalidUsageException(
                    MessageHelper.builder()
                        .translation(TranslationKey.COMMAND_ERROR_PARAMETER_PARSING)
                        .lazyPlaceholder("parameter_name", e.getParameter().getName())
                        .lazyPlaceholder("error", e.getMessageBuilder())
                    , this, parentCommands, false, e);
            } else {
                throw new InvalidUsageException(
                    MessageHelper.builder()
                        .translation(TranslationKey.COMMAND_ERROR_ARGUMENT_PARSING)
                        .lazyPlaceholder("error", e.getMessageBuilder())
                    , this, parentCommands, false, e);
            }
        } catch (MessageArgumentException e) {
            throw new CommandException(e.getMessageBuilder(), e);
        } catch (ArgumentException e) { // Something else wrong with an argument
            throw new InvalidUsageException(
                MessageHelper.builder()
                    .translation(TranslationKey.COMMAND_ERROR_ARGUMENT_PARSING)
                    .lazyPlaceholder("error", e.getMessageBuilder())
                , this, parentCommands, false, e);

        } catch (CommandException e) { // Thrown by commands
            throw e;

        } catch (ProvisionException e) { // Argument binding failed
            throw new InvocationCommandException(
                MessageHelper.builder()
                    .translation(TranslationKey.COMMAND_ERROR_INTERNAL)
                    .lazyPlaceholder("error", e.getMessageBuilder())
                , e);

        } catch (InterruptedException e) { // Thrown by execution
            throw new InvocationCommandException(
                MessageHelper.builder()
                    .translation(TranslationKey.COMMAND_ERROR_INTERRUPTED)
                , e.getCause());

        } catch (Throwable e) { // Catch all
            for (ExceptionConverter converter : builder.getExceptionConverters()) {
                converter.convert(e);
            }

            throw new InvocationCommandException(
                MessageHelper.builder()
                    .component(Component.text(Optional.ofNullable(e.getMessage()).orElse("")))
                , e);
        }

        return true;
    }

    /**
     * Called with parsed arguments to execute the command.
     *
     * @param args The arguments parsed into the appropriate Java objects
     * @throws Exception on any exception
     */
    protected abstract void call(Object[] args) throws Exception;

    @Override
    public List<String> getSuggestions(String arguments, Namespace locals) throws CommandException {
        if (builder.getDefaultCompleter() instanceof NullCompleter) {
            return parser.getSuggestions(locals, CommandContext.split(arguments));
        }
        return builder.getDefaultCompleter().getSuggestions(arguments, locals);
    }

}
