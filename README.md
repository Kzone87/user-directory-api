# User Directory

![CI](https://github.com/Kzone87/user-directory-api/actions/workflows/ci.yml/badge.svg)

Spring Boot + MyBatis 기반 사용자 관리 애플리케이션입니다. REST API, Swagger 문서, 브라우저 관리 UI, Excel export, H2 quick-start, MySQL runtime profile, Testcontainers 검증과 Docker 실행 구성을 한 저장소에서 제공합니다.

> 초기 Spring/MyBatis 학습 프로젝트의 사용자 조회·검색·Excel 출력 아이디어를 현대적인 Spring Boot 애플리케이션으로 다시 설계했습니다. 단순 CRUD 예제를 넘어서 입력 계약, 오류 계약, pagination/sort, 실제 MySQL 호환성, 운영 상태 확인과 UI 사용 흐름까지 검증하는 것이 목표입니다.

## Product surface

애플리케이션 실행 후 바로 사용할 수 있는 화면과 엔드포인트입니다.

- Management Console: `http://localhost:8080/`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`

브라우저 관리 화면에서는 다음 작업을 수행할 수 있습니다.

- 이름/이메일 keyword 검색
- email domain 필터
- pagination / sort / page size 변경
- 사용자 등록, 수정, 삭제
- 조건에 맞는 Excel export
- API/health 문서로 직접 이동
- API validation / conflict 오류를 필드와 상태 메시지로 표시

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
GET /api/users/page?page=0&size=20&sort=name&direction=asc&keyword=kim
```

허용 sort 값은 `id`, `name`, `email`, `createdAt`이며 `size`는 1~100 범위입니다. 정렬 컬럼은 사용자 입력을 SQL에 직접 삽입하지 않고 Service whitelist와 MyBatis `<choose>`로 제한합니다.

응답 예시:

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

오류 계약:

- 잘못된 request body → `400 VALIDATION_ERROR`
- 잘못된 pagination/sort query → `400 INVALID_QUERY`
- 존재하지 않는 사용자 → `404 USER_NOT_FOUND`
- 중복 이메일 → `409 DATA_CONFLICT`

## Architecture

```text
Browser Management Console
          │
          ├──────────────┐
          ▼              ▼
     REST API        Swagger / Health
          │
          ▼
UserController / UserExcelController
          │
          ▼
UserService / UserExcelService
          │
          ▼
      UserMapper
          │
          ▼
     MyBatis XML
          │
     ┌────┴────┐
     ▼         ▼
H2 quick start   MySQL runtime / Testcontainers
```

## Quick start — H2

Requirements: Java 17+, Maven 3.9+

```bash
mvn spring-boot:run
```

기본 프로필은 `jdbc:h2:mem:userdb`를 사용하며 `schema.sql`, `data.sql`로 초기화됩니다. 외부 DB 없이 관리 UI와 Swagger를 바로 확인할 수 있습니다.

## Full runtime — MySQL + Docker Compose

Docker가 있다면 애플리케이션과 MySQL을 함께 실행할 수 있습니다.

```bash
docker compose up --build
```

구성:

- `mysql:8.4`
- Spring Boot application container
- MySQL healthcheck 이후 애플리케이션 시작
- `/actuator/health` 애플리케이션 healthcheck
- `application-mysql.yml`에서 DB 연결을 환경변수로 분리

실서비스에서는 `DB_URL`, `DB_USER`, `DB_PASSWORD`를 배포 환경의 secret/configuration으로 주입해야 합니다. `compose.yml`의 값은 로컬 포트폴리오 실행용입니다.

## Test and verification

```bash
mvn verify
```

테스트 범위:

- Management Console 정적 화면 제공
- health endpoint
- 사용자 조회/검색
- 등록/수정/삭제
- Bean Validation
- 중복 이메일 `409`
- pagination / sort / metadata
- 잘못된 query `400`
- Excel XLSX export
- OpenAPI 문서 생성
- Testcontainers 기반 실제 MySQL query compatibility

GitHub Actions는 두 개의 품질 게이트를 수행합니다.

1. `mvn verify` — H2 + MockMvc + Testcontainers MySQL
2. `docker build` — 실행 이미지가 실제로 생성되는지 검증

## Tech stack

- Java 17
- Spring Boot 3.3.4
- Spring MVC / Bean Validation / Actuator
- MyBatis 3
- H2 + MySQL 8.4
- Testcontainers
- springdoc-openapi 2.6
- Apache POI
- Vanilla JavaScript management UI
- JUnit 5 / MockMvc
- Docker / Docker Compose
- GitHub Actions

## Engineering decisions

### Browser UI without a separate frontend build

이 프로젝트의 핵심은 backend 계약과 데이터 흐름이므로 별도 React 프로젝트를 추가하지 않았습니다. `src/main/resources/static`의 작은 관리 UI가 실제 REST API를 직접 호출하도록 구성해 API 동작을 눈으로 검증하면서도 backend 중심 프로젝트라는 성격을 유지합니다.

### H2 + real MySQL verification

H2는 검토자가 설치 없이 즉시 실행하기 위한 기본 경로입니다. 반면 SQL 호환성을 H2 compatibility mode에만 의존하지 않도록 Testcontainers에서 실제 MySQL 8.4를 실행해 pagination과 동적 정렬 query를 검증합니다.

### Safe sorting

`sort=name` 같은 입력을 `${sort}`로 SQL에 그대로 삽입하지 않습니다. Service에서 허용된 정렬 키만 받고 MyBatis `<choose>`가 실제 DB column을 선택합니다.

### Explicit error contract

HTTP status만 반환하지 않고 `code`, `message`, `timestamp`, `fieldErrors` 형식으로 클라이언트가 처리 가능한 오류 계약을 유지합니다. 관리 UI 역시 이 계약을 그대로 사용합니다.

## Portfolio focus

이 프로젝트가 보여주려는 것은 CRUD 기능 개수보다 다음의 연결입니다.

> Browser UI → REST contract → validation/error handling → service boundary → MyBatis SQL → H2/MySQL → Excel → OpenAPI → automated verification → container runtime
