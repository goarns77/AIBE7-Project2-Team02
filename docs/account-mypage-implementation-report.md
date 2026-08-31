# MatchEAT 저장소 및 마이페이지 API 구현 보고서

## 1. 작업 범위

계정 기능 명세에 따라 기존 테이블 구조를 유지하면서 다음 기능을 구현했다.

- 현재 회원 정보 조회
- 회원 이름 수정
- 현재 비밀번호 확인 후 회원탈퇴
- 판매자 등록 신청
- 탈퇴 직후 기존 JWT 무효화

마이페이지 HTML 화면, 관리자 기능, 소셜 인증은 이번 범위에 포함하지 않았다.

## 2. 저장소 및 도메인 변경

### 사용자 계정

- 신규 가입용 `save()`와 기존 계정 변경 경로를 분리했다.
- 관리형 `UserAccountEntity`에 이름 변경과 탈퇴 상태 변경 메서드를 추가했다.
- 탈퇴 시 `status=WITHDRAWN`, `withdrawn_at`, `token_version + 1`을 한 트랜잭션에서 반영한다.
- 사용자 ID 조회와 변경 결과는 기존 도메인 `UserAccount`로 반환한다.

### 판매자 신청

- `SellerApplicationRepository`와 JPA 어댑터를 추가해 서비스에서 JPA 엔티티를 직접 다루지 않게 했다.
- 사용자별 판매자 상태 조회, 사용자·사업자번호 중복 확인, 신규 신청 저장을 지원한다.
- 신청 및 심사 시각은 엔티티 내부 시스템 시각 대신 서비스의 `Clock`에서 전달하도록 변경했다.
- 저장 상태는 항상 `PENDING`이다.

DB 테이블이나 컬럼은 추가하지 않았다. 현재 `users`, `seller_profiles`와 `ddl-auto=update` 설정을 그대로 사용한다.

## 3. API 구현

| Method | Path | 결과 |
|---|---|---|
| `GET` | `/api/v1/account/me` | 현재 회원과 판매자 심사 상태, `200` |
| `PATCH` | `/api/v1/account/me` | 이름 수정 결과, `200` |
| `DELETE` | `/api/v1/account/me` | 회원탈퇴, `204` |
| `POST` | `/api/v1/account/seller-applications` | 판매자 신청 결과, `201` |

모든 API는 JWT 인증이 필요하며 회원 ID는 JWT `sub`에서만 가져온다. 요청 body나 query의 사용자 ID는 사용하지 않는다.

## 4. 유효성 및 오류 처리

- 이름은 trim 후 1~50자로 저장한다.
- 탈퇴 전 현재 비밀번호를 BCrypt로 확인한다.
- 사업자명은 trim 후 1~100자로 저장한다.
- 사업자번호는 숫자와 구분 기호 입력을 허용하고 숫자 10자리로 정규화해 저장한다.
- 위도와 경도는 함께 입력해야 하며 각각 -90~90, -180~180 범위와 소수점 7자리를 검사한다.
- 배송 반경은 0 이상, 소수점 2자리 이하로 제한한다.
- 한 회원의 중복 신청 또는 같은 사업자번호는 `409 SELLER_APPLICATION_ALREADY_EXISTS`로 처리한다.
- 없는 회원은 `404 USER_NOT_FOUND`, 현재 비밀번호 불일치는 `400 CURRENT_PASSWORD_MISMATCH`로 처리한다.
- 정지·탈퇴 계정은 보호 API에서 거부한다.

## 5. JWT 연동

탈퇴 트랜잭션에서 `token_version`을 증가시킨다. 이미 발급된 JWT의 `ver` claim과 DB 값이 달라지므로 다음 보호 API 요청부터 `401 INVALID_TOKEN`이 반환된다.

일반 이름 수정과 판매자 신청은 토큰 버전을 변경하지 않는다.

## 6. 테스트 결과

실행 명령:

```powershell
.\gradlew.bat test --rerun-tasks
```

결과: 전체 36개 테스트 통과

추가 검증 항목:

- 판매자 상태를 포함한 현재 회원 조회
- 이름 trim 및 변경
- 현재 비밀번호 일치·불일치
- 서비스 `Clock`을 사용한 탈퇴 시각
- 사업자번호 정규화와 `PENDING` 신청 생성
- 사용자·사업자 중복 신청 거부
- 잘못된 사업자번호와 불완전한 좌표 거부
- 실제 JPA 저장 후 마이페이지 조회·수정
- 판매자 신청 후 `sellerStatus=PENDING` 조회
- 탈퇴 직후 기존 JWT의 401 거부

통합 테스트는 H2 PostgreSQL 호환 모드와 실제 Spring SecurityFilterChain을 사용했다. 현재 실행 터미널에는 실제 PostgreSQL 접속 환경변수와 `psql`이 없어 운영 DB 대상 쿼리는 실행하지 않았다.

## 7. 남은 작업

1. `/mypage` 페이지와 외부 JavaScript 구현
2. 헤더 인증 상태, 로그아웃, 401 로그인 리다이렉트 연결
3. 회원가입·로그인 필드별 오류 표시 보완
4. 관리자 회원 상태 관리와 판매자 승인·거부 API
5. `/admin`, `/admin/sellers` 페이지 구현
6. 실제 PostgreSQL의 UNIQUE·FK·CHECK·index 적용 상태 확인
