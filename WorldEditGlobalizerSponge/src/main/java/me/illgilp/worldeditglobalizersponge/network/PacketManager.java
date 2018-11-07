
package me.illgilp.worldeditglobalizersponge.network;
import com.sk89q.worldedit.regions.iterator.FlatRegion3DIterator;
import me.illgilp.worldeditglobalizersponge.events.PacketReceivedEvent;
import me.illgilp.worldeditglobalizersponge.network.packets.Packet;
import me.illgilp.worldeditglobalizersponge.util.PacketDataSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;

import java.util.HashMap;
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

   public void callPacket(Player player, int packetId, byte[] data){
       //System.out.println(0);
       try{
           if(registeredPackets
                   .get(Packet.Direction.TO_BUKKIT)
                   .containsKey(packetId)) {
               //System.out.println(1);
               Packet packet = null;
               try {
                   //System.out.println(2);
                   packet = (Packet) registeredPackets.get(Packet.Direction.TO_BUKKIT).get(packetId).newInstance();
                   //System.out.println(3);
                   packet.read(new PacketDataSerializer(data));
               } catch (InstantiationException e) {
                   e.printStackTrace();
               } catch (IllegalAccessException e) {
                   e.printStackTrace();
               }
               PacketReceivedEvent event = new PacketReceivedEvent(player,packet,packetId, Cause.builder().append(player).build(EventContext.empty()));
               Sponge.getEventManager().post(event);
           }
       }catch (Exception e){
           e.printStackTrace();
       }
       return;
   }


}
