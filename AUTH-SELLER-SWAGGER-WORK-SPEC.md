# 인증·판매자 권한·Swagger 연동 작업 명세

## 1. 문서 목적

이 문서는 회원 계정의 `SELLER` 권한 승격, JWT 권한 변환, 판매자 API 접근 제어, Swagger/OpenAPI 인증 표시 패치의 구현 상태와 검증 기준을 정의한다. 사람 또는 LLM 작업자는 이 문서를 기준으로 변경 사항을 검토하고 후속 작업을 수행한다.

## 2. 적용 범위와 현재 상태

- 회원 역할에 `USER`, `SELLER`, `ADMIN`을 사용한다.
- 신규 회원은 기존과 동일하게 `USER`로 생성한다.
- 판매자 신청 승인 시 사용자 역할을 `SELLER`로 승격한다.
- 판매자는 구매자 기능도 사용할 수 있어야 하므로 SELLER JWT를 `ROLE_USER`, `ROLE_SELLER` 두 권한으로 변환한다.
- 판매자 전용 API는 Spring Security에서 `ROLE_SELLER`를 요구한다.
- Swagger의 Bearer 인증 스키마는 하나의 설정 클래스에서만 관리한다.
- `/api/v1/auth/**`와 공개 상품 조회는 공개 API로 문서화하고, 그 외 `/api/**`에는 Bearer 인증 요구사항을 표시한다.

현재 위 항목은 작업 트리에 구현되어 있으며 전체 Gradle 테스트가 통과한 상태다. 아직 Git 커밋 여부는 별도로 확인해야 한다.

## 3. 권한 모델

| 계정 역할 | JWT `role` 클레임 | Spring Security 권한 | 허용 범위 |
| --- | --- | --- | --- |
| `USER` | `USER` | `ROLE_USER` | 인증 사용자 및 구매자 기능 |
| `SELLER` | `SELLER` | `ROLE_USER`, `ROLE_SELLER` | 구매자 기능과 판매자 기능 |
| `ADMIN` | `ADMIN` | `ROLE_ADMIN` | 관리자 기능 |

판매자 승격 시 `tokenVersion`을 1 증가시킨다. 따라서 승인 전에 발급된 JWT는 무효화되며, 승인된 사용자는 다시 로그인해 `role=SELLER` 토큰을 발급받아야 한다.

## 4. 판매자 전용 API

다음 요청은 `ROLE_SELLER`가 없으면 HTTP 403을 반환해야 한다.

| HTTP 메서드 | 경로 | 용도 |
| --- | --- | --- |
| `POST` | `/api/v1/products` | 상품 등록 |
| `POST` | `/api/v1/products/**` | 상품 등록 계열 요청 |
| `PATCH` | `/api/v1/products/**` | 상품 수정 |
| `DELETE` | `/api/v1/products/**` | 상품 삭제 |
| `GET` | `/api/v1/products/mine` | 내 상품 조회 |
| `POST` | `/api/v1/requests/{requestId}/proposals` | 판매 제안 생성 |
| `GET` | `/api/v1/proposals/sent` | 보낸 제안 조회 |
| `GET` | `/api/v1/proposals/eligibility` | 판매자 제안 자격 확인 |
| `GET` | `/api/v1/estimates/received` | 판매자가 받은 견적 요청 조회 |
| `GET` | `/api/products/{productId}/order-requests/recommendations` | 상품 기반 주문 추천 조회 |
| `POST` | `/api/v1/quotes/to-buyer` | 판매자에서 구매자로 직접 견적 발송 |

그 외 `/api/**`는 기본적으로 인증만 요구하며, 리소스 소유자 및 거래 참여자 검증은 각 서비스 계층에서 계속 수행한다. URL 권한만으로 소유권 검증을 대체하지 않는다.

상품 목록 `GET /api/v1/products`, 검색 `GET /api/v1/products/search`, 상세 `GET /api/v1/products/{id}`는 비로그인 사용자에게도 공개한다. `GET /api/v1/products/mine`은 경로가 유사하더라도 판매자 전용이며 공개하면 안 된다. 판매자 matcher를 공개 상품 matcher보다 먼저 선언하거나 공개 경로를 정확히 열거해 이 우선순위를 유지한다.

## 5. 판매자 승인 흐름

1. 일반 회원이 판매자 등록을 신청한다.
2. 신청 상태는 `PENDING`으로 저장된다.
3. 관리자가 승인하면 `SellerProfileEntity.approve(...)`가 호출된다.
4. 연결된 `UserAccountEntity.promoteToSeller()`가 역할을 `SELLER`로 변경한다.
5. `tokenVersion` 증가로 기존 JWT를 무효화한다.
6. 판매자가 다시 로그인하면 JWT의 `role` 클레임에 `SELLER`가 포함된다.
7. JWT 변환기는 해당 토큰에 `ROLE_USER`, `ROLE_SELLER`를 부여한다.

`USER`가 아닌 계정을 판매자로 승격하려 하면 예외가 발생해야 한다. 이미 `SELLER`인 계정의 승격 호출은 중복 변경 없이 종료한다.

## 6. DB 적용 규칙

신규 DB 생성 스키마의 `users.role` 체크 제약은 다음 값을 허용해야 한다.

```sql
check (role in ('USER', 'SELLER', 'ADMIN'))
```

기존 PostgreSQL DB에는 애플리케이션 배포 전에 `src/main/resources/db/add-seller-role.sql`을 한 번 적용한다. 이 스크립트는 다음 작업을 수행한다.

- 기존 `ck_users_role` 제약을 제거하고 `SELLER` 허용 제약으로 교체한다.
- 이미 승인된 판매자 프로필을 가진 `USER` 계정을 `SELLER`로 보정한다.
- 보정 대상 계정의 `token_version`을 증가시켜 기존 JWT를 무효화한다.

운영 적용 전 DB 백업, 대상 제약 이름, 승인 판매자 보정 건수를 확인해야 한다. 마이그레이션 적용 후 아래 조건을 조회해 검증한다.

```sql
select u.user_id, u.role, s.verification_status
from users u
join seller_profiles s on s.user_id = u.user_id
where s.verification_status = 'APPROVED'
  and u.role <> 'SELLER';
```

결과는 0건이어야 한다.

## 7. Swagger/OpenAPI 규칙

- 설정 클래스는 `src/main/java/org/example/matcheat/config/OpenApiConfig.java` 하나만 사용한다.
- 보안 스키마 이름은 `bearerAuth`다.
- 보안 타입은 HTTP Bearer이며 포맷은 JWT다.
- OpenAPI 루트에 전역 `security`를 추가하지 않는다.
- `/api/v1/auth/**` 작업에는 인증 요구사항을 추가하지 않는다.
- 상품 목록, 검색, 상세의 `GET` 작업에는 인증 요구사항을 추가하지 않는다.
- 같은 `/api/v1/products` 경로라도 `POST` 작업에는 `bearerAuth` 요구사항을 추가한다.
- `GET /api/v1/products/mine`을 포함한 그 외 `/api/**` 작업에는 `bearerAuth` 요구사항을 추가한다.
- 기존 `SwaggerConfig.java`는 중복 Bean과 로그인 API의 전역 잠금 표시를 방지하기 위해 제거한다.

Swagger UI에서 인증이 필요한 API를 호출할 때는 `Authorize`에 로그인 응답의 Access Token을 입력한다. 입력 형식은 UI 동작에 따라 토큰 원문을 우선 사용하며, UI가 접두사를 자동으로 붙이지 않는 구성에서만 `Bearer <token>` 형식을 사용한다.

## 8. 변경 파일

| 파일 | 책임 |
| --- | --- |
| `src/main/java/org/example/matcheat/domain/account/enums/UserRole.java` | `SELLER` 역할 정의 |
| `src/main/java/org/example/matcheat/domain/account/entity/UserAccountEntity.java` | 판매자 승격 및 토큰 버전 증가 |
| `src/main/java/org/example/matcheat/domain/account/entity/SellerProfileEntity.java` | 관리자 승인과 계정 승격 연결 |
| `src/main/java/org/example/matcheat/domain/account/config/AccountSecuritySupportConfiguration.java` | SELLER JWT 복합 권한 변환 |
| `src/main/java/org/example/matcheat/config/SecurityConfig.java` | 판매자 전용 API 접근 제어 |
| `src/main/java/org/example/matcheat/config/OpenApiConfig.java` | Swagger Bearer 스키마와 경로별 인증 표시 |
| `src/main/java/org/example/matcheat/config/SwaggerConfig.java` | 제거 대상인 중복 Swagger 설정 |
| `src/main/resources/db/auth-schema.sql` | 신규 DB의 SELLER 역할 제약 |
| `src/main/resources/db/add-seller-role.sql` | 기존 DB 마이그레이션 및 데이터 보정 |
| `src/test/java/org/example/matcheat/config/SecurityConfigMvcTest.java` | USER 403, SELLER 허용 검증 |
| `src/test/java/org/example/matcheat/config/OpenApiConfigTest.java` | 경로별 OpenAPI 인증 규칙 검증 |
| `src/test/java/org/example/matcheat/config/OpenApiIntegrationTest.java` | 실제 `/v3/api-docs` 출력 검증 |
| `src/test/java/org/example/matcheat/domain/account/config/JwtSecuritySupportTest.java` | SELLER JWT 클레임 및 권한 검증 |
| `src/test/java/org/example/matcheat/domain/account/service/AdminAccountIntegrationTest.java` | 승인, 역할 승격, 기존 JWT 무효화 검증 |

## 9. 검증 절차

전체 자동 테스트를 실행한다.

```powershell
.\gradlew.bat test
```

변경 파일의 공백 및 패치 오류를 확인한다.

```powershell
git diff --check
git status --short
```

수동 API 검증은 다음 순서로 수행한다.

1. 일반 USER로 로그인해 JWT를 발급받는다.
2. 해당 JWT로 판매자 전용 API를 호출하고 403을 확인한다.
3. 관리자 계정으로 판매자 신청을 승인한다.
4. 승인 전 USER JWT로 다시 호출하고 인증 실패를 확인한다.
5. 승인된 계정으로 다시 로그인하고 JWT의 `role=SELLER`를 확인한다.
6. 새 JWT로 판매자 전용 API 호출 성공을 확인한다.
7. 같은 SELLER JWT로 마이페이지 등 일반 사용자 API 호출 성공을 확인한다.
8. `/v3/api-docs`에서 로그인 작업에는 `security`가 없고 보호 API에는 `bearerAuth`가 있는지 확인한다.

## 10. 완료 기준

- 판매자 승인과 계정 역할 승격이 하나의 트랜잭션 흐름에서 처리된다.
- 승인 전 JWT는 승인 후 사용할 수 없다.
- USER의 판매자 전용 API 호출은 403이다.
- SELLER는 판매자 전용 API와 일반 사용자 API를 모두 사용할 수 있다.
- ADMIN 경로는 기존과 동일하게 `ROLE_ADMIN`만 허용한다.
- Swagger 로그인 API는 공개 상태로 표시되고 보호 API에는 Bearer 인증이 표시된다.
- 기존 DB의 역할 제약과 승인 판매자 데이터가 보정된다.
- 전체 테스트와 `git diff --check`가 통과한다.

## 11. 후속 작업 시 주의사항

- 새 판매자 API를 추가하면 `SecurityConfig`의 판매자 경로와 보안 테스트를 함께 갱신한다.
- URI 문자열만으로 권한을 판단하지 말고, 판매자 승인 상태 및 리소스 소유권 검증을 서비스 계층에도 유지한다.
- JWT의 `role` 값과 Spring Authority는 동일 개념이 아니다. SELLER의 복합 권한 변환을 제거하면 판매자의 일반 사용자 기능이 회귀할 수 있다.
- Swagger 설정 클래스를 추가로 만들거나 전역 `SecurityRequirement`를 추가하면 로그인 API까지 잠긴 것으로 표시될 수 있다.
- DB 마이그레이션 도구를 도입하면 수동 SQL을 버전 마이그레이션으로 이전하되 동일한 제약 변경과 데이터 보정 의미를 유지한다.

## 12. 신규 커밋 `a703720` 영향 기록

`Merge pull request #32 from feat/order-auth-integration` 반영 후 다음 영향을 확인하고 호환 패치를 적용했다.

- 신규 커밋이 `SecurityConfig`에 상품 GET 공개 규칙을 추가하면서 미커밋 상태였던 판매자 matcher가 사라졌다. 판매자 matcher를 복구했다.
- 커밋의 `/api/v1/products/**` GET 공개 패턴은 `/api/v1/products/mine`까지 포함했다. 공개 범위를 목록, 검색, 상세로 좁히고 판매자 전용 `mine`을 우선 처리했다.
- OpenAPI 설정을 경로 단위에서 HTTP 메서드 단위로 변경했다. 따라서 같은 `/api/v1/products`라도 GET은 공개, POST는 Bearer 인증 대상으로 문서화된다.
- 새 매칭 API `/api/v1/requests/{requestId}/matches`는 기존 `/api/**` 기본 규칙에 따라 인증이 필요하며 컨트롤러에서 주문 소유자도 검사한다.
- 상품 서비스의 승인 판매자 검증은 유지되어 SELLER URL 권한과 서비스 계층 검증이 함께 동작한다.
- `auth-header.js`의 관리자 메뉴 변경은 SELLER 권한 판정과 직접 충돌하지 않는다. SELLER는 관리자가 아니므로 일반 회원 메뉴가 계속 표시된다.
- Kakao 경로 API 사용을 위한 `spring-boot-starter-restclient`와 `KAKAO_REST_API_KEY`가 추가됐다. JWT/Swagger 동작에는 직접 영향이 없지만 실제 매칭 실행 환경에는 해당 키가 필요하다.

검토가 남은 항목이 있다. 화면 경로 `/requests/{requestId}/matches`는 기존 `/requests/**` 공개 정책을 상속하면서 서버에서 주문 정보를 조회한다. API는 보호되지만 HTML 응답에 주문 정보가 포함될 수 있으므로 정보 노출 여부를 별도 검토해야 한다. 현재 인증 토큰이 localStorage에 있고 일반 페이지 이동에는 Authorization 헤더가 실리지 않으므로, 화면 경로를 단순히 인증 필수로 변경하기 전에 쿠키 인증 또는 클라이언트 전용 데이터 조회 구조를 결정해야 한다.
