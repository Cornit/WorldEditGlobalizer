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

import java.lang.annotation.Annotation;
import java.util.List;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.ArgumentException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.CommandArgs;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.Binding;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.Injector;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.Key;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.Module;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.Provider;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.ProvisionException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.provider.DefaultModule;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.Nullable;

public class InternalInjector implements Injector {

    private final BindingList bindings = new BindingList();

    public InternalInjector() {
        install(new DefaultModule());
    }

    @Override
    public void install(Module module) {
        checkNotNull(module, "module");
        module.configure(new InternalBinder(bindings));
    }

    @Override
    @Nullable
    public <T> Binding<T> getBinding(Key<T> key) {
        return bindings.getBinding(key);
    }

    @Override
    @Nullable
    public <T> Binding<T> getBinding(Class<T> type) {
        return getBinding(Key.get(type));
    }

    @Override
    @Nullable
    public <T> Provider<T> getProvider(Key<T> key) {
        Binding<T> binding = getBinding(key);
        return binding != null ? binding.getProvider() : null;
    }

    @Override
    @Nullable
    public <T> Provider<T> getProvider(Class<T> type) {
        return getProvider(Key.get(type));
    }

    @Override
    public <T> T getInstance(Key<T> key, CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        Provider<T> provider = getProvider(key);
        if (provider != null) {
            return provider.get(arguments, modifiers);
        } else {
            throw new ProvisionException(
                MessageHelper.builder()
                    .translation(TranslationKey.COMMAND_ERROR_NO_BINDING)
                    .tagResolver(Placeholder.unparsed("key", String.valueOf(key)))
            );
        }
    }

    @Override
    public <T> T getInstance(Class<T> type, CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        return getInstance(Key.get(type), arguments, modifiers);
    }

}
