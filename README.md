# Business Ops Dashboard · V6

![CI](https://github.com/Kzone87/user-directory-api/actions/workflows/ci.yml/badge.svg)

중소기업의 고객·업무 운영을 웹 시스템으로 전환하는 상황을 가정한 **React + Spring Boot 풀스택 CRM/업무관리 공개 사례**입니다. V6는 V5의 Customer ↔ Work Order 관계형 설계 위에 **업무 우선순위와 마감일 기반 운영계획**을 추가합니다.

> 저장소 이름은 기존 Git 이력을 보존하기 위해 `user-directory-api`를 유지합니다. 모든 데이터는 공개 포트폴리오용 가상 데이터입니다.

## V6 핵심

```text
Customer
   │ customer_id FK
   ▼
Work Order
├─ status: RECEIVED / IN_PROGRESS / DONE / CANCELLED
├─ priority: LOW / NORMAL / HIGH / URGENT
├─ dueDate
└─ Activity History
```

- 고객/거래처 검색·상태관리
- 고객과 업무의 FK 연결
- Work Order priority + optional due date
- 목록 기본 정렬: 긴급도 → 마감일 → 최근 변경
- 로그인 후 Planning Panel에서 기한 초과 / 오늘 마감 / 긴급 / 진행 중 KPI
- HIGH/URGENT 업무 focus list
- ADMIN/STAFF RBAC
- 상태 머신과 조건부 UPDATE 기반 동시성 방어
- 인증 actor가 기록되는 transactional audit history
- 400/401/403/404/409 JSON 오류 계약

## Stack

- React 18 / TypeScript / Vite
- Java 17 / Spring Boot 3.3
- Spring Security
- MyBatis / H2
- Bean Validation
- Apache POI
- OpenAPI / Swagger
- JUnit 5 / MockMvc
- GitHub Actions

## Main API

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/customers` | 고객 통합검색 + 상태 필터 |
| POST | `/api/customers` | 고객 등록 |
| PUT | `/api/customers/{id}` | 고객 수정/상태 변경 |
| GET | `/api/work-orders` | 업무 목록 |
| POST | `/api/work-orders` | 고객/priority/dueDate 기반 업무 접수 |
| PATCH | `/api/work-orders/{id}/status` | 상태 전이 |
| GET | `/api/work-orders/{id}/activities` | 변경 이력 |
| GET | `/api/auth/me` | 현재 principal / role |
| GET | `/api/users/export` | XLSX export |

## Verification

```bash
mvn verify
```

```bash
cd frontend
npm install
npm run build
```

GitHub Actions에서 backend와 frontend를 별도 job으로 검증합니다.

자동화 테스트는 Customer 검색/CRUD-style lifecycle, Work Order FK, priority/dueDate 저장, 상태 전이, Audit Log, RBAC, 사용자 API와 Excel export를 포함합니다.

## Run locally

```bash
mvn spring-boot:run
```

```bash
cd frontend
npm install
npm run dev
```

- API `http://localhost:8080`
- Swagger `http://localhost:8080/swagger-ui.html`
- UI `http://localhost:5173`

## Demo accounts

- ADMIN: `demo-admin` / `admin-demo`
- STAFF: `demo-staff` / `staff-demo`

## Client-facing value

이 프로젝트는 관리자페이지, 고객/거래처 CRM, 관계형 업무 데이터, 업무 우선순위·마감 관리, RBAC, Audit Log, REST API/DB, Excel output, 테스트/CI 기반 납품 역량을 공개 코드로 증명합니다.

## Portfolio policy

이 저장소는 공개 포트폴리오 전용 코드와 가상 demo 데이터만 포함합니다. 비공개 사업 프로젝트의 코드·구조·데이터는 사용하지 않습니다.
