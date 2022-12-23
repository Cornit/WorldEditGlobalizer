rootProject.name = "worldeditglobalizer"
include("weg-common")
include("weg-proxy", "weg-proxy:core", "weg-proxy:bungeecord")
include(
    "weg-server",
    "weg-server:core",
    "weg-server:bukkit",
    "weg-server:bukkit:impl",
    "weg-server:bukkit:adapter_1_13_worldedit_7_2_12",
    "weg-server:bukkit:adapter_1_8_worldedit_6_1_5",
    "weg-server:bukkit:adapter_1_8_fawe_22_3_9",
    "weg-server:bukkit:adapter_1_13_fawe_2_5_1",
)
include("preprocessor")
