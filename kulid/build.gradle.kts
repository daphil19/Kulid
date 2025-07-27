@file:OptIn(ExperimentalWasmDsl::class, ExperimentalAbiValidation::class)

import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.ktlint)

    // TODO if we need to override any config we might need a build-logic plugin!
    alias(libs.plugins.detekt)
    alias(libs.plugins.versions)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.kotlin.serialization)
}

group = "dev.phillipslabs"
version = "0.2.0"

kotlin {
    explicitApi()

    abiValidation {
        enabled = true
    }

    jvm()
    jvmToolchain(
        libs.versions.jdk
            .get()
            .toInt(),
    )

    js {
        nodejs()
        browser()
    }

    wasmJs {
        browser()
        nodejs()
    }

    wasmWasi {
        nodejs()
    }

    // TODO android!
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // "tiers" here are taken from https://kotlinlang.org/docs/native-target-support.html
    // tier 1
    macosX64()
    macosArm64()
    iosSimulatorArm64()
    iosX64()
    iosArm64()

    // tier 2
    linuxX64()
    linuxArm64()
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()

    // tier 3
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    mingwX64()
    watchosDeviceArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.crypto.rand)
                // TODO we may eventually be able to replace this with a part of the standard library
                api(libs.kotlinx.datetime)
                api(libs.kotlinx.serialization.core)
                api(libs.kotlinx.io)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "dev.phillipslabs.kulid"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()
    defaultConfig {
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), "kulid", version.toString())

    pom {
        name = "Kulid"
        description = "ULID implementation for Kotlin Multiplatform"
        inceptionYear = "2025"
        url = "https://github.com/daphil19/kulid"
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id = "daphil19"
                name = "David Phillips"
                url = "https://github.com/daphil19"
            }
        }
        scm {
            url = "https://github.com/daphil19/kulid"
            connection = "scm:git:git://github.com/daphil19/kulid.git"
            developerConnection = "scm:git:ssh://git@github.com/daphil19/kulid.git"
        }
    }
}
