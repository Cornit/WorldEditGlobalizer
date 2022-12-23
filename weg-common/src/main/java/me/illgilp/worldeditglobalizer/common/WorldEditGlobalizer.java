package me.illgilp.worldeditglobalizer.common;

public class WorldEditGlobalizer {

    private static WegManifest WEG_MANIFEST;

    public static WegVersion getVersion() {
        if (WEG_MANIFEST == null) {
            WEG_MANIFEST = WegManifest.load();
        }
        return WEG_MANIFEST.getVersion();
    }

    public static String getBuildTag() {
        if (WEG_MANIFEST == null) {
            WEG_MANIFEST = WegManifest.load();
        }
        return WEG_MANIFEST.getBuildTag();
    }

}
