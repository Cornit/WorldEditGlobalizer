package me.illgilp.worldeditglobalizerbungee.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import me.illgilp.intake.Command;
import me.illgilp.intake.CommandMapping;
import me.illgilp.intake.Require;
import me.illgilp.intake.parametric.annotation.Optional;
import me.illgilp.worldeditglobalizerbungee.clipboard.Clipboard;
import me.illgilp.worldeditglobalizerbungee.manager.ClipboardManager;
import me.illgilp.worldeditglobalizerbungee.manager.CommandManager;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import me.illgilp.worldeditglobalizerbungee.manager.SchematicManager;
import me.illgilp.worldeditglobalizerbungee.network.PacketSender;
import me.illgilp.worldeditglobalizerbungee.player.Player;
import me.illgilp.worldeditglobalizerbungee.util.ComponentUtils;
import me.illgilp.worldeditglobalizerbungee.util.MathUtil;
import me.illgilp.worldeditglobalizerbungee.util.StringUtils;
import me.illgilp.worldeditglobalizercommon.network.packets.ClipboardSendPacket;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

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
                MessageManager.sendMessage(sender, "invalide.number");
                return;
            }
        } else {
            page = 1;
        }
        List<String> schematics = SchematicManager.getInstance().getSchematics();
        if (page < 1) page = 1;
        page = page - 1;
        List<String> show = new ArrayList<>();
        int till = ((page + 1) * 10);
        if (till > schematics.size()) till = schematics.size();
        if (!schematics.isEmpty()) {
            for (int i = page * 10; i < till; i++) {
                show.add(schematics.get(i));
            }
        }
        String msg = "";
        int index = 0;
        for (String s : show) {
            msg += "§2" + s + "(" + StringUtils.humanReadableByteCount(SchematicManager.getInstance().getSchematicFile(s).length(), true) + ")" + ((index < (show.size() - 1)) ? ", " : "");
            index++;
        }
        MessageManager.sendMessage(sender, "command.schematic.list", (page + 1), MathUtil.getPages(schematics.size(), 10), msg);
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
    public void save(CommandSender sender, String name) {

        if (!(sender instanceof ProxiedPlayer)) {
            MessageManager.sendMessage(sender, "command.console");
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        if (!ClipboardManager.getInstance().hasClipboard(player.getUniqueId())) {
            MessageManager.sendMessage(sender, "clipboard.empty.own");
            return;
        }
        SchematicManager.getInstance().saveSchematic(name, ClipboardManager.getInstance().getClipboard(player.getUniqueId()));
        MessageManager.sendMessage(sender, "command.schematic.save");
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
    public void load(CommandSender sender, String name) {

        if (!(sender instanceof ProxiedPlayer)) {
            MessageManager.sendMessage(sender, "command.console");
            return;
        }


        ProxiedPlayer player = (ProxiedPlayer) sender;
        Clipboard clipboard = SchematicManager.getInstance().loadSchematicInto(name, player.getUniqueId());
        if (clipboard == null) {
            MessageManager.sendMessage(sender, "command.schematic.load.notFound");
            return;
        }
        ClipboardManager.getInstance().saveClipboard(clipboard);
        MessageManager.sendMessage(sender, "command.schematic.load.success");
        if (Player.getPlayer(player).isPluginOnCurrentServerInstalled()) {
            if (Player.getPlayer(player).hasClipboard()) {
                MessageManager.sendMessage(player, "clipboard.start.downloading");
                ClipboardSendPacket packet = new ClipboardSendPacket();
                packet.setClipboardHash(Player.getPlayer(player).getClipboard().getHash());
                packet.setData(Player.getPlayer(player).getClipboard().getData());
                PacketSender.sendPacket(player, packet);
            } else {
                MessageManager.sendMessage(sender, "clipboard.empty.own");
            }
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
    public void delete(CommandSender sender, String name) {


        File schematicFile = SchematicManager.getInstance().getSchematicFile(name);
        if (!schematicFile.exists()) {
            MessageManager.sendMessage(sender, "command.schematic.load.notFound");
            return;
        }
        schematicFile.delete();
        MessageManager.sendMessage(sender, "command.schematic.delete");
    }

}
