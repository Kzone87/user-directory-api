# Business Ops Dashboard · V7

![CI](https://github.com/Kzone87/user-directory-api/actions/workflows/ci.yml/badge.svg)

중소기업의 고객·업무 운영을 웹 시스템으로 전환하는 상황을 가정한 **React + Spring Boot 풀스택 CRM/업무관리 공개 사례**입니다. V7은 V6의 Customer ↔ Work Order, RBAC, Audit, Priority/Due date 위에 **서버 집계 Analytics & Reporting 경계**를 추가합니다.

> 저장소 이름은 기존 Git 이력을 보존하기 위해 `user-directory-api`를 유지합니다. 모든 데이터는 공개 포트폴리오용 가상 데이터입니다.

## V7 핵심

```text
Customer
   │ customer_id FK
   ▼
Work Order
├─ status: RECEIVED / IN_PROGRESS / DONE / CANCELLED
├─ priority: LOW / NORMAL / HIGH / URGENT
├─ dueDate
└─ Activity History
          ↓
Server-side Analytics
├─ Customer KPI
├─ Open / Overdue / Done
├─ Status / Priority distribution
├─ Assignee workload
└─ 14-day completion trend
```

- 고객/거래처 검색·상태관리
- 고객과 업무의 FK 연결
- Work Order priority + optional due date
- 로그인 후 Planning Panel에서 기한 초과 / 오늘 마감 / 긴급 / 진행 중 KPI
- HIGH/URGENT 업무 focus list
- ADMIN/STAFF RBAC
- 상태 머신과 조건부 UPDATE 기반 동시성 방어
- 인증 actor가 기록되는 transactional audit history
- **서버 집계 `/api/analytics/operations`**
- 전체/활성 고객, 열린 업무, 기한 초과, 이번 달 완료 KPI
- 업무 상태/우선순위 distribution
- 담당자별 열린 업무 workload
- 최근 14일 완료 trend
- Frontend Analytics Panel + lightweight CSS bars
- 400/401/403/404/409 JSON 오류 계약

## 왜 Analytics를 서버에서 계산하는가

V6 Planning Panel은 화면에서 현재 work-order list를 바탕으로 즉시 필요한 KPI를 계산합니다. V7 Analytics는 범위를 넓혀 **DB 전체 집계 결과를 서버 API 계약으로 제공**합니다.

```text
Browser
  ↓ GET /api/analytics/operations
Spring Controller
  ↓
Analytics Service
  ↓
MyBatis aggregate SQL
  ↓
H2 demo database
```

이렇게 하면 데이터가 커져도 전체 레코드를 브라우저로 내려보낸 뒤 다시 집계할 필요가 없고, 동일 Analytics 계약을 다른 관리자 화면/리포트에서도 재사용할 수 있습니다.

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
| GET | `/api/analytics/operations` | 서버 집계 운영 Analytics |
| GET | `/api/auth/me` | 현재 principal / role |
| GET | `/api/users/export` | XLSX export |

## Analytics response

```json
{
  "totalCustomers": 5,
  "activeCustomers": 3,
  "openWorkOrders": 2,
  "overdueWorkOrders": 1,
  "doneThisMonth": 1,
  "statusDistribution": [],
  "priorityDistribution": [],
  "workloadByAssignee": [],
  "completedTrend": []
}
```

집계 endpoint도 `/api/**` 보안 경계 안에 있으므로 인증되지 않은 요청은 `401`입니다. ADMIN/STAFF는 운영 Analytics를 조회할 수 있습니다.

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

자동화 테스트는 Customer 검색/CRUD lifecycle, Work Order FK, priority/dueDate, 상태 전이, Audit Log, RBAC, Analytics aggregate/API 인증 경계, 사용자 API와 Excel export를 포함합니다.

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

이 프로젝트는 관리자페이지, 고객/거래처 CRM, 관계형 업무 데이터, 업무 우선순위·마감 관리, RBAC, Audit Log에 더해 **서버 집계 Analytics/KPI API와 관리자 reporting UI**까지 공개 코드로 증명합니다.

실제 외주 범위에서는 고객/매출/신청/작업 데이터에 맞춰 KPI, 기간 조건, 사용자별 집계, CSV/XLSX/PDF report 등을 별도 설계할 수 있습니다.

## Portfolio policy

이 저장소는 공개 포트폴리오 전용 코드와 가상 demo 데이터만 포함합니다. 비공개 사업 프로젝트의 코드·구조·데이터는 사용하지 않습니다.
