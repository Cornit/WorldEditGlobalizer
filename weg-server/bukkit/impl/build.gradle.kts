plugins {
    `java-library`
}
applyCommonConfig()

dependencies {
    "api"(project(":weg-server:core"))
    "api"("net.kyori:adventure-platform-bukkit:${Versions.ADVENTURE_PLATFORM}")
}

tasks.named<Copy>("processResources") {
    inputs.property("version", rootProject.version)
    filesMatching("plugin.yml") {
        expand("version" to rootProject.version)
    }
}
