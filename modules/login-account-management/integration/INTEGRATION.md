# Login Account Management Integration

## Module status

`modules/login-account-management` is the only source of account module Java code. Do not copy the same sources into the root `src` tree.

The required implementation covers signup, email availability, login, JWT issuing and verification support, account entities, administrator bootstrap, and functional mock pages. Profile and administrator CRUD are extension ports only.

## Isolated build

From the repository root on Windows:

```powershell
$env:GRADLE_USER_HOME="$PWD\.gradle-account-module"
.\gradlew.bat -p modules\login-account-management clean test
```

The dedicated cache path avoids contention with another Gradle process using the default user cache.

## Root integration

Coordinate these edits before applying them because the target files are shared by the team.

1. Merge `root-settings.gradle.snippet` into the root `settings.gradle`.
2. Merge `root-build.gradle.snippet` into the root `build.gradle` dependencies.
3. Copy the content of `application-account.example.yaml` into `src/main/resources/application-account.yaml`.
4. Copy the content of `application-db.example.yaml` into `src/main/resources/application-db.yaml`, unless the team already owns that file.
5. Merge `security-config.snippet` into the application's single `SecurityFilterChain`.
6. Merge the following profile selection into the existing root `application.yaml` without replacing other settings.

```yaml
spring:
  profiles:
    active: local,db,account

---
spring:
  config:
    # The local profile enables this import; config.import performs the actual .env read.
    activate:
      on-profile: local
    import: optional:file:.env[.properties]
```

7. Create the root `.env` from `env.example`. Never commit its values.
8. Apply `auth-schema.sql` manually or move it into the team's central Flyway migration sequence.
9. Run the full root build and tests.

## JWT secret generation

Generate at least 32 random bytes and store their Base64 representation as `JWT_SECRET`. Do not use a memorable sentence, example value, or repository default.

## Security boundaries

- Browser authentication uses `Authorization: Bearer`; no authentication cookie is created.
- HTML routes remain public because browser navigation cannot attach a Bearer header.
- Protected access is enforced at `/api/**`.
- The access token is stored under `sessionStorage['matcheat.accessToken']`.
- Access tokens expire after one hour by default and cannot be revoked immediately in this module version.
- A suspended or withdrawn user's already-issued token can remain usable until expiration. Refresh tokens and token-version checks are deferred.

## Database ownership

This module owns `users` and `seller_profiles`. Other modules should reference their primary keys and must not redefine these tables or JPA entities.

Flyway adoption remains a team decision. Until then, `auth-schema.sql` is the authoritative schema contract and the module does not force `ddl-auto=update`.

## Future extension points

- `ProfileUseCase`: current-user query, name update, withdrawal
- `SellerApplicationUseCase`: seller application
- `AdminAccountManagementUseCase`: user status and seller review

Other administrator domains such as products, orders, reports, inquiries, and contracts remain outside this module.
