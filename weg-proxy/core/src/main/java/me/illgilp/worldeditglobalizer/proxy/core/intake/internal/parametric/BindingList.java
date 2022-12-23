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

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.Binding;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.Key;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.Provider;
import org.jetbrains.annotations.Nullable;

class BindingList {

    private final Map<Type, Collection<BindingEntry<?>>> providers = new HashMap<>();

    public <T> void addBinding(Key<T> key, Provider<T> provider) {
        checkNotNull(key, "key");
        checkNotNull(provider, "provider");
        providers.computeIfAbsent(key.getType(), type -> new TreeSet<>()).add(new BindingEntry<T>(key, provider));
    }

    @SuppressWarnings( { "rawtypes", "unchecked" })
    @Nullable
    public <T> Binding<T> getBinding(Key<T> key) {
        checkNotNull(key, "key");
        for (BindingEntry binding : providers.get(key.getType())) {
            if (binding.getKey().matches(key)) {
                return (Binding<T>) binding;
            }
        }

        return null;
    }

    private static final class BindingEntry<T> implements Binding<T>, Comparable<BindingEntry<?>> {
        private final Key<T> key;
        private final Provider<T> provider;

        private BindingEntry(Key<T> key, Provider<T> provider) {
            this.key = key;
            this.provider = provider;
        }

        @Override
        public Key<T> getKey() {
            return key;
        }

        @Override
        public Provider<T> getProvider() {
            return provider;
        }

        @Override
        public int compareTo(BindingEntry<?> o) {
            return key.compareTo(o.key);
        }

        @Override
        public String toString() {
            return "BindingEntry{" +
                "key=" + key +
                ", provider=" + provider +
                '}';
        }
    }
}
