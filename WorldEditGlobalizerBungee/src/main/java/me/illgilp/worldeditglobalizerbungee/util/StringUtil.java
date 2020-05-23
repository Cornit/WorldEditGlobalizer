package me.illgilp.worldeditglobalizerbungee.util;

public class StringUtil {

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String intToLengthedString(int number, int length) {
        String num = Integer.toString(number);
        int zeros = Math.max(length - num.length(), 0);
        StringBuilder finalString = new StringBuilder();
        for (int i = 0; i < zeros; i++) {
            finalString.append("0");
        }
        finalString.append(num);
        return finalString.toString();
    }


}
