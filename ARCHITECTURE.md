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

## 📦 TypeScript Client Conventions

The generated TypeScript client exposes a single `RMSApiClient` class that bundles every API group. Frontend code does not need to import individual `*Api` classes.

### Usage

```ts
import { RMSApiClient } from '@m-amirazmi/rms-api-client';

const api = new RMSApiClient({ basePath: 'https://rms-devapi.merazmi.com/api/v1' });

await api.repair.list();
await api.user.list();
await api.catalog.category.list();
await api.catalog.brand.list();
```

### Method names — `operationId`

Each endpoint's `operationId` becomes the method name on its API class. Use short REST verbs:

| HTTP method | Convention | Example method |
|-------------|------------|----------------|
| `GET` (collection) | `list` | `api.repair.list()` |
| `GET` (item) | `get` | `api.repair.get({ id })` |
| `POST` | `create` | `api.repair.create(body)` |
| `PUT` / `PATCH` | `update` | `api.repair.update({ id }, body)` |
| `DELETE` | `delete` | `api.repair.delete({ id })` |

Because each tag generates its own class, the same `operationId` (e.g. `list`) can — and should — be reused across tags. Spec-level uniqueness validation is intentionally disabled (`validateSpec.set(false)` in `modules/typescript-client/build.gradle.kts`) so this works.

### Namespacing — tag names

The tag on each operation drives the property name on `RMSApiClient`. PascalCase compound tags become nested objects:

| Tag | Generated access |
|-----|------------------|
| `Repair` | `api.repair` |
| `User` | `api.user` |
| `CatalogCategory` | `api.catalog.category` |
| `CatalogBrand` | `api.catalog.brand` |
| `CatalogModel` | `api.catalog.model` |

**Rule:** Any tags that share a PascalCase prefix are grouped under that prefix as a nested object. Single-word tags stay flat. No manual edits to the wrapper are needed — the `doLast` block in `modules/typescript-client/build.gradle.kts` scans `apis/` and rebuilds `RMSApiClient.ts` on every generation.

### Adding a new endpoint

1. Add the path under `paths:` in `specs/api-spec.yaml`.
2. Set `tags:` to the API group it belongs to (e.g. `Repair`, `CatalogBrand`).
3. Set `operationId:` to the REST verb (`list`, `get`, `create`, `update`, `delete`).
4. Run `./gradlew generateTs`. The wrapper picks up the new tag automatically.
