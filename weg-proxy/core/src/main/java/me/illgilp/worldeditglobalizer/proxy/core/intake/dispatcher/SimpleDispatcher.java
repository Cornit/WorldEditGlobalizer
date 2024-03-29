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

package me.illgilp.worldeditglobalizer.proxy.core.intake.dispatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import me.illgilp.worldeditglobalizer.proxy.core.intake.CommandCallable;
import me.illgilp.worldeditglobalizer.proxy.core.intake.CommandException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.CommandMapping;
import me.illgilp.worldeditglobalizer.proxy.core.intake.Description;
import me.illgilp.worldeditglobalizer.proxy.core.intake.ImmutableCommandMapping;
import me.illgilp.worldeditglobalizer.proxy.core.intake.ImmutableDescription;
import me.illgilp.worldeditglobalizer.proxy.core.intake.ImmutableParameter;
import me.illgilp.worldeditglobalizer.proxy.core.intake.InvalidUsageException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.InvocationCommandException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.OptionType;
import me.illgilp.worldeditglobalizer.proxy.core.intake.Parameter;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.CommandContext;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.Namespace;
import me.illgilp.worldeditglobalizer.proxy.core.intake.util.auth.AuthorizationException;
import net.kyori.adventure.text.Component;

/**
 * A simple implementation of {@link Dispatcher}.
 */
public class SimpleDispatcher implements Dispatcher {

    private final Map<String, CommandMapping> commands = new HashMap<String, CommandMapping>();
    private final Description description;

    /**
     * Create a new instance.
     */
    public SimpleDispatcher() {
        this(null);
    }

    public SimpleDispatcher(Description description) {
        List<Parameter> parameters = new ArrayList<>();

        parameters.add(
            new ImmutableParameter.Builder()
                .setName(MessageHelper.builder().component(Component.text("subcommand")))
                .setOptionType(OptionType.positional())
                .build());

        parameters.add(
            new ImmutableParameter.Builder()
                .setName(MessageHelper.builder().component(Component.text("....")))
                .setOptionType(OptionType.optionalPositional())
                .build());

        final ImmutableDescription.Builder builder = new ImmutableDescription.Builder()
            .setParameters(Optional.ofNullable(description).map(Description::getParameters).orElse(parameters));
        Optional.ofNullable(description).map(Description::getHelp).ifPresent(builder::setHelp);
        Optional.ofNullable(description).map(Description::getShortDescription).ifPresent(builder::setShortDescription);
        Optional.ofNullable(description).map(Description::getPermissions).ifPresent(builder::setPermissions);
        this.description = builder.build();
    }

    @Override
    public void registerCommand(CommandCallable callable, String... alias) {
        CommandMapping mapping = new ImmutableCommandMapping(callable, alias);

        // Check for replacements
        for (String a : alias) {
            String lower = a.toLowerCase();
            if (commands.containsKey(lower)) {
                throw new IllegalArgumentException(
                    "Can't add the command '" + a + "' because SimpleDispatcher does not support replacing commands");
            }
        }

        for (String a : alias) {
            String lower = a.toLowerCase();
            commands.put(lower, mapping);
        }
    }

    @Override
    public Set<CommandMapping> getCommands() {
        return Collections.unmodifiableSet(new HashSet<CommandMapping>(commands.values()));
    }

    @Override
    public Set<String> getAliases() {
        return Collections.unmodifiableSet(commands.keySet());
    }

    @Override
    public Set<String> getPrimaryAliases() {
        Set<String> aliases = new HashSet<String>();
        for (CommandMapping mapping : getCommands()) {
            aliases.add(mapping.getPrimaryAlias());
        }
        return Collections.unmodifiableSet(aliases);
    }

    @Override
    public boolean contains(String alias) {
        return commands.containsKey(alias.toLowerCase());
    }

    @Override
    public CommandMapping get(String alias) {
        return commands.get(alias.toLowerCase());
    }

    @Override
    public boolean call(String arguments, Namespace namespace, List<String> parentCommands) throws CommandException, InvocationCommandException, AuthorizationException {
        // We have permission for this command if we have permissions for subcommands
        if (!testPermission(namespace, false)) {
            throw new AuthorizationException();
        }

        String[] split = CommandContext.split(arguments);
        Set<String> aliases = getPrimaryAliases();

        if (aliases.isEmpty()) {
            throw new InvalidUsageException(
                MessageHelper.builder()
                    .translation(TranslationKey.COMMAND_ERROR_NO_SUB_COMMANDS)
                , this, parentCommands);
        } else if (split.length > 0) {
            String subCommand = split[0];
            String subArguments = String.join(" ", Arrays.copyOfRange(split, 1, split.length));
            List<String> subParents = new ArrayList<>(parentCommands);
            subParents.add(subCommand);
            subParents = Collections.unmodifiableList(subParents);
            CommandMapping mapping = get(subCommand);

            if (mapping != null) {
                try {
                    mapping.getCallable().call(subArguments, namespace, subParents);
                } catch (AuthorizationException e) {
                    throw e;
                } catch (CommandException e) {
                    throw e;
                } catch (InvocationCommandException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new InvocationCommandException(t);
                }

                return true;
            }

        }

        throw new InvalidUsageException(
            MessageHelper.builder()
                .translation(TranslationKey.COMMAND_ERROR_CHOOSE_SUB_COMMAND)
            , this, parentCommands, true);
    }

    @Override
    public List<String> getSuggestions(String arguments, Namespace locals) throws CommandException {
        String[] split = CommandContext.split(arguments);

        if (split.length <= 1) {
            String prefix = split.length > 0 ? split[0] : "";

            List<String> suggestions = new ArrayList<String>();

            for (CommandMapping mapping : getCommands()) {
                if (mapping.getCallable().testPermission(locals, true)) {
                    for (String alias : mapping.getAllAliases()) {
                        if (prefix.isEmpty() || alias.startsWith(arguments)) {
                            suggestions.add(mapping.getPrimaryAlias());
                            break;
                        }
                    }
                }
            }

            return suggestions;
        } else {
            String subCommand = split[0];
            CommandMapping mapping = get(subCommand);
            String passedArguments = String.join(" ", Arrays.copyOfRange(split, 1, split.length));
            if (mapping != null) {
                return mapping.getCallable().getSuggestions(passedArguments, locals);
            } else {
                return Collections.emptyList();
            }
        }
    }

    @Override
    public Description getDescription() {
        return description;
    }

    @Override
    public boolean testPermission(Namespace locals, boolean allowCached) {
        for (CommandMapping mapping : getCommands()) {
            if (mapping.getCallable().testPermission(locals, allowCached)) {
                return true;
            }
        }

        return false;
    }

}
