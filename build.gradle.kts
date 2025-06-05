
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.*

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.versions)
}

group = "dev.phillipslabs"
version = "1.0"

kotlin {
    jvm()
    jvmToolchain(11)

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
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // mobile (including simlulators)
    iosArm64()
    iosSimulatorArm64()
    iosX64()

    // native non-mobile
    macosX64()
    macosArm64()
    linuxX64()
    linuxArm64()
    mingwX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.crypto.rand)
                // TODO we may eventually be able to replace this with a part of the standard library
                implementation(libs.kotlinx.datetime)
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

// checks to see if a release is "stable," meaning we don't have to worry about rc versions polluting a dependencyUpdates check
// this can be found in the versions plugin readme (https://github.com/ben-manes/gradle-versions-plugin)
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase(Locale.getDefault()).contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

// fully qualified import helps simplify disabling this in case we ever needed to
tasks.named("dependencyUpdates", com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask::class.java).configure {
    // disallow release candidates as upgradable versions from stable versions
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }

    // don't look at gradle rc versions either
    gradleReleaseChannel = "current"
}
