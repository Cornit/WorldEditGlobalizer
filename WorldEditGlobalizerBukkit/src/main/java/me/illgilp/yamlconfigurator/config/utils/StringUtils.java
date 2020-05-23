package me.illgilp.yamlconfigurator.config.utils;

import me.illgilp.yamlconfigurator.config.ConfigManager;
import net.md_5.bungee.api.ChatColor;

public class StringUtils {
    public static String replacePlaceholder(String string, ConfigManager configManager) {
        String st = string;
        for (String pl : configManager.getPlaceholders().keySet()) {
            st = st.replace(pl, configManager.getPlaceholders().get(pl) + "");
        }
        return st;
    }

    public static String[] replacePlaceholderInArray(String[] string, ConfigManager configManager) {
        String[] res = new String[string.length];
        int i = 0;
        for (String sts : string) {
            String st = sts;
            for (String pl : configManager.getPlaceholders().keySet()) {
                st = st.replace(pl, configManager.getPlaceholders().get(pl) + "");
            }
            res[i] = st;
            i++;
        }
        return res;
    }

    public static String removeColorCodes(String s) {
        for (ChatColor value : ChatColor.values()) {
            s = s.replace(value.toString(), "");
        }
        return s;
    }

}


