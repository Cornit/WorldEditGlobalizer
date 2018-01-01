package me.illgilp.worldeditglobalizerbungee.events;

import me.illgilp.worldeditglobalizerbungee.network.packets.Packet;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.AsyncEvent;
import net.md_5.bungee.api.plugin.Event;

public class PacketReceivedEvent extends AsyncEvent<PacketReceivedEvent> {

    private ProxiedPlayer player;
    private Packet packet;
    private int packetid;
    private ServerConnection server;

    public PacketReceivedEvent(ProxiedPlayer player, Packet packet, int packetid, ServerConnection server) {
        super(new Callback<PacketReceivedEvent>() {
            @Override
            public void done(PacketReceivedEvent packetReceivedEvent, Throwable throwable) {

            }
        });
        this.player = player;
        this.packet = packet;
        this.packetid = packetid;
        this.server = server;
    }

    public ProxiedPlayer getPlayer() {
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
