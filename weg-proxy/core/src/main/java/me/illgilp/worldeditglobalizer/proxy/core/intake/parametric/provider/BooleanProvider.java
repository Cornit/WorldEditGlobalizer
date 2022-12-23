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
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.ArgumentException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.CommandArgs;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.Provider;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.annotation.Switch;
import org.jetbrains.annotations.Nullable;

class BooleanProvider implements Provider<Boolean> {

    static final BooleanProvider INSTANCE = new BooleanProvider();

    @Override
    public boolean isProvided() {
        return false;
    }

    @Nullable
    @Override
    public Boolean get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException {
        return arguments.nextBoolean();
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return Collections.emptyList();
    }

    @Override
    public boolean needPermission(Boolean value, List<? extends Annotation> modifiers, boolean argumentProvided) {
        if (modifiers.stream().anyMatch(annotation -> annotation instanceof Switch)) {
            return Boolean.TRUE.equals(value);
        }
        return Provider.super.needPermission(value, modifiers, argumentProvided);
    }
}
