plugins {
    `java-library`
    `maven-publish`
    id("org.openapi.generator")
}

val domains = listOf("repair", "tenant")

domains.forEach { domain ->
    tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerate_${domain}") {
        generatorName.set("spring")
        inputSpec.set("$rootDir/specs/${domain}-spec.yaml")
        outputDir.set("${layout.buildDirectory.get().asFile}/generated/${domain}")
        apiPackage.set("com.rms.${domain}.api")
        modelPackage.set("com.rms.${domain}.model")
        configOptions.set(mapOf(
            "interfaceOnly" to "true",
            "useSpringBoot3" to "true",
            "useJakartaEe" to "true"
        ))
    }
}

tasks.register("generateJava") {
    group = "generation"
    dependsOn(domains.map { "openApiGenerate_${it}" })
}

// Ensure the generated code is included in the JAR
java {
    sourceSets {
        main {
            domains.forEach { domain ->
            java.srcDir("${layout.buildDirectory.get().asFile}/generated/$domain/src/main/java")
            }
        }
    }
}

// Make sure generation happens before compilation
tasks.withType<JavaCompile> {
    dependsOn("generateJava")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = "rms-contracts-java"
            version = project.version.toString()
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/OWNER/rms-contracts")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
