package me.illgilp.worldeditglobalizerbungee.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import me.illgilp.intake.Command;
import me.illgilp.intake.CommandMapping;
import me.illgilp.intake.Require;
import me.illgilp.intake.parametric.annotation.Optional;
import me.illgilp.intake.parametric.annotation.Text;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.callback.Callback;
import me.illgilp.worldeditglobalizerbungee.clipboard.Clipboard;
import me.illgilp.worldeditglobalizerbungee.manager.ClipboardManager;
import me.illgilp.worldeditglobalizerbungee.manager.CommandManager;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import me.illgilp.worldeditglobalizerbungee.message.MessageFile;
import me.illgilp.worldeditglobalizerbungee.message.template.CustomMessageFile;
import me.illgilp.worldeditglobalizerbungee.network.PacketSender;
import me.illgilp.worldeditglobalizerbungee.player.OfflinePlayer;
import me.illgilp.worldeditglobalizerbungee.player.Player;
import me.illgilp.worldeditglobalizerbungee.util.ComponentUtils;
import me.illgilp.worldeditglobalizerbungee.util.StringUtil;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;
import me.illgilp.worldeditglobalizercommon.network.packets.ClipboardRequestPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.ClipboardSendPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.PluginSendPacket;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import org.apache.commons.io.IOUtils;

public class WEGSubCommands {


    @Command(
            aliases = {"help"},
            min = 0,
            max = 0,
            help = "help",
            desc = "shows the help list"
    )
    @Require(
            value = "worldeditglobalizer.command.weg"
    )
    public void help(CommandSender sender) {
        sender.sendMessage(ComponentUtils.addText(null, MessageManager.getInstance().getPrefix() + "§7Help: "));
        for (CommandMapping map : CommandManager.getInstance().getCommands()) {
            boolean hasPerm = false;
            for (String permission : map.getDescription().getPermissions()) {
                if (sender.hasPermission(permission)) hasPerm = true;
            }
            if (hasPerm) {
                sender.sendMessage(ComponentUtils.addText(null, "§6§l>> §r§f§o/weg " + map.getDescription().getHelp() + " §r§6= §a" + map.getDescription().getShortDescription()));
            }
        }
        sender.sendMessage(ComponentUtils.addText(null, "\n"));
        MessageManager.sendMessage(sender, "command.help.discord");
    }

    @Command(
            aliases = {"download"},
            min = 0,
            max = 0,
            help = "download",
            desc = "downloads your clipboard to the server you are currently on"
    )
    @Require(
            value = "worldeditglobalizer.command.download"
    )
    public void download(Player player) {
        if (player.sendIncompatibleMessage(player.getServerUsability())) {
            return;
        }
        if (player.hasClipboard()) {
            MessageManager.sendMessage(player, "clipboard.start.downloading");
            ClipboardSendPacket packet = new ClipboardSendPacket();
            packet.setClipboardHash(player.getClipboard().getHash());
            packet.setData(player.getClipboard().getData());
            PacketSender.sendPacket(player, packet);
        } else {
            MessageManager.sendMessage(player, "clipboard.empty.own");
        }
    }

    @Command(
            aliases = {"upload"},
            min = 0,
            max = 0,
            help = "upload",
            desc = "uploads your clipboard to the network"
    )
    @Require(
            value = "worldeditglobalizer.command.upload"
    )
    public void upload(Player player) {
        if (player.sendIncompatibleMessage(player.getServerUsability())) {
            return;
        }
        ClipboardRequestPacket req = new ClipboardRequestPacket();
        Callback callback = new Callback(15000, req.getIdentifier()) {
            @Override
            public void onTimeOut(Callback callback) {
                Player player = (Player) getUserData();
                MessageManager.sendMessage(player, "timedOut", getTimeOut());
            }

            @Override
            public void onCallback(Callback callback, Object response) {
                if (response instanceof ClipboardSendPacket) {
                    ClipboardSendPacket packet = (ClipboardSendPacket) response;
                    Player player = (Player) getUserData();
                    if (packet.getAction() == ClipboardSendPacket.Action.SEND) {
                        Clipboard clipboard = new Clipboard(player.getUniqueId(), packet.getData(), packet.getClipboardHash(), player.getProxiedPlayer().getServer().getInfo().getName());
                        player.setClipboard(clipboard);
                        MessageManager.sendMessage(player, "clipboard.finish.uploading", StringUtil.humanReadableByteCount(clipboard.getData().length, true));
                    } else {
                        if (packet.getAction() == ClipboardSendPacket.Action.CLEAR) {
                            player.removeClipboard();
                            MessageManager.sendMessage(player, "clipboard.clear");
                        } else if (packet.getAction() == ClipboardSendPacket.Action.TOO_BIG) {
                            PacketDataSerializer ser = new PacketDataSerializer(packet.getData());
                            MessageManager.sendMessage(player, "clipboard.tooBig",
                                StringUtil.humanReadableByteCount(WorldEditGlobalizerBungee.getInstance().getMainConfig().getMaxClipboardBytes(), true),
                                StringUtil.humanReadableByteCount(ser.readLong(), true));
                        }
                    }
                } else {
                }
            }
        };
        PacketSender.sendPacket(player, req);
        callback.setUserData(player);
        MessageManager.sendMessage(player, "clipboard.start.uploading");
        callback.start();
    }


    @Command(
            aliases = {"reload"},
            min = 0,
            max = 0,
            help = "reload",
            desc = "reloads all configs and all message files"
    )
    @Require(
            value = "worldeditglobalizer.command.reload"
    )
    public void reload(CommandSender sender) {
        MessageManager.sendMessage(sender, "command.start.reload");
        WorldEditGlobalizerBungee.getInstance().getConfigManager().reloadAllConfigs();
        MessageManager.getInstance().reload();
        String lang = WorldEditGlobalizerBungee.getInstance().getMainConfig().getLanguage();
        if (!MessageManager.getInstance().hasMessageFile(lang)) {
            MessageFile file = new CustomMessageFile(lang, new File(MessageManager.getInstance().getMessageFolder(), "messages_" + lang + ".yml"));
            MessageManager.getInstance().addMessageFile(file);
        }
        MessageManager.getInstance().setLanguage(lang);
        MessageManager.getInstance().setPrefix(ChatColor.translateAlternateColorCodes('&', WorldEditGlobalizerBungee.getInstance().getMainConfig().getPrefix()));

        MessageManager.sendMessage(sender, "command.finish.reload");
    }

    @Command(
            aliases = {"info"},
            min = 0,
            max = 1,
            help = "info [player]",
            desc = "shows info's about your clipboard or a given player ones"
    )
    @Require(
            value = "worldeditglobalizer.command.info"
    )
    public void info(CommandSender sender, @Optional OfflinePlayer offlinePlayer) {

        String time = "";
        String clipboardSize = "";

        if (offlinePlayer != null) {
            if (!sender.hasPermission("worldeditglobalizer.command.info.other")) {
                MessageManager.sendMessage(sender, "command.permissionDenied");
                return;
            }

            if (!offlinePlayer.isExisting()) {
                MessageManager.sendMessage(sender, "command.playerNotFound", offlinePlayer.getName());
                return;
            }
            if (!offlinePlayer.hasClipboard()) {
                MessageManager.sendMessage(sender, "clipboard.empty.other", offlinePlayer.getName());
                return;
            }
            time = new SimpleDateFormat(MessageManager.getInstance().getRawMessage("timeFormat")).format(new Date(ClipboardManager.getInstance().getClipboardFile(offlinePlayer.getUniqueId()).lastModified()));
            clipboardSize = StringUtil.humanReadableByteCount(ClipboardManager.getInstance().getClipboardFile(offlinePlayer.getUniqueId()).length(), true);
            MessageManager.sendMessage(sender, "command.info.format", offlinePlayer.getName(), offlinePlayer.getName(), offlinePlayer.getUniqueId().toString(), time, clipboardSize);
        } else {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!player.hasClipboard()) {
                    MessageManager.sendMessage(sender, "clipboard.empty.own");
                    return;
                }
                time = new SimpleDateFormat(MessageManager.getInstance().getRawMessage("timeFormat")).format(new Date(ClipboardManager.getInstance().getClipboardFile(player.getUniqueId()).lastModified()));
                clipboardSize = StringUtil.humanReadableByteCount(ClipboardManager.getInstance().getClipboardFile(player.getUniqueId()).length(), true);
                MessageManager.sendMessage(sender, "command.info.format", player.getName(), player.getName(), player.getUniqueId().toString(), time, clipboardSize);
            } else {
                MessageManager.sendMessage(sender, "command.console");
            }
        }
    }


    @Command(
            aliases = {"stats", "statistics"},
            min = 0,
            max = 0,
            help = "stats",
            desc = "shows statistics about the plugin"
    )
    @Require(
            value = "worldeditglobalizer.command.stats"
    )
    public void stats(CommandSender sender) {
        List<Long> sizes = new ArrayList<>();
        long full = 0;
        for (UUID uuid : ClipboardManager.getInstance().getSavedClipboards()) {
            long size = ClipboardManager.getInstance().getClipboardFile(uuid).length();
            sizes.add(size);
            full += size;
        }

        long average = 0;
        try {
            average = full / (long) sizes.size();
        } catch (ArithmeticException e) {

        }


        MessageManager.sendMessage(sender, "command.stats.format", sizes.size(), StringUtil.humanReadableByteCount(full, true), StringUtil.humanReadableByteCount(average, true));
    }

    @Command(
        aliases = {"clear", "clearSchematic"},
        min = 0,
        max = 1,
        help = "clear [player]",
        desc = "clears your or the given players clipboard"
    )
    @Require(
        value = "worldeditglobalizer.command.clear"
    )
    public void clear(CommandSender sender, @Optional OfflinePlayer offlinePlayer) {

        if (offlinePlayer != null) {
            if (!sender.hasPermission("worldeditglobalizer.command.clear.other")) {
                MessageManager.sendMessage(sender, "command.permissionDenied");
                return;
            }

            if (!offlinePlayer.isExisting()) {
                MessageManager.sendMessage(sender, "command.playerNotFound", offlinePlayer.getName());
                return;
            }
            if (!offlinePlayer.hasClipboard()) {
                MessageManager.sendMessage(sender, "clipboard.empty.other", offlinePlayer.getName());
                return;
            }
            offlinePlayer.removeClipboard();
            MessageManager.sendMessage(sender, "command.clear.success", offlinePlayer.getName());
        } else {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!player.hasClipboard()) {
                    MessageManager.sendMessage(sender, "clipboard.empty.own");
                    return;
                }
                player.removeClipboard();
                MessageManager.sendMessage(sender, "command.clear.success", player.getName());
            } else {
                MessageManager.sendMessage(sender, "command.console");
            }
        }
    }

    @Command(
        aliases = {"syncVersions"},
        min = 0,
        max = 0,
        help = "syncVersions",
        desc = "syncs the plugin-versions between BungeeCord and the current subserver"
    )
    @Require(
        value = "worldeditglobalizer.command.syncversions"
    )
    public void syncVersions(Player player) {

        try {
            PluginSendPacket pluginSendPacket = new PluginSendPacket();
            FileInputStream fileInputStream = new FileInputStream(WorldEditGlobalizerBungee.getInstance().getFile());
            byte[] data = IOUtils.toByteArray(fileInputStream);
            fileInputStream.close();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            pluginSendPacket.setData(data);
            pluginSendPacket.setHash(hash);
            pluginSendPacket.setTryNum(0);
            MessageManager.sendMessage(player, "command.start.syncversions");
            PacketSender.sendPacket(player, pluginSendPacket);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Command(
            aliases = {"schematic"},
            min = 0,
            max = -1,
            help = "schematic [subcommand]",
            desc = "schematic system"
    )
    @Require(
            value = "worldeditglobalizer.command.schematic"
    )
    public void schematic(CommandSender sender, @Optional @Text String args) {
        CommandManager.getInstance().handleSubCommand("weg schematic", "schematic", args, sender);
    }

}
