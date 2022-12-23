plugins {
    `java-library`
}

applyCommonConfig()


sourceSets {
    main {
        java {
            srcDirs(
                "build/generated/sources/translationkeys/main/java",
                "build/generated/sources/permissions/main/java",
            )
        }
    }
}

dependencies {
    "api"("net.kyori:adventure-api:${Versions.ADVENTURE}")
    "api"("net.kyori:adventure-text-serializer-gson:${Versions.ADVENTURE}")
    "api"("net.kyori:adventure-text-serializer-plain:${Versions.ADVENTURE}")
    "api"("net.kyori:adventure-text-minimessage:${Versions.ADVENTURE}")
    "api"("org.yaml:snakeyaml:${Versions.SNAKE_YAML}")
}

tasks.named<Copy>("processResources") {
    exclude("permissions.json")
}

registerTranslationKeySourcesGenerationTask(sourceSets.main.get().resources.srcDirs)
registerPermissionSourcesGenerationTask()
