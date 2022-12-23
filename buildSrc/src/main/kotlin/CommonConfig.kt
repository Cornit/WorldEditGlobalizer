import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*


fun Project.applyCommonConfig() {
    apply(plugin = "com.github.johnrengelman.shadow")

    apply(plugin = "java-library")
    apply(plugin = "eclipse")
    apply(plugin = "idea")

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral {
            mavenContent {
                releasesOnly()
            }
        }
    }
    applyCommonRepositories()

    configurations.all {
        resolutionStrategy {
            cacheChangingModulesFor(5, "MINUTES")
        }
    }

    plugins.withId("java") {
        the<JavaPluginExtension>().sourceCompatibility = JavaVersion.VERSION_1_8
        the<JavaPluginExtension>().targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks
        .withType<JavaCompile>()
        .matching { it.name == "compileJava" || it.name == "compileTestJava" }
        .configureEach {
            options.encoding = "UTF-8"
            options.compilerArgs.add("-parameters")
        }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:${Versions.JUNIT}")
        "testImplementation"("org.junit.jupiter:junit-jupiter-params:${Versions.JUNIT}")
        "testImplementation"("org.mockito:mockito-core:${Versions.MOCKITO}")
        "testImplementation"("org.mockito:mockito-junit-jupiter:${Versions.MOCKITO}")
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT}")
        "compileOnly"("org.projectlombok:lombok:${Versions.LOMBOK}")
        "annotationProcessor"("org.projectlombok:lombok:${Versions.LOMBOK}")
        "testCompileOnly"("org.projectlombok:lombok:${Versions.LOMBOK}")
        "testAnnotationProcessor"("org.projectlombok:lombok:${Versions.LOMBOK}")
    }
}

fun Project.applyCommonRepositories() {
    repositories {
        maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
        maven { url = uri("https://hub.spigotmc.org/nexus/content/groups/public") }
        mavenCentral()
    }
}

fun Project.applyBukkitWorldEditAdapterConfig() {
    applyCommonConfig()
    dependencies {
        "annotationProcessor"(project(":preprocessor"))
        "api"(project(":weg-server:bukkit:impl"))
        "compileOnly"(project(":preprocessor"))
    }
}
