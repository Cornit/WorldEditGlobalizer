package me.illgilp.worldeditglobalizersponge.network;


import me.illgilp.worldeditglobalizersponge.WorldEditGlobalizerSponge;
import me.illgilp.worldeditglobalizersponge.network.packets.Packet;
import me.illgilp.worldeditglobalizersponge.runnables.PacketRunnable;
import me.illgilp.worldeditglobalizersponge.task.AsyncTask;
import me.illgilp.worldeditglobalizersponge.task.QueuedAsyncTask;
import me.illgilp.worldeditglobalizersponge.util.PacketDataSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.scheduler.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PacketSender {

    private static Map<String,AsyncTask> tasks = new HashMap<>();

    public static boolean sendPacket(Player player, Packet packet){
        QueuedAsyncTask runnable = new PacketRunnable(player,packet) {
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
                        ser.writeVarInt(WorldEditGlobalizerSponge.getInstance().getPacketManager().getPacketId(Packet.Direction.TO_BUNGEE,getPacket().getClass()));
                        ser.writeBoolean(true);
                        ser.writeInt(Math.toIntExact(maxPos));
                        ser.writeInt(Math.toIntExact(pos));
                        ser.writeArray(datas);
                        if(getPlayer().isOnline()) {
                            System.out.println("LOL1");
                            WorldEditGlobalizerSponge.getInstance().getDataChannel().sendTo(getPlayer(), new Consumer<ChannelBuf>() {
                                @Override
                                public void accept(ChannelBuf channelBuf) {
                                    channelBuf.writeBytes(ser.toByteArray());
                                }
                            });
                        }

                        pos++;
                    }
                }else{
                    PacketDataSerializer ser = new PacketDataSerializer();
                    ser.writeVarInt(WorldEditGlobalizerSponge.getInstance().getPacketManager().getPacketId(Packet.Direction.TO_BUNGEE,getPacket().getClass()));
                    ser.writeBoolean(false);
                    ser.writeArray(serializer.toByteArray());
                    if(getPlayer().isOnline()) {
                        System.out.println("LOL2");
                        WorldEditGlobalizerSponge.getInstance().getDataChannel().sendTo(getPlayer(), new Consumer<ChannelBuf>() {
                            @Override
                            public void accept(ChannelBuf channelBuf) {
                                channelBuf.writeBytes(ser.toByteArray());
                            }
                        });
                    }
                }
                tasks.remove(getPlayer().getName());
            }
        };
        runnable.addToQueue();
        tasks.put(player.getName(),runnable);
        return true;
    }

}
