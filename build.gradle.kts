
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.*

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.versions)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.kotlin.serialization)
}

group = "dev.phillipslabs"
version = "0.2.0"

kotlin {
    explicitApi()

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
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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
