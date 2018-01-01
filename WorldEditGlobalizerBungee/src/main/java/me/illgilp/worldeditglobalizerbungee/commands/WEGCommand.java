package me.illgilp.worldeditglobalizerbungee.commands;

import com.sk89q.intake.CommandMapping;
import me.illgilp.worldeditglobalizerbungee.manager.CommandManager;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import me.illgilp.worldeditglobalizerbungee.util.ComponentUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class WEGCommand extends Command {


    public WEGCommand() {
        super("worldeditglobalizer", "", "weg");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender.hasPermission("worldeditglobalizer.command.weg")){
        if(args.length == 0){
            sender.sendMessage(ComponentUtils.addText(null,MessageManager.getInstance().getPrefix()+"§7Help: "));
            for(CommandMapping map : CommandManager.getInstance().getCommands()){
                sender.sendMessage(ComponentUtils.addText(null,"§6§l>> §r§f§o/weg "+map.getDescription().getHelp()+" §r§6= §a"+map.getDescription().getShortDescription()));
            }
        }else{
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i<args.length;i++){
                builder.append(args[i]+((i <(args.length-1)) ? " ":""));
            }
            CommandManager.getInstance().handleCommand(builder.toString().trim(), sender);
        }
        }else {
            MessageManager.sendMessage(sender,"command.permissionDenied");
        }

    }
}
