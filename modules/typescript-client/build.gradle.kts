import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("base") // clean task, delete build/
    id("org.openapi.generator")
}

val npmScope = "@m-amirazmi"
val npmName = "rms-api-client"
val npmVersion = project.version.toString()
val npmDescription = "Repair Management System Contracts"

val baseConfigOptions = mapOf(
    "supportsES6" to "true",
    "withSeparateModelsAndApi" to "true",
    "withInterfaces" to "true",
    "snapshot" to "false",
)

val genOutputDir = layout.buildDirectory.dir("generated")

// Disable the default task to use our custom one
tasks.named("openApiGenerate") {
    enabled = false
}

// Register the generateTs task
tasks.register<GenerateTask>("generateTs") {
    description = "TypeScript client generation"
    generatorName.set("typescript-fetch")
    inputSpec.set("$rootDir/specs/api-spec.yaml")
    outputDir.set(genOutputDir.get().asFile)

    // NPM configuration
    configOptions.set(baseConfigOptions + mapOf(
        "npmName" to "$npmScope/$npmName",
        "npmVersion" to npmVersion,
        "npmRepository" to "https://npm.pkg.github.com",
    ))

    doLast {
        val pkgJsonFile = file("${outputDir.get()}/package.json")
        if (pkgJsonFile.exists()) {
            var content = pkgJsonFile.readText()
            content = content.replace("GIT_USER_ID", "m-amirazmi")
            content = content.replace("GIT_REPO_ID", "rms-contracts")
            pkgJsonFile.writeText(content)
        }
    }
}

// Ensure the task is grouped correctly for visibility in Gradle
tasks.named("generateTs") {
    group = "generation"
}
