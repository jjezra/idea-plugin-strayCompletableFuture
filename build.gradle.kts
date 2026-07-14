plugins {
    id("java")
    // IntelliJ Platform Gradle Plugin (classic 1.x). Requires JDK 17 to build,
    // because the 2023.2 platform is compiled for Java 17.
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.example"
version = "0.1.0"

repositories {
    mavenCentral()
}

intellij {
    // Target IntelliJ IDEA Community 2023.2. Bump as needed.
    version.set("2023.2.6")
    type.set("IC")

    // We inspect Java PSI, so we need the bundled Java plugin.
    plugins.set(listOf("com.intellij.java"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    patchPluginXml {
        sinceBuild.set("232")
        // Leave the upper bound open so the plugin keeps loading in newer IDEs.
        untilBuild.set(provider { null })
    }

    // Not needed for a single inspection; speeds up the build.
    buildSearchableOptions {
        enabled = false
    }
}
