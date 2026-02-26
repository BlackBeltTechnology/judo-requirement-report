# Development Version and Branch Handling

This document describes the branching strategy, version numbering, and CI/CD pipeline for judo-requirement-report. The project follows a GitFlow-based workflow with automated GitHub Actions for building, testing, and releasing.

## Table of Contents

- [Branches](#branches)
- [Version Numbers](#version-numbers)
- [GitHub Actions Workflows](#github-actions-workflows)
  - [build.yml](#buildyml)
  - [merge-pr-tagged.yml](#merge-pr-taggedyml)
  - [create-release-on-master.yml](#create-release-on-masteryml)
  - [release.yml](#releaseyml)
- [How to Develop](#how-to-develop)

## Branches

The versioning policy is based on [GitFlow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow).

| Branch Pattern | Base Branch | Purpose |
|---|---|---|
| `develop` | — | Main integration branch with latest development sources |
| `feature/JNG-xxx_summary` | `develop` | New features for the next release |
| `(release/)x.y.z` | `develop` | Release stabilization (`release/` prefix reserved for CI) |
| `bugfix/JNG-xxx_summary` | release branch | Bug fixes applied to release branches; must also be applied to newer versions |
| `support/JNG-xxx_summary` | release branch | Minor changes for previous releases; merged back to the release branch |
| `master` | — | Latest released sources |

### Branch Flow

```mermaid
gitGraph
    commit id: "init"
    branch develop
    checkout develop
    commit id: "dev-1"
    branch feature/JNG-1
    commit id: "feat-1a"
    commit id: "feat-1b"
    checkout develop
    merge feature/JNG-1 id: "merge-feat-1"
    branch feature/JNG-3
    commit id: "feat-3"
    checkout develop
    merge feature/JNG-3 id: "merge-feat-3"
    branch release/1.0-beta1
    commit id: "rc-1"
    branch bugfix/JNG-4
    commit id: "fix-4"
    checkout release/1.0-beta1
    merge bugfix/JNG-4 id: "merge-fix"
    checkout develop
    merge release/1.0-beta1 id: "merge-release"
    checkout main
    merge release/1.0-beta1 id: "release-1.0"
```

## Version Numbers

Version numbers follow semantic versioning with these rules:

| Event | Version Change |
|---|---|
| Start a `feature/` branch | No change — inherits from `develop` |
| Start a `release/` branch | 2nd number in `develop` version is incremented |
| Start a `bugfix/` branch | No change — applied on release branches before merging to master |
| Start a `support/` branch | 3rd number is incremented; merged back to release branch without going to master |
| Start a `hotfix/` branch | 4th number is incremented; applied to both release and master branches |

## GitHub Actions Workflows

### build.yml

The main CI workflow, triggered on pushes to `develop` or pull requests targeting `develop`, `master`, `increment/*`, or `release/*` branches.

```mermaid
flowchart TD
    A["<b>Trigger</b><br/>Push on develop<br/>PR on develop / master / increment/* / release/*"] --> B{Base branch?}
    B -->|"master, release/*"| C["Set <b>version</b> from pom.xml<br/><i>(without -SNAPSHOT)</i>"]
    B -->|"develop, increment/*"| D["Set version as<br/><b>major.minor.qualifier.date_commitId_branch</b>"]
    C --> E["Build & deploy to Nexus"]
    D --> E
    E --> F["Create git tag <b>v&lt;version&gt;</b>"]
    F --> G{Base branch?}
    G -->|"increment/*, release/*"| H["Create tag <b>merge-pr/&lt;version&gt;</b>"]
    H --> I["Triggers <b>merge-pr-tagged.yml</b>"]
    G -->|"develop"| J["Build changelog"]
    J --> K["Create GitHub prerelease with changelog"]
    G -->|other| L["Done"]
```

### merge-pr-tagged.yml

Triggered when a `merge-pr/*` tag is pushed. Handles automated merging of pull requests.

```mermaid
flowchart TD
    A["<b>Trigger</b><br/>Push on merge-pr/* tag"] --> B["Extract version from tag name"]
    B --> C{Version format?}
    C -->|"major.minor.qualifier"| D["Merge PR to <b>master</b>"]
    D --> E["Triggers <b>create-release-on-master.yml</b>"]
    C -->|other format| F["Squash PR to <b>develop</b>"]
    F --> G["Triggers <b>build.yml</b>"]
    D --> H["Delete merge-pr tag"]
    F --> H
```

### create-release-on-master.yml

Triggered on pushes to `master`. Creates the official GitHub release.

```mermaid
flowchart LR
    A["Push on <b>master</b>"] --> B["Get version from tag"]
    B --> C["Build changelog"]
    C --> D["Create <b>GitHub release</b><br/><i>(latest)</i>"]
```

### release.yml

Manually triggered workflow to initiate a release. Takes a version parameter (`'auto'` or a specific `major.minor.qualifier` version).

```mermaid
flowchart TD
    A["<b>Manual trigger</b><br/>with version input"] --> B{Version is 'auto'?}
    B -->|Yes| C["Read version from pom.xml<br/><i>(without -SNAPSHOT)</i>"]
    B -->|No| D["Use given version"]
    C --> E["Set next version = qualifier + 1"]
    D --> E
    E --> F["Create PR on <b>master</b><br/>with release version"]
    E --> G["Create PR on <b>develop</b><br/>with next version"]
    F --> H["Triggers build.yml"]
    G --> I["Triggers build.yml"]
```

### Workflow Interaction Overview

```mermaid
graph LR
    build["build.yml"] -->|"creates merge-pr/* tag"| merge["merge-pr-tagged.yml"]
    merge -->|"merges to master"| master["create-release-on-master.yml"]
    merge -->|"squashes to develop"| build
    release["release.yml<br/><i>(manual)</i>"] -->|"creates PRs"| build
```

## How to Develop

For issue tracking, the project uses [JIRA](https://blackbelt.atlassian.net/jira/dashboards).

> **Important:** There is no commit without a ticket number. Every pull request and commit must include a `JNG-xxx` reference.
