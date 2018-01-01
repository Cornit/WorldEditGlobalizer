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

package me.illgilp.worldeditglobalizerbungee.intake.parametric;

import com.sk89q.intake.argument.Namespace;
import com.sk89q.intake.util.auth.Authorizer;
import net.md_5.bungee.api.CommandSender;

import static com.google.common.base.Preconditions.checkNotNull;

public class WEGAuthorizer implements Authorizer {

    @Override
    public boolean testPermission(Namespace namespace, String permission) {
        return checkNotNull(namespace.get(CommandSender.class), "Current user not available").hasPermission(permission);
    }

}
