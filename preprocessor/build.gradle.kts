plugins {
    `java-library`
}

applyCommonConfig()

dependencies {
    annotationProcessor("com.google.auto.service:auto-service:1.0")
    implementation("com.google.auto.service:auto-service:1.0")
    "implementation"(project(":weg-server:bukkit:impl"))
}
