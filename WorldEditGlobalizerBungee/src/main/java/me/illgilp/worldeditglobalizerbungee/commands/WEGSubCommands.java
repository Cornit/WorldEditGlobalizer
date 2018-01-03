package me.illgilp.worldeditglobalizerbungee.commands;

import com.sk89q.intake.Command;
import com.sk89q.intake.CommandMapping;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Optional;
import com.sk89q.intake.parametric.annotation.Text;
import jline.internal.Nullable;
import me.illgilp.worldeditglobalizerbungee.Callback;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.clipboard.Clipboard;
import me.illgilp.worldeditglobalizerbungee.manager.ClipboardManager;
import me.illgilp.worldeditglobalizerbungee.manager.CommandManager;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import me.illgilp.worldeditglobalizerbungee.message.MessageFile;
import me.illgilp.worldeditglobalizerbungee.message.template.CustomMessageFile;
import me.illgilp.worldeditglobalizerbungee.network.PacketSender;
import me.illgilp.worldeditglobalizerbungee.network.packets.ClipboardRequestPacket;
import me.illgilp.worldeditglobalizerbungee.network.packets.ClipboardSendPacket;
import me.illgilp.worldeditglobalizerbungee.player.Player;
import me.illgilp.worldeditglobalizerbungee.util.ComponentUtils;
import me.illgilp.worldeditglobalizerbungee.util.PacketDataSerializer;
import me.illgilp.worldeditglobalizerbungee.util.StringUtils;
import me.illgilp.worldeditglobalizerbungee.util.UUIDFetcher;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
    public void help(CommandSender sender){
        sender.sendMessage(ComponentUtils.addText(null,MessageManager.getInstance().getPrefix()+"§7Help: "));
        for(CommandMapping map : CommandManager.getInstance().getCommands()){
            sender.sendMessage(ComponentUtils.addText(null,"§6§l>> §r§f§o/weg "+map.getDescription().getHelp()+" §r§6= §a"+map.getDescription().getShortDescription()));
        }
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
    public void download(CommandSender sender){
        if(sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            if (!Player.getPlayer(player).isPluginOnCurrentServerInstalled()){
                MessageManager.sendMessage(player,"command.cannotUse");
                return;
            }
            if (Player.getPlayer(player).hasClipboard()) {
                MessageManager.sendMessage(player, "clipboard.start.downloading");
                ClipboardSendPacket packet = new ClipboardSendPacket();
                packet.setClipboardhash(Player.getPlayer(player).getClipboard().getHash());
                packet.setData(Player.getPlayer(player).getClipboard().getData());
                PacketSender.sendPacket(player, packet);
            } else {
                MessageManager.sendMessage(sender,"clipboard.empty.own");
            }
        }else {
            MessageManager.sendMessage(sender,"command.console");
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
    public void upload(CommandSender sender){
        if(sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            if (!Player.getPlayer(player).isPluginOnCurrentServerInstalled()){
                MessageManager.sendMessage(player,"command.cannotUse");
                return;
            }
            ClipboardRequestPacket req = new ClipboardRequestPacket();
            Callback callback = new Callback(15000,req.getIdentifier()) {
                @Override
                public void onTimeOut(Callback callback) {
                    ProxiedPlayer player = (ProxiedPlayer) getUserData();
                    MessageManager.sendMessage(player,"timedOut",getTimeOut());
                }

                @Override
                public void onCallback(Callback callback, Object response) {
                    if(response instanceof ClipboardSendPacket){
                        ClipboardSendPacket packet = (ClipboardSendPacket) response;
                        ProxiedPlayer player = (ProxiedPlayer) getUserData();
                        if(packet.getClipboardhash() > 0) {
                            Clipboard clipboard = new Clipboard(player.getUniqueId(), packet.getData(), packet.getClipboardhash(), player.getServer().getInfo().getName());
                            Player.getPlayer(player).setClipboard(clipboard);
                            MessageManager.sendMessage(player,"clipboard.finish.uploading", StringUtils.humanReadableByteCount(clipboard.getData().length,true));
                        }else {
                            if(packet.getClipboardhash() == -1) {
                                ClipboardManager.getInstance().removeClipboard(player.getUniqueId());
                                MessageManager.sendMessage(player, "clipboard.clear");
                            }else if(packet.getClipboardhash() == -3){
                                PacketDataSerializer ser = new PacketDataSerializer(packet.getData());
                                MessageManager.sendMessage(player,"clipboard.tooBig",
                                        StringUtils.humanReadableByteCount(WorldEditGlobalizerBungee.getInstance().getMainConfig().getMaxClipboardBytes(),true),
                                        StringUtils.humanReadableByteCount(ser.readLong(),true));
                            }
                        }
                    }else{

                    }
                }
            };
            PacketSender.sendPacket(player,req);
            callback.setUserData(player);
            MessageManager.sendMessage(player,"clipboard.start.uploading");
        }else {
            MessageManager.sendMessage(sender,"command.console");
        }
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
    public void reload(CommandSender sender){
        MessageManager.sendMessage(sender,"command.start.reload");
        WorldEditGlobalizerBungee.getInstance().getConfigManager().reloadAllConfigs();
        MessageManager.getInstance().reload();
        String lang = WorldEditGlobalizerBungee.getInstance().getMainConfig().getLanguage();
        if(!MessageManager.getInstance().hasMessageFile(lang)){
            MessageFile file = new CustomMessageFile(lang,new File(MessageManager.getInstance().getMessageFolder(),"messages_"+lang+".yml"));
            MessageManager.getInstance().addMessageFile(file);
        }
        MessageManager.getInstance().setLanguage(lang);
        MessageManager.getInstance().setPrefix(ChatColor.translateAlternateColorCodes('&',WorldEditGlobalizerBungee.getInstance().getMainConfig().getPrefix()));

        MessageManager.sendMessage(sender,"command.finish.reload");
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
    public void info(CommandSender sender,  @Optional String playerName){

        String time = "";
        UUIDFetcher.PlayerData pd;
        String clipboardSize = "";

        if(playerName != null){
            if(!sender.hasPermission("worldeditglobalizer.command.info.other")){
                MessageManager.sendMessage(sender,"command.permissionDenied");
                return;
            }

            ProxiedPlayer player = BungeeCord.getInstance().getPlayer(playerName);
            if(player == null){
                pd = UUIDFetcher.getPlayerData(playerName);
            }else {
                pd = new UUIDFetcher.PlayerData(player.getUniqueId(),player.getName());
            }

            if(pd == null) {
                MessageManager.sendMessage(sender, "command.playerNotFound", playerName);
                return;
            }
            if(!ClipboardManager.getInstance().hasClipboard(pd.getUUID())){
                MessageManager.sendMessage(sender,"clipboard.empty.other",pd.getName());
                return;
            }
            time = new SimpleDateFormat(MessageManager.getInstance().getRawMessage("timeFormat")).format(new Date(ClipboardManager.getInstance().getClipboardFile(pd.getUUID()).lastModified()));
            clipboardSize = StringUtils.humanReadableByteCount(ClipboardManager.getInstance().getClipboardFile(pd.getUUID()).length(),true);
            MessageManager.sendMessage(sender,"command.info.format",pd.getName(),pd.getName(),pd.getUUID().toString(),time,clipboardSize);
        }else {
            if(sender instanceof ProxiedPlayer){
                ProxiedPlayer player = (ProxiedPlayer) sender;
                if(!ClipboardManager.getInstance().hasClipboard(player.getUniqueId())){
                    MessageManager.sendMessage(sender,"clipboard.empty.own");
                    return;
                }
                time = new SimpleDateFormat(MessageManager.getInstance().getRawMessage("timeFormat")).format(new Date(ClipboardManager.getInstance().getClipboardFile(player.getUniqueId()).lastModified()));
                clipboardSize = StringUtils.humanReadableByteCount(ClipboardManager.getInstance().getClipboardFile(player.getUniqueId()).length(),true);
                MessageManager.sendMessage(sender,"command.info.format",player.getName(),player.getName(),player.getUniqueId().toString(),time,clipboardSize);
            }else {
                MessageManager.sendMessage(sender,"command.console");
            }
        }
    }


    @Command(
            aliases = {"stats","statistics"},
            min = 0,
            max = 0,
            help = "stats",
            desc = "shows statistics about the plugin"
    )
    @Require(
            value = "worldeditglobalizer.command.stats"
    )
    public void stats(CommandSender sender){
        List<Long> sizes = new ArrayList<>();
        long full = 0;
        for(UUID uuid : ClipboardManager.getInstance().getSavedClipboards()){
            long size = ClipboardManager.getInstance().getClipboardFile(uuid).length();
            sizes.add(size);
            full+=size;
        }

        long average = 0;
        try {
            average = full / (long) sizes.size();
        }catch (ArithmeticException e){

        }


        MessageManager.sendMessage(sender,"command.stats.format",sizes.size(),StringUtils.humanReadableByteCount(full,true),StringUtils.humanReadableByteCount(average,true));
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
    public void schematic(CommandSender sender, @Optional @Text String args){
        CommandManager.getInstance().handleSubCommand("weg schematic","schematic",args,sender);
    }

}
