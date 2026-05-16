import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("org.openapi.generator") version "7.22.0" apply false
}

subprojects {
    repositories {
        mavenCentral()
    }

    // Use Java 25 toolchain for all Java subprojects
    plugins.withType(JavaPlugin::class.java) {
        extensions.configure(JavaPluginExtension::class.java) {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(25))
            }
        }
    }
}

tasks.register("generateJava") {
    group = "generation"
    dependsOn(":modules:java-server:generateJava")
}

tasks.register("generateTs") {
    group = "generation"
    dependsOn(":modules:typescript-client:generateTs")
}
