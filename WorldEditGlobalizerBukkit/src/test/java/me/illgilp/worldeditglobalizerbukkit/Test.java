package me.illgilp.worldeditglobalizerbukkit;

import me.illgilp.worldeditglobalizerbukkit.util.PacketDataSerializer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Locale;
import java.util.logging.Level;

public class Test {

    public static void main(String[] args){
        double end = (double)5485594/(double)32750;
        System.out.println(end);
        String number = end+"";
        String[] split = number.split("\\.");
        long full = Long.parseLong(split[0]);
        long dec = Long.parseLong(split[1]);
        if(dec > 0)full++;
        System.out.println(full);

        //JKDSIKFJHNSF

        PacketDataSerializer serializer = new PacketDataSerializer();
        serializer.writeVarInt(1);
        serializer.writeBoolean(true);
        serializer.writeInt(1800674445);
        serializer.writeInt(6745453);
        System.out.println(serializer.toByteArray().length);

        String input = "fdszjgkhn";
        double max = 0;
        try {
            max = Long.parseLong(input);
        }catch (NumberFormatException e){
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            try {
                max = Double.parseDouble(""+engine.eval(input));
            } catch (ScriptException ex) {
                System.out.println("BAD INPUT");
            }
        }
        System.out.println(max);


    }

}
