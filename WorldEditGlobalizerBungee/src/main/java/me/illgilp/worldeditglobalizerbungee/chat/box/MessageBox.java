package me.illgilp.worldeditglobalizerbungee.chat.box;

import java.util.List;
import joptsimple.internal.Strings;
import me.illgilp.worldeditglobalizerbungee.chat.FontInfo;
import me.illgilp.worldeditglobalizerbungee.chat.chatevent.ChatEvent;
import me.illgilp.worldeditglobalizerbungee.util.ComponentUtils;
import me.illgilp.yamlconfigurator.config.utils.StringUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class MessageBox {

    private TextComponent title;
    private ChatColor borderColor;

    public MessageBox(TextComponent title, ChatColor borderColor) {
        this.title = title;
        this.borderColor = borderColor;
    }

    public abstract List<MessageBoxEntry> getEntries();

    public TextComponent create() {
        TextComponent header = centerAndBorder(title);
        int headerPxLength = FontInfo.getPxLength(StringUtils.removeColorCodes(header.toLegacyText()));
        TextComponent result = header;
        List<MessageBoxEntry> entries = getEntries();
        for (MessageBoxEntry entry : entries) {
            result.addExtra(ComponentUtils.newLine());
            if (entry != null) {
                result.addExtra(entry.getText(headerPxLength).toComponent());
                int textSize = entry.getTextPxLength();
                int buttonsSize = entry.getButtonsPxLength();
                int spaceSize = headerPxLength - textSize - buttonsSize;

                if (spaceSize != headerPxLength && entry.getButtons().size() > 0) {
                    String chars = FontInfo.characterToString(entry.getText().getText().length() == 0 ? ' ' : '.', spaceSize);

                    result.addExtra(ComponentUtils.of("§8" + chars));
                }
                for (int i = 0; i < entry.getButtons().size(); i++) {
                    ChatEvent button = entry.getButtons().get(i);
                    result.addExtra(button.toComponent());
                    if (i < (entry.getButtons().size() - 1)) {
                        result.addExtra(ComponentUtils.of(" "));
                    }
                }
            }
        }

        return result;

    }

    protected TextComponent centerAndBorder(TextComponent text) {
        TextComponent line = new TextComponent();
        int leftOver = 47 - getLength(text);
        int side = (int)Math.floor((double)leftOver / 2.0D);
        if (side > 0) {
            if (side > 1) {
                line.addExtra(this.createBorder(side - 1));
            }

            line.addExtra(ComponentUtils.addText(null, " "));
        }

        line.addExtra(text);
        if (side > 0) {
            line.addExtra(ComponentUtils.addText(null, " "));
            if (side > 1) {
                line.addExtra(this.createBorder(side - 1));
            }
        }

        return line;
    }

    private TextComponent createBorder(int count) {
        return new TextComponent(
            new ComponentBuilder(Strings.repeat('-', count))
            .color(this.borderColor)
            .strikethrough(true)
            .create()
        );
    }

    private int getLength(TextComponent text) {
        return StringUtils.removeColorCodes(text.toLegacyText()).length();
    }


}
