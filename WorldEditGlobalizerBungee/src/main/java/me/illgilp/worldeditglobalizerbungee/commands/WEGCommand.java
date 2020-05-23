package me.illgilp.worldeditglobalizerbungee.commands;

import me.illgilp.intake.CommandMapping;
import me.illgilp.worldeditglobalizerbungee.chat.chatevent.ChatEvent;
import me.illgilp.worldeditglobalizerbungee.manager.ChatEventManager;
import me.illgilp.worldeditglobalizerbungee.manager.CommandManager;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import me.illgilp.worldeditglobalizerbungee.manager.PlayerManager;
import me.illgilp.worldeditglobalizerbungee.util.ComponentUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class WEGCommand extends Command implements TabExecutor {


    public WEGCommand() {
        super("worldeditglobalizer", "", "weg");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender.hasPermission("worldeditglobalizer.command.weg")) {
            if (args.length == 0) {
                sender.sendMessage(ComponentUtils.addText(null, MessageManager.getInstance().getPrefix() + "§7Help: "));
                for (CommandMapping map : CommandManager.getInstance().getCommands()) {
                    sender.sendMessage(ComponentUtils.addText(null, "§6§l>> §r§f§o/weg " + map.getDescription().getHelp() + " §r§6= §a" + map.getDescription().getShortDescription()));
                }
            } else {
                if (args.length == 2) {
                    if (args[0].equals(ChatEvent.SECRET)) {
                        if (sender instanceof ProxiedPlayer) {
                            ChatEventManager.getInstance().callListener(PlayerManager.getInstance().getPlayer(((ProxiedPlayer) sender).getUniqueId()), args[1]);
                            return;
                        }
                    }
                }
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < args.length; i++) {
                    builder.append(args[i] + ((i < (args.length - 1)) ? " " : ""));
                }
                CommandManager.getInstance().handleCommand(builder.toString().trim(), sender);
            }
        } else {
            MessageManager.sendMessage(sender, "command.permissionDenied");
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] strings) {

        if (strings.length > 1) {
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < strings.length; i++) {
                builder.append(strings[i] + ((i < (strings.length - 1)) ? " " : ""));
            }
            if (CommandManager.getInstance().getSubCommands(strings[0]).size() == 0) {
                StringBuilder builder1 = new StringBuilder();
                for (int i = 0; i < strings.length; i++) {
                    builder1.append(strings[i] + ((i < (strings.length - 1)) ? " " : ""));
                }
                return CommandManager.getInstance().getSuggestions(builder1.toString(), commandSender);
            }
            return CommandManager.getInstance().getSubSuggestions(strings[0], builder.toString(), commandSender);
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            builder.append(strings[i] + ((i < (strings.length - 1)) ? " " : ""));
        }
        return CommandManager.getInstance().getSuggestions(builder.toString(), commandSender);
    }
}
