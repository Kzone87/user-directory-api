# Business Ops Dashboard · V2

![CI](https://github.com/Kzone87/user-directory-api/actions/workflows/ci.yml/badge.svg)

중소규모 업무시스템 외주를 가정해 만든 **React + Spring Boot 풀스택 운영관리 시스템 공개 사례**입니다. 고객/연락처 CRUD와 Excel export에 더해 V2에서는 실제 업무에서 자주 필요한 **접수 → 진행 → 완료/취소 상태 머신**을 추가했습니다.

> 저장소 이름은 기존 Git 이력을 보존하기 위해 `user-directory-api`를 유지하지만, 프로젝트 역할은 백엔드 단독 예제가 아니라 `Business Ops Dashboard` 풀스택 사례입니다.

## Business use case

실제 외주에서 반복되는 요구를 작은 CRM/업무관리 시스템 형태로 구현합니다.

- 관리자 화면에서 고객/연락처 조회·검색·등록·수정·삭제
- 중복/잘못된 입력 차단
- Excel 결과 다운로드
- 신규 업무 접수
- 담당자 지정
- 업무 상태 필터
- 접수 → 진행 → 완료/취소 상태 전이
- 허용되지 않은 상태 변경과 동시 변경 방어
- API 문서와 자동화된 회귀 검증

## V2 implemented

### Frontend

- React 18 + TypeScript + Vite
- 운영 현황 metric dashboard
- 사용자/연락처 검색·CRUD UI
- 업무 접수 form
- 업무 상태 필터
- 상태별 badge와 허용 action만 노출
- API 오류 사용자 피드백
- responsive admin layout

### Backend

- Spring Boot REST API
- MyBatis 기반 사용자 CRUD / 검색 / pagination / sort
- Work Order API
- `RECEIVED → IN_PROGRESS → DONE` 상태 흐름
- `RECEIVED/IN_PROGRESS → CANCELLED` 취소 흐름
- 완료/취소 상태를 terminal state로 처리
- 조건부 `UPDATE ... WHERE status = expectedStatus`로 stale/concurrent transition 방어
- Bean Validation
- 400 / 404 / 409 오류 계약
- Excel XLSX export
- OpenAPI / Swagger UI
- H2 demo DB
- MockMvc integration tests

## Architecture

```text
React / TypeScript
        │
        │ HTTP JSON
        ▼
Spring Boot Controllers
        │
        ▼
Services
├─ User CRUD / query validation
└─ Work-order state machine
        │
        ▼
MyBatis
        │
        ▼
H2 demo DB
├─ users
└─ work_orders
```

## Work-order state machine

```text
RECEIVED
   ├──> IN_PROGRESS ───> DONE
   │          └────────> CANCELLED
   └──────────────────> CANCELLED
```

`DONE`, `CANCELLED`은 종료 상태입니다. 서버가 전이 규칙을 최종 검증하므로 프런트에서 임의 요청을 보내더라도 허용되지 않은 전이는 `409 INVALID_WORK_ORDER_TRANSITION`으로 거부합니다.

또한 상태 변경 SQL에 현재 상태를 다시 조건으로 포함합니다.

```text
UPDATE work_orders
SET status = next
WHERE id = ?
  AND status = expected
```

따라서 사용자가 오래된 화면에서 상태를 변경하려고 해 이미 다른 요청이 상태를 바꾼 경우 변경 건수 0을 확인해 충돌로 처리합니다.

## Main API

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/users` | 이름/이메일 검색 |
| GET | `/api/users/page` | pagination + sort |
| POST | `/api/users` | 연락처 등록 |
| PUT | `/api/users/{id}` | 연락처 수정 |
| DELETE | `/api/users/{id}` | 연락처 삭제 |
| GET | `/api/users/export` | XLSX export |
| GET | `/api/work-orders` | 업무 목록 / status filter |
| GET | `/api/work-orders/{id}` | 업무 단건 |
| POST | `/api/work-orders` | 신규 업무 접수 |
| PATCH | `/api/work-orders/{id}/status` | 상태 전이 |

## Run locally in VS Code

Eclipse가 필요하지 않습니다.

### Backend

Requirements: Java 17+, Maven 3.9+

```bash
mvn spring-boot:run
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

- UI: `http://localhost:5173`

Vite는 개발 중 `/api` 요청을 Spring Boot로 proxy합니다.

## Verification

Backend:

```bash
mvn verify
```

Frontend:

```bash
cd frontend
npm install
npm run build
```

GitHub Actions에서 두 job을 각각 검증합니다.

### Integration tests

- 사용자 검색/생성/validation/pagination/export
- OpenAPI 계약
- 업무 seed/filter
- 업무 생성 시 `RECEIVED`
- `RECEIVED → IN_PROGRESS` 정상 전이
- `RECEIVED → DONE` 단계 건너뛰기 거부
- terminal 상태 재전이 거부

## Tech stack

- React 18
- TypeScript
- Vite
- Java 17
- Spring Boot 3.3
- Spring MVC / Bean Validation
- MyBatis 3
- H2
- Apache POI
- OpenAPI
- JUnit 5 / MockMvc
- GitHub Actions

## Client-facing value

이 프로젝트는 단순 홈페이지 제작보다 다음 외주 범위를 증명하기 위한 공개 사례입니다.

- 관리자페이지
- 고객/회원/거래처 관리
- 업무/신청/문의 상태 처리
- REST API와 DB
- 입력 검증과 오류 계약
- Excel 출력
- 비즈니스 상태 머신
- 경쟁조건을 고려한 변경 처리
- 테스트/CI 기반 납품

## Next production-oriented improvements

현재 공개 포트폴리오에서 과도하게 기능을 늘리기보다 다음 개선만 단계적으로 추가할 가치가 있습니다.

1. 인증 / 역할 기반 권한 (`ADMIN`, `STAFF`)
2. 고객 도메인 필드 확대(회사, 전화번호, 상태)
3. Excel bulk import + validation report
4. 업무 메모 / 변경 이력 audit log
5. Docker 또는 저비용 공개 demo 배포

## Portfolio policy

이 저장소는 공개 포트폴리오 전용 코드만 포함하며 실제 사업 진행 중인 비공개 프로젝트의 코드·구조·데이터를 사용하지 않습니다.
