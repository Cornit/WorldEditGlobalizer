package me.illgilp.worldeditglobalizerbukkit.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.illgilp.worldeditglobalizerbukkit.network.PacketSender;
import me.illgilp.worldeditglobalizercommon.network.packets.PermissionCheckRequestPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.PermissionCheckResponsePacket;
import org.bukkit.entity.Player;

public class PermissionManager {

    private static PermissionManager instance;
    private Map<UUID, String> tmpPerms = new HashMap<>();
    private Map<UUID, PermissionCheckResponsePacket> tmpResponse = new HashMap<>();

    private PermissionManager() {
    }

    public static PermissionManager getInstance() {

        if (instance == null) instance = new PermissionManager();

        return instance;
    }


    public boolean hasPermission(Player player, String permission) {
        if (player == null || permission == null) return false;

        tmpPerms.put(player.getUniqueId(), permission);
        PermissionCheckRequestPacket packet = new PermissionCheckRequestPacket();
        packet.setPermissions(new String[]{permission});
        packet.setPlayer(player.getUniqueId());
        PacketSender.sendPacket(player, packet);
        synchronized (tmpPerms.get(player.getUniqueId())) {
            try {
                tmpPerms.get(player.getUniqueId()).wait(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (tmpResponse.containsKey(player.getUniqueId())) {
            PermissionCheckResponsePacket responsePacket = tmpResponse.get(player.getUniqueId());
            if (responsePacket.getPermissions().containsKey(permission)) {
                tmpPerms.remove(player.getUniqueId());
                tmpResponse.remove(player.getUniqueId());
                return responsePacket.getPermissions().get(permission);
            }
        }
        tmpPerms.remove(player.getUniqueId());
        tmpResponse.remove(player.getUniqueId());
        return false;

    }

    public void callPermissionResponse(PermissionCheckResponsePacket packet) {
        if (tmpPerms.containsKey(packet.getPlayer()) && tmpPerms.get(packet.getPlayer()) != null) {
            tmpResponse.put(packet.getPlayer(), packet);
            synchronized (tmpPerms.get(packet.getPlayer())) {
                tmpPerms.get(packet.getPlayer()).notify();
            }
        }
    }
}
