import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.openapi.generator")
}

val genOutputDir = layout.buildDirectory.dir("generated/ts-client")

tasks.register<GenerateTask>("generateTs") {
    group = "generation"

    generatorName.set("typescript-axios")
    inputSpec.set("$rootDir/specs/api-spec.yaml")
    outputDir.set(genOutputDir.get().asFile.absolutePath)

    configOptions.set(
        mapOf(
            "npmName" to "@rms/contracts",
            "npmVersion" to project.version.toString(),

            // Core TS behavior
            "supportsES6" to "true",
            "stringEnums" to "true",
            "enumPropertyNaming" to "original",
            "withSeparateModelsAndApi" to "true",
            "useSingleRequestParameter" to "true",

            // clean structure
            "apiPackage" to "api",
            "modelPackage" to "model"
        )
    )
}