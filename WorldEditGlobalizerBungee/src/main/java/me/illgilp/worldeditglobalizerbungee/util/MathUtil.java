package me.illgilp.worldeditglobalizerbungee.util;

public class MathUtil {

    public static int getPages(int size, int pageSize) {
        double pagesC = (double) size / (double) pageSize;
        String[] spl = Double.toString(pagesC).split("\\.");
        long first = Long.parseLong(spl[0]);
        long second = Long.parseLong(spl[1]);
        if (second > 0) first++;
        return new Long(first).intValue();
    }

}
