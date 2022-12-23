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

package me.illgilp.worldeditglobalizer.proxy.core.intake.parametric;

import java.util.Optional;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.proxy.core.intake.MessageBuilderHolder;

/**
 * Thrown when the value for a parameter cannot be provisioned due to errors
 * that are not related to the parsing of user-provided arguments.
 */
public class ProvisionException extends Exception implements MessageBuilderHolder {

    private MessageHelper.Builder messageBuilder;

    public ProvisionException() {
        super();
    }

    public ProvisionException(MessageHelper.Builder messageBuilder) {
        super(Optional.ofNullable(messageBuilder).map(MessageHelper.Builder::buildPlain).orElse(null));
        this.messageBuilder = messageBuilder;
    }

    public ProvisionException(MessageHelper.Builder messageBuilder, Throwable cause) {
        super(Optional.ofNullable(messageBuilder).map(MessageHelper.Builder::buildPlain).orElse(null), cause);
        this.messageBuilder = messageBuilder;
    }

    public ProvisionException(Throwable cause) {
        super(cause);
    }

    public MessageHelper.Builder getMessageBuilder() {
        return messageBuilder;
    }
}
