plugins {
    id("org.openapi.generator")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateTs") {
    group = "generation"
    generatorName.set("typescript-axios")
    inputSpec.set("$rootDir/specs/api-spec.yaml")
    outputDir.set("${layout.buildDirectory.get().asFile}/generated")
    configOptions.set(mapOf(
        "npmName" to "@rms/contracts",
        "supportsES6" to "true",
        "npmVersion" to project.version.toString(),
        "apiPackage" to "api",
        "modelPackage" to "model",
        "withSeparateModelsAndApi" to "true",
        "npmRepository" to "https://npm.pkg.github.com"
    ))
    doLast {
        // Add a default export to index.ts for the desired usage
        val indexFile = file("${outputDir.get()}/index.ts")
        val indexContent = indexFile.readText()
        val newContent = """
            $indexContent
            import { DefaultApi } from './api';
            export { DefaultApi as ApiContracts } from './api';
            export default DefaultApi;
        """.trimIndent()
        indexFile.writeText(newContent)
    }
}

// Task to publish to GitHub NPM Registry
// This would typically be run in CI
tasks.register("publishTs") {
    group = "publishing"
    dependsOn("generateTs")
    doLast {
        project.providers.exec {
            workingDir("${layout.buildDirectory.get().asFile}/generated")
            commandLine("npm", "install")
        }.result.get()
        project.providers.exec {
            workingDir("${layout.buildDirectory.get().asFile}/generated")
            commandLine("npm", "publish")
        }.result.get()
    }
}
