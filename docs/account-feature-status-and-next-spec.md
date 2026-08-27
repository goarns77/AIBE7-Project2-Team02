# MatchEAT 계정 기능 현황 및 다음 작업 명세

## 1. 문서 목적과 범위

이 문서는 회원가입, 로그인, 인증, 마이페이지, 관리자 권한의 5개 **기능 영역**에 대해 현재 구현 상태를 확인하고 다음 구현의 API, 화면, 권한, 테스트 기준을 확정한다. 여기서 5개는 REST 엔드포인트가 정확히 5개라는 의미가 아니다.

기준 자료는 다음과 같다.

- 프로젝트의 `domain/account` 코드와 계정 화면
- 사이트 플로우 와이어프레임의 `REQ-MEM-01~07`, WBS `2.x`, `4.x`, `8.1~8.2`
- DB 구조 이미지의 `users 1 : 0..1 seller_profiles` 관계
- 기존 로그인·회원가입 모듈 명세

상품, 주문, 신고, 문의, 계약에 대한 관리자 처리는 각 도메인의 후속 범위다. 이 문서의 관리자 기능은 회원 상태 관리와 판매자 승인/거부까지만 다룬다.

소셜 회원가입과 소셜 로그인은 전체 필수 기능 완료 후 시도하는 확장 작업으로 분리한다. 현재 단계의 완료 판정에는 포함하지 않는다.

## 2. 현재 구현 판정

| 기능 영역 | API | 페이지 | 도메인/보안 | 판정 |
|---|---|---|---|---|
| 회원가입 | `POST /api/v1/auth/signup`, 이메일 중복 확인 구현 | `GET /signup` 구현 | 이메일 정규화, 비밀번호 정책, BCrypt, 중복 방어 구현 | 완료 |
| 로그인 | `POST /api/v1/auth/login` 구현 | `GET /login` 구현 | 활성 상태 확인, HS256 Access Token 발급 구현 | 완료 |
| 인증 | Bearer JWT 검증과 `/api/**` 인증 적용 | 별도 페이지 없음 | issuer/audience/만료/현재 계정 상태·역할·토큰 버전 검증 구현 | 부분 완료 |
| 마이페이지 | 조회·이름 수정·탈퇴·판매자 신청 구현 | 없음 | 관리형 엔티티 상태 변경과 판매자 신청 저장소 구현 | 부분 완료 |
| 관리자 권한 | 계정 관리 API 없음 | 없음 | `ADMIN` 경로 인가와 초기 관리자 생성 구현 | 부분 완료 |

현재 외부에 노출된 계정 REST API는 7개이고 계정 페이지는 2개다. 전체 Gradle 테스트 36개가 통과하며, 남은 핵심 범위는 마이페이지 화면과 관리자 관리 API·화면이다.

### 2.1 구현된 구성

- 공개 API: 이메일 중복 확인, 회원가입, 로그인
- 보호 API: 내 정보 조회, 이름 수정, 회원탈퇴, 판매자 등록 신청
- 공개 화면: 로그인, 회원가입
- JWT: HS256, `sub=userId`, `role`, `ver`, `iss`, `aud`, `iat`, `exp`, `jti`
- 보호 요청: 현재 계정의 `ACTIVE` 상태, 역할, `token_version`을 DB에서 검증
- 브라우저 토큰 저장: `sessionStorage['matcheat.accessToken']`
- DB 모델: `users`, `seller_profiles`
- 계정 상태: `ACTIVE`, `SUSPENDED`, `WITHDRAWN`
- 판매자 심사 상태: `PENDING`, `APPROVED`, `REJECTED`
- 관리자 생성: 환경변수 기반 bootstrap
- 보안 경계: `/api/v1/admin/**`는 `ROLE_ADMIN`, 그 외 `/api/**`는 인증 필수

### 2.2 주요 누락과 위험

1. 관리자 회원 상태 관리와 판매자 심사 서비스·API가 없다.
2. 마이페이지와 관리자 대시보드/판매자 승인 페이지가 없다.
3. 로그아웃은 토큰 제거 함수만 있고 화면이나 헤더 동작에 연결되지 않았다.
4. 판매자 여부는 `role` claim으로 표현되지 않는다. 승인된 `seller_profiles`를 조회하는 인가 규칙이 별도로 필요하다.
5. `auth-schema.sql`은 존재하지만 실행형 migration이 아니며 현재 `ddl-auto=update`에 의존한다.
6. HTML 경로는 모두 공개 정책이므로 화면 접근 자체가 아니라 보호 API 응답으로 권한을 강제해야 한다.

## 3. 데이터와 권한 규칙

### 3.1 회원과 판매자

- 일반 회원과 구매자는 `users.role=USER`다.
- 관리자는 `users.role=ADMIN`이며 공개 회원가입으로 생성할 수 없다.
- 판매 가능 여부는 `users.role`을 변경하지 않고 `seller_profiles.verification_status=APPROVED`로 판정한다.
- 한 회원은 판매자 프로필을 최대 하나만 가진다.
- 판매자 신청 중복은 `user_id`와 `business_number`의 DB UNIQUE 제약까지 포함해 방어한다.

### 3.2 인증 주체

- JWT `sub`를 인증된 `userId`의 유일한 출처로 사용한다.
- 클라이언트가 body/path/query로 보낸 `userId`를 현재 사용자 식별에 사용하지 않는다.
- 관리자 변경 작업의 `adminId`도 JWT `sub`에서 추출한다.
- 비밀번호, 해시, 사업자번호 전체 값은 JWT와 로그에 넣지 않는다.

### 3.3 토큰 무효화

`token_version`은 JWT 검증에 연결되어 있다.

- 발급 JWT에 `ver` claim을 추가한다.
- 회원 탈퇴 또는 관리자 정지 시 DB의 `token_version`을 1 증가시킨다.
- 보호 API 인증 시 계정 상태가 `ACTIVE`이고 JWT `ver`가 DB 값과 같은지 확인한다.
- 불일치, 정지, 탈퇴는 `401 INVALID_TOKEN`으로 처리한다.
- Redis와 Refresh Token은 이 범위에 포함하지 않는다.

## 4. 목표 API 계약

### 4.1 회원가입

기존 계약을 유지한다.

| Method | Path | 인증 | 성공 |
|---|---|---|---|
| `GET` | `/api/v1/auth/email-availability?email=` | 불필요 | `200` |
| `POST` | `/api/v1/auth/signup` | 불필요 | `201` |

추가 완료 조건:

- 가입 성공 후 `/login?signup=success`로 이동한다.
- 이메일 중복 확인 결과와 실제 가입 시 UNIQUE 제약을 모두 사용한다.
- 요청에서 `role`, `status`, `tokenVersion`을 받지 않는다.
- 이름, 이메일, 비밀번호, 비밀번호 확인 오류를 필드별로 표시한다.
- 서버의 `fieldErrors`를 해당 입력 요소에 연결하고 `aria-invalid`를 갱신한다.
- `novalidate`를 유지한다면 JavaScript에서 필수값, 이메일 형식, 길이, 비밀번호 정책을 모두 검증한다.

### 4.2 로그인

기존 `POST /api/v1/auth/login` 계약을 유지한다. 응답 JWT에는 `ver` claim을 추가한다.

- 성공: `200`, `Cache-Control: no-store`
- 실패: 존재하지 않는 이메일과 비밀번호 불일치를 모두 `401 INVALID_CREDENTIALS`로 통일
- 정지/탈퇴 계정: `403 ACCOUNT_SUSPENDED` 또는 `403 ACCOUNT_WITHDRAWN`
- 성공 후 토큰을 sessionStorage에 저장하고 원래 접근하려던 경로 또는 `/`로 이동
- 빈 값과 이메일 형식은 API 요청 전에 화면에서 검증한다.
- 잘못된 자격 증명, 정지 계정, 탈퇴 계정, 네트워크 오류를 구분해 사용자 메시지로 표시한다.
- 이미 저장된 토큰이 유효하면 로그인 화면에서 메인 또는 마이페이지로 이동한다.

### 4.3 인증 상태와 로그아웃

| Method | Path | 인증 | 성공 응답 |
|---|---|---|---|
| `GET` | `/api/v1/account/me` | USER/ADMIN | 현재 회원과 판매자 심사 요약, `200` |

`GET /api/v1/account/me` 응답 예시:

```json
{
  "userId": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "role": "USER",
  "status": "ACTIVE",
  "sellerStatus": "PENDING"
}
```

로그아웃은 현재 Access Token만 사용하는 구조이므로 서버 API를 만들지 않고 클라이언트가 `matcheat.accessToken`만 삭제한다. 로그아웃 버튼은 헤더에 연결하고 `/login`으로 이동한다. 서버 측 즉시 폐기가 필요한 요구가 생기면 별도 denylist 또는 token-version 변경 API를 새 범위로 정의한다.

일반 로그아웃에서는 `token_version`을 증가시키지 않는다. 토큰 버전 증가는 탈퇴, 관리자 정지 또는 추후 별도로 정의할 “전체 기기 로그아웃”에만 사용한다.

### 4.4 마이페이지

| Method | Path | 인증 | 설명 | 성공 |
|---|---|---|---|---|
| `PATCH` | `/api/v1/account/me` | USER/ADMIN | 이름 수정 | `200` |
| `DELETE` | `/api/v1/account/me` | USER/ADMIN | 현재 비밀번호 확인 후 탈퇴 | `204` |
| `POST` | `/api/v1/account/seller-applications` | USER | 판매자 등록 신청 | `201` |

이름 수정 요청:

```json
{ "name": "새 이름" }
```

탈퇴 요청:

```json
{ "currentPassword": "password1234" }
```

판매자 신청 요청:

```json
{
  "businessName": "매치잇 상회",
  "businessNumber": "123-45-67890",
  "latitude": 37.5665,
  "longitude": 126.9780,
  "deliveryRadiusKm": 10.0
}
```

판매자 신청 규칙:

- 기존 프로필이 없을 때만 신청 가능하다.
- 사업자명과 사업자번호는 필수다.
- 위도는 -90~90, 경도는 -180~180, 배송 반경은 0 이상이다.
- 생성 상태는 항상 `PENDING`이다.
- 재신청 정책은 이번 단계에서 “기존 REJECTED 레코드 수정”이 아니라 `409 SELLER_APPLICATION_ALREADY_EXISTS`로 고정한다.

### 4.5 관리자 회원/판매자 관리

| Method | Path | 인증 | 설명 | 성공 |
|---|---|---|---|---|
| `GET` | `/api/v1/admin/users` | ADMIN | 회원 검색/페이징 | `200` |
| `PATCH` | `/api/v1/admin/users/{userId}/status` | ADMIN | 회원 활성/정지 | `200` |
| `GET` | `/api/v1/admin/seller-applications` | ADMIN | 판매자 신청 검색/페이징 | `200` |
| `PATCH` | `/api/v1/admin/seller-applications/{sellerId}` | ADMIN | 승인/거부 | `200` |

회원 목록 query는 `keyword`, `status`, `page`, `size`를 지원한다. 기본 정렬은 `createdAt DESC`다.

회원 상태 변경 요청:

```json
{ "status": "SUSPENDED" }
```

- 허용 상태는 `ACTIVE`, `SUSPENDED`다. 탈퇴는 사용자 본인의 탈퇴 흐름으로만 처리한다.
- 관리자는 자기 계정을 정지할 수 없다.
- 정지 시 `token_version`을 증가시킨다.

판매자 심사 요청:

```json
{
  "status": "REJECTED",
  "rejectionReason": "사업자 정보를 확인할 수 없습니다."
}
```

- 허용 상태는 `APPROVED`, `REJECTED`다.
- `REJECTED`에는 사유가 필수이고 `APPROVED`에는 사유를 저장하지 않는다.
- 이미 처리된 신청을 다시 처리하면 `409 SELLER_APPLICATION_ALREADY_REVIEWED`를 반환한다.
- 심사자와 심사 시각을 저장한다.

## 5. 화면 명세

### 5.1 공통 헤더

- 비로그인: 로그인, 회원가입 표시
- 로그인: 마이페이지, 로그아웃 표시
- 관리자: 관리자 메뉴 추가 표시
- 페이지 로드 후 `GET /api/v1/account/me`로 상태를 확인한다.
- 401이면 저장 토큰을 제거하고 비로그인 상태로 전환한다.
- 보호 화면에서 401이 발생하면 현재 내부 경로를 `redirect` query로 보존해 `/login`으로 이동한다.
- UI 숨김은 편의 기능일 뿐이며 권한 강제는 API에서 수행한다.

### 5.2 회원가입 `/signup`

현재 화면을 유지하며 중복 확인, 비밀번호 정책, 비밀번호 일치 오류를 필드 단위로 표시한다. 제출 중 중복 요청을 막고 성공 후 로그인 페이지로 이동한다.

### 5.3 로그인 `/login`

현재 화면을 유지하며 공통 오류를 표시한다. 이미 유효한 토큰이 있으면 `/api/v1/account/me` 확인 후 메인으로 이동한다.

### 5.4 마이페이지 `/mypage`

와이어프레임 WBS `4.1`, `4.2`, `4.3`, `4.7`, `4.8`을 우선 구현한다.

- 회원 기본정보 조회
- 이름 수정
- 판매자 신청 또는 현재 심사 상태 표시
- 현재 비밀번호 재확인 후 탈퇴
- 채팅/구매/판매 목록은 각 도메인 페이지 링크만 배치하고 계정 작업 범위에서 CRUD를 구현하지 않는다.

### 5.5 관리자 `/admin`, `/admin/sellers`

와이어프레임 WBS `8.1`, `8.2`를 우선 구현한다.

- 관리자 대시보드에 회원 수와 판매자 심사 대기 수 표시
- 회원 검색과 상태 변경
- 판매자 신청 목록, 상세 확인, 승인/거부
- 신고/문의/상품/주문/계약 메뉴는 후속 도메인 연결점으로만 표시
- 일반 사용자가 관리자 API를 호출하면 `403`을 반환

## 6. 오류 계약

기존 `ApiErrorResponse` 형식을 모든 계정 API와 인증 실패에 사용한다.

추가 오류 코드:

- `INVALID_TOKEN`
- `CURRENT_PASSWORD_MISMATCH`
- `SELLER_APPLICATION_ALREADY_EXISTS`
- `SELLER_APPLICATION_NOT_FOUND`
- `SELLER_APPLICATION_ALREADY_REVIEWED`
- `USER_NOT_FOUND`
- `CANNOT_SUSPEND_SELF`
- `FORBIDDEN`

유효성 오류는 `400`, 미인증/무효 토큰은 `401`, 권한 부족은 `403`, 없는 리소스는 `404`, 상태 충돌은 `409`를 사용한다.

## 7. 구현 순서

1. **부분 완료:** 계정 엔티티 상태 변경과 마이페이지 저장소 확장 완료, 관리자 페이징 확장 대기
2. **부분 완료:** `AccountProfileService`, `SellerApplicationService` 완료, `AdminAccountService` 대기
3. **완료:** JWT `ver` 발급 및 보호 요청의 계정 상태/역할/버전 검증, 공통 401/403 응답
4. **부분 완료:** 계정 DTO·REST controller 완료, 관리자 DTO·controller 대기
5. `/mypage`, `/admin`, `/admin/sellers` 페이지와 외부 JavaScript 구현
6. 회원가입·로그인 필드별 검증과 서버 오류 매핑 보완
7. 헤더 인증 상태, 로그아웃, 401 로그인 리다이렉트 연결
8. 단위, MVC, Security, 페이지 통합 테스트 실행

## 8. 테스트와 완료 기준

다음 항목을 자동 테스트로 검증해야 한다.

- 현재 사용자 조회에서 JWT `sub`를 사용하고 요청의 임의 userId를 신뢰하지 않음
- 이름 수정과 현재 비밀번호 불일치 처리
- 탈퇴 시 상태, 탈퇴 시각, token version 변경
- 판매자 신청 정상/회원 중복/사업자번호 중복/범위 유효성
- 관리자만 회원 목록과 판매자 심사 API 접근 가능
- 일반 사용자 `403`, 미인증 `401` 및 공통 오류 JSON
- 관리자 자기 정지 방지와 사용자 정지 후 기존 JWT 거부
- 판매자 승인/거부, 거부 사유 필수, 중복 심사 방지
- 로그인/회원가입 기존 회귀 테스트 유지
- 회원가입 필드 오류와 로그인 오류 코드의 화면 메시지 매핑
- 로그아웃 시 계정 토큰만 제거하고 다른 storage 값을 보존
- 401 발생 시 안전한 내부 redirect 경로만 사용
- 페이지 controller 렌더링과 JavaScript API 경로 일치
- 전체 `gradlew test` 통과

완료 판정은 API 구현만으로 하지 않는다. 5개 기능 영역의 화면 흐름, 권한 실패 경로, DB 상태 전이, 자동 테스트가 함께 충족되어야 한다.

## 9. 확정된 작업 전제

### 9.1 토큰 버전 검증

DB 기반 `token_version` 검증을 적용한다. 이는 선택 사항이 아니라 이번 구현의 완료 조건이다.

- JWT 발급 시 현재 계정의 `token_version`을 `ver` claim으로 넣는다.
- 보호 API 요청마다 계정을 조회해 `ACTIVE` 상태와 토큰 버전 일치를 검증한다.
- 탈퇴 또는 관리자 정지 시 트랜잭션 안에서 `token_version`을 증가시킨다.
- 증가 전에 발급된 JWT는 다음 보호 API 요청부터 `401 INVALID_TOKEN`으로 거부한다.
- 관리자 권한도 JWT claim만 신뢰하지 않고 조회한 현재 계정의 역할과 상태를 함께 확인한다.

### 9.2 DB 스키마 관리

이번 작업에서는 현재 DB 스키마 관리 방식을 유지한다.

- `spring.jpa.hibernate.ddl-auto=update`를 그대로 사용한다.
- 기존 `auth-schema.sql`과 JPA 엔티티 구조를 기준으로 계정 기능을 구현한다.
- Flyway 의존성이나 migration 파일을 이번 작업에서 추가하지 않는다.
- `users.token_version` 등 기존 컬럼을 활용하므로 토큰 버전 검증을 위한 신규 컬럼은 추가하지 않는다.
- Flyway 전환과 운영 환경의 `ddl-auto=validate` 적용은 별도 후속 작업으로 남긴다.

## 10. 후속 확장: 소셜 회원가입·로그인

소셜 인증은 회원가입, 로그인, 인증, 마이페이지, 관리자 권한의 필수 작업과 테스트가 모두 완료된 뒤 별도 작업으로 착수한다.

- 대상 공급자(Google, Kakao, Naver 등)를 먼저 확정한다.
- `social_accounts`와 같은 별도 연결 모델을 사용하고 `(provider, provider_subject)`에 UNIQUE 제약을 둔다.
- 기존 이메일 계정과 소셜 계정의 자동 병합 여부를 정책으로 확정한다.
- OAuth callback에서 Access Token을 URL query로 직접 전달하지 않는다.
- 일회용 교환 코드 또는 동등한 안전한 JWT 전달 흐름을 사용한다.
- 앱 로그아웃은 소셜 공급자 계정의 전체 로그아웃과 분리한다.
- 와이어프레임의 중복된 `REQ-MEM-06`을 사용하지 않고 소셜 인증 요구사항에는 별도 ID를 부여한다.
