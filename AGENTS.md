# JUDO Requirement Report - Project Documentation

## Project Overview


**Repository:** BlackBeltTechnology/judo-requirement-report
**License:** Eclipse Public License 2.0 (EPL-2.0)
**Java Version:** 21
**Build System:** Maven 3.9.4 with Maven Wrapper (`./mvnw`)

1. Provides `@Requirement` and `@TestCase` Java annotations for tagging test methods with requirement IDs and test-case identifiers
2. Includes a compile-time annotation processor that generates semicolon-separated CSV traceability reports (`requirements-report.csv` and `testcases-report.csv`)
3. Validates annotation correctness at compile time — checks for missing `@Test`, duplicate test-case IDs, empty requirement arrays, and missing `@TestCase`
4. Integrates seamlessly into Maven builds via the `-AreportPath` compiler argument
5. Part of the [judo-community](https://github.com/BlackBeltTechnology/judo-community) ecosystem

## Code Instructions

1. First think through the problem, read the codebase for relevant files.
2. Before you make any major changes, check in with me and I will verify the plan.
3. Please every step of the way just give me a high level explanation of what changes you made.
4. Make every task and code change you do as simple as possible. We want to avoid making any massive or complex changes. Every change should impact as little code as possible. Everything is about simplicity.
5. Maintain a documentation file that describes how the architecture of the app works inside and out.
6. Never speculate about code you have not opened. If the user references a specific file, you MUST read the file before answering. Make sure to investigate and read relevant files BEFORE answering questions about the codebase. Never make any claims about code before investigating unless you are certain of the correct answer - give grounded and hallucination-free answers.
7. For implementation use TDD (Test-Driven Development): write or update tests first to define the expected behaviour, verify they fail, then write the minimal implementation to make them pass.
8. Use DRY (Don't Repeat Yourself): extract reusable logic into separate classes, utilities, or components. If the same pattern appears in multiple places, refactor it into a shared helper.

## Directory Structure

```
judo-requirement-report/
├── judo-requirement-report-annotation/   # Annotation definitions
│   └── src/main/java/.../annotation/
│       ├── Requirement.java              # @Requirement(reqs={"REQ-001"})
│       └── TestCase.java                 # @TestCase("TC-001")
├── judo-requirement-report-processor/    # Annotation processor
│   └── src/main/java/.../processor/
│       ├── RequirementProcessor.java     # Main processor (entry point)
│       ├── AnnotatedElement.java         # Wraps & validates annotated elements
│       └── RequirementReportRow.java     # CSV row data class
├── judo-requirement-report-test/         # End-to-end tests
│   └── src/test/java/.../test/
│       └── TestRequirementProcessor.java # Test data + verification tests
├── .github/                              # CI workflows & templates
│   └── workflows/                        # GitHub Actions (build, release, etc.)
├── openspec/                             # OpenSpec configuration
├── pom.xml                               # Parent POM
├── mvnw / mvnw.cmd                       # Maven wrapper
└── logback-test.xml                      # Test logging configuration
```

## Core Modules

### Annotation Layer

| Module | Type | Purpose |
|--------|------|---------|
| `judo-requirement-report-annotation/` | Library | Defines `@Requirement` and `@TestCase` annotations with `SOURCE` retention — available only during compilation, zero runtime footprint |

### Processing Layer

| Module | Type | Purpose |
|--------|------|---------|
| `judo-requirement-report-processor/` | Annotation Processor | Discovers `@Requirement`/`@TestCase` at compile time, validates usage, checks uniqueness of test-case IDs, and generates two CSV reports via OpenCSV. Registered automatically via `@AutoService(Processor.class)` |

### Test Layer

| Module | Type | Purpose |
|--------|------|---------|
| `judo-requirement-report-test/` | Integration Test | Contains annotated test methods that serve as processor input data. During compilation, CSV files are generated to `target/classes/`. JUnit tests then parse and verify the CSV content using Hamcrest matchers |

## Technology Stack

### Core Technologies
- **Java 21** — language level and compilation target
- **Java Annotation Processing API** (`javax.annotation.processing`) — compile-time code analysis
- **Google Auto Service 1.0.1** — automatic `META-INF/services/javax.annotation.processing.Processor` registration
- **OpenCSV 5.7.1** — CSV file generation with semicolon separator
- **Google Guava 30.0-jre** — utility library

### Build & Quality
- **Maven 3.9.4** with Maven Wrapper
- **JUnit Jupiter 5.5.1** — testing framework
- **Hamcrest 2.2** — matcher-based test assertions
- **Mockito 4.8.0** — mocking framework
- **JaCoCo 0.8.12** — code coverage
- **Lombok 1.18.34** — boilerplate reduction
- **SLF4J 2.0.16 + Logback 1.5.12** — logging
- **flatten-maven-plugin 1.3.0** — CI-friendly `${revision}` version handling
- **maven-bundle-plugin 5.1.8** — OSGi bundle support

## Build Commands

```sh
# Full build (compile + test + install)
./mvnw clean install

# Run tests only
./mvnw clean test

# Run a single test class
./mvnw -pl judo-requirement-report-test test -Dtest=TestRequirementProcessor

# Run a single test method
./mvnw -pl judo-requirement-report-test test -Dtest=TestRequirementProcessor#testReal02

# Skip submodules (build parent POM only)
./mvnw clean install -DskipModules=true
```

### Maven Profiles

| Profile | Purpose |
|---------|---------|
| `modules` | Activates submodules (active by default unless `-DskipModules=true`) |
| `generate-checksum` | Generates artifact checksum CSV/XML (active by default unless `-Dignore_checksum=true`) |
| `sign-artifacts` | Signs artifacts for release using sign-maven-plugin |
| `release-dummy` | Deploys to local filesystem (`/tmp/`) for testing |
| `release-judong` | Deploys to judong Nexus repository |
| `release-central` | Deploys to Maven Central via Sonatype OSSRH |
| `generate-github-asciidoc-diagrams` | Generates PlantUML diagrams from AsciiDoc documentation |
| `update-source-code-license` | Updates EPL 2.0 license headers in source files |

## Key Configuration Files

| File | Purpose |
|------|---------|
| `pom.xml` | Parent POM — defines shared dependencies, plugin management, profiles, and module list |
| `logback-test.xml` | Logback configuration for test execution logging |
| `mvnw` / `mvnw.cmd` | Maven wrapper scripts (no global Maven install required) |
| `.github/workflows/build.yml` | Main CI workflow — build, test, deploy, tag, release |
| `.github/workflows/release.yml` | Manual release trigger workflow |
| `judo-requirement-report-test/pom.xml` | Configures `-AreportPath` compiler arg and `reportPath` system property for surefire |

## Development Environment

**Required:**
- Java 21 JDK
- Maven 3.9.4+ (or use included `./mvnw`)

**Processor configuration note:** Consuming projects must pass `-AreportPath=<directory>` to the Java compiler for the processor to know where to write CSV files. The test module demonstrates this:

```xml
<plugin>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <compilerArgs>
            <arg>-AreportPath=${project.basedir}/target/classes</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

## Git Workflow

- **Main Branch:** `develop`
- **Versioning:** `${revision}` property (currently `1.0.0-SNAPSHOT`), resolved by flatten-maven-plugin
- **Branching:** GitFlow — `develop`, `feature/JNG-xxx`, `release/x.y.z`, `bugfix/JNG-xxx`, `hotfix/JNG-xxx`, `master`
- **Commit rule:** Every commit must reference a JIRA ticket (`JNG-xxx`)
- **CI:** GitHub Actions on self-hosted `judong` runner, deploys to judong Nexus

## Important Notes

1. Both annotations have `SOURCE` retention — they exist only at compile time and are not present in compiled bytecode or at runtime
2. The `RequirementProcessor` requires the `-AreportPath` compiler argument. It throws a `RuntimeException` with a helpful error message if missing
3. The test module's `TestRequirementProcessor.java` serves a dual purpose: its annotated methods (`test00`–`test07`) are processor input data, while `testReal01`–`testReal03` are JUnit tests that verify the generated CSV files
4. CSV files use semicolons as separators (not commas), configured via OpenCSV's `CSVParserBuilder`
5. The processor validates test-case ID uniqueness across all annotated methods — duplicate IDs are flagged in the status column with the names of conflicting methods
6. The `@Requirement` annotation can be placed on methods or types; `@TestCase` is method-only

## Related Documentation

- [README.md](README.md) — Project introduction and quick start
- [CONTRIBUTING.md](CONTRIBUTING.md) — Development setup and contribution guidelines
- [.github/CIFLOW.md](.github/CIFLOW.md) — Detailed CI/CD workflow with Mermaid diagrams
