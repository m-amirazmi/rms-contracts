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
    validateSpec.set(false)

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

        // Generate unified RMSApiClient that wraps all API groups
        val apisDir = file("${outputDir.get()}/src/apis")
        val apiClassNames: List<String> = apisDir.listFiles { f -> f.name.endsWith("Api.ts") }
            ?.map { it.nameWithoutExtension }
            ?.sorted()
            ?: emptyList()

        if (apiClassNames.isNotEmpty()) {
            // Split PascalCase into words: "CatalogCategory" → ["Catalog", "Category"]
            val pascalSplit = Regex("(?<=[a-z])(?=[A-Z])")

            // Triple: (className, logicalName, words)
            val apiInfo = apiClassNames.map { className ->
                val logical = className.removeSuffix("Api")
                Triple(className, logical, pascalSplit.split(logical).filter { it.isNotEmpty() })
            }

            // Group by first word (e.g. "Catalog" groups CatalogCategory + CatalogBrand)
            val grouped = apiInfo.groupBy { it.third[0] }

            val imports = apiClassNames.joinToString("\n") { "import { $it } from './apis/$it';" }

            val propLines = mutableListOf<String>()
            val assignLines = mutableListOf<String>()

            for ((groupKey, entries) in grouped.entries.sortedBy { it.key }) {
                val propName = groupKey.replaceFirstChar { it.lowercase() }
                val allNested = entries.all { it.third.size > 1 }

                if (!allNested) {
                    // Single-word tag → flat property: readonly repair: RepairApi
                    val e = entries.first()
                    propLines.add("readonly $propName: ${e.first};")
                    assignLines.add("this.$propName = new ${e.first}(config);")
                } else {
                    // Multi-word tags sharing a prefix → nested object: readonly catalog: { ... }
                    val nestedProps = entries.joinToString("\n    ") {
                        val sub = it.third.drop(1).joinToString("").replaceFirstChar { c -> c.lowercase() }
                        "readonly $sub: ${it.first};"
                    }
                    propLines.add("readonly $propName: {\n    $nestedProps\n  };")

                    val nestedAssigns = entries.joinToString(",\n      ") {
                        val sub = it.third.drop(1).joinToString("").replaceFirstChar { c -> c.lowercase() }
                        "$sub: new ${it.first}(config)"
                    }
                    assignLines.add("this.$propName = {\n      $nestedAssigns,\n    };")
                }
            }

            val clientContent = buildString {
                appendLine("import { Configuration, ConfigurationParameters } from './runtime';")
                appendLine(imports)
                appendLine()
                appendLine("export class RMSApiClient {")
                appendLine("  ${propLines.joinToString("\n  ")}")
                appendLine()
                appendLine("  constructor(params?: ConfigurationParameters) {")
                appendLine("    const config = new Configuration(params);")
                appendLine("    ${assignLines.joinToString("\n    ")}")
                appendLine("  }")
                appendLine("}")
            }

            file("${outputDir.get()}/src/RMSApiClient.ts").writeText(clientContent)

            val indexFile = file("${outputDir.get()}/src/index.ts")
            if (indexFile.exists()) {
                indexFile.appendText("export * from './RMSApiClient';\n")
            }
        }
    }
}

// Ensure the task is grouped correctly for visibility in Gradle
tasks.named("generateTs") {
    group = "generation"
}
