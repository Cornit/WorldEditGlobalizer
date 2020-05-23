package me.illgilp.worldeditglobalizerbungee.events;

import me.illgilp.worldeditglobalizerbungee.player.Player;
import me.illgilp.worldeditglobalizercommon.network.packets.Packet;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.event.AsyncEvent;

public class PacketReceivedEvent extends AsyncEvent<PacketReceivedEvent> {

    private Player player;
    private Packet packet;
    private int packetid;
    private ServerConnection server;

    public PacketReceivedEvent(Player player, Packet packet, int packetid, ServerConnection server) {
        super((packetReceivedEvent, throwable) -> {});
        this.player = player;
        this.packet = packet;
        this.packetid = packetid;
        this.server = server;
    }

    public Player getPlayer() {
        return player;
    }

    public Packet getPacket() {
        return packet;
    }

    public int getPacketId() {
        return packetid;
    }

    public ServerConnection getServer() {
        return server;
    }
}
