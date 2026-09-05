# Business Ops Dashboard · V1

![CI](https://github.com/Kzone87/user-directory-api/actions/workflows/ci.yml/badge.svg)

중소규모 업무시스템 외주를 가정해 만든 **React + Spring Boot 풀스택 관리 시스템 공개 사례**입니다. 기존 Spring/MyBatis 사용자 관리 API를 기반으로 React/TypeScript 관리 화면을 연결해 검색, 등록, 수정, 삭제, 입력 검증과 오류 피드백이 실제 HTTP API를 통해 동작합니다.

> 현재 저장소 이름은 기존 `user-directory-api`를 유지하고 있지만, 프로젝트 역할은 백엔드 단독 포트폴리오에서 `Business Ops Dashboard` 풀스택 사례로 확장 중입니다.

## Why this project exists

실제 외주에서 반복되는 요구는 단순한 화면 제작보다 다음과 같은 **운영 데이터 흐름**에 가깝습니다.

- 관리자 화면에서 데이터 조회/검색
- 신규 등록과 수정
- 중복/잘못된 입력 차단
- 삭제와 오류 처리
- Excel export
- API 계약과 자동화된 회귀 검증
- 이후 고객/업무상태/권한 모듈로 확장 가능한 구조

이 프로젝트는 이런 기능을 작은 범위부터 실제로 동작하게 만든 뒤 단계적으로 확장하는 공개 포트폴리오입니다.

## V1 implemented

### Frontend

- React 18 + TypeScript + Vite
- 운영 현황 metric 카드
- 사용자/연락처 검색
- 등록 / 수정 / 삭제 UI
- API 오류 메시지 표시
- responsive admin layout
- Vite `/api` development proxy

### Backend

- Spring Boot REST API
- MyBatis 기반 CRUD / 검색
- pagination / sort
- Bean Validation
- 중복 이메일 `409`, validation/query `400`, not found `404`
- Excel XLSX export
- OpenAPI / Swagger UI
- H2 실행용 DB
- MockMvc 통합 테스트

## Current full-stack flow

```text
React / TypeScript
        │
        │  HTTP JSON
        ▼
Spring Boot REST API
        │
        ▼
Service / Validation
        │
        ▼
MyBatis Mapper
        │
        ▼
H2 (demo) / MySQL-compatible SQL
```

개발 환경에서는 Vite가 `/api` 요청을 `http://localhost:8080`으로 프록시하므로 별도 CORS 설정 없이 프런트와 백엔드를 함께 실행할 수 있습니다.

## API

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/users` | 이름/이메일 keyword 검색 |
| GET | `/api/users/page` | 검색 + pagination + sort |
| GET | `/api/users/{id}` | 단건 조회 |
| POST | `/api/users` | 등록 |
| PUT | `/api/users/{id}` | 수정 |
| DELETE | `/api/users/{id}` | 삭제 |
| GET | `/api/users/export` | 검색 결과 XLSX 다운로드 |

## Run locally

### 1. Backend

Requirements: Java 17+, Maven 3.9+

```bash
mvn spring-boot:run
```

Backend: `http://localhost:8080`

Swagger UI: `http://localhost:8080/swagger-ui.html`

### 2. Frontend

Requirements: Node.js 22+

```bash
cd frontend
npm install
npm run dev
```

Frontend: `http://localhost:5173`

### Production API URL

프런트엔드를 별도 배포할 경우 빌드 환경에 API 주소를 지정할 수 있습니다.

```bash
VITE_API_BASE_URL=https://api.example.com npm run build
```

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

GitHub Actions에서는 백엔드 `mvn verify`와 프런트 TypeScript/Vite build를 **별도 job으로 모두 검증**합니다.

## Engineering decisions

- **기존 검증된 API를 재사용**: 새 UI 때문에 백엔드 안정성을 버리지 않고, 이미 테스트된 REST 계약 위에 프런트를 얹었습니다.
- **frontend/backend 분리**: 외주 환경에서 프런트/백엔드를 독립 배포하거나 하나의 배포 파이프라인으로 합칠 수 있습니다.
- **서버 검증 우선**: 브라우저 required/type 검증과 별개로 Spring Validation 및 DB unique 제약을 최종 기준으로 유지합니다.
- **예측 가능한 오류 계약**: 프런트가 HTTP 상태와 오류 메시지를 사용자 피드백으로 변환할 수 있도록 합니다.
- **CI 분리**: UI build 실패와 API test 실패를 독립적으로 확인할 수 있습니다.

## Roadmap

외주 대표작으로 완성하기 위해 다음 순서로 확장합니다.

1. **Customer module** — 거래처/고객 CRUD, 연락처, 상태
2. **Work-order module** — 접수 → 진행 → 완료/취소 상태 관리
3. **Authentication / RBAC** — ADMIN / STAFF 권한 분리
4. **Excel import** — 대량 고객 데이터 업로드 및 검증 결과
5. **File attachment** — 업무 건별 파일 첨부
6. **Deployment demo** — 공개 샘플 데이터 기반 Live Demo

## Tech stack

- React 18
- TypeScript 5
- Vite 5
- Java 17
- Spring Boot 3.3
- Spring MVC / Bean Validation
- MyBatis 3
- H2
- Apache POI
- OpenAPI
- JUnit 5 / MockMvc
- GitHub Actions

## Portfolio positioning

이 프로젝트가 증명하려는 범위는 단순 CRUD 예제가 아닙니다.

**관리자페이지 + 업무 데이터 + REST API + 검증 + Excel + 테스트/CI**를 하나의 외주형 업무시스템으로 설계하고 확장할 수 있다는 것을 단계별 결과물로 보여주는 것이 목적입니다.
