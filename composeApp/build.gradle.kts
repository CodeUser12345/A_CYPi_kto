import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "1.9.10"
    id("org.jetbrains.dokka") version "1.9.10"
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            implementation("org.xerial:sqlite-jdbc:3.44.1.0")
            implementation("org.jetbrains.exposed:exposed-core:0.44.0")
            implementation("org.jetbrains.exposed:exposed-dao:0.44.0")
            implementation("org.jetbrains.exposed:exposed-jdbc:0.44.0")
            implementation("org.jetbrains.exposed:exposed-java-time:0.44.0")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(compose.materialIconsExtended)
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}

tasks.withType<Test> {
    // Включаем поддержку JUnit
    useJUnitPlatform()

    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}