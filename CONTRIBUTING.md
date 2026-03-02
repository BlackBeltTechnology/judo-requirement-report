# Contributing to JUDO Requirement Report

Thank you for your interest in contributing! This guide covers how to set up your development environment, build the project, and submit changes.

## Development Environment

### Requirements

| Tool | Version | Notes |
|---|---|---|
| Java JDK | 21 | Required for compilation and tests |
| Maven | 3.9.4+ | Or use the included `./mvnw` wrapper |

For full environment setup details, see the parent project's [CONTRIBUTING guide](https://github.com/BlackBeltTechnology/judo-community/blob/develop/CONTRIBUTING.adoc).

## Code Structure

This is a multi-module Maven project with three submodules:

| Module | Purpose |
|---|---|
| `judo-requirement-report-annotation` | Defines `@Requirement` and `@TestCase` annotations (SOURCE retention) |
| `judo-requirement-report-processor` | Annotation processor that generates CSV traceability reports at compile time |
| `judo-requirement-report-test` | End-to-end tests — annotated test methods act as processor input, then JUnit verifies the generated CSV output |

All Java source code lives under the `hu.blackbelt.judo.requirement.report` package namespace.

## Build Commands

```sh
# Run tests only
./mvnw clean test

# Full build (compile + test + install to local repo)
./mvnw clean install

# Run a single test class
./mvnw -pl judo-requirement-report-test test -Dtest=TestRequirementProcessor

# Run a single test method
./mvnw -pl judo-requirement-report-test test -Dtest=TestRequirementProcessor#testReal02
```

## Submitting an Issue

Before submitting, search the [issue tracker](https://github.com/BlackBeltTechnology/judo-requirement-report/issues) to check if your issue has already been reported.

When reporting a bug, include:

- Output of `java -version` and `mvn -version`
- Relevant `pom.xml` or `.flattened-pom.xml` contents
- A minimal reproduction case that demonstrates the failure

We require a minimal reproduction to efficiently confirm and fix bugs.

File new issues using the [issue form](https://github.com/BlackBeltTechnology/judo-requirement-report/issues/new/choose).

## Submitting a Pull Request

This project follows [GitHub's standard forking model](https://guides.github.com/activities/forking/). Fork the repository and submit pull requests from your fork.

> **Important:** Every commit must reference a JIRA ticket number (e.g., `JNG-xxx`). There is no commit without a ticket number.

## Git Workflow

The project uses GitFlow branching:

- **`develop`** — main integration branch for active development
- **`feature/JNG-xxx_description`** — feature branches based on develop
- **`release/x.y.z`** — release stabilization branches
- **`bugfix/JNG-xxx_description`** — fixes applied to release branches
- **`master`** — contains the latest released version

See [CIFLOW.md](.github/CIFLOW.md) for detailed CI/CD workflow documentation including version numbering and GitHub Actions flows.
