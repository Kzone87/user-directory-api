# User Directory API

![CI](https://github.com/Kzone87/user-directory-api/actions/workflows/ci.yml/badge.svg)

Spring Boot와 MyBatis로 구현한 **업무용 사용자/거래처 관리 백엔드 레퍼런스**입니다. 검색, CRUD, pagination/sort, Excel export, validation, 일관된 오류 응답과 OpenAPI 문서를 제공합니다.

## Business use case

관리자 페이지나 내부 업무시스템에는 단순 CRUD보다 다음 요구가 반복적으로 발생합니다.

- 이름/이메일 등 조건 검색
- 대량 데이터의 pagination과 정렬
- 중복 및 잘못된 입력 검증
- 검색 결과 Excel 다운로드
- 프런트엔드가 예측할 수 있는 오류 응답
- API 문서와 자동화된 회귀 검증

이 프로젝트는 그런 **관리형 데이터 API의 기본 구조**를 실행 가능한 형태로 정리한 공개 사례입니다.

### 외주 프로젝트로 확장할 수 있는 유형

- 고객 / 거래처 / 회원 관리 API
- 주문 / 신청 / 문의 데이터 관리 백엔드
- 관리자 검색·필터·정렬·페이지네이션 API
- Excel export가 필요한 내부 시스템
- 기존 프런트엔드와 연결할 REST API

## API

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/users` | 이름/이메일 keyword 및 emailDomain 검색 |
| GET | `/api/users/page` | 검색 + pagination + sort + page metadata |
| GET | `/api/users/{id}` | 사용자 단건 조회 |
| POST | `/api/users` | 사용자 생성 |
| PUT | `/api/users/{id}` | 사용자 수정 |
| DELETE | `/api/users/{id}` | 사용자 삭제 |
| GET | `/api/users/export` | 현재 검색 조건의 결과를 XLSX로 다운로드 |

### Paginated search

```http
GET /api/users/page?page=0&size=20&sort=name&direction=asc
```

허용 sort 값은 `id`, `name`, `email`, `createdAt`이며 `size`는 1~100 범위로 제한합니다. 잘못된 query는 `400 INVALID_QUERY`로 정규화합니다.

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "sort": "name",
  "direction": "asc"
}
```

### Create

```json
{
  "name": "Kim Developer",
  "email": "kim@example.com"
}
```

## Architecture

```text
HTTP
  ↓
UserController / UserExcelController
  ↓
UserService / UserExcelService
  ↓
UserMapper
  ↓
MyBatis XML
  ↓
H2 (MySQL compatibility mode)
```

### Design decisions

- **Controller → Service → Mapper 분리**: HTTP 처리, 비즈니스 규칙, SQL 책임을 구분합니다.
- **Request/Response DTO 분리**: 외부 API 계약과 DB 모델을 직접 결합하지 않습니다.
- **Bean Validation**: 잘못된 입력은 DB까지 도달하기 전에 차단합니다.
- **Error contract**: 조회 실패 `404`, 이메일 중복 `409`, validation/query 오류 `400`으로 정규화합니다.
- **Safe sorting**: 요청 문자열을 SQL에 그대로 넣지 않고 whitelist와 MyBatis `<choose>`를 사용합니다.
- **Excel service 분리**: HTTP 응답 처리와 XLSX 생성 책임을 나눕니다.

## OpenAPI / Swagger UI

애플리케이션 실행 후 다음 경로에서 API 계약을 확인할 수 있습니다.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Test and verification

```bash
mvn verify
```

MockMvc 통합 테스트에서 다음 흐름을 검증합니다.

- 사용자 조회 및 검색
- 생성 및 Bean Validation
- pagination / sort / metadata
- 잘못된 pagination query의 오류 계약
- Excel XLSX export
- OpenAPI 문서 생성

동일한 `mvn verify`를 GitHub Actions에서도 수행합니다.

## Run locally

Requirements: Java 17+, Maven 3.9+

```bash
mvn spring-boot:run
```

기본 DB는 `jdbc:h2:mem:userdb`이며 실행 시 `schema.sql`, `data.sql`로 초기화됩니다.

## Tech stack

- Java 17
- Spring Boot 3.3.4
- Spring MVC / Bean Validation
- MyBatis 3
- H2 (MySQL compatibility mode)
- springdoc-openapi 2.6
- Apache POI
- JUnit 5 / MockMvc
- GitHub Actions

## Project background

초기 Spring/MyBatis 학습 프로젝트에서 출발했지만, 기존 코드를 그대로 포트폴리오에 남기지 않고 현재 기준으로 다시 설계했습니다.

기존의 사용자 조회·검색·Excel 출력 아이디어만 유지하고 Spring Boot 자동 설정, 계층 분리, DTO, validation, 오류 계약, pagination, OpenAPI, 테스트와 CI를 추가해 **실행·검증 가능한 백엔드 레퍼런스**로 재구성했습니다.

## Production considerations

공개 데모의 실행 편의를 위해 기본 DB는 H2를 사용합니다. 실제 외주/운영 환경에서는 프로젝트 요구에 따라 PostgreSQL/MySQL 등으로 datasource를 분리하고 다음 항목을 추가 검토합니다.

- 인증 및 역할 기반 권한
- 실제 운영 DB migration
- transaction / optimistic locking
- Testcontainers 기반 DB 통합 테스트
- API versioning
- observability / structured logging
- 배포 환경별 configuration과 secret 관리
