plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.benchmark)
    alias(libs.plugins.kotlin.allopen)
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

kotlin {
    jvm()
    jvmToolchain(
        libs.versions.jdk
            .get()
            .toInt(),
    )

    js {
        nodejs()
    }

    wasmJs {
        nodejs()
    }

    // TODO android?

    // These native targets are the ones that are supported by the benchmark toolkit
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()

    // TODO simulators?

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":kulid"))
                implementation(libs.kotlinx.benchmark.runtime)
            }
        }
    }
}

benchmark {
    targets {
        register("jvm")
        register("js")
        register("wasmJs")
        register("linuxX64")
        register("macosX64")
        register("macosArm64")
        register("mingwX64")
    }
    configurations {
        named("main") {
            outputTimeUnit = "ms"
        }
        register("monotonic") {
            include("Monotonic")
            outputTimeUnit = "ms"
        }
        register("ulidOnly") {
            include("ULID")
            outputTimeUnit = "ms"
        }
    }
}
