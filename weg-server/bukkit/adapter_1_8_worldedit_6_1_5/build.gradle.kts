plugins {
    `java-library`
}
//applyCommonConfig()
applyBukkitWorldEditAdapterConfig()

repositories {
    maven(url = "https://maven.enginehub.org/repo")
}

dependencies {
    "compileOnly"("com.sk89q.worldedit:worldedit-bukkit:6.1.5")
    "compileOnly"("com.sk89q.worldedit:worldedit-core:6.1.4-SNAPSHOT")
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
