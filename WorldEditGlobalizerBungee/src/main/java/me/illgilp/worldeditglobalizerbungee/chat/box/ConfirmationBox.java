package me.illgilp.worldeditglobalizerbungee.chat.box;

import java.util.ArrayList;
import java.util.List;
import me.illgilp.worldeditglobalizerbungee.chat.box.builder.MessageBoxEntryBuilder;
import me.illgilp.worldeditglobalizerbungee.chat.chatevent.ChatEventBuilder;
import me.illgilp.worldeditglobalizerbungee.chat.chatevent.UserDataChatClickListener;
import me.illgilp.worldeditglobalizerbungee.player.Player;
import me.illgilp.worldeditglobalizerbungee.util.ComponentUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class ConfirmationBox<T> extends MessageBox {

    private String message;
    private T userData;

    private String confirmButtonTitle;
    private String confirmButtonTooltip;

    public ConfirmationBox(TextComponent title, String message, T userData, String confirmButtonTitle, String confirmButtonTooltip) {
        super(title, ChatColor.AQUA);
        this.message = message;
        this.userData = userData;
        this.confirmButtonTitle = confirmButtonTitle;
        this.confirmButtonTooltip = confirmButtonTooltip;
    }

    public ConfirmationBox(TextComponent title, String message, String confirmButtonTitle, String confirmButtonTooltip) {
        super(title, ChatColor.AQUA);
        this.message = message;
        this.confirmButtonTitle = confirmButtonTitle;
        this.confirmButtonTooltip = confirmButtonTooltip;
        this.userData = userData;
    }

    @Override
    public List<MessageBoxEntry> getEntries() {
        List<MessageBoxEntry> entries = new ArrayList<>();
        entries.add(new MessageBoxEntryBuilder().setText(new ChatEventBuilder().setText("").create()).create());
        entries.add(new MessageBoxEntryBuilder().setText(new ChatEventBuilder().setText(message).create()).create());
        entries.add(new MessageBoxEntryBuilder().setText(new ChatEventBuilder().setText("").create()).create());
        entries.add(
            new MessageBoxEntryBuilder()
                .setText(
                    new ChatEventBuilder()
                        .setText("")
                        .create()
                )
                .addButton(
                    new ChatEventBuilder()
                    .setText("§a" + confirmButtonTitle)
                    .setHover(confirmButtonTooltip)
                    .setListener(new UserDataChatClickListener<T>(userData) {
                        @Override
                        public void onClick(Player player, T t) {
                            onAccept(player, t);
                        }
                    })
                    .create()
                )
                .create()
        );
        return entries;
    }

    @Override
    public TextComponent create() {
        TextComponent result = super.create();
        result.addExtra(ComponentUtils.newLine());
        result.addExtra(ComponentUtils.newLine());
        result.addExtra(super.centerAndBorder(ComponentUtils.of("")));
        return result;
    }

    public abstract void onAccept(Player player, T userData);

    public T getUserData() {
        return userData;
    }

    public void setUserData(T userData) {
        this.userData = userData;
    }

    public boolean hasUserData() {
        return userData != null;
    }
}
