# test Specification

## Purpose
Provides end-to-end verification of the annotation processor by using annotated test methods as processor input data, then parsing and validating the generated CSV output files.

## Architecture
`TestRequirementProcessor` serves a dual role: its annotated methods (`test00`–`test07`) provide various combinations of `@Requirement`, `@TestCase`, and `@Test` annotations that the processor consumes during compilation. The `testReal01`–`testReal03` methods are JUnit 5 tests that verify the processor's behavior by reading the generated CSV files and asserting their content using Hamcrest matchers. CSV parsing uses OpenCSV with a semicolon separator to match the processor's output format. The `reportPath` system property (set by maven-surefire-plugin) tells tests where to find the generated CSV files.

## Requirements

### Requirement: Test module shall verify processor rejects missing reportPath
The test module SHALL verify that `RequirementProcessor.process()` throws a `RuntimeException` with `ERROR_MSG_NO_REPORT_PATH` when invoked without a `reportPath` option.

#### Scenario: Process with empty options map
- **GIVEN** a `RequirementProcessor` initialized with a `ProcessingEnvironment` whose `getOptions()` returns an empty map
- **WHEN** `process()` is called with empty annotation and round environment sets
- **THEN** a `RuntimeException` is thrown with the expected error message

### Requirement: Test module shall verify requirements-report.csv content
The test module SHALL parse the generated `requirements-report.csv` and assert that it contains the correct header row and one data row per requirement-per-method combination, with accurate STATUS values.

#### Scenario: Verify all expected rows exist
- **GIVEN** the processor has run during compilation against the annotated test methods `test00`–`test07`
- **WHEN** `testReal02()` reads `requirements-report.csv`
- **THEN** the CSV contains exactly 12 data rows (plus header) matching all expected method/requirement/status combinations

### Requirement: Test module shall verify testcases-report.csv content
The test module SHALL parse the generated `testcases-report.csv` and assert that it contains the correct header row and one data row per annotated method, with accurate STATUS values including duplicate-ID warnings.

#### Scenario: Verify all expected rows exist
- **GIVEN** the processor has run during compilation against the annotated test methods `test00`–`test07`
- **WHEN** `testReal03()` reads `testcases-report.csv`
- **THEN** the CSV contains exactly 8 data rows (plus header) with correct statuses including "Test case id isn't unique" for the three methods sharing `TC02`

### Requirement: Test data shall cover all validation scenarios
The annotated methods `test00`–`test07` SHALL collectively cover all processor validation paths.

#### Scenario: Empty requirement array
- **GIVEN** `test00` annotated with `@Requirement(reqs={})`, `@TestCase("")`, and `@Test`
- **THEN** this exercises the "no requirement ID" and "empty TestCase value" validation paths

#### Scenario: Missing @TestCase
- **GIVEN** `test01` annotated with `@Requirement(reqs={"R01"})` and `@Test` but no `@TestCase`
- **THEN** this exercises the "missing @TestCase" validation path

#### Scenario: Missing @Test
- **GIVEN** `test05` annotated with `@Requirement` and `@TestCase` but no `@Test`
- **THEN** this exercises the "missing @Test" validation path

#### Scenario: Duplicate test-case IDs
- **GIVEN** `test04`, `test06`, and `test07` all annotated with `@TestCase("TC02")`
- **THEN** this exercises the duplicate test-case ID detection
