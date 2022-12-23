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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import me.illgilp.worldeditglobalizer.proxy.core.intake.Require;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.ArgumentException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.CommandArgs;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.MissingArgumentException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.Namespace;
import me.illgilp.worldeditglobalizer.proxy.core.intake.util.auth.AuthorizationException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.util.auth.Authorizer;
import org.jetbrains.annotations.Nullable;

/**
 * An object that provides instances given a key and some arguments.
 *
 * <p>Providers do the heavy work of reading passed in arguments and
 * transforming them into Java objects.</p>
 */
public interface Provider<T> {

    /**
     * Gets whether this provider does not actually consume values
     * from the argument stack and instead generates them otherwise.
     *
     * @return Whether values are provided without use of the arguments
     */
    boolean isProvided();

    /**
     * Provide a value given the arguments.
     *
     * @param arguments The arguments
     * @param modifiers The modifiers on the parameter
     * @return The value provided
     * @throws ArgumentException  If there is a problem with the argument
     * @throws ProvisionException If there is a problem with the provider
     */
    @Nullable
    T get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException;

    /**
     * Get a list of suggestions for the given parameter and user arguments.
     *
     * <p>If no suggestions could be enumerated, an empty list should
     * be returned.</p>
     *
     * @param namespace The namespace used
     * @param prefix    What the user has typed so far (may be an empty string)
     * @return A list of suggestions
     */
    default List<String> getSuggestions(Namespace namespace, String prefix) {
        return getSuggestions(prefix);
    }

    /**
     * Get a list of suggestions for the given parameter and user arguments.
     *
     * <p>If no suggestions could be enumerated, an empty list should
     * be returned.</p>
     *
     * @param prefix What the user has typed so far (may be an empty string)
     * @return A list of suggestions
     */
    List<String> getSuggestions(String prefix);

    /**
     * Provide a value given the arguments.
     *
     * @param value            the value
     * @param modifiers        The modifiers on the parameter
     * @param argumentProvided if an argument was provided
     * @return Whether user need permission or not
     */
    default boolean needPermission(T value, List<? extends Annotation> modifiers, boolean argumentProvided) {
        return argumentProvided;
    }

    /**
     * Provide a value given the arguments.
     *
     * @param arguments  The arguments
     * @param modifiers  The modifiers on the parameter
     * @param authorizer The authorizer for permissions
     * @return The value provided
     * @throws ArgumentException      If there is a problem with the argument
     * @throws ProvisionException     If there is a problem with the provider
     * @throws AuthorizationException If there is a problem with the permissions
     */
    @Nullable
    default T getAuthorized(CommandArgs arguments, List<? extends Annotation> modifiers, Authorizer authorizer) throws ArgumentException, ProvisionException, AuthorizationException {
        ArgumentException argumentException = null;
        ProvisionException provisionException = null;
        boolean argumentProvided = true;
        T value = null;
        try {
            value = get(arguments, modifiers);
        } catch (ArgumentException e) {
            argumentException = e;
            if (e instanceof MissingArgumentException) {
                argumentProvided = false;
            }
        } catch (ProvisionException e) {
            provisionException = e;
        }
        Optional<Require> require = modifiers.stream()
            .filter(annotation -> annotation instanceof Require)
            .map(annotation -> (Require) annotation)
            .findFirst();
        if (require.isPresent()) {
            if (needPermission(value, modifiers, argumentProvided)) {
                long trues = Arrays.stream(require.get().value())
                    .map(permission -> authorizer.testPermission(arguments.getNamespace(), permission, false))
                    .filter(Boolean.TRUE::equals)
                    .count();
                if (trues == 0) {
                    throw new AuthorizationException();
                }
            }
        }
        if (argumentException != null) {
            throw argumentException;
        } else if (provisionException != null) {
            throw provisionException;
        }
        return value;
    }

}
