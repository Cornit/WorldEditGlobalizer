package me.illgilp.worldeditglobalizerbungee.util;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;

public class UUIDFetcher {

    /**
     * @param player The player
     * @return The UUID of the given player
     */
    //Uncomment this if you want the helper method for BungeeCord:
	/*
	public static UUID getUUID(ProxiedPlayer player) {
		return getUUID(player.getName());
	}
	*/

    /**
     * @param player The player
     * @return The UUID of the given player
     */
    //Uncomment this if you want the helper method for Bukkit/Spigot:
	/*
	public static UUID getUUID(Player player) {
		return getUUID(player.getName());
	}
	*/

    /**
     * @param playername The name of the player
     * @return The UUID of the given player
     */
    public static PlayerData getPlayerData(String playername) {
        String output = callURL("https://api.mojang.com/users/profiles/minecraft/" + playername);

        StringBuilder result = new StringBuilder();
        if (output.isEmpty()) {
            return null;
        }
        Map<String, Object> map = readData(output);

        String u = (String) map.get("id");

        String uuid = "";

        for (int i = 0; i <= 31; i++) {
            uuid = uuid + u.charAt(i);
            if (i == 7 || i == 11 || i == 15 || i == 19) {
                uuid = uuid + "-";
            }
        }

        return new PlayerData(UUID.fromString(uuid), (String) map.get("name"));
    }

    private static Map<String, Object> readData(String toRead) {
        return new Gson().fromJson(toRead, Map.class);
    }

    private static String callURL(String URL) {
        StringBuilder sb = new StringBuilder();
        URLConnection urlConn = null;
        InputStreamReader in = null;
        try {
            URL url = new URL(URL);
            urlConn = url.openConnection();

            if (urlConn != null) urlConn.setReadTimeout(60 * 1000);

            if (urlConn != null && urlConn.getInputStream() != null) {
                in = new InputStreamReader(urlConn.getInputStream(), Charset.defaultCharset());
                BufferedReader bufferedReader = new BufferedReader(in);

                if (bufferedReader != null) {
                    int cp;

                    while ((cp = bufferedReader.read()) != -1) {
                        sb.append((char) cp);
                    }

                    bufferedReader.close();
                }
            }

            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public static class PlayerData {

        private UUID uuid;
        private String name;

        public PlayerData(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        public UUID getUUID() {
            return uuid;
        }

        public String getName() {
            return name;
        }
    }
}