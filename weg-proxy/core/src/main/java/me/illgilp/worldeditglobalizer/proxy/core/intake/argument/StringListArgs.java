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

import static me.illgilp.worldeditglobalizer.proxy.core.intake.util.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class StringListArgs extends AbstractCommandArgs {

    private final List<String> arguments;
    private final Map<Character, String> flags;
    private final Namespace namespace;
    private int position = 0;

    StringListArgs(List<String> arguments, Map<Character, String> flags, Namespace namespace) {
        checkNotNull(arguments, "arguments");
        checkNotNull(flags, "flags");
        checkNotNull(namespace, "namespace");
        this.arguments = new ArrayList<>(arguments);
        this.flags = flags;
        this.namespace = namespace;
    }

    protected void insert(String argument) {
        arguments.add(position, argument);
    }

    @Override
    public boolean hasNext() {
        return position < arguments.size();
    }

    @Override
    public String next() throws MissingArgumentException {
        try {
            return arguments.get(position++);
        } catch (IndexOutOfBoundsException ignored) {
            throw new MissingArgumentException();
        }
    }

    @Override
    public String peek() throws MissingArgumentException {
        try {
            return arguments.get(position);
        } catch (IndexOutOfBoundsException ignored) {
            throw new MissingArgumentException();
        }
    }

    @Override
    public int position() {
        return position;
    }

    @Override
    public int size() {
        return arguments.size();
    }

    @Override
    public void markConsumed() {
        position = arguments.size();
    }

    @Override
    public Map<Character, String> getFlags() {
        return flags;
    }

    @Override
    public Namespace getNamespace() {
        return namespace;
    }

}
