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

package me.illgilp.worldeditglobalizer.proxy.core.intake.argument;

import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

abstract class AbstractCommandArgs implements CommandArgs {

    @Override
    public int nextInt() throws MissingArgumentException, ArgumentParseException {
        String next = next();
        try {
            return Integer.parseInt(next);
        } catch (NumberFormatException ignored) {
            throw new ArgumentParseException(
                MessageHelper.builder()
                    .translation(TranslationKey.COMMAND_ERROR_ARGUMENT_EXPECTED_NUMBER)
                    .tagResolver(Placeholder.unparsed("value", next))
            );
        }
    }

    @Override
    public short nextShort() throws MissingArgumentException, ArgumentParseException {
        String next = next();
        try {
            return Short.parseShort(next);
        } catch (NumberFormatException ignored) {
            throw new ArgumentParseException(
                MessageHelper.builder()
                    .translation(TranslationKey.COMMAND_ERROR_ARGUMENT_EXPECTED_NUMBER)
                    .tagResolver(Placeholder.unparsed("value", next))
            );
        }
    }

    @Override
    public byte nextByte() throws MissingArgumentException, ArgumentParseException {
        String next = next();
        try {
            return Byte.parseByte(next);
        } catch (NumberFormatException ignored) {
            throw new ArgumentParseException(
                MessageHelper.builder()
                    .translation(TranslationKey.COMMAND_ERROR_ARGUMENT_EXPECTED_NUMBER)
                    .tagResolver(Placeholder.unparsed("value", next))
            );
        }
    }

    @Override
    public double nextDouble() throws MissingArgumentException, ArgumentParseException {
        String next = next();
        try {
            return Double.parseDouble(next);
        } catch (NumberFormatException ignored) {
            throw new ArgumentParseException(
                MessageHelper.builder()
                    .translation(TranslationKey.COMMAND_ERROR_ARGUMENT_EXPECTED_NUMBER)
                    .tagResolver(Placeholder.unparsed("value", next))
            );
        }
    }

    @Override
    public float nextFloat() throws MissingArgumentException, ArgumentParseException {
        String next = next();
        try {
            return Float.parseFloat(next);
        } catch (NumberFormatException ignored) {
            throw new ArgumentParseException(
                MessageHelper.builder()
                    .translation(TranslationKey.COMMAND_ERROR_ARGUMENT_EXPECTED_NUMBER)
                    .tagResolver(Placeholder.unparsed("value", next))
            );
        }
    }

    @Override
    public boolean nextBoolean() throws MissingArgumentException, ArgumentParseException {
        String next = next();
        if (next.equalsIgnoreCase("yes") || next.equalsIgnoreCase("true") || next.equalsIgnoreCase("y") || next.equalsIgnoreCase("1")) {
            return true;
        } else if (next.equalsIgnoreCase("no") || next.equalsIgnoreCase("false") || next.equalsIgnoreCase("n") || next.equalsIgnoreCase("0")) {
            return false;
        } else {
            throw new ArgumentParseException(
                MessageHelper.builder()
                    .translation(TranslationKey.COMMAND_ERROR_ARGUMENT_EXPECTED_BOOLEAN)
                    .tagResolver(Placeholder.unparsed("value", next))
            );
        }
    }

}
