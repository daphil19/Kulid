# Kulid

Kulid is a Kotlin Multiplatform implementation of [ULIDs](https://github.com/ulid/spec) (Universally Unique Lexicographically Sortable Identifiers).

## What are ULIDs?

ULIDs are identifiers that provide:

- 128-bit compatibility with UUID
- 1.21e+24 unique ULIDs per millisecond
- Lexicographically sortable (by time, then random component)
- Canonically encoded as a 26 character string
- Uses Crockford's base32 for better human readability
- Case insensitive
- No special characters (URL safe)
- Monotonic sort order (correctly detects and handles the same millisecond)

## Features

- Pure Kotlin implementation
- Multiplatform support (JVM, JS, Native, WASM)
- Cryptographically secure random generation
- Timestamp-based generation
- Compliant with the ULID specification
- Strongly typed with low overhead by leveraging kotlin [value classes](https://kotlinlang.org/docs/inline-classes.html)
- Benchmark suite for performance testing

## Versioning

This project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html) with an emphasis on ABI compatibility. Major version numbers will be bumped when changes break ABI compatibility, even if the API remains backward compatible.

For detailed information about changes between versions, please see the [CHANGELOG.md](CHANGELOG.md) file.

## TODOs
This library is still being developed! As a result, there are still features of the [ULID spec](https://github.com/ulid/spec) that are missing, namely:
- [ ] [Monotonicity](https://github.com/ulid/spec?tab=readme-ov-file#monotonicity)
- [x] [Binary Layout](https://github.com/ulid/spec?tab=readme-ov-file#binary-layout-and-byte-order)

## Installation

### Gradle (Kotlin DSL)

```kotlin
// For Kotlin Multiplatform projects
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("dev.phillipslabs:kulid:1.0") // or whatever the latest version is
            }
        }
    }
}

// For JVM-only projects
dependencies {
    implementation("dev.phillipslabs:kulid:1.0") // or whatever the latest version is
}
```

### Gradle (Groovy DSL)

```groovy
// For Kotlin Multiplatform projects
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation "dev.phillipslabs:kulid:1.0" // or whatever the latest version is
            }
        }
    }
}

// For JVM-only projects
dependencies {
    implementation "dev.phillipslabs:kulid:1.0" // or whatever the latest version is
}
```

## Usage

### Basic Usage

```kotlin
import dev.phillipslabs.kulid.ULID

// Generate a ULID with the current timestamp
val ulid = ULID.generate()
println(ulid.value) // Example: 01H1VECZJXS2YFSJVS77XKG8DT

// Generate a ULID with a specific timestamp (in milliseconds)
val timestampInMillis = 1625097600000L // 2021-07-01T00:00:00Z
val ulidWithTimestamp = ULID.generate(timestampInMillis)
println(ulidWithTimestamp.value)
```

### Constants

```kotlin
// Maximum possible ULID value
val maxUlid = ULID.MAX
println(maxUlid.value) // 7ZZZZZZZZZZZZZZZZZZZZZZZZZ

// Minimum possible ULID value
val minUlid = ULID.MIN
println(minUlid.value) // 00000000000000000000000000
```

## Supported Platforms

Kulid supports every official Kotlin platform, including all [native targets](https://kotlinlang.org/docs/native-target-support.html) 

## Project Structure

Kulid is organized as a Gradle multi-project build:

- `:kulid` - The main library module containing the ULID implementation
- `:benchmark` - A benchmark suite for performance testing

## Dependencies

Kulid has minimal dependencies:
- [kotlinx.datetime](https://github.com/Kotlin/kotlinx-datetime) for timestamp handling
- [org.kotlincrypto.random](https://github.com/kotlincrypto/random) for secure random number generation
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) (core) for serialization support

## Benchmarks

The project includes a benchmark suite built with [kotlinx.benchmark](https://github.com/Kotlin/kotlinx.benchmark) to measure the performance of ULID operations.

### Running Benchmarks

To run the benchmarks, use the following Gradle command:

```bash
./gradlew :benchmark:benchmark
```

This will execute all benchmarks and generate a report in the `benchmark/build/reports/benchmarks` directory.

The benchmark suite includes tests for:
- ULID generation with current timestamp
- UUID generation, as reference

## Learn More
If you want to chat more about this library, feel free to talk about it on the [Kotlin Slack](https://slack-chats.kotlinlang.org/), or, if you're on matrix, check out [#kulid:phillipslabs.dev](https://matrix.to/#/#kulid:phillipslabs.dev).

## License

```
Copyright 2025 David Phillips

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
