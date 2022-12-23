plugins {
    `java-library`
}
//applyCommonConfig()
applyBukkitWorldEditAdapterConfig()

repositories {
    maven(url = "https://maven.enginehub.org/repo")
    ivy {
        url = uri("https://ci.athion.net/job/FastAsyncWorldEdit-Legacy/lastSuccessfulBuild/artifact/target/")

        patternLayout {
            artifact("/[module]-[revision].jar")
        }

        metadataSources { artifact() }
    }
}

dependencies {
    "compileOnly"("com.sk89q.worldedit:worldedit-bukkit:6.1.5")
    "compileOnly"("com.sk89q.worldedit:worldedit-core:6.1.4-SNAPSHOT")
    "compileOnly"("ci.athion.net:FastAsyncWorldEdit-bukkit:21.03.26-5ff3a9b-1286-22.3.9")
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
