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

package me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.provider;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.ArgumentParseException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.Provider;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.annotation.Range;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.Nullable;

abstract class NumberProvider<T extends Number> implements Provider<T> {

    /**
     * Try to parse numeric input as either a number or a mathematical expression.
     *
     * @param input input
     * @return a number
     * @throws ArgumentParseException thrown on parse error
     */
    @Nullable
    protected static Double parseNumericInput(@Nullable String input) throws ArgumentParseException {
        if (input == null || input.equals("")) {
            return null;
        }

        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException ignored) {
            throw new ArgumentParseException(
                MessageHelper.builder()
                    .translation(TranslationKey.COMMAND_ERROR_ARGUMENT_EXPECTED_NUMBER)
                    .tagResolver(Placeholder.unparsed("value", input))
            );
        }
    }

    /**
     * Validate a number value using relevant modifiers.
     *
     * @param number    the number
     * @param modifiers the list of modifiers to scan
     * @throws ArgumentParseException on a validation error
     */
    protected static void validate(double number, List<? extends Annotation> modifiers) throws ArgumentParseException {
        for (Annotation modifier : modifiers) {
            if (modifier instanceof Range) {
                Range range = (Range) modifier;
                if (number < range.min()) {
                    throw new ArgumentParseException(
                        MessageHelper.builder()
                            .translation(TranslationKey.COMMAND_ERROR_ARGUMENT_VALIDATE_NUMBER_GREATER_EQUALS)
                            .tagResolver(Placeholder.unparsed("expected", String.valueOf(range.min())))
                            .tagResolver(Placeholder.unparsed("value", String.valueOf(number)))
                    );
                } else if (number > range.max()) {
                    throw new ArgumentParseException(
                        MessageHelper.builder()
                            .translation(TranslationKey.COMMAND_ERROR_ARGUMENT_VALIDATE_NUMBER_LESS_EQUALS)
                            .tagResolver(Placeholder.unparsed("expected", String.valueOf(range.max())))
                            .tagResolver(Placeholder.unparsed("value", String.valueOf(number)))
                    );
                }
            }
        }
    }

    /**
     * Validate a number value using relevant modifiers.
     *
     * @param number    the number
     * @param modifiers the list of modifiers to scan
     * @throws ArgumentParseException on a validation error
     */
    protected static void validate(int number, List<? extends Annotation> modifiers) throws ArgumentParseException {
        for (Annotation modifier : modifiers) {
            if (modifier instanceof Range) {
                Range range = (Range) modifier;
                if (number < range.min()) {
                    throw new ArgumentParseException(
                        MessageHelper.builder()
                            .translation(TranslationKey.COMMAND_ERROR_ARGUMENT_VALIDATE_NUMBER_GREATER_EQUALS)
                            .tagResolver(Placeholder.unparsed("expected", String.valueOf(range.min())))
                            .tagResolver(Placeholder.unparsed("value", String.valueOf(number)))
                    );
                } else if (number > range.max()) {
                    throw new ArgumentParseException(
                        MessageHelper.builder()
                            .translation(TranslationKey.COMMAND_ERROR_ARGUMENT_VALIDATE_NUMBER_LESS_EQUALS)
                            .tagResolver(Placeholder.unparsed("expected", String.valueOf(range.max())))
                            .tagResolver(Placeholder.unparsed("value", String.valueOf(number)))
                    );
                }
            }
        }
    }

    @Override
    public boolean isProvided() {
        return false;
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return Collections.emptyList();
    }

}
