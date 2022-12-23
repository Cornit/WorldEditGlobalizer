package me.illgilp.worldeditglobalizer.common.messages.box;

import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@AllArgsConstructor
public abstract class MessageBox {

    private MessageHelper.Builder title;

    public MessageHelper.Builder render(MessageHelper.Builder builder) {
        return builder.component(centerMessage(
                46,
                title
            ))
            .component(Component.newline());
    }

    protected MessageHelper.Builder centerMessage(int len, MessageHelper.Builder text) {
        Component borderChar = Component.text("=", NamedTextColor.DARK_AQUA)
            .decoration(TextDecoration.STRIKETHROUGH, true);
        int msgLen = PlainTextComponentSerializer.plainText().serialize(text.build()).length();
        int remaining = (len - (msgLen + 2)) / 2;
        MessageHelper.Builder builder = MessageHelper.builder();
        return builder.component(Stream.generate(() -> borderChar).limit(remaining).collect(Component.toComponent()))
            .component(Component.space())
            .component(text)
            .component(Component.space())
            .component(Stream.generate(() -> borderChar).limit(remaining).collect(Component.toComponent()));
    }
}
