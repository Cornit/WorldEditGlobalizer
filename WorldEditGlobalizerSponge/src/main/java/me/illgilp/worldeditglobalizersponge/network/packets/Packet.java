package me.illgilp.worldeditglobalizersponge.network.packets;


import me.illgilp.worldeditglobalizersponge.util.PacketDataSerializer;

public abstract class Packet {

    public abstract void read(PacketDataSerializer buf);


    public abstract void write(PacketDataSerializer  buf);


    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();


    public enum Direction {
        TO_BUNGEE, TO_BUKKIT
    }

}
