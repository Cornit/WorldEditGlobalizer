package me.illgilp.worldeditglobalizer.proxy.core.intake.argument;

import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;

public class MessageArgumentException extends ArgumentException {

    public MessageArgumentException(MessageHelper.Builder messageBuilder) {
        super(messageBuilder);
    }
}
