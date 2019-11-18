# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0] - 2019-11-18

### Added
- New list method overloads for full listing, with and without sorting.
- New URL filter converter for enumerator constants. URL filter is based on [QueryDecoder] (https://github.com/paulosalonso/query-decoder).

### Changed
- CrudService has been changed from abstract class to interface.
- BaseEntity ID type changed from fixed Long type to generic type.
- The predicate construction has been moved to specialized classes for each comparison type.
