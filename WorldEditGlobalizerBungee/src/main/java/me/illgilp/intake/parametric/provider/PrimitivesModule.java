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

import me.illgilp.intake.parametric.AbstractModule;
import me.illgilp.intake.parametric.annotation.Text;

/**
 * Provides values for primitives as well as Strings.
 */
public final class PrimitivesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Boolean.class).toProvider(me.illgilp.intake.parametric.provider.BooleanProvider.INSTANCE);
        bind(boolean.class).toProvider(me.illgilp.intake.parametric.provider.BooleanProvider.INSTANCE);
        bind(Integer.class).toProvider(me.illgilp.intake.parametric.provider.IntegerProvider.INSTANCE);
        bind(int.class).toProvider(me.illgilp.intake.parametric.provider.IntegerProvider.INSTANCE);
        bind(Short.class).toProvider(me.illgilp.intake.parametric.provider.ShortProvider.INSTANCE);
        bind(short.class).toProvider(me.illgilp.intake.parametric.provider.ShortProvider.INSTANCE);
        bind(Double.class).toProvider(me.illgilp.intake.parametric.provider.DoubleProvider.INSTANCE);
        bind(double.class).toProvider(me.illgilp.intake.parametric.provider.DoubleProvider.INSTANCE);
        bind(Float.class).toProvider(me.illgilp.intake.parametric.provider.FloatProvider.INSTANCE);
        bind(float.class).toProvider(me.illgilp.intake.parametric.provider.FloatProvider.INSTANCE);
        bind(String.class).toProvider(me.illgilp.intake.parametric.provider.StringProvider.INSTANCE);
        bind(String.class).annotatedWith(Text.class).toProvider(me.illgilp.intake.parametric.provider.TextProvider.INSTANCE);
    }

}