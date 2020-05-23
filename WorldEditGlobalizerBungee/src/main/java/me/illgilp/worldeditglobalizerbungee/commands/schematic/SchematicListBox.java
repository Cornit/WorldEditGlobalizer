package me.illgilp.worldeditglobalizerbungee.commands.schematic;

import java.util.List;
import java.util.stream.Collectors;
import me.illgilp.worldeditglobalizerbungee.chat.box.ConfirmationBox;
import me.illgilp.worldeditglobalizerbungee.chat.box.PaginationBox;
import me.illgilp.worldeditglobalizerbungee.chat.box.builder.MessageBoxEntryBuilder;
import me.illgilp.worldeditglobalizerbungee.chat.chatevent.ChatEventBuilder;
import me.illgilp.worldeditglobalizerbungee.chat.chatevent.UserDataChatClickListener;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import me.illgilp.worldeditglobalizerbungee.manager.SchematicManager;
import me.illgilp.worldeditglobalizerbungee.player.Player;
import me.illgilp.worldeditglobalizerbungee.util.ComponentUtils;
import me.illgilp.worldeditglobalizerbungee.util.StringUtil;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;

public class SchematicListBox extends PaginationBox {
    public SchematicListBox(List<String> schematics) {
        super(ComponentUtils.of(MessageManager.getRawMessageOrEmpty("chat.box.schematic.list.title")), ChatColor.AQUA, "/weg schematic list %s",
            schematics.stream().map(
                s -> new MessageBoxEntryBuilder()
                    .setText(
                        new ChatEventBuilder()
                            .setText( "§6" + s + " (" + StringUtil.humanReadableByteCount(SchematicManager.getInstance().getSchematicFile(s).length(), true) + ")")
                            .create()
                    )
                    .addButton(
                        new ChatEventBuilder()
                            .setText("§a[" + MessageManager.getRawMessageOrEmpty("chat.box.schematic.list.button.load.title") + "]")
                            .setHover(MessageManager.getRawMessageOrEmpty("chat.box.schematic.list.button.load.tooltip", s))
                            .setCommand("/weg schematic load " + s)
                            .create()
                    )
                    .addButton(
                        new ChatEventBuilder()
                            .setText("§c[" + MessageManager.getRawMessageOrEmpty("chat.box.schematic.list.button.delete.title") + "]")
                            .setHover(MessageManager.getRawMessageOrEmpty("chat.box.schematic.list.button.delete.tooltip", s))
                            .setListener(new UserDataChatClickListener<String>(s) {
                                @Override
                                public void onClick(Player player, String s) {
                                    ConfirmationBox confirmationBox = new ConfirmationBox<String>(
                                        ComponentUtils.of(
                                            "§7" + MessageManager.getRawMessageOrEmpty("chat.box.schematic.delete.title")
                                        ),
                                        MessageManager.getRawMessageOrEmpty("chat.box.schematic.delete.message", s),
                                        s,
                                        "§a[" + MessageManager.getRawMessageOrEmpty("chat.box.schematic.delete.button.confirm.title") + "§a]",
                                        MessageManager.getRawMessageOrEmpty("chat.box.schematic.delete.button.confirm.tooltip")
                                    ) {
                                        @Override
                                        public void onAccept(Player player, String userData) {
                                            BungeeCord.getInstance().getPluginManager().dispatchCommand(player, "weg schematic delete " + userData);
                                        }
                                    };
                                    player.sendMessage(confirmationBox.create());
                                }
                            })
                            .create()
                    )
                    .create()
            ).collect(Collectors.toList())
        );
    }
}
