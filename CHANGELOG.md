# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

**Note on versioning:** This project follows semantic versioning with an emphasis on ABI compatibility. Major version numbers will be bumped when changes break ABI compatibility, even if the API remains backward compatible.

## [0.4.0] - 2025-11-01

### Breaking Changes
Version 0.4.0 is _binary_ incompatible with 0.3.0. This means library consumers will have to re-compile against the newest version of the library, as opposed to just dropping in a compiled library artifact. It _is_, however, _source_ compatible with 0.3.0, meaning consumers do not have to modify source that interfaced with this library.

#### Source-compatible breaking changes
None!

#### Binary-compatible breaking changes
- Added monotonicity API

### Added
- Added monotonicity support.
  - This implementation is not thread-safe yet! Thread-safety will be added in a future release.

### Changed
- Bump Kotlin version to 2.2.21
- Various other dependency bumps

## [0.3.0] - 2025-08-03

### Added
- Added support for binary format.
- Added an option to disable secure random for ULIDs.
- Added KDocs for all public APIs.

### Changed
- Kulid is now Kotlin [explicit api mode](https://kotlinlang.org/docs/api-guidelines-simplicity.html#use-explicit-api-mode) compliant.
- Bump AGP version to 8.10.1
- Add benchmark harness, comparing Kulid to Kotlin's UUID implementation

## [0.2.0] - 2025-06-17

### Added
- Added `kotlinx.serialization` support to ULID.

### Changed
- Made `kotlinx.datetime` an API dependency.
- Updated `gradle.properties` to use parallel builds.
- Bump gradle version and gradle build plugins.

## [0.1.0] - 2025-06-13

This is the initial release of Kulid!

### Added
- Ability to generate a ULID, either with the current time or any valid timestamp.
