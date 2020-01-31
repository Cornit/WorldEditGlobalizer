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

package me.illgilp.intake.parametric.provider;

import com.google.common.collect.ImmutableList;
import java.lang.annotation.Annotation;
import java.util.List;
import me.illgilp.intake.argument.ArgumentException;
import me.illgilp.intake.argument.CommandArgs;
import me.illgilp.intake.parametric.Provider;
import me.illgilp.intake.parametric.ProvisionException;

class CommandArgsProvider implements Provider<CommandArgs> {

    @Override
    public boolean isProvided() {
        return true;
    }


    @Override
    public CommandArgs get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        CommandArgs commandArgs = arguments.getNamespace().get(CommandArgs.class);
        if (commandArgs != null) {
            commandArgs.markConsumed();
            return commandArgs;
        } else {
            throw new ProvisionException("CommandArgs object not found in Namespace");
        }
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return ImmutableList.of();
    }

}
