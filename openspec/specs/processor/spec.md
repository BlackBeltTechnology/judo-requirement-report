# processor Specification

## Purpose
Compile-time annotation processor that discovers `@Requirement` and `@TestCase` annotations, validates their usage, and generates two semicolon-separated CSV traceability reports: `requirements-report.csv` and `testcases-report.csv`.

## Architecture
The `RequirementProcessor` extends `AbstractProcessor` and is registered via `@AutoService(Processor.class)`. It processes annotations in a single round, wrapping each annotated element in an `AnnotatedElement` for validation and metadata extraction. `RequirementReportRow` is a simple data class representing a row in the requirements CSV. CSV output is handled by OpenCSV with a semicolon separator.

Key classes:
- `RequirementProcessor` — entry point, orchestrates processing and CSV writing
- `AnnotatedElement` — wraps a `javax.lang.model.element.Element`, extracts metadata, validates annotations, and implements `Comparable` for sorting by test-case ID then method name
- `RequirementReportRow` — data holder for a single requirements-report CSV row (testMethod, testCaseId, status, reqId)

## Requirements

### Requirement: Processor shall require reportPath compiler argument
The `RequirementProcessor` SHALL throw a `RuntimeException` with a descriptive error message including a pom.xml example if the `-AreportPath` compiler argument is missing or blank.

#### Scenario: Missing reportPath
- **GIVEN** the processor is invoked without `-AreportPath`
- **WHEN** `process()` is called
- **THEN** a `RuntimeException` is thrown with `ERROR_MSG_NO_REPORT_PATH`

### Requirement: Processor shall generate requirements-report.csv
The processor SHALL write a `requirements-report.csv` file to the reportPath directory with columns: TEST METHOD, TEST CASE ID, STATUS, REQUIREMENT.

#### Scenario: Method with single requirement
- **GIVEN** a method annotated with `@Requirement(reqs={"R01"})`, `@TestCase("TC01")`, and `@Test`
- **WHEN** the processor runs
- **THEN** `requirements-report.csv` contains a row: `TestClass.method;TC01;OK;R01`

#### Scenario: Method with multiple requirements
- **GIVEN** a method annotated with `@Requirement(reqs={"R01","R02"})`, `@TestCase("TC01")`, and `@Test`
- **WHEN** the processor runs
- **THEN** `requirements-report.csv` contains two rows, one for each requirement ID

### Requirement: Processor shall generate testcases-report.csv
The processor SHALL write a `testcases-report.csv` file to the reportPath directory with columns: TEST CASE ID, TEST METHOD, STATUS.

#### Scenario: Valid annotated method
- **GIVEN** a method annotated with `@Requirement(reqs={"R01"})`, `@TestCase("TC01")`, and `@Test`
- **WHEN** the processor runs
- **THEN** `testcases-report.csv` contains a row: `TC01;TestClass.method;OK`

### Requirement: Processor shall validate presence of @Test annotation
The processor SHALL report status "Missing annotation: @Test." for methods annotated with `@Requirement` but lacking a `@Test` annotation.

#### Scenario: Requirement without @Test
- **GIVEN** a method annotated with `@Requirement(reqs={"R01"})` and `@TestCase("TC04")` but without `@Test`
- **WHEN** the processor runs
- **THEN** the requirements report shows status "Missing annotation: @Test."

### Requirement: Processor shall validate presence of @TestCase annotation
The processor SHALL report status "Missing annotation: @TestCase." in the test-case report for methods annotated with `@Requirement` but lacking a `@TestCase` annotation.

#### Scenario: Requirement without @TestCase
- **GIVEN** a method annotated with `@Requirement(reqs={"R01"})` and `@Test` but without `@TestCase`
- **WHEN** the processor runs
- **THEN** the test-case report shows status "Missing annotation: @TestCase."

### Requirement: Processor shall detect empty requirement arrays
The processor SHALL report status "There isn't any requirement id." for methods annotated with `@Requirement(reqs={})` (empty array).

#### Scenario: Empty reqs array
- **GIVEN** a method annotated with `@Requirement(reqs={})` and `@Test`
- **WHEN** the processor runs
- **THEN** the requirements report shows status "There isn't any requirement id."

### Requirement: Processor shall detect empty @TestCase values
The processor SHALL report status "Empty string isn't a valid value of a @TestCase annotation." for `@TestCase("")`.

#### Scenario: Empty string test case ID
- **GIVEN** a method annotated with `@TestCase("")`
- **WHEN** the processor runs
- **THEN** the test-case report shows status "Empty string isn't a valid value of a @TestCase annotation."

### Requirement: Processor shall detect duplicate test-case IDs
The processor SHALL detect when multiple methods share the same non-empty `@TestCase` value and append "Test case id isn't unique. It is used by ..." to their test-case report status.

#### Scenario: Three methods with same test case ID
- **GIVEN** methods `test04`, `test06`, and `test07` all annotated with `@TestCase("TC02")`
- **WHEN** the processor runs
- **THEN** each method's test-case report status includes the names of the other conflicting methods

### Requirement: Processor shall sort output by test-case ID then method name
The processor SHALL sort `AnnotatedElement` entries by test-case ID first, then by method name (both lexicographic), using `Comparable<AnnotatedElement>`.

#### Scenario: Mixed test-case IDs
- **GIVEN** multiple annotated methods with various test-case IDs
- **WHEN** the processor generates CSV files
- **THEN** rows appear sorted by test-case ID, then by method name within the same ID
