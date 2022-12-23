plugins {
    `java-library`
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.github.johnrengelman.shadow")

    dependencies {
        "compileOnly"("org.spigotmc:spigot-api:1.8-R0.1-SNAPSHOT")
    }
}


