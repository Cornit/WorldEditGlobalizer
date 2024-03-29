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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import me.illgilp.worldeditglobalizer.common.permission.Permission;
import me.illgilp.worldeditglobalizer.proxy.core.intake.Command;
import me.illgilp.worldeditglobalizer.proxy.core.intake.CommandCallable;
import me.illgilp.worldeditglobalizer.proxy.core.intake.Description;
import me.illgilp.worldeditglobalizer.proxy.core.intake.ImmutableDescription;
import me.illgilp.worldeditglobalizer.proxy.core.intake.InvocationCommandException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.Require;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.Namespace;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.handler.InvokeListener;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

/**
 * The implementation of a {@link CommandCallable} for the
 * {@link ParametricBuilder}.
 */
final class MethodCallable extends AbstractParametricCallable {

    private final Object object;
    private final Method method;
    private final Description description;
    private final List<Permission> permissions;

    private MethodCallable(ParametricBuilder builder, ArgumentParser parser, Object object, Method method, Description description, List<Permission> permissions) {
        super(builder, parser);
        this.object = object;
        this.method = method;
        this.description = description;
        this.permissions = permissions;
    }

    static MethodCallable create(ParametricBuilder builder, Object object, Method method) throws IllegalParameterException {
        checkNotNull(builder, "builder");
        checkNotNull(object, "object");
        checkNotNull(method, "method");

        Set<Annotation> commandAnnotations = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(method.getAnnotations())));

        Command definition = method.getAnnotation(Command.class);
        checkNotNull(definition, "Method lacks a @Command annotation");

        boolean ignoreUnusedFlags = definition.anyFlags();
        Set<Character> unusedFlags = new HashSet<>();
        for (char c : definition.flags().toCharArray()) {
            unusedFlags.add(c);
        }
        unusedFlags = Collections.unmodifiableSet(unusedFlags);

        Annotation[][] annotations = method.getParameterAnnotations();
        Type[] types = method.getGenericParameterTypes();
        Parameter[] parameters = method.getParameters();

        ArgumentParser.Builder parserBuilder = new ArgumentParser.Builder(builder.getInjector());
        for (int i = 0; i < types.length; i++) {
            parserBuilder.addParameter(parameters[i], types[i], Arrays.asList(annotations[i]));
        }
        ArgumentParser parser = parserBuilder.build();

        ImmutableDescription.Builder descBuilder = new ImmutableDescription.Builder()
            .setParameters(parser.getUserParameters())
            .setShortDescription(definition.desc())
            .setHelp(!definition.help().isEmpty() ? definition.help() : null)
            .setUsageOverride(!definition.usage().isEmpty() ? definition.usage() : null);

        Require permHint = method.getAnnotation(Require.class);
        List<Permission> permissions = null;
        if (permHint != null) {
            descBuilder.setPermissions(Arrays.asList(permHint.value()));
            permissions = Arrays.asList(permHint.value());
        }

        for (InvokeListener listener : builder.getInvokeListeners()) {
            listener.updateDescription(commandAnnotations, parser, descBuilder);
        }

        Description description = descBuilder.build();

        MethodCallable callable = new MethodCallable(builder, parser, object, method, description, permissions);
        callable.setCommandAnnotations(Collections.unmodifiableList(Arrays.asList(method.getAnnotations())));
        callable.setIgnoreUnusedFlags(ignoreUnusedFlags);
        callable.setUnusedFlags(unusedFlags);
        return callable;
    }

    @Override
    protected void call(Object[] args) throws Exception {
        try {
            method.invoke(object, args);
        } catch (IllegalAccessException e) {
            throw new InvocationCommandException(
                MessageHelper.builder()
                    .translation(TranslationKey.COMMAND_ERROR_INVOKE_METHOD)
                    .tagResolver(Placeholder.unparsed("method_name", String.valueOf(method)))
                , e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            } else {
                throw new InvocationCommandException(
                    MessageHelper.builder()
                        .translation(TranslationKey.COMMAND_ERROR_INVOKE_METHOD)
                        .tagResolver(Placeholder.unparsed("method_name", String.valueOf(method)))
                    , e);
            }
        }
    }

    @Override
    public Description getDescription() {
        return description;
    }

    @Override
    public boolean testPermission(Namespace namespace, boolean allowCached) {
        if (permissions != null) {
            for (Permission perm : permissions) {
                if (getBuilder().getAuthorizer().testPermission(namespace, perm, allowCached)) {
                    return true;
                }
            }

            return false;
        } else {
            return true;
        }
    }

}
