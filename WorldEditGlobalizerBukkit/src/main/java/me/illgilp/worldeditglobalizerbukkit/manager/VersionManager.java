package me.illgilp.worldeditglobalizerbukkit.manager;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.illgilp.worldeditglobalizerbukkit.version.Version;
import org.bukkit.Bukkit;

public class VersionManager {

    private static List<Version> SUPPORTED_VERSIONS = Arrays.asList(
        new Version(1,13),
        new Version(1,8)
    );

    private static VersionManager instance;

    public static VersionManager getInstance() {
        return instance == null ? (instance = new VersionManager()) : instance;
    }

    private Version minecraftVersion;

    private Version usedVersion;
    private WorldEditManager worldEditManager;

    private VersionManager() {
        Matcher mcVersionMatcher = Pattern.compile("v(\\d+)\\_+(\\d+)\\_+R(\\d+)").matcher(Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3]);
        if (mcVersionMatcher.find()) {
            minecraftVersion = new Version(Integer.parseInt(mcVersionMatcher.group(1)), Integer.parseInt(mcVersionMatcher.group(2)));
        }
        if (minecraftVersion != null) {
            for (Version supportedVersion : SUPPORTED_VERSIONS) {
                if (minecraftVersion.compareTo(supportedVersion) >= 0) {
                    usedVersion = supportedVersion;
                    break;
                }
            }

            if (usedVersion != null) {
                try {
                    Class<? extends WorldEditManager> wemClass = (Class<? extends WorldEditManager>) Class.forName("me.illgilp.worldeditglobalizerbukkit.version.v" + usedVersion.getMajor() + "_" + usedVersion.getMinor() + ".WorldEditManager_"+ usedVersion.getMajor() + "_" + usedVersion.getMinor());
                    worldEditManager = wemClass.newInstance();
                } catch (ClassNotFoundException e) {
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Version getMinecraftVersion() {
        return minecraftVersion;
    }

    public Version getUsedVersion() {
        return usedVersion;
    }

    public WorldEditManager getWorldEditManager() {
        return worldEditManager;
    }
}
