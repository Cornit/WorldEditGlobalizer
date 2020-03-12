package me.illgilp.worldeditglobalizerbukkit.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.illgilp.worldeditglobalizerbukkit.network.PacketSender;
import me.illgilp.worldeditglobalizercommon.network.packets.PluginConfigRequestPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.PluginConfigResponsePacket;
import org.bukkit.entity.Player;

public class ConfigManager {

    private static ConfigManager instance;

    private Map<UUID, String> tmpName = new HashMap<>();
    private Map<UUID, PluginConfigResponsePacket> tmpResponse = new HashMap<>();

    public static ConfigManager getInstance() {
        if (instance == null) instance = new ConfigManager();
        return instance;
    }

    public PluginConfigResponsePacket getPluginConfig(Player player) {
        if (player == null) {
            return getDefaultPluginConfigPacket();
        }
        PluginConfigRequestPacket req = new PluginConfigRequestPacket();
        tmpName.put(req.getIdentifier(), player.getName());
        PacketSender.sendPacket(player, req);
        synchronized (tmpName.get(req.getIdentifier())) {
            try {
                tmpName.get(req.getIdentifier()).wait(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (tmpResponse.containsKey(req.getIdentifier())) {
            PluginConfigResponsePacket res = tmpResponse.get(req.getIdentifier());
            tmpName.remove(req.getIdentifier());
            tmpResponse.remove(req.getIdentifier());
            return res;
        } else {
            tmpName.remove(req.getIdentifier());
            tmpResponse.remove(req.getIdentifier());
            return getDefaultPluginConfigPacket();
        }

    }

    private PluginConfigResponsePacket getDefaultPluginConfigPacket() {
        PluginConfigResponsePacket res = new PluginConfigResponsePacket();
        res.setKeepClipboard(false);
        res.setLanguage("en");
        res.setPrefix("§3WEG §7§l>> §r");
        res.setMaxClipboardSize(1024 * 1024 * 30);
        res.setEnableClipboardAutoDownload(true);
        res.setEnableClipboardAutoUpload(true);
        return res;
    }

    public void callPluginConfigResponse(PluginConfigResponsePacket packet) {
        if (tmpName.containsKey(packet.getIdentifier())) {
            tmpResponse.put(packet.getIdentifier(), packet);
            synchronized (tmpName.get(packet.getIdentifier())) {
                tmpName.get(packet.getIdentifier()).notify();
            }
        }
    }
}
