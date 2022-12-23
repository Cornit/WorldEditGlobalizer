import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
}

applyCommonConfig()

dependencies {
    "api"(project(":weg-proxy:core"))
    for (module in listOf("api", "chat", "config", "event", "protocol")) {
        "compileOnly"("net.md-5:bungeecord-${module}:1.19-R0.1-SNAPSHOT")
    }
    "api"("net.kyori:adventure-platform-bungeecord:${Versions.ADVENTURE_PLATFORM}")
    "api"("org.bstats:bstats-bungeecord:3.0.0")
}

val relocations = mapOf(
    "org.bstats" to "me.illgilp.worldeditglobalizer.proxy.bungee.bstats"
)

tasks.named<ShadowJar>("shadowJar") {
    relocations.forEach { (from, to) ->
        relocate(from, to)
    }
}

tasks.named<Copy>("processResources") {
    inputs.property("version", rootProject.version)
    filesMatching("bungee.yml") {
        expand("version" to rootProject.version)
    }
}
