# Business Ops Dashboard · V5

![CI](https://github.com/Kzone87/user-directory-api/actions/workflows/ci.yml/badge.svg)

중소기업의 고객·업무 운영을 웹 시스템으로 전환하는 상황을 가정한 **React + Spring Boot 풀스택 CRM/업무관리 공개 사례**입니다. V5에서는 기존의 자유 입력 고객명을 제거하고 **Customer 도메인과 Work Order를 관계형 FK로 연결**했습니다.

> 저장소 이름은 기존 Git 이력을 보존하기 위해 `user-directory-api`를 유지합니다. 모든 데이터는 공개 포트폴리오용 가상 데이터입니다.

## V5 핵심 개선

```text
Customer
  ├─ company / contact / email / phone
  ├─ LEAD / ACTIVE / INACTIVE
  └─ memo
       │
       │ customer_id FK
       ▼
Work Order
  ├─ RECEIVED → IN_PROGRESS → DONE
  └─ Activity History
```

- 고객/거래처 검색·상태 필터·등록·수정
- 삭제 대신 `INACTIVE` 상태를 사용해 업무 이력이 있는 고객 보존
- Work Order가 `customer_id` FK를 참조
- 업무 생성 전 Customer 존재 여부 서버 검증
- Work Order 응답에 `customerId`와 JOIN된 `customerName` 제공
- 고객별 업무 화면 필터
- 업무 등록 시 등록된 고객 선택
- 기존 ADMIN/STAFF RBAC, 상태 머신, Audit Log 유지

## Frontend

- React 18 + TypeScript + Vite
- Customer dashboard metrics
- 고객 통합검색(회사/담당자/이메일/전화)
- 고객 상태 필터
- 고객 등록/수정 폼
- 고객별 업무 필터
- Work Order 고객 select
- 업무 상태 액션과 Activity Timeline
- ADMIN/STAFF 역할 표시
- responsive admin UI

## Backend

- Java 17 / Spring Boot 3.3
- Spring Security stateless HTTP Basic demo auth
- MyBatis
- Bean Validation
- H2 demo DB
- Customer CRUD-style API(create/read/update + status management)
- Customer ↔ Work Order FK
- Work Order state machine
- conditional status update로 stale/concurrent transition 방어
- work-order 변경과 audit insert를 같은 transaction에서 처리
- 400 / 401 / 403 / 404 / 409 JSON 오류 계약
- OpenAPI / Swagger
- Apache POI XLSX export
- MockMvc integration tests

## Architecture

```text
React / TypeScript
        │
        ▼
Spring Security
        │
        ▼
Controllers
├─ CustomerController
├─ WorkOrderController
└─ UserController
        │
        ▼
Services
├─ CustomerService
├─ WorkOrderService
│  ├─ customer existence validation
│  ├─ state-machine validation
│  ├─ conditional update
│  └─ audit log transaction
└─ UserService
        │
        ▼
MyBatis
        │
        ▼
H2
├─ customers
├─ work_orders (customer_id FK)
├─ work_order_activities
└─ users
```

## Main API

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/auth/me` | current principal / role |
| GET | `/api/customers` | keyword + status 고객 검색 |
| GET | `/api/customers/{id}` | 고객 단건 |
| POST | `/api/customers` | 고객 등록 |
| PUT | `/api/customers/{id}` | 고객 수정/상태 변경 |
| GET | `/api/work-orders` | 업무 목록 / status filter |
| GET | `/api/work-orders/{id}` | 업무 단건 |
| GET | `/api/work-orders/{id}/activities` | 변경 이력 |
| POST | `/api/work-orders` | customerId 기반 업무 접수 |
| PATCH | `/api/work-orders/{id}/status` | 상태 전이 + audit log |
| GET | `/api/users` | 디렉터리 검색 |
| GET | `/api/users/export` | XLSX export |

## Demo accounts

| Role | Username | Password |
| --- | --- | --- |
| ADMIN | `demo-admin` | `admin-demo` |
| STAFF | `demo-staff` | `staff-demo` |

인증 정보는 프런트 메모리에만 유지합니다.

## Run locally

Backend:

```bash
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`
- UI: `http://localhost:5173`

## Verification

```bash
mvn verify
```

```bash
cd frontend
npm install
npm run build
```

GitHub Actions는 backend와 frontend를 별도 job으로 검증합니다.

### Integration coverage

- Customer seed/list/status filter/keyword search
- Customer create/update/validation/404
- Work Order가 존재하는 customerId만 참조하는지
- Work Order 응답의 customerId/company join
- Work Order state transition / terminal state
- Audit actor/from/to history
- User CRUD/search/export
- anonymous 401 / STAFF vs ADMIN 403

## Client-facing value

V5는 단순 CRUD가 아니라 다음 외주 범위를 보여줍니다.

- 고객/거래처 CRM
- 관리자페이지
- 고객과 업무의 관계형 데이터 모델
- 업무 접수/처리 상태 머신
- 역할 기반 접근제어
- 변경 이력/감사 로그
- REST API + DB + React UI
- 입력검증/오류계약/동시성 방어
- 테스트/CI 기반 납품

## Next

V6에서는 업무 `priority`, `dueDate`와 overdue/today dashboard를 추가해 운영 우선순위 관리까지 확장합니다.

## Portfolio policy

이 저장소는 공개 포트폴리오 전용 코드와 가상 demo 데이터만 포함합니다. 비공개 사업 프로젝트의 코드·구조·데이터는 사용하지 않습니다.
