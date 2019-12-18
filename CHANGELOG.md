# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 2.2.0 - 2019-12-17

### Changed
- Now converters must be assigned via CrudService setters. The ResourceDtoConverterProvider interface and the EntityConverterProvider class have been removed.

## 2.1.0 - 2019-12-16

### Added
- The StartsWith (SW) match type.
- The EndsWith (EW) match type.
- Negation for all match types.
- The method setDtoConverterProvider in the CrudResource class. It now has an EntityDtoConverterProvider by default.

### Changed
- The getId() method has been changed to id() in the BaseEntity interface, because it should not be considered a getter.
- ListOutput now has the static method of() to build a ListOutput based on Page and OutputConverter instances.
- The CrudResource class hooks have been changed from CheckedFunction to Function, providing greater overall compatibility.
- The CrusService now has a standard for EntityConverterProvider, no longer need to set ever.

### Removed
- Some generic types of the CrudService declaration.
- The method setId(id) has been removed from the BaseEntity interface.
- The method getDefaultOrder() has been removed from the CrudService interface. Now, the default order is the physical order in the database.
- The CheckedFunction interface.

### Fixed
- Fixed total element assignment (totalSize) of paged list conversion in class EntityListOutputConverter.

## 2.0.0 - 2019-11-18

### Added
- New list method overloads for full listing, with and without sorting.
- New URL filter converter for enumerator constants. URL filter is based on [QueryDecoder] (https://github.com/paulosalonso/query-decoder).

### Changed
- CrudService has been changed from abstract class to interface.
- BaseEntity ID type changed from fixed Long type to generic type.
- The predicate construction has been moved to specialized classes for each comparison type.
