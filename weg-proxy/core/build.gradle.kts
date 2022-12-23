plugins {
    `java-library`
}


applyCommonConfig()
val genImplementation: Configuration by configurations.creating
dependencies {
    "api"(project(":weg-common"))
    "genImplementation"("com.github.Steppschuh:Java-Markdown-Generator:master-SNAPSHOT")
}

repositories {
    maven(url = "https://jitpack.io")
}

java.sourceSets.create("gen")

sourceSets["gen"].compileClasspath += sourceSets.main.get().compileClasspath
sourceSets["gen"].compileClasspath += sourceSets.main.get().output
sourceSets["gen"].runtimeClasspath += sourceSets.main.get().runtimeClasspath
sourceSets["gen"].runtimeClasspath += sourceSets.main.get().output

val generateCommandAndPermissionsDocumentation =
    tasks.register("generateCommandAndPermissionsDocumentation", JavaExec::class.java) {
        group = "documentation"
        classpath = sourceSets["gen"].runtimeClasspath
        mainClass.set("me.illgilp.worldeditglobalizer.proxy.core.gen.CommandDocumentationGenerator")
        workingDir = rootProject.projectDir
    }
tasks.build.get().dependsOn(generateCommandAndPermissionsDocumentation)
