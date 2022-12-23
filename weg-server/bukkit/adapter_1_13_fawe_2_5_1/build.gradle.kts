plugins {
    `java-library`
}

applyBukkitWorldEditAdapterConfig()

repositories {
    ivy {
        url = uri("https://ci.athion.net/job/FastAsyncWorldEdit/lastSuccessfulBuild/artifact/artifacts/")

        patternLayout {
            artifact("/[module]-[revision].jar")
        }

        metadataSources { artifact() }
    }
}

dependencies {
    "compileOnly"("ci.athion.net:FastAsyncWorldEdit-Bukkit:2.5.1-SNAPSHOT-332")
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
