package me.illgilp.worldeditglobalizerbungee.runnables;

import com.google.gson.Gson;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class UpdateRunnable implements Runnable {
    @Override
    public void run() {
        while (true) {
            final String USER_AGENT = "WorldEditGlobalizer-UpdateCheck-User-Agent";
            final String REQUEST_URL = "https://raw.githubusercontent.com/IllgiLP/WorldEditGlobalizer/1.13/version.json";
            try {
                URL url = new URL(REQUEST_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.addRequestProperty("User-Agent", USER_AGENT);
                InputStream inputStream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                Map<String, Object> map = new Gson().fromJson(reader, Map.class);

                if (isNewerVersion(WorldEditGlobalizerBungee.getInstance().getDescription().getVersion(), map.get("latest") + "")) {
                    for (ProxiedPlayer player : BungeeCord.getInstance().getPlayers()) {
                        if (player.hasPermission("weg.update.notify")) {
                            MessageManager.sendMessage(player, "update.notify", WorldEditGlobalizerBungee.getInstance().getDescription().getVersion(), map.get("latest"), map.get("msg"), "https://www.spigotmc.org/resources/worldeditglobalizer.51527/");
                        }
                    }
                }


            } catch (IOException e) {
                // TODO: handle exception
                e.printStackTrace();
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }

        }
    }

    private boolean isNewerVersion(String currentVersion, String newVersion) {
        String[] cSplit = currentVersion.replaceAll("(v)|(-SNAPSHOT)", "").split("\\.");
        String[] nSplit = newVersion.replaceAll("(v)|(-SNAPSHOT)", "").split("\\.");
        for (int i = 0; i < Math.max(cSplit.length, nSplit.length); i++) {
            if (cSplit.length <= i) {
                return true;
            } else if (nSplit.length <= i) {
                return false;
            }
            if (cSplit[i].matches("[0-9]+") && nSplit[i].matches("[0-9]+")) {
                int cInt = Integer.parseInt(cSplit[i]);
                int nInt = Integer.parseInt(nSplit[i]);
                if (cInt == nInt) {
                    continue;
                }
                return cInt < nInt;
            }
            int compareResult = cSplit[i].compareTo(nSplit[i]);
            if (compareResult == 0) {
                continue;
            }
            return compareResult < 0;
        }
        return false;
    }

}
