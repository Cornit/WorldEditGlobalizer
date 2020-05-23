package me.illgilp.worldeditglobalizerbungee.commands.schematic;

import java.io.File;
import me.illgilp.intake.Command;
import me.illgilp.intake.CommandMapping;
import me.illgilp.intake.Require;
import me.illgilp.intake.parametric.annotation.Optional;
import me.illgilp.worldeditglobalizerbungee.clipboard.Clipboard;
import me.illgilp.worldeditglobalizerbungee.intake.parametric.annotation.SavedSchematicArg;
import me.illgilp.worldeditglobalizerbungee.manager.CommandManager;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import me.illgilp.worldeditglobalizerbungee.manager.SchematicManager;
import me.illgilp.worldeditglobalizerbungee.network.PacketSender;
import me.illgilp.worldeditglobalizerbungee.player.Player;
import me.illgilp.worldeditglobalizerbungee.util.ComponentUtils;
import me.illgilp.worldeditglobalizercommon.network.packets.ClipboardSendPacket;
import net.md_5.bungee.api.CommandSender;

public class WEGSchematicCommands {

    @Command(
            aliases = {"help"},
            min = 0,
            max = 0,
            help = "help",
            desc = "shows the help list"
    )
    @Require(
            value = "worldeditglobalizer.command.schematic"
    )
    public void help(CommandSender sender) {
        sender.sendMessage(ComponentUtils.addText(null, MessageManager.getInstance().getPrefix() + "§7Help: "));
        for (CommandMapping map : CommandManager.getInstance().getSubCommands("schematic")) {
            sender.sendMessage(ComponentUtils.addText(null, "§6§l>> §r§f§o/weg schematic " + map.getDescription().getHelp() + " §r§6= §a" + map.getDescription().getShortDescription()));
        }
    }


    @Command(
            aliases = {"list"},
            min = 0,
            max = 0,
            help = "list",
            desc = "shows a list with saved schematics"
    )
    @Require(
            value = "worldeditglobalizer.command.schematic.list"
    )
    public void list(CommandSender sender, @Optional String Spage) {
        int page = 0;
        if (Spage != null) {
            try {
                page = Integer.parseInt(Spage);
            } catch (NumberFormatException e) {
                MessageManager.sendMessage(sender, "invalid.number");
                return;
            }
        } else {
            page = 1;
        }
//        List<String> schematics = SchematicManager.getInstance().getSchematics();
//        if (page < 1) page = 1;
//        page = page - 1;
//        List<String> show = new ArrayList<>();
//        int till = ((page + 1) * 10);
//        if (till > schematics.size()) till = schematics.size();
//        if (!schematics.isEmpty()) {
//            for (int i = page * 10; i < till; i++) {
//                show.add(schematics.get(i));
//            }
//        }

//        MessageBox messageBox = new MessageBox(ComponentUtils.of("§7Available schematics"), ChatColor.AQUA) {
//            @Override
//            public List<MessageBoxEntry> getEntries() {
//                List<MessageBoxEntry> entries = new ArrayList<>();
//                for (String s : show) {
//                    entries.add(
//                        new MessageBoxEntryBuilder()
//                        .setText(
//                            new ChatEventBuilder()
//                            .setText( "§2" + s + "(" + StringUtils.humanReadableByteCount(SchematicManager.getInstance().getSchematicFile(s).length(), true) + ")")
//                            .create()
//                        )
//                        .addButton(
//                            new ChatEventBuilder()
//                            .setText("§a[Load]")
//                            .setHover("Click to load")
//                            .setCommand("/weg schematic load " + s)
//                            .create()
//                        )
//                        .addButton(
//                            new ChatEventBuilder()
//                            .setText("§c[Delete]")
//                            .setHover("Click to delete schematic")
//                            .setCommand("/weg schematic delete " + s)
//                            .create()
//                        )
//                        .create()
//                    );
//                }
//
//                return entries;
//            }
//        };

        SchematicListBox schematicListBox = new SchematicListBox(SchematicManager.getInstance().getSchematics());

        sender.sendMessage(schematicListBox.create(page-1));

//        String msg = "";
//        int index = 0;
//        for (String s : show) {
//            msg += new ChatEventBuilder()
//                .setText( "§2" + s + "(" + StringUtils.humanReadableByteCount(SchematicManager.getInstance().getSchematicFile(s).length(), true) + ")" + ((index < (show.size() - 1)) ? ", " : ""))
//                .setHover("Click to load schematic")
//                .setListener(new UserDataChatClickListener<String>(s) {
//                    @Override
//                    public void onClick(Player player, String s) {
//                        WEGSchematicCommands.this.load(player, s);
//                    }
//                }).create().toString();
//            index++;
//        }
        //MessageManager.sendMessage(sender, "command.schematic.list", (page + 1), MathUtil.getPages(schematics.size(), 10), msg);
    }


    @Command(
            aliases = {"save"},
            min = 1,
            max = 1,
            help = "save <name>",
            desc = "saves your clipboard to a schematic file"
    )
    @Require(
            value = "worldeditglobalizer.command.schematic.save"
    )
    public void save(Player player, String name) {
        if (!player.hasClipboard()) {
            MessageManager.sendMessage(player, "clipboard.empty.own");
            return;
        }
        SchematicManager.getInstance().saveSchematic(name, player.getClipboard());
        MessageManager.sendMessage(player, "command.schematic.save");
    }

    @Command(
            aliases = {"load"},
            min = 1,
            max = 1,
            help = "load <name>",
            desc = "loads a schematic file to your clipboard"
    )
    @Require(
            value = "worldeditglobalizer.command.schematic.load"
    )
    public void load(Player player, @SavedSchematicArg String name) {
        Clipboard clipboard = SchematicManager.getInstance().loadSchematicInto(name, player.getUniqueId());
        if (clipboard == null) {
            MessageManager.sendMessage(player, "command.schematic.load.notFound");
            return;
        }
        player.setClipboard(clipboard);
        if (player.sendIncompatibleMessage(player.getServerUsability())) {
            return;
        }
        if (player.hasClipboard()) {
            MessageManager.sendMessage(player, "clipboard.start.downloading");
            ClipboardSendPacket packet = new ClipboardSendPacket();
            packet.setClipboardHash(player.getClipboard().getHash());
            packet.setData(player.getClipboard().getData());
            PacketSender.sendPacket(player, packet);
            MessageManager.sendMessage(player, "command.schematic.load.success");
        } else {
            MessageManager.sendMessage(player, "clipboard.empty.own");
        }



    }


    @Command(
            aliases = {"delete"},
            min = 1,
            max = 1,
            help = "delete <name>",
            desc = "deletes a schematic file"
    )
    @Require(
            value = "worldeditglobalizer.command.schematic.delete"
    )
    public void delete(CommandSender sender, @SavedSchematicArg String name) {
        File schematicFile = SchematicManager.getInstance().getSchematicFile(name);
        if (!schematicFile.exists()) {
            MessageManager.sendMessage(sender, "command.schematic.load.notFound");
            return;
        }
        if (!schematicFile.delete()) {
            MessageManager.sendMessage(sender, "command.schematic.delete.error");
            return;
        }
        MessageManager.sendMessage(sender, "command.schematic.delete.success");
    }

}
