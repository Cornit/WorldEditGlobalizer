
package me.illgilp.worldeditglobalizerbungee.network;






import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.events.PacketReceivedEvent;
import me.illgilp.worldeditglobalizerbungee.network.packets.Packet;
import me.illgilp.worldeditglobalizerbungee.util.PacketDataSerializer;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketManager {

    private Map<Packet.Direction,Map<Integer,Class>> registeredPackets = new HashMap<>();


    public void registerPacket(Packet.Direction direction, Class clazz, int packetId){
        int packetid = -1;
        packetid = packetId;

        if(packetid == -1){
            throw new IllegalArgumentException("PacketId for PacketClass: "+clazz.getName()+" wasn't set!");
        }
        Map<Integer,Class> packets = registeredPackets.get(direction);
        if(packets == null)packets = new HashMap<>();
        packets.put(packetid,clazz);
        registeredPackets.put(direction,packets);

    }

    public Map<Packet.Direction, Map<Integer, Class>> getRegisteredPackets() {
        return registeredPackets;
    }

    public int getPacketId(Packet.Direction direction, Class clazz){
        for(int id : registeredPackets
                .get(direction)
                .keySet()){
            if(registeredPackets.get(direction).get(id).equals(clazz)){
                return id;
            }
        }
        return -1;
    }

   public void callPacket(ProxiedPlayer player, int packetId, byte[] data, ServerConnection server){
       //System.out.println(0);
       try{
           if(registeredPackets
                   .get(Packet.Direction.TO_BUNGEE)
                   .containsKey(packetId)) {
               //System.out.println(1);
               Packet packet = null;
               try {
                   //System.out.println(2);
                   packet = (Packet) registeredPackets.get(Packet.Direction.TO_BUNGEE).get(packetId).newInstance();
                   //System.out.println(3);
                   packet.read(new PacketDataSerializer(data));
               } catch (InstantiationException e) {
                   e.printStackTrace();
               } catch (IllegalAccessException e) {
                   e.printStackTrace();
               }

               PacketReceivedEvent event = new PacketReceivedEvent(player,packet,packetId,server);
               WorldEditGlobalizerBungee.getInstance().getProxy().getPluginManager().callEvent(event);
           }
       }catch (Exception e){
           e.printStackTrace();
       }
       return;
   }



}
