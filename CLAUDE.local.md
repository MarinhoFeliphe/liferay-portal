# Liferay Portal - Local Development Notes

## Project Overview

Liferay Portal is a large-scale enterprise Java platform built as a monorepo. It uses a combination of Ant (for the core platform) and Gradle (for OSGi modules). The platform runs on an application server (Tomcat by default) and uses OSGi for modularity.

## Build System

- **Core platform**: Built with Ant (`build.xml` at root). Run `ant all` to build everything, `ant compile` for compilation only.
- **Modules**: Built with Gradle via the `gradlew` wrapper. Located under `modules/`.
- **Gradle daemon is disabled** (`org.gradle.daemon=false` in `modules/gradle.properties`).
- **Configure on demand is enabled** (`org.gradle.configureondemand=true`).

### Common Build Commands

```bash
# Build everything (core + modules)
ant all

# Compile core only
ant compile

# Build a specific module (from modules/ dir)
../gradlew :apps:<app-name>:<module-name>:deploy

# Format source code
ant format-source

# Build language files
../gradlew buildLang
```

## Project Structure

```
liferay-portal/
  build.xml              # Ant build for core platform
  build.properties       # Main build properties (DO NOT EDIT - use build.me.properties)
  build.me.properties    # User-specific build overrides
  app.server.properties  # App server config (DO NOT EDIT - use app.server.me.properties)
  app.server.me.properties  # User-specific app server overrides
  portal-kernel/         # Core kernel API (com.liferay.portal.kernel)
  portal-impl/           # Core implementation
  portal-web/            # Portal web resources
  portal-test/           # Test utilities
  util-java/             # Java utilities
  util-taglib/           # Taglib utilities
  util-bridges/          # Bridge utilities
  modules/               # OSGi modules (Gradle-based)
    apps/                # Application modules
    core/                # Core modules
    dxp/                 # DXP-specific modules
  sql/                   # Database SQL scripts
  definitions/           # XML/XSD definitions
  tools/                 # Build and development tools
  lib/                   # Libraries
```

## Module Structure

Modules follow a consistent pattern under `modules/apps/<app-name>/`:
- `<app-name>-api/` - Service API (exported packages, interfaces)
- `<app-name>-impl/` or `<app-name>-service/` - Service implementation
- `<app-name>-web/` - Web/portlet module
- `<app-name>-rest-api/` - REST API definitions
- `<app-name>-rest-impl/` - REST implementation
- `<app-name>-rest-client/` - REST client
- `<app-name>-rest-test/` - REST integration tests
- `<app-name>-test/` - Tests

Each module has:
- `bnd.bnd` - OSGi bundle metadata (Bundle-Name, Bundle-SymbolicName, Bundle-Version, Export-Package)
- `build.gradle` - Gradle dependencies
- Source in `src/main/java/com/liferay/...`
- Resources in `src/main/resources/`

## Coding Conventions

- **Package naming**: `com.liferay.<module-path>` (dots replace hyphens, e.g., `ai-hub-cell-impl` -> `com.liferay.ai.hub.cell.impl`)
- **OSGi components**: Use `@Component` annotations from `org.osgi.service.component.annotations`
- **Dependencies**: Use `compileOnly` scope in build.gradle; reference other modules via `project(":apps:<app>:<module>")`
- **Portal kernel**: Referenced as `group: "com.liferay.portal", name: "com.liferay.portal.kernel", version: "default"`
- **Source formatting**: Liferay has strict source formatting rules enforced by Source Formatter. Run `ant format-source` or the SF gradle task before committing.
- **Properties files**: Never edit `build.properties` or `app.server.properties` directly. Use `build.me.properties` and `app.server.me.properties` for local overrides.

## Local Environment

- **App server**: Tomcat (configured in `app.server.me.properties`)
- **Bundles directory**: `/home/me/dev/bundles/master`
- **JVM**: Requires `--add-opens` flags for Java module system compatibility
- **Node.js**: Development mode (`NODE_ENV=development`)

## Source Formatter

The project uses a custom Source Formatter tool with exclusion patterns defined in `source-formatter.properties`. It enforces:
- Java term ordering
- Import ordering
- Code style consistency
- Upgrade process conventions

Exclusion paths include: `.cache`, `.m2`, `bower_components`, `build_gradle`, `bundles` directories.

## Key Technologies

- Java (OSGi / BND)
- Gradle 8.5 (patched) + Ant
- Jakarta Servlet API 6.0
- Petra (Liferay's utility library, e.g., `petra-string`)
- Service Builder (code generation for service layer)
- REST Builder (code generation for REST APIs)
