package me.illgilp.worldeditglobalizerbungee.network;

import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.network.packets.Packet;
import me.illgilp.worldeditglobalizerbungee.runnables.PacketRunnable;
import me.illgilp.worldeditglobalizerbungee.util.PacketDataSerializer;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.scheduler.BungeeTask;

import java.util.HashMap;
import java.util.Map;

public class PacketSender {

    private static Map<String,ScheduledTask> tasks = new HashMap<>();

    public static boolean sendPacket(ProxiedPlayer player, Packet packet){
        Runnable runnable = new PacketRunnable(player,packet) {
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
                        ser.writeVarInt(WorldEditGlobalizerBungee.getInstance().getPacketManager().getPacketId(Packet.Direction.TO_BUKKIT,getPacket().getClass()));
                        ser.writeBoolean(true);
                        ser.writeInt(Math.toIntExact(maxPos));
                        ser.writeInt(Math.toIntExact(pos));
                        ser.writeArray(datas);
                        if(getPlayer().isConnected()) {
                            getPlayer().getServer().sendData("WorldEditGlobalizer", ser.toByteArray());
                        }

                        pos++;
                    }
                }else{
                    PacketDataSerializer ser = new PacketDataSerializer();
                    ser.writeVarInt(WorldEditGlobalizerBungee.getInstance().getPacketManager().getPacketId(Packet.Direction.TO_BUKKIT,getPacket().getClass()));
                    ser.writeBoolean(false);
                    ser.writeArray(serializer.toByteArray());
                    if(getPlayer().isConnected()) {
                        if(getPlayer() != null) {
                            if (getPlayer().getServer() != null) {
                                if (ser != null) {
                                    getPlayer().getServer().sendData("WorldEditGlobalizer", ser.toByteArray());
                                }
                            }
                        }
                    }
                }
                tasks.remove(getPlayer().getName());
            }
        };

        tasks.put(player.getName(),BungeeCord.getInstance().getScheduler().runAsync(WorldEditGlobalizerBungee.getInstance(),runnable));
        return true;
    }

}
