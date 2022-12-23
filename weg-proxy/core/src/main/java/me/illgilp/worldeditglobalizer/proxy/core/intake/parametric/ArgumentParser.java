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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import me.illgilp.worldeditglobalizer.common.permission.Permission;
import me.illgilp.worldeditglobalizer.proxy.core.intake.ImmutableParameter;
import me.illgilp.worldeditglobalizer.proxy.core.intake.OptionType;
import me.illgilp.worldeditglobalizer.proxy.core.intake.Parameter;
import me.illgilp.worldeditglobalizer.proxy.core.intake.Require;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.ArgumentException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.ArgumentParseException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.Arguments;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.CommandArgs;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.MissingArgumentException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.Namespace;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.UnusedArgumentException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.annotation.Classifier;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.annotation.Described;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.annotation.OptionalArg;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.annotation.Switch;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.annotation.Translated;
import me.illgilp.worldeditglobalizer.proxy.core.intake.util.auth.AuthorizationException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.util.auth.Authorizer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

/**
 * An argument parser takes in a list of tokenized arguments and parses
 * them, converting them into appropriate Java objects using a provided
 * {@link Injector}.
 */
public final class ArgumentParser {

    private final List<ParameterEntry> parameters;
    private final List<Parameter> userParams;
    private final Set<Character> valueFlags;

    private ArgumentParser(List<ParameterEntry> parameters, List<Parameter> userParams, Set<Character> valueFlags) {
        this.parameters = Collections.unmodifiableList(new ArrayList<>(parameters));
        this.userParams = Collections.unmodifiableList(new ArrayList<>(userParams));
        this.valueFlags = Collections.unmodifiableSet(new HashSet<>(valueFlags));
    }

    /**
     * Get a list of parameters that are user-provided and not provided.
     *
     * @return A list of user parameters
     */
    public List<Parameter> getUserParameters() {
        return userParams;
    }

    /**
     * Get a list of value flags that have been requested by the parameters.
     *
     * @return A list of value flags
     */
    public Set<Character> getValueFlags() {
        return valueFlags;
    }


    /**
     * Parse the given arguments into Java objects.
     *
     * @param args              The tokenized arguments
     * @param ignoreUnusedFlags Whether unused flags should not throw an exception
     * @param unusedFlags       List of flags that can be unconsumed
     * @param authorizer        Authorizer to check permissions
     * @return The list of Java objects
     * @throws ArgumentException      If there is a problem with the provided arguments
     * @throws ProvisionException     If there is a problem with the binding itself
     * @throws AuthorizationException If there is a problem with the permissions
     */
    public Object[] parseArguments(CommandArgs args, boolean ignoreUnusedFlags, Set<Character> unusedFlags, Authorizer authorizer) throws ArgumentException, ProvisionException, AuthorizationException {
        Object[] parsedObjects = new Object[parameters.size()];

        for (int i = 0; i < parameters.size(); i++) {
            ParameterEntry entry = parameters.get(i);
            OptionType optionType = entry.getParameter().getOptionType();
            CommandArgs argsForParameter = optionType.transform(args);

            try {
                parsedObjects[i] = entry.getBinding().getProvider().getAuthorized(argsForParameter, entry.getModifiers(), authorizer);
            } catch (ArgumentParseException e) {
                throw new ArgumentParseException(e.getMessageBuilder(), e, entry.getParameter());
            } catch (MissingArgumentException e) {
                if (!optionType.isOptional()) {
                    throw new MissingArgumentException(e, entry.getParameter());
                }

                parsedObjects[i] = getDefaultValue(entry, args);
            }
        }

        // Check for unused arguments
        checkUnconsumed(args, ignoreUnusedFlags, unusedFlags);

        return parsedObjects;
    }

    private Object getDefaultValue(ParameterEntry entry, CommandArgs arguments) {
        Provider<?> provider = entry.getBinding().getProvider();

        List<String> defaultValue = entry.getParameter().getDefaultValue();
        if (defaultValue.isEmpty()) {
            return null;
        } else {
            try {
                return provider.get(Arguments.copyOf(defaultValue, arguments.getFlags(), arguments.getNamespace()), entry.getModifiers());
            } catch (ArgumentException | ProvisionException e) {
                throw new IllegalParameterException(
                    MessageHelper.builder()
                        .translation(TranslationKey.COMMAND_ERROR_PARAMETER_DEFAULT_VALUE)
                        .lazyPlaceholder("parameter_name", entry.getParameter().getName())
                        .tagResolver(Placeholder.unparsed("default_value", String.join(" ", defaultValue)))
                        .lazyPlaceholder("error", e.getMessageBuilder())
                );
            }
        }
    }

    private void checkUnconsumed(CommandArgs arguments, boolean ignoreUnusedFlags, Set<Character> unusedFlags) throws UnusedArgumentException {
        List<String> unconsumedArguments = new ArrayList<>();

        if (!ignoreUnusedFlags) {
            Set<Character> unconsumedFlags = null;

            for (char flag : arguments.getFlags().keySet()) {
                boolean found = false;

                if (unusedFlags.contains(flag)) {
                    break;
                }

                for (ParameterEntry parameter : parameters) {
                    Character paramFlag = parameter.getParameter().getOptionType().getFlag();
                    if (paramFlag != null && flag == paramFlag) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    if (unconsumedFlags == null) {
                        unconsumedFlags = new HashSet<Character>();
                    }
                    unconsumedFlags.add(flag);
                }
            }

            if (unconsumedFlags != null) {
                for (Character flag : unconsumedFlags) {
                    unconsumedArguments.add("-" + flag);
                }
            }
        }

        while (true) {
            try {
                unconsumedArguments.add(arguments.next());
            } catch (MissingArgumentException ignored) {
                break;
            }
        }

        if (!unconsumedArguments.isEmpty()) {
            throw new UnusedArgumentException(String.join(" ", unconsumedArguments));
        }
    }


    public List<String> getSuggestions(Namespace namespace, String[] args) {
        String arg = args[args.length - 1];
        List<ParameterEntry> entries = parameters.stream().filter(e -> {
            if (e.binding == null) {
                return false;
            }
            if (e.binding.getProvider() == null) {
                return false;
            }
            return !e.binding.getProvider().isProvided();
        }).collect(Collectors.toList());
        if (entries.size() > (args.length - 1)) {
            ParameterEntry entry = entries.get(args.length - 1);
            return entry.getBinding().getProvider().getSuggestions(namespace, arg);
        }
        return Collections.emptyList();
    }

    /**
     * Builds instances of ArgumentParser.
     */
    public static class Builder {
        private final Injector injector;
        private final List<ParameterEntry> parameters = new ArrayList<>();
        private final List<Parameter> userProvidedParameters = new ArrayList<>();
        private final Set<Character> valueFlags = new HashSet<>();
        private boolean seenOptionalParameter = false;

        /**
         * Create a new instance.
         *
         * @param injector The injector
         */
        public Builder(Injector injector) {
            checkNotNull(injector, "injector");
            this.injector = injector;
        }

        private static String getFriendlyName(java.lang.reflect.Parameter parameter, Type type, Annotation classifier, int index) {
            if (parameter != null) {
                return parameter.getName();
            } else if (classifier != null) {
                return classifier.annotationType().getSimpleName().toLowerCase();
            } else {
                return type instanceof Class<?> ? ((Class<?>) type).getSimpleName().toLowerCase() : "unknown" + index;
            }
        }

        /**
         * Add a parameter to parse.
         *
         * @param type        The type of the parameter
         * @param annotations A list of annotations on the parameter
         * @throws IllegalParameterException If there is a problem with the parameter
         */
        public void addParameter(java.lang.reflect.Parameter reflectionsParameter, Type type, List<? extends Annotation> annotations) throws IllegalParameterException {
            checkNotNull(type, "type");
            checkNotNull(annotations, "annotations");

            int index = parameters.size();
            OptionType optionType = null;
            List<String> defaultValue = Collections.emptyList();
            Annotation classifier = null;
            List<Annotation> modifiers = new ArrayList<>();

            Optional<TranslationKey> translation = Optional.empty();
            Optional<TranslationKey> description = Optional.empty();

            List<Permission> permissions = new ArrayList<>();

            for (Annotation annotation : annotations) {
                if (annotation.annotationType().getAnnotation(Classifier.class) != null) {
                    classifier = annotation;
                } else {
                    modifiers.add(annotation);
                    if (annotation instanceof Translated) {
                        translation = Optional.of(((Translated) annotation).value());
                    } else if (annotation instanceof Described) {
                        description = Optional.of(((Described) annotation).value());
                    } else if (annotation instanceof Require) {
                        permissions.addAll(Arrays.asList(((Require) annotation).value()));
                    } else if (annotation instanceof Switch) {
                        if (optionType != null) {
                            throw new IllegalParameterException(
                                MessageHelper.builder()
                                    .translation(TranslationKey.COMMAND_ERROR_PARAMETER_OPTIONAL_AND_SWITCH_USED)
                                    .tagResolver(Placeholder.unparsed("index", String.valueOf(index)))
                            );
                        }

                        optionType = (type == boolean.class || type == Boolean.class) ? OptionType.flag(((Switch) annotation).value()) : OptionType.valueFlag(((Switch) annotation).value());

                    } else if (annotation instanceof OptionalArg) {
                        if (optionType != null) {
                            throw new IllegalParameterException(
                                MessageHelper.builder()
                                    .translation(TranslationKey.COMMAND_ERROR_PARAMETER_OPTIONAL_AND_SWITCH_USED)
                                    .tagResolver(Placeholder.unparsed("index", String.valueOf(index)))
                            );
                        }

                        seenOptionalParameter = true;

                        optionType = OptionType.optionalPositional();

                        String[] value = ((OptionalArg) annotation).value();
                        if (value.length > 0) {
                            defaultValue = Collections.unmodifiableList(Arrays.asList(value));
                        }
                    }
                }
            }

            if (optionType == null) {
                optionType = OptionType.positional();
            }

            if (seenOptionalParameter && !optionType.isOptional()) {
                throw new IllegalParameterException(
                    MessageHelper.builder()
                        .translation(TranslationKey.COMMAND_ERROR_PARAMETER_NON_OPTIONAL_FOLLOWED_AFTER_OPTIONAL)
                        .tagResolver(Placeholder.unparsed("index", String.valueOf(index)))
                );
            }

            ImmutableParameter.Builder builder = new ImmutableParameter.Builder();
            builder.setName(
                translation.map(key ->
                    MessageHelper.builder()
                        .translation(key)
                ).orElse(
                    MessageHelper.builder()
                        .component(Component.text(getFriendlyName(reflectionsParameter, type, classifier, index)))
                )
            );
            builder.setOptionType(optionType);
            builder.setDefaultValue(defaultValue);
            builder.setDescription(description.map(MessageHelper.builder()::translation).orElse(null));
            builder.setPermissions(permissions);
            Parameter parameter = builder.build();

            Key<?> key = Key.get(type, classifier != null ? classifier.annotationType() : null);
            Binding<?> binding = injector.getBinding(key);
            if (binding == null) {
                throw new IllegalParameterException(
                    MessageHelper.builder()
                        .translation(TranslationKey.COMMAND_ERROR_PARAMETER_NO_BINDING)
                        .tagResolver(Placeholder.unparsed("type", String.valueOf(type)))
                );
            }

            ParameterEntry entry = new ParameterEntry(parameter, key, binding, modifiers);

            if (optionType.isValueFlag()) {
                valueFlags.add(optionType.getFlag());
            }
            if (!binding.getProvider().isProvided()) {
                userProvidedParameters.add(parameter);
            }

            parameters.add(entry);
        }

        /**
         * Create a new argument parser.
         *
         * @return A new argument parser
         */
        public ArgumentParser build() {
            return new ArgumentParser(parameters, userProvidedParameters, valueFlags);
        }
    }

    private static class ParameterEntry {
        private final Parameter parameter;
        private final Key<?> key;
        private final Binding<?> binding;
        private final List<Annotation> modifiers;

        ParameterEntry(Parameter parameter, Key<?> key, Binding<?> binding, List<Annotation> modifiers) {
            this.parameter = parameter;
            this.key = key;
            this.binding = binding;
            this.modifiers = modifiers;
        }

        public Parameter getParameter() {
            return parameter;
        }

        public Key<?> getKey() {
            return key;
        }

        public Binding<?> getBinding() {
            return binding;
        }

        public List<Annotation> getModifiers() {
            return modifiers;
        }
    }

}
