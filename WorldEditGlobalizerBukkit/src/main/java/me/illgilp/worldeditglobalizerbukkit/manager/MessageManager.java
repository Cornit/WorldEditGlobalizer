package me.illgilp.worldeditglobalizerbukkit.manager;

import me.illgilp.worldeditglobalizerbukkit.network.PacketSender;
import me.illgilp.worldeditglobalizercommon.network.packets.MessageRequestPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.MessageResponsePacket;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessageManager {

    private static MessageManager instance;
    private Map<UUID, String> tmpPath = new HashMap<>();
    private Map<UUID, MessageResponsePacket> tmpResponse = new HashMap<>();

    private MessageManager() {
    }


    public String getMessage(Player player, String path, Object... placeholder) {
        if (player == null || path == null) return "none";
        MessageRequestPacket req = new MessageRequestPacket();
        req.setLanguage("default");
        req.setPath(path);
        req.setPlaceholders(placeholder);
        tmpPath.put(req.getIdentifier(), path);
        PacketSender.sendPacket(player, req);
        synchronized (tmpPath.get(req.getIdentifier())) {
            try {
                tmpPath.get(req.getIdentifier()).wait(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (tmpResponse.containsKey(req.getIdentifier())) {
            MessageResponsePacket res = tmpResponse.get(req.getIdentifier());
            tmpPath.remove(res.getIdentifier());
            tmpResponse.remove(res.getIdentifier());
            return res.getMessage();
        } else {
            tmpPath.remove(req.getIdentifier());
            tmpResponse.remove(req.getIdentifier());
            return "none";
        }

    }

    public void callMessageResponse(MessageResponsePacket packet) {
        if (tmpPath.containsKey(packet.getIdentifier())) {
            tmpResponse.put(packet.getIdentifier(), packet);
            synchronized (tmpPath.get(packet.getIdentifier())) {
                tmpPath.get(packet.getIdentifier()).notify();
            }
        }
    }


    public static String sendMessage(Player player, String path, Object... placeholders) {
        String msg = getInstance().getMessage(player, path, placeholders);
        if (!msg.equalsIgnoreCase("none")) {
            player.sendMessage(msg);
        }
        return msg;
    }

    public static MessageManager getInstance() {

        if (instance == null) instance = new MessageManager();

        return instance;
    }
}
