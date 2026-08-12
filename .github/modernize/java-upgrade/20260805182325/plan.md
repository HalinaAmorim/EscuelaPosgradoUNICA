# Upgrade Plan: EscuelaPosgradoUNICA (20260805182325)

- **Generated**: 2026-08-05 18:23:25
- **HEAD Branch**: main
- **HEAD Commit ID**: f8af7b75ffa90331a92f8e75795057a1a90d35ae

## Available Tools

**JDKs**
- JDK 21.0.5: C:\Program Files\Java\jdk-21\bin (available, required by Step 3 and Step 5)
- JDK 17.0.16: C:\Users\alice\.jdk\jdk-17.0.16\bin (available, not required)
- Java 24: not available (baseline for backend/Intranet using current java.version 24 will be skipped)

**Build Tools**
- Maven Wrapper 3.9.10: backend/Autenticacion/.mvn/wrapper/maven-wrapper.properties (compatible with Java 21)
- Maven Wrapper 3.9.10: backend/Intranet/.mvn/wrapper/maven-wrapper.properties (compatible with Java 21)
- Maven Wrapper 3.9.10: backend/Matricula/.mvn/wrapper/maven-wrapper.properties (compatible with Java 21)
- Maven CLI: no external Maven installation found; wrapper usage is required for reproducible execution

## Guidelines

> Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred.

## Options

- Working branch: appmod/java-upgrade-20260805182325
- Run tests before and after the upgrade: true

## Upgrade Goals

- Align all backend Maven modules to Java 21 (latest LTS) runtime

## Technology Stack

| Technology/Dependency | Current | Min Compatible | Why Incompatible |
| --------------------- | ------- | -------------- | ---------------- |
| Java | 21 / 24 | 21 | Target is latest LTS; Intranet must be aligned from 24 → 21 |
| Spring Boot | 3.5.3 | 3.5.3 | Already compatible with Java 21 |
| Maven Wrapper | 3.9.10 | 3.9.10 | Compatible with Java 21 |
| maven-compiler-plugin | managed by Spring Boot parent | 3.11.0+ | Required for Java 21 support and correct compiler release handling |

## Derived Upgrades

- `backend/Intranet/pom.xml` must be adjusted from `<java.version>24</java.version>` to `<java.version>21</java.version>` to standardize runtime across backend modules and satisfy the latest LTS requirement.
- No Spring Boot version upgrade is required because Spring Boot 3.5.3 already supports Java 21.
- No web framework or dependency runtime upgrade is required solely for Java 21 compatibility at this stage.
- No Maven wrapper upgrade is required; the existing 3.9.10 distribution supports Java 21.

## Impact Analysis

### Dependency Changes

| File | Dependency | Current | Action | Target | Reason |
|------|------------|---------|--------|--------|--------|
| backend/Intranet/pom.xml | `<java.version>` | 24 | replace | 21 | Align Intranet runtime with latest LTS Java 21 |
| README.md | Java runtime documentation | Java 24 | replace | Java 21 | Keep project documentation consistent with the target runtime |
| backend/Intranet/INTRANET_BACKEND_COMPLETE.md | Java runtime documentation | Java 24 | replace | Java 21 | Keep module documentation consistent with the target runtime |

### Source Code Changes

No source code changes are required for the Java runtime alignment itself. Any compile-time incompatibilities discovered during verification will be addressed in execution.

### Configuration Changes

| File | Property/Setting | Current | Required Change | Reason |
|------|------------------|---------|-----------------|--------|
| backend/Intranet/pom.xml | `<java.version>` | 24 | 21 | Align runtime with Java 21 latest LTS |

### CI/CD Changes

None identified. Build wrapper and module configuration already support the target runtime.

### Risks & Warnings

- **Intranet Java version downgrade**: The `backend/Intranet` module currently targets Java 24. If that module uses Java 24 language features or library APIs not available in Java 21, the compile step may fail. **Mitigation**: compile with JDK 21 and fix any source compatibility issues before final validation.
- **Baseline gap for Intranet**: JDK 24 is not installed, so current-state baseline verification for `backend/Intranet` cannot be performed on the exact existing runtime. The upgrade will rely on available JDK 21 for runtime alignment and will verify the target state directly.

## Upgrade Steps

- Step 1: Setup Environment
  - **Rationale**: Ensure the target Java 21 runtime and module Maven wrappers are available before making changes.
  - **Changes to Make**: Verify JDK 21 availability, confirm Maven wrapper executables in each backend module, and record that JDK 24 is unavailable.
  - **Verification**: `java -version`, `.ackend\Autenticacion\mvnw.cmd -v`, `.ackend\Intranet\mvnw.cmd -v`, `.ackend\Matricula\mvnw.cmd -v`

- Step 2: Setup Baseline
  - **Rationale**: Capture current compile/test behavior for available backend modules before changing the runtime alignment.
  - **Changes to Make**: Run baseline compile and tests for `backend/Autenticacion` and `backend/Matricula` using JDK 21. Skip full baseline for `backend/Intranet` because JDK 24 is not installed.
  - **Verification**: `.ackend\Autenticacion\mvnw.cmd -q clean test-compile && .\backend\Autenticacion\mvnw.cmd -q clean test`, `.ackend\Matricula\mvnw.cmd -q clean test-compile && .\backend\Matricula\mvnw.cmd -q clean test`

- Step 3: Align Intranet runtime to Java 21
  - **Rationale**: Standardize the backend runtime to the latest LTS and avoid a mixed Java runtime environment.
  - **Changes to Make**: Update `backend/Intranet/pom.xml` `<java.version>` from `24` to `21`; update Java runtime references in project documentation.
  - **Verification**: `.ackend\Intranet\mvnw.cmd -q clean test-compile`, plus re-run `backend/Autenticacion` and `backend/Matricula` compile checks to ensure cross-module consistency.

- Step 4: CVE Validation & Fix
  - **Rationale**: Scan direct backend dependencies for known CVEs after alignment and resolve any issues without changing the target Java runtime.
  - **Changes to Make**: Extract direct dependencies and run CVE validation for the backend modules. If CVEs are reported, upgrade only the minimum patch levels or BOM-managed versions needed to resolve them.
  - **Verification**: `.ackend\Autenticacion\mvnw.cmd -q clean test-compile`, `.ackend\Intranet\mvnw.cmd -q clean test-compile`, `.ackend\Matricula\mvnw.cmd -q clean test-compile`

- Step 5: Final Validation
  - **Rationale**: Confirm the upgrade target is met and the backend modules compile and pass tests under Java 21.
  - **Changes to Make**: None beyond any fixes from Step 4.
  - **Verification**: `.ackend\Autenticacion\mvnw.cmd -q clean test`, `.ackend\Intranet\mvnw.cmd -q clean test`, `.ackend\Matricula\mvnw.cmd -q clean test`
