package me.illgilp.worldeditglobalizerbungee.util;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ComponentUtils {


    public static TextComponent getClickAbleLink(String msg, String link, String hover){
        TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(msg));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new TextComponent[]{new TextComponent(TextComponent.fromLegacyText(hover))}));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,link));
        return textComponent;
    }
    public static TextComponent addText(TextComponent comp, String text){
        if(comp == null){
            comp = new TextComponent(TextComponent.fromLegacyText(""));
        }
        comp.addExtra(new TextComponent(TextComponent.fromLegacyText(text)));
        return comp;
    }
    public static TextComponent addText(TextComponent comp, TextComponent text){
        if(comp == null){
            comp = new TextComponent(TextComponent.fromLegacyText(""));
        }
        if(text == null){
            text = new TextComponent(TextComponent.fromLegacyText(""));
        }
        comp.addExtra(text);
        return comp;
    }
}
