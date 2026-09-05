# Business Ops Dashboard · V4

![CI](https://github.com/Kzone87/user-directory-api/actions/workflows/ci.yml/badge.svg)

중소기업의 반복 업무를 웹 시스템으로 전환하는 외주 상황을 가정해 만든 **React + Spring Boot 풀스택 운영관리 공개 사례**입니다. 연락처 관리, 업무 상태 머신, ADMIN/STAFF 권한 제어에 더해 V4에서는 **업무 변경 감사 로그(Activity History)** 를 추가했습니다.

> 저장소 이름은 기존 Git 이력을 보존하기 위해 `user-directory-api`를 유지하지만, 현재 프로젝트 역할은 백엔드 단독 예제가 아니라 `Business Ops Dashboard` 풀스택 사례입니다.

## Business use case

실제 관리자페이지/업무관리 외주에서 반복되는 요구를 하나의 작은 운영 시스템으로 구현합니다.

- 고객/연락처 조회·검색·등록·수정·삭제
- Excel XLSX export
- 신규 업무 접수와 담당자 지정
- 업무 상태 필터
- `RECEIVED → IN_PROGRESS → DONE/CANCELLED` 상태 머신
- stale/concurrent 상태 변경 방어
- ADMIN / STAFF 역할 기반 권한 제어
- JSON 401 / 403 오류 계약
- 업무 생성·상태 변경 시 인증 사용자 기록
- 업무별 Activity History timeline
- API 문서와 자동 회귀 검증

## V4 implemented

### Frontend

- React 18 + TypeScript + Vite
- demo login / current principal 표시
- ADMIN / STAFF 권한 차이에 맞춘 UI
- 운영 metric dashboard
- 사용자/연락처 CRUD
- 업무 접수 / 상태 필터 / 상태별 action
- 업무별 `이력` 조회
- actor, action, 상태 전이, 시각을 보여주는 activity timeline
- 상태 변경 후 선택된 업무 이력 자동 refresh
- responsive admin layout

### Backend

- Spring Boot REST API
- Spring Security stateless HTTP Basic demo auth
- ADMIN / STAFF RBAC
- MyBatis 기반 사용자 CRUD / 검색 / pagination / sort
- Work Order state machine
- 조건부 `UPDATE ... WHERE status = expectedStatus`를 이용한 동시 변경 방어
- `work_order_activities` 감사 로그 테이블
- 업무 변경과 activity insert를 같은 transaction에서 처리
- 인증 principal 이름을 actor로 기록
- Bean Validation
- 400 / 401 / 403 / 404 / 409 오류 계약
- Excel XLSX export
- OpenAPI / Swagger UI
- H2 demo DB
- MockMvc integration tests

## Architecture

```text
React / TypeScript
        │
        │ HTTP JSON + Basic Auth
        ▼
Spring Security
        │
        ▼
Spring Boot Controllers
        │
        ▼
Services
├─ User CRUD / validation
└─ Work-order transaction
   ├─ state-machine validation
   ├─ conditional status update
   └─ audit activity insert
        │
        ▼
MyBatis
        │
        ▼
H2 demo DB
├─ users
├─ work_orders
└─ work_order_activities
```

## Work-order state machine

```text
RECEIVED
   ├──> IN_PROGRESS ───> DONE
   │          └────────> CANCELLED
   └──────────────────> CANCELLED
```

`DONE`, `CANCELLED`은 종료 상태입니다. 서버가 전이 규칙을 최종 검증하므로 프런트에서 임의 요청을 보내더라도 허용되지 않은 전이는 `409 INVALID_WORK_ORDER_TRANSITION`으로 거부합니다.

상태 변경 SQL에는 현재 상태를 다시 조건으로 포함합니다.

```text
UPDATE work_orders
SET status = next
WHERE id = ?
  AND status = expected
```

변경 건수가 0이면 오래된 화면 또는 경쟁 요청으로 판단해 충돌 처리합니다. 정상 변경이 성공하면 같은 transaction 안에서 actor와 상태 전이를 `work_order_activities`에 기록합니다.

## Audit log model

기록 예시:

```text
actor        action           from          to
------------------------------------------------------
demo-admin   CREATED          -             RECEIVED
demo-staff   STATUS_CHANGED   RECEIVED      IN_PROGRESS
demo-admin   STATUS_CHANGED   IN_PROGRESS   DONE
```

각 로그에는 다음 정보가 저장됩니다.

- work order ID
- 인증된 actor
- action (`CREATED`, `STATUS_CHANGED`)
- 이전 상태 / 다음 상태
- 사람이 읽을 수 있는 detail
- created timestamp

업무 변경과 로그 기록이 하나의 DB transaction으로 묶여 있어, 상태만 바뀌고 이력이 누락되는 부분 성공을 방지합니다.

## Main API

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/auth/me` | 현재 principal / role |
| GET | `/api/users` | 이름/이메일 검색 |
| GET | `/api/users/page` | pagination + sort |
| POST | `/api/users` | 연락처 등록 |
| PUT | `/api/users/{id}` | 연락처 수정 |
| DELETE | `/api/users/{id}` | 연락처 삭제 · ADMIN only |
| GET | `/api/users/export` | XLSX export |
| GET | `/api/work-orders` | 업무 목록 / status filter |
| GET | `/api/work-orders/{id}` | 업무 단건 |
| GET | `/api/work-orders/{id}/activities` | 업무 감사 로그 |
| POST | `/api/work-orders` | 신규 업무 접수 + CREATED log |
| PATCH | `/api/work-orders/{id}/status` | 상태 전이 + STATUS_CHANGED log |

## Demo accounts

공개 포트폴리오 데모 전용 계정입니다.

| Role | Username | Password | Main difference |
| --- | --- | --- | --- |
| ADMIN | `demo-admin` | `admin-demo` | 전체 기능 + 연락처 삭제 |
| STAFF | `demo-staff` | `staff-demo` | 조회/등록/수정/업무 처리 |

인증 정보는 프런트 메모리에만 두며 브라우저 저장소에 영구 저장하지 않습니다.

## Run locally

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

Vite 개발 서버는 `/api` 요청을 Spring Boot로 proxy합니다.

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

GitHub Actions에서 backend와 frontend를 독립 job으로 검증합니다.

### Integration coverage

- 사용자 검색/생성/validation/pagination/export
- OpenAPI 계약
- 익명 요청 401
- STAFF / ADMIN 권한 차이와 403
- `/api/auth/me`
- 업무 seed/filter
- 업무 생성 시 `RECEIVED`
- 정상 상태 전이
- 단계 건너뛰기 거부
- terminal 상태 재전이 거부
- seed activity history 조회
- 상태 변경 actor / from / to 감사 로그 기록

## Tech stack

- React 18
- TypeScript
- Vite
- Java 17
- Spring Boot 3.3
- Spring MVC / Bean Validation
- Spring Security
- MyBatis 3
- H2
- Apache POI
- OpenAPI
- JUnit 5 / MockMvc / Spring Security Test
- GitHub Actions

## Client-facing value

이 프로젝트는 다음 외주 범위를 공개 코드로 증명하기 위한 사례입니다.

- 관리자페이지 / 업무관리 웹앱
- 고객/회원/거래처 관리
- 로그인 및 역할별 권한
- 신청/문의/작업 상태 처리
- REST API + DB
- Excel 출력
- 비즈니스 상태 머신
- 경쟁조건을 고려한 변경 처리
- 변경 이력 / 감사 로그
- 테스트/CI 기반 납품

## Next production-oriented improvements

V4 이후에는 무작정 기능을 늘리기보다 외주 증명력이 큰 항목만 단계적으로 추가할 계획입니다.

1. 고객 도메인 필드 확장(회사, 전화번호, 상태)
2. Excel bulk import + validation report
3. 업무 메모 / 첨부파일
4. Docker 또는 저비용 공개 demo 배포
5. production auth 전략(JWT/session/OAuth) 분리

## Portfolio policy

이 저장소는 공개 포트폴리오 전용 코드와 가상 demo 데이터만 포함합니다. 실제 사업 진행 중인 비공개 프로젝트의 코드·구조·데이터는 사용하지 않습니다.
