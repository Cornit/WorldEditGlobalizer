import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
}

apply(plugin = "com.github.johnrengelman.shadow")

group = "me.illgilp"
version = "3.0.1-beta"

applyCommonRepositories()

dependencies {
    implementation(project(path = ":weg-common", configuration = "shadow"))
    implementation(project(path = ":weg-proxy:core", configuration = "shadow"))
    implementation(project(path = ":weg-proxy:bungeecord", configuration = "shadow"))
    implementation(project(path = ":weg-server:core", configuration = "shadow"))
    implementation(project(path = ":weg-server:bukkit:impl", configuration = "shadow"))
    implementation(project(path = ":weg-server:bukkit:adapter_1_13_worldedit_7_2_12", configuration = "shadow"))
    implementation(project(path = ":weg-server:bukkit:adapter_1_8_worldedit_6_1_5", configuration = "shadow"))
    implementation(project(path = ":weg-server:bukkit:adapter_1_8_fawe_22_3_9", configuration = "shadow"))
    implementation(project(path = ":weg-server:bukkit:adapter_1_13_fawe_2_5_1", configuration = "shadow"))
}


tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
}

val relocations = mapOf(
    "net.kyori.adventure" to "me.illgilp.worldeditglobalizer.common.adventure",
    "net.kyori.examination" to "me.illgilp.worldeditglobalizer.common.util.kyori.examination",
    "org.yaml.snakeyaml" to "me.illgilp.worldeditglobalizer.common.util.yaml.snakeyaml",
    "com.google.gson" to "me.illgilp.worldeditglobalizer.common.util.gson",
)

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(tasks.named("clean"))
    archiveClassifier.set("")
    exclude("GradleStart**")
    exclude(".cache")
    exclude("LICENSE*")
    exclude("META-INF/maven/**")
    exclude("META-INF/versions/**")

    append("me/illgilp/worldeditglobalizer/server/bukkit/api/worldedit/adapter/adapters.txt")

    relocations.forEach { (from, to) ->
        relocate(from, to)
    }
}

tasks.named<Jar>("jar") {
    val version = rootProject.version
    inputs.property("version", version)
    val attributes = mutableMapOf(
        "Implementation-Version" to version,
        "WorldEditGlobalizer-Version" to version,
        "Build-Tag" to (System.getenv("GITHUB_REF_NAME") ?: "(unknown)")
    )
    manifest.attributes(attributes)
}
