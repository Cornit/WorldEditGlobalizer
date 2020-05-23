package me.illgilp.worldeditglobalizerbukkit.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.illgilp.worldeditglobalizerbukkit.network.PacketSender;
import me.illgilp.worldeditglobalizercommon.network.packets.MessageRequestPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.MessageResponsePacket;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;

public class MessageManager {

    private static MessageManager instance;
    private Map<UUID, String> tmpPath = new HashMap<>();
    private Map<UUID, MessageResponsePacket> tmpResponse = new HashMap<>();

    private MessageManager() {
    }

    public static TextComponent sendMessage(Player player, String path, Object... placeholders) {
        TextComponent textComponent = getInstance().getMessage(player, path, placeholders);
        if (textComponent != null) {
            player.spigot().sendMessage(textComponent);
        }
        return textComponent;
    }

    public static MessageManager getInstance() {

        if (instance == null) instance = new MessageManager();

        return instance;
    }

    public TextComponent getMessage(Player player, String path, Object... placeholder) {
        if (player == null || path == null) return null;
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
            return res.getJson().equalsIgnoreCase("none") ? null : new TextComponent(ComponentSerializer.parse(res.getJson()));
        } else {
            tmpPath.remove(req.getIdentifier());
            tmpResponse.remove(req.getIdentifier());
            return null;
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
}
