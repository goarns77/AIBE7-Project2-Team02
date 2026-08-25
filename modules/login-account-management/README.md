# Login Account Management Module

Independent MatchEAT account module for email signup, email/password login, HS256 Bearer JWT support, and future profile/admin extension points.

## Start here

- Implementation contract: `integration/ACCOUNT_SPEC.md`
- Integration guide: `integration/INTEGRATION.md`
- PostgreSQL schema contract: `integration/auth-schema.sql`
- Environment variable template: `integration/env.example`

## Verify

Run from the repository root:

```powershell
$env:GRADLE_USER_HOME="$PWD\.gradle-account-module"
.\gradlew.bat -p modules\login-account-management clean test
```

The root application does not include this module until the team applies the Gradle and security snippets under `integration`. The module directory is the single source of truth; do not duplicate its Java sources in the root `src` directory.
