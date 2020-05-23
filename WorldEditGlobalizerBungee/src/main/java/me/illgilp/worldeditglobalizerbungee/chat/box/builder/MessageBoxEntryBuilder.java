package me.illgilp.worldeditglobalizerbungee.chat.box.builder;

import java.util.ArrayList;
import java.util.List;
import me.illgilp.worldeditglobalizerbungee.chat.box.MessageBoxEntry;
import me.illgilp.worldeditglobalizerbungee.chat.chatevent.ChatEvent;

public class MessageBoxEntryBuilder {
    private ChatEvent text;
    private List<ChatEvent> buttons = new ArrayList<>();

    public MessageBoxEntryBuilder setText(ChatEvent text) {
        this.text = text;
        return this;
    }

    public MessageBoxEntryBuilder setButtons(List<ChatEvent> buttons) {
        this.buttons = buttons;
        return this;
    }

    public MessageBoxEntryBuilder addButton(ChatEvent chatEvent) {
        if (buttons == null) buttons = new ArrayList<>();
        this.buttons.add(chatEvent);
        return this;
    }

    public MessageBoxEntry create() {
        return new MessageBoxEntry(text, buttons);
    }
}