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

package me.illgilp.intake.completion;

import java.util.Collections;
import java.util.List;
import me.illgilp.intake.CommandException;
import me.illgilp.intake.argument.Namespace;

/**
 * Always returns an empty list of suggestions.
 */
public class NullCompleter implements CommandCompleter {

    @Override
    public List<String> getSuggestions(String arguments, Namespace locals) throws CommandException {
        return Collections.emptyList();
    }

}
