package me.illgilp.worldeditglobalizerbungee.chat.chatevent;

import net.md_5.bungee.api.chat.TextComponent;

public class ChatEventBuilder {
    private String text;
    private String hover;
    private String link;
    private String command;
    private ChatClickListener listener;

    public ChatEventBuilder setText(String text) {
        this.text = text;
        return this;
    }

    public ChatEventBuilder setHover(String hover) {
        this.hover = hover;
        return this;
    }

    public ChatEventBuilder setHover(TextComponent textComponent) {
        if (textComponent != null) {
            this.hover = textComponent.toLegacyText();
        }
        return this;
    }

    public ChatEventBuilder setLink(String link) {
        this.link = link;
        return this;
    }

    public ChatEventBuilder setCommand(String command) {
        this.command = command;
        return this;
    }

    public ChatEventBuilder setListener(ChatClickListener listener) {
        this.listener = listener;
        return this;
    }

    public ChatEvent create() {
        return new ChatEvent(text, hover, link, command, listener);
    }
}