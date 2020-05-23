package me.illgilp.worldeditglobalizerbungee.chat.chatevent;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.UUID;
import me.illgilp.worldeditglobalizerbungee.manager.ChatEventManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatEvent {

    public static final String SECRET;

    static {
        SECRET = UUID.randomUUID().toString().replace("-","");
    }


    public static ChatEvent parse(JsonObject jsonObject) {
        if (!jsonObject.has("chatevent")) return null;
        JsonObject chatEventJson = jsonObject.getAsJsonObject("chatevent");
        if (
            !chatEventJson.has("text")
            || !chatEventJson.has("secret")
        ) return null;

        ChatEventBuilder chatEventBuilder = new ChatEventBuilder();
        chatEventBuilder.setText(chatEventJson.get("text").getAsString());
        if (!chatEventJson.get("secret").getAsString().equals(SECRET)) return null;
        if (chatEventJson.has("hover")) {
            chatEventBuilder.setHover(chatEventJson.get("hover").getAsString());
        }

        if (chatEventJson.has("link")) {
            chatEventBuilder.setLink(chatEventJson.get("link").getAsString());
        }

        if (chatEventJson.has("command")) {
            chatEventBuilder.setCommand(chatEventJson.get("command").getAsString());
        }

        if (chatEventJson.has("listener")) {
            ChatClickListener chatClickListener = ChatEventManager.getInstance().getListener(chatEventJson.get("listener").getAsString());
            if (chatClickListener == null) return null;
            chatEventBuilder.setListener(chatClickListener);
        }

        return chatEventBuilder.create();

    }

    private String text;
    private String secret = SECRET;
    private String hover;
    private String link;
    private String command;
    private ChatClickListener listener;

    public ChatEvent(String text, String hover, String link, String command, ChatClickListener listener) {
        this.text = text;
        this.hover = hover;
        this.link = link;
        this.command = command;
        this.listener = listener;

        if (this.listener != null) {
            ChatEventManager.getInstance().registerListener(this.listener);
        }
    }

    public String getText() {
        return text;
    }

    public String getSecret() {
        return secret;
    }

    public String getHover() {
        return hover;
    }

    public String getLink() {
        return link;
    }

    public String getCommand() {
        return command;
    }

    public ChatClickListener getListener() {
        return listener;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        JsonObject chatEvent = new JsonObject();
        JsonObject chatEventEntry = new JsonObject();
        chatEventEntry.addProperty("text", this.text);
        chatEventEntry.addProperty("secret", this.secret);
        if (hover != null) {
            chatEventEntry.addProperty("hover", this.hover);
        }

        if (link != null) {
            chatEventEntry.addProperty("link", this.link);
        }

        if (command != null) {
            chatEventEntry.addProperty("command", this.command);
            chatEventEntry.remove("link");
        }

        if (listener != null) {
            chatEventEntry.addProperty("listener", this.listener.getId());
            chatEventEntry.remove("link");
            chatEventEntry.remove("command");
        }
        chatEvent.add("chatevent", chatEventEntry);

        return new Gson().toJson(chatEvent);
    }

    public TextComponent toComponent() {
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(this.text));
        if (this.hover != null) {
            component.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                TextComponent.fromLegacyText(this.hover)
            ));
        }

        if (this.link != null) {
            component.setClickEvent(new ClickEvent(
                ClickEvent.Action.OPEN_URL,
                this.link
            ));
        }
        if (this.command != null) {
            component.setClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                this.command
            ));
        }

        if (this.listener != null) {
            component.setClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/weg " + SECRET + " " + this.listener.getId()
            ));
        }

        return component;
    }
}
