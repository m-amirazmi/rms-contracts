# ARCHITECTURE.md - OpenAPI Contract Generation and Publishing

This repository manages OpenAPI API contracts for the RMS project. It generates both Java Spring Boot server interfaces and TypeScript Axios client libraries, which are published to GitHub Packages.

## 📂 Repository Structure

- `specs/`: Contains the OpenAPI specification files.
  - `shared-spec.yaml`: Common schemas (Address, Error, etc.).
  - `repair-spec.yaml`: Repair domain API.
  - `tenant-spec.yaml`: Tenant domain API.
- `modules/`: Contains the generation logic.
  - `java-server/`: Java (Spring Boot) generation and Maven publishing.
  - `typescript-client/`: TypeScript (Axios) generation and NPM publishing.
- `.github/workflows/`: CI/CD pipeline for automation.

## 🛠 Usage

### Local Generation

You can generate the contracts locally using Gradle:

- **Generate Java:** `./gradlew generateJava`
- **Generate TypeScript:** `./gradlew generateTs`

The generated files will be located in:
- Java: `modules/java-server/build/generated/`
- TypeScript: `modules/typescript-client/build/generated/`

### Adding a New Domain

1. Create a new spec file in `specs/your-domain-spec.yaml`.
2. Reference `shared-spec.yaml` for common models if needed.
3. Add the domain name to the `domains` list in both:
   - `modules/java-server/build.gradle.kts`
   - `modules/typescript-client/build.gradle.kts`

## 🚀 CI/CD and Publishing

Every push to the `main` branch or a version tag (e.g., `v1.0.0`) triggers the `.github/workflows/publish.yml` workflow.

- **Java Artifacts:** Published to GitHub Maven Registry under the group `com.rms`.
- **TypeScript Artifacts:** Published to GitHub NPM Registry under the scope `@rms`.

### Repository Configuration

Ensure the following are set up in GitHub:
- **Permissions:** GitHub Actions must have `packages: write` permission.
- **NPM Config:** The `publishTs` task expects to be able to run `npm publish`. In CI, this uses the `NODE_AUTH_TOKEN`.
