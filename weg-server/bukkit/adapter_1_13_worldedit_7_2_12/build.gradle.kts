plugins {
    `java-library`
}

applyBukkitWorldEditAdapterConfig()

repositories {
    maven(url = "https://maven.enginehub.org/repo")
}

dependencies {
    "compileOnly"("com.sk89q.worldedit:worldedit-bukkit:7.2.12")
}

tasks.named<Copy>("processResources") {
    dependsOn("compileJava")
}
val annotationProcessorOutputDir = "${buildDir.absolutePath}/generated/sources/annotationProcessor/java/main"
java.sourceSets["main"].java {
    srcDir("${annotationProcessorOutputDir}/java")
}
java.sourceSets["main"].resources {
    srcDir("${annotationProcessorOutputDir}/resources")
}
