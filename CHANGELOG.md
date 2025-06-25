# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Kulid is now Kotlin [explicit api mode](https://kotlinlang.org/docs/api-guidelines-simplicity.html#use-explicit-api-mode) compliant.
- Bump AGP version to 8.10.1
- Add benchmark harness, comparing to Kotlin's UUID implementation

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
