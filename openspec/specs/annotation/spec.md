# annotation Specification

## Purpose
Defines the `@Requirement` and `@TestCase` Java annotations used to tag test methods with requirement IDs and test-case identifiers for compile-time traceability report generation.

## Architecture
Two annotation interfaces in the `hu.blackbelt.judo.requirement.report.annotation` package, both with `SOURCE` retention (discarded after compilation). `@Requirement` targets methods and types; `@TestCase` targets methods only.

## Requirements

### Requirement: Requirement annotation shall accept multiple requirement IDs
The `@Requirement` annotation SHALL accept an array of requirement ID strings via its `reqs` attribute.

#### Scenario: Single requirement ID
- **GIVEN** a test method annotated with `@Requirement(reqs = {"REQ-001"})`
- **WHEN** the annotation is processed
- **THEN** the single requirement ID `REQ-001` is extracted

#### Scenario: Multiple requirement IDs
- **GIVEN** a test method annotated with `@Requirement(reqs = {"REQ-001", "REQ-002"})`
- **WHEN** the annotation is processed
- **THEN** both requirement IDs are extracted and reported separately

### Requirement: Requirement annotation shall target methods and types
The `@Requirement` annotation SHALL be applicable to both methods and types (`ElementType.METHOD`, `ElementType.TYPE`).

#### Scenario: Annotation on a method
- **GIVEN** a `@Requirement` annotation placed on a method
- **WHEN** the code is compiled
- **THEN** the annotation is accepted without compiler errors

#### Scenario: Annotation on a class
- **GIVEN** a `@Requirement` annotation placed on a class declaration
- **WHEN** the code is compiled
- **THEN** the annotation is accepted without compiler errors

### Requirement: TestCase annotation shall accept a single string value
The `@TestCase` annotation SHALL accept a single string value representing the test-case identifier.

#### Scenario: Valid test case ID
- **GIVEN** a test method annotated with `@TestCase("TC-001")`
- **WHEN** the annotation is processed
- **THEN** the test-case ID `TC-001` is extracted

### Requirement: TestCase annotation shall target methods only
The `@TestCase` annotation SHALL only be applicable to methods (`ElementType.METHOD`).

#### Scenario: Annotation on a method
- **GIVEN** a `@TestCase` annotation placed on a method
- **WHEN** the code is compiled
- **THEN** the annotation is accepted without compiler errors

### Requirement: Annotations shall have SOURCE retention
Both `@Requirement` and `@TestCase` SHALL have `RetentionPolicy.SOURCE`, meaning they are discarded after compilation and have zero runtime footprint.

#### Scenario: Annotations not present at runtime
- **WHEN** a class with `@Requirement` and `@TestCase` annotations is compiled and loaded at runtime
- **THEN** reflection-based annotation queries return null for both annotations
