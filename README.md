# Business Ops Dashboard · V8

![CI](https://github.com/Kzone87/user-directory-api/actions/workflows/ci.yml/badge.svg)

중소기업의 고객·업무 운영을 웹 시스템으로 전환하는 상황을 가정한 **React + Spring Boot 풀스택 CRM/업무관리 공개 사례**입니다. V8은 V7의 Customer ↔ Work Order, RBAC, Audit, Priority/Due date, 서버 Analytics 위에 **Human Approval Workflow와 기간별 CSV/XLSX 운영 리포트**를 추가합니다.

> 저장소 이름은 기존 Git 이력을 보존하기 위해 `user-directory-api`를 유지합니다. 모든 데이터는 공개 포트폴리오용 가상 데이터입니다.

## V8 핵심

```text
Customer
   │ customer_id FK
   ▼
Work Order
├─ RECEIVED
├─ IN_PROGRESS
├─ WAITING_APPROVAL
├─ APPROVED
├─ DONE
└─ CANCELLED
       │
       ├─ Activity History
       ├─ Approval History
       └─ Operations Analytics / Report
```

### Approval Workflow

```text
STAFF / ADMIN
IN_PROGRESS
   ↓ approval request
WAITING_APPROVAL
   ↓ ADMIN
APPROVED ──→ DONE
   or
REJECTED ──→ IN_PROGRESS
```

- STAFF/ADMIN은 진행 중 업무의 승인 요청 가능
- 승인 결정은 ADMIN 전용
- 승인 요청, 승인, 반려를 Activity Log에 기록
- 별도 `work_order_approvals` 테이블에 요청자/결정자/메모/시간 이력 보존
- 승인 레코드 변경 + 상태 전이 + Activity Log를 동일 DB transaction으로 처리
- 조건부 UPDATE로 동시 변경 충돌 방어
- 승인된 업무만 일반 상태 API를 통해 DONE으로 전환 가능

### Operations / Reporting

- `GET /api/analytics/operations` 서버 집계 KPI
- 전체/활성 고객, 열린 업무, 기한 초과, 이번 달 완료
- 상태/우선순위 distribution
- 담당자별 열린 업무 workload
- 최근 14일 완료 trend
- ADMIN 전용 기간별 업무 CSV/XLSX report
- CSV/XLSX spreadsheet formula injection 방어
- React Approval & Reporting Panel

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
| PATCH | `/api/work-orders/{id}/status` | 일반 상태 전이 |
| GET | `/api/work-orders/{id}/activities` | 변경 이력 |
| GET | `/api/work-orders/{id}/approvals` | 승인 이력 |
| POST | `/api/work-orders/{id}/approval-request` | 승인 요청 |
| POST | `/api/work-orders/{id}/approval-decision` | ADMIN 승인/반려 |
| GET | `/api/analytics/operations` | 서버 집계 운영 Analytics |
| GET | `/api/reports/work-orders.csv` | ADMIN CSV report |
| GET | `/api/reports/work-orders.xlsx` | ADMIN XLSX report |
| GET | `/api/auth/me` | 현재 principal / role |
| GET | `/api/users/export` | 사용자 XLSX export |

## Why this matters

실제 관리자/업무시스템은 CRUD에서 끝나지 않습니다. 누가 업무를 완료했다고 주장했는지, 누가 최종 승인했는지, 반려 후 어떤 상태로 돌아갔는지, 운영 데이터를 기간별로 어떻게 전달하는지가 필요합니다.

V8은 다음 경계를 공개 코드로 보여줍니다.

```text
입력
↓
업무 진행
↓
사람의 승인
↓
상태 확정
↓
Audit
↓
Analytics / Report
```

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

자동화 테스트는 Customer lifecycle, Work Order FK/priority/dueDate, 상태 전이, Audit, Analytics, STAFF 승인요청, ADMIN 승인/반려, 403 RBAC, CSV formula injection 방어, XLSX 생성, 기간 validation을 포함합니다.

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

이 프로젝트는 다음 외주 범위를 하나의 공개 사례로 증명합니다.

- 관리자페이지 / 사내 업무시스템
- 고객/거래처 CRM
- 관계형 업무 DB
- 우선순위·마감 관리
- STAFF/ADMIN 역할 권한
- 승인/반려 Workflow
- 변경·승인 Audit Trail
- 서버 KPI / Dashboard
- CSV/XLSX 운영 Report
- REST API / OpenAPI
- 테스트 / CI

## Portfolio policy

이 저장소는 공개 포트폴리오 전용 코드와 가상 demo 데이터만 포함합니다. 비공개 사업 프로젝트의 코드·구조·데이터는 사용하지 않습니다.
