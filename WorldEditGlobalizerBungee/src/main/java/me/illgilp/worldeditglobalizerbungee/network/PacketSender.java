package me.illgilp.worldeditglobalizerbungee.network;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import me.illgilp.worldeditglobalizerbungee.player.Player;
import me.illgilp.worldeditglobalizerbungee.runnables.PacketRunnable;
import me.illgilp.worldeditglobalizerbungee.util.StringUtil;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;
import me.illgilp.worldeditglobalizercommon.network.packets.ClipboardSendPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.Packet;
import me.illgilp.worldeditglobalizercommon.util.Signature;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class PacketSender {

    private static Map<String, ScheduledTask> tasks = new HashMap<>();

    public static boolean sendPacket(Player player, Packet packet) {
        return sendPacket(player.getProxiedPlayer(), packet);
    }

    public static boolean sendPacket(ProxiedPlayer player, Packet packet) {
        Runnable runnable = new PacketRunnable(player, packet) {
            @Override
            public void run() {
                PacketDataSerializer serializer = new PacketDataSerializer();
                getPacket().write(serializer);
                int maxPacketSize = 32710;
                int packetSize = serializer.toByteArray().length;
                if (packetSize > maxPacketSize) {
                    byte[] data = serializer.toByteArray();
                    int pos = 0;
                    double end = (double) packetSize / (double) maxPacketSize;
                    String number = end + "";
                    String[] split = number.split("\\.");
                    long maxPos = Long.parseLong(split[0]);
                    long dec = Long.parseLong(split[1]);
                    if (dec > 0) maxPos++;
                    long writtenBytes = 0;
                    while (pos < maxPos) {

                        int size = (((packetSize - (maxPacketSize * (pos))) < maxPacketSize) ? (packetSize - (maxPacketSize * (pos))) : maxPacketSize);
                        byte[] datas = new byte[size];
                        int in = 0;
                        for (int offset = (maxPacketSize * pos); offset < ((maxPacketSize * pos) + size); offset++) {
                            datas[in++] = data[offset];
                        }
                        PacketDataSerializer ser = new PacketDataSerializer();
                        ser.writeVarInt(WorldEditGlobalizerBungee.getInstance().getPacketManager().getPacketId(Packet.Direction.TO_BUKKIT, getPacket().getClass()));
                        ser.writeBoolean(true);
                        ser.writeVarLong(packetSize);
                        ser.writeInt(Math.toIntExact(maxPos));
                        ser.writeInt(Math.toIntExact(pos));
                        ser.writeArray(datas);
                        if (getPlayer().isConnected()) {
                            PacketDataSerializer pp = new PacketDataSerializer();
                            Signature signature = new Signature();
                            signature.setKey(WorldEditGlobalizerBungee.getInstance().getMainConfig().getSecretKey().getBytes(StandardCharsets.UTF_8));
                            signature.setData(ser.toByteArray());
                            pp.writeByteArray(signature.sign());
                            pp.writeByteArray(signature.getData());
                            getPlayer().getServer().sendData("weg:connection", pp.toByteArray());
                            writtenBytes += datas.length;
                            int perc = (int) Math.round((double)writtenBytes / (double) packetSize * 100.0);
                            double decPerc = (double)writtenBytes / (double) packetSize;
                            String msg = MessageManager.getRawMessageOrEmpty("actionbar.progress.download", StringUtil.intToLengthedString(perc, 3));
                            msg = ChatColor.stripColor(msg);
                            int po = (int) Math.round((double) msg.length() * decPerc);
                            String fin = "§a";
                            if (po < msg.length()) {
                                fin += msg.substring(0, po);
                                fin += "§r";
                                fin += msg.substring(po);
                            } else {
                                fin += msg;
                            }

                            if (perc < 100 || !(packet instanceof ClipboardSendPacket)) {
                                getPlayer().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(fin));
                            } else {
                                MessageManager.sendActionBar(getPlayer(), "actionbar.progress.setClipboard");
                                //getPlayer().sendMessage(ChatMessageType.ACTION_BAR, MessageManager.getMessage());
                            }
                        } else {
                            return;
                        }

                        pos++;
                    }
                } else {
                    PacketDataSerializer ser = new PacketDataSerializer();
                    ser.writeVarInt(WorldEditGlobalizerBungee.getInstance().getPacketManager().getPacketId(Packet.Direction.TO_BUKKIT, getPacket().getClass()));
                    ser.writeBoolean(false);
                    ser.writeArray(serializer.toByteArray());
                    if (getPlayer().isConnected()) {
                        if (getPlayer() != null) {
                            if (getPlayer().getServer() != null) {
                                if (ser != null) {
                                    PacketDataSerializer pp = new PacketDataSerializer();
                                    Signature signature = new Signature();
                                    signature.setKey(WorldEditGlobalizerBungee.getInstance().getMainConfig().getSecretKey().getBytes(StandardCharsets.UTF_8));
                                    signature.setData(ser.toByteArray());
                                    pp.writeByteArray(signature.sign());
                                    pp.writeByteArray(signature.getData());
                                    getPlayer().getServer().sendData("weg:connection", pp.toByteArray());
                                }
                            }
                        }
                    }
                }
                tasks.remove(getPlayer().getName());
            }
        };

        tasks.put(player.getName(), BungeeCord.getInstance().getScheduler().runAsync(WorldEditGlobalizerBungee.getInstance(), runnable));
        return true;
    }

}
