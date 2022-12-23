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

package me.illgilp.worldeditglobalizer.proxy.core.intake.internal.parametric;

import static me.illgilp.worldeditglobalizer.proxy.core.intake.util.Preconditions.checkNotNull;

import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.Key;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.binder.Binder;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.binder.BindingBuilder;

class InternalBinder implements Binder {

    private final BindingList bindings;

    InternalBinder(BindingList bindings) {
        checkNotNull(bindings, "bindings");
        this.bindings = bindings;
    }

    @Override
    public <T> BindingBuilder<T> bind(Class<T> type) {
        return new InternalBinderBuilder<T>(bindings, Key.get(type));
    }

    @Override
    public <T> BindingBuilder<T> bind(Key<T> type) {
        return new InternalBinderBuilder<T>(bindings, type);
    }

}
