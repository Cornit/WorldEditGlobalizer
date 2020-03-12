package me.illgilp.worldeditglobalizerbungee.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringMathParser {

    public static long parseString(String string) {
        Pattern pattern = Pattern.compile("[0-9\\*]+");
        Matcher matcher = pattern.matcher(string);
        if (!matcher.matches()) {
            return -1;
        }

        long data = 0;

        String[] spl = string.split("\\*");
        data = Long.parseLong(spl[0]);

        for (int i = 1; i < spl.length; i++) {
            data = data * Long.parseLong(spl[i]);
        }

        return data;
    }

}
