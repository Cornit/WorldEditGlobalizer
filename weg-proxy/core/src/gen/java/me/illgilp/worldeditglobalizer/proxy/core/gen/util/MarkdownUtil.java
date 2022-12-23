package me.illgilp.worldeditglobalizer.proxy.core.gen.util;

public class MarkdownUtil {

    public static String getIdFromHeadingText(String headingText) {
        headingText = headingText.toLowerCase();
        headingText = headingText.replace(" ", "-");
        String s;
        String oldHeading;
        do {
            s = headingText.replace("--", "-");
            oldHeading = headingText;
            if (!s.equals(oldHeading)) {
                headingText = s;
            }
        } while (!s.equals(oldHeading));
        StringBuilder b = new StringBuilder();
        for (char c : headingText.toCharArray()) {
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c > 127 || c == '-') {
                b.append(c);
            }
        }
        return b.toString();
    }

}
