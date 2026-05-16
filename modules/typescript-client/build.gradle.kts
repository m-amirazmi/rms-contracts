import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.openapi.generator")
}

val genOutputDir = layout.buildDirectory.dir("generated/ts-client")

tasks.register<GenerateTask>("generateTs") {
    group = "generation"

    generatorName.set("typescript-fetch")
    inputSpec.set("$rootDir/specs/api-spec.yaml")
    outputDir.set(genOutputDir.get().asFile.absolutePath)

    configOptions.set(
        mapOf(
            // ===== NPM PACKAGE =====
            "npmName" to "@rms/contracts",
            "npmVersion" to project.version.toString(),

            // ===== CORE STRIPE-STYLE SETTINGS =====
            "supportsES6" to "true",
            "stringEnums" to "true",
            "withSeparateModelsAndApi" to "true",
            "useSingleRequestParameter" to "true",

            // ===== CLEAN OUTPUT =====
            "modelPackage" to "models",
            "apiPackage" to "apis",

            // ===== IMPORTANT (MODERN TS MODE) =====
            "typescriptThreePlus" to "true"
        )
    )
}