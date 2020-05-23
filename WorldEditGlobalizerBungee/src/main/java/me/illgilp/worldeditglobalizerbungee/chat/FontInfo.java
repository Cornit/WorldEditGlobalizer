package me.illgilp.worldeditglobalizerbungee.chat;

public class FontInfo {
    public static int getPxLength(char c) {
        switch(c) {
            case '*':
            case 't':
            case ' ':
                return 3;
            case ':':
            case 'i':
            case '.':
                return 1;
            case 'f':
            case 'k':
                return 4;
            case 'l':
                return 2;
            default:
                return 5;
        }
    }
    public static int getPxLength(String string) {

        int len = 0;

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c == '§') {
                i += 1;
                continue;
            }
            len += getPxLength(c) + 1;
        }

        return len;
    }

    public static String characterToString(char ch, int pxLength) {
        String s = "";
        while (getPxLength(s) <= pxLength) {
            s+=ch;
        }
        return s;
    }

    public static String addNewLineAfterTooLong(String text, int maxSize) {
        StringBuilder result = new StringBuilder();
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            line.append(c);
            if (getPxLength(line.toString()) >= maxSize) {
                result.append(line);
                line = new StringBuilder();
                if (i < (text.length() - 1)) {
                    result.append("\n");
                }
            }
        }
        if (line.length() > 0) {
            result.append(line);
        }
        return result.toString();
    }
}
