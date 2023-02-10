import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("net.kyori.blossom") version "1.2.0"
}

applyCommonConfig()

blossom {
    replaceToken("@version@", rootProject.version)
}

dependencies {
    "api"(project(":weg-proxy:core"))
    "compileOnly"("com.velocitypowered:velocity-api:3.1.1")
    "testImplementation"("com.velocitypowered:velocity-api:3.1.1")
    "annotationProcessor"("com.velocitypowered:velocity-api:3.1.1")
    "api"("org.bstats:bstats-velocity:3.0.0")
}

val relocations = mapOf(
    "org.bstats" to "me.illgilp.worldeditglobalizer.proxy.velocity.bstats"
)

tasks.named<ShadowJar>("shadowJar") {
    relocations.forEach { (from, to) ->
        relocate(from, to)
    }
}
