# CI

This project uses GitHub Actions plus a local reusable script.

## Local Check

Run the same checks as CI:

```bash
GLASSFISH_HOME=/path/to/glassfish7/glassfish bash scripts/ci.sh
```

The script verifies:

- Java source and main-style tests compile on Java 17.
- `common.logger.audit.AuditSupportTest` passes.
- `module.bussiness.admin.AdminServiceSupportTest` passes.
- NetBeans Ant `clean dist` builds `dist/Ecommerce.war`.
- JSP compilation runs with `javac.source=1.8` because the bundled NetBeans JSP compiler rejects `compilerSourceVM=17`.

## GitHub Actions

`.github/workflows/ci.yml` runs on pushes, pull requests to `main`, and manual dispatch. It installs Temurin 17, downloads pinned GlassFish 7.0.12, caches GlassFish, runs `scripts/ci.sh`, uploads the WAR artifact, and performs a Docker build smoke test without pushing.

`.github/workflows/deploy.yml` is gated by the `CI` workflow. Pushes to `main` deploy only after CI finishes successfully.
