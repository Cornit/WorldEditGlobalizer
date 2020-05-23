package me.illgilp.worldeditglobalizerbungee.chat.box;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import me.illgilp.worldeditglobalizerbungee.chat.FontInfo;
import me.illgilp.worldeditglobalizerbungee.chat.chatevent.ChatEvent;
import me.illgilp.yamlconfigurator.config.utils.StringUtils;

public class MessageBoxEntry {

    private ChatEvent text;
    private List<ChatEvent> buttons = new ArrayList<>();

    public MessageBoxEntry(ChatEvent text) {
        this.text = text;
    }

    public MessageBoxEntry(ChatEvent text, List<ChatEvent> buttons) {
        this.text = text;
        this.buttons = buttons;
        if (this.buttons == null) {
            this.buttons = new ArrayList<>();
        }
    }

    public ChatEvent getText() {
        return text;
    }

    public ChatEvent getText(int maxSize) {
        ChatEvent text = this.text;
        text.setText(FontInfo.addNewLineAfterTooLong(text.getText(), maxSize));
        return text;
    }

    public List<ChatEvent> getButtons() {
        return buttons;
    }

    public int getTextPxLength() {
        return FontInfo.getPxLength(StringUtils.removeColorCodes(this.text.getText()));
    }

    public int getButtonsPxLength(){
        return FontInfo.getPxLength(buttons.stream().map(b -> StringUtils.removeColorCodes(b.getText())).collect(Collectors.joining(" ")));
    }
}
