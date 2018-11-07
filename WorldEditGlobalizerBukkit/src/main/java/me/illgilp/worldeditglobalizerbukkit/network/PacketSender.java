package me.illgilp.worldeditglobalizerbukkit.network;


import me.illgilp.worldeditglobalizerbukkit.WorldEditGlobalizerBukkit;
import me.illgilp.worldeditglobalizerbukkit.util.PacketDataSerializer;
import me.illgilp.worldeditglobalizerbukkit.network.packets.Packet;
import me.illgilp.worldeditglobalizerbukkit.runnables.PacketRunnable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class PacketSender {

    private static Map<String,BukkitTask> tasks = new HashMap<>();

    public static boolean sendPacket(Player player, Packet packet){
        BukkitRunnable runnable = new PacketRunnable(player,packet) {
            @Override
            public void run() {
                PacketDataSerializer serializer = new PacketDataSerializer();
                getPacket().write(serializer);
                int maxPacketSize = 32750;
                int packetSize = serializer.toByteArray().length;
                if(packetSize > maxPacketSize){
                    byte[] data = serializer.toByteArray();
                    int pos = 0;
                    double end = (double)packetSize/(double)maxPacketSize;
                    String number = end+"";
                    String[] split = number.split("\\.");
                    long maxPos = Long.parseLong(split[0]);
                    long dec = Long.parseLong(split[1]);
                    if(dec > 0)maxPos++;
                    while (pos < maxPos){

                        int size = (((packetSize-(maxPacketSize*(pos))) < maxPacketSize) ? (packetSize-(maxPacketSize*(pos))):maxPacketSize);
                        byte[] datas = new byte[size];
                        int in = 0;
                        for(int offset = (maxPacketSize*pos); offset < ((maxPacketSize*pos)+size);offset++){
                            datas[in++] = data[offset];
                        }
                        PacketDataSerializer ser = new PacketDataSerializer();
                        ser.writeVarInt(WorldEditGlobalizerBukkit.getInstance().getPacketManager().getPacketId(Packet.Direction.TO_BUNGEE,getPacket().getClass()));
                        ser.writeBoolean(true);
                        ser.writeInt(Math.toIntExact(maxPos));
                        ser.writeInt(Math.toIntExact(pos));
                        ser.writeArray(datas);
                        if(getPlayer().isOnline()) {
                            getPlayer().sendPluginMessage(WorldEditGlobalizerBukkit.getInstance(),"worldeditglobalizer:connection", ser.toByteArray());
                        }

                        pos++;
                    }
                }else{
                    PacketDataSerializer ser = new PacketDataSerializer();
                    ser.writeVarInt(WorldEditGlobalizerBukkit.getInstance().getPacketManager().getPacketId(Packet.Direction.TO_BUNGEE,getPacket().getClass()));
                    ser.writeBoolean(false);
                    ser.writeArray(serializer.toByteArray());
                    if(getPlayer().isOnline()) {
                        getPlayer().sendPluginMessage(WorldEditGlobalizerBukkit.getInstance(),"worldeditglobalizer:connection", ser.toByteArray());
                    }
                }
                tasks.remove(getPlayer().getName());
            }
        };

        tasks.put(player.getName(),runnable.runTaskAsynchronously(WorldEditGlobalizerBukkit.getInstance()));
        return true;
    }

}
