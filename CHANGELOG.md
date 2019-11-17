# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

### Changed

### Removed

### Fixed

## [2.0.0] - 2019-11-17

### Added
- New URL filter converter ([based on QueryDecoder] (https://github.com/paulosalonso/query-decoder)) for enumerator constants.

### Changed
- CrudService has been changed from abstract class to interface.
- BaseEntity ID type changed from fixed Long type to generic type.
- The predicate construction has been moved to specialized classes for each comparison type.
