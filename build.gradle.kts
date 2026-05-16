import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("org.openapi.generator") version "7.22.0" apply false
}

/**
 * Shared repository configuration
 */
allprojects {
    repositories {
        mavenCentral()
    }
}

/**
 * Apply Java toolchain ONLY to Java-based subprojects
 */
subprojects {
    plugins.withType(JavaPlugin::class.java) {
        extensions.configure(JavaPluginExtension::class.java) {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(25))
            }
        }
    }
}

/**
 * Root orchestration task
 * Keeps contract generation centralized
 */
tasks.register("generateAll") {
    group = "generation"
    dependsOn(
        ":modules:typescript-client:generateTs"
//        ":modules:spring-server:generateSpring"
    )
}