package me.illgilp.worldeditglobalizersponge.events;


import me.illgilp.worldeditglobalizersponge.network.packets.Packet;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.entity.living.humanoid.player.TargetPlayerEvent;
import org.spongepowered.api.event.impl.AbstractEvent;

public class PacketReceivedEvent extends AbstractEvent implements TargetPlayerEvent {


    private Player player;
    private Packet packet;
    private int packetid;
    private Cause cause;

    public PacketReceivedEvent(Player player, Packet packet, int packetid, Cause cause) {
        this.player = player;
        this.packet = packet;
        this.packetid = packetid;
        this.cause = cause;
    }

    @Override
    public Player getTargetEntity() {
        return player;
    }

    public Packet getPacket() {
        return packet;
    }

    public int getPacketid() {
        return packetid;
    }

    @Override
    public Cause getCause() {
        return cause;
    }
}
