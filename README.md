# User Directory API

Spring Boot와 MyBatis를 사용해 사용자 검색, CRUD, Excel export를 제공하는 포트폴리오용 REST API입니다.

> 초기 Spring/MyBatis 학습 프로젝트에서 출발했지만, 당시 구현했던 사용자 조회·검색·Excel 출력 아이디어를 현대적인 Spring Boot 애플리케이션으로 다시 설계해 포트폴리오용 API로 재구성했습니다.

## What changed

초기 버전은 Spring XML 설정, 혼재된 패키지명, IDE 산출물, 불완전한 Mapper namespace와 샘플 계정 설정이 섞인 학습 코드였습니다. 현재 버전은 다음 원칙으로 다시 설계했습니다.

- Spring Boot 기반 자동 설정
- Controller → Service → Mapper 계층 분리
- Request/Response DTO 분리
- Bean Validation
- 전역 예외 처리
- MyBatis XML 동적 검색
- H2 인메모리 DB로 즉시 실행 가능
- Apache POI Excel export
- MockMvc 통합 테스트
- GitHub Actions CI

## API

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/users` | 이름/이메일 keyword 및 emailDomain 검색 |
| GET | `/api/users/{id}` | 사용자 단건 조회 |
| POST | `/api/users` | 사용자 생성 |
| PUT | `/api/users/{id}` | 사용자 수정 |
| DELETE | `/api/users/{id}` | 사용자 삭제 |
| GET | `/api/users/export` | 현재 검색 조건의 결과를 XLSX로 다운로드 |

### Search example

```http
GET /api/users?keyword=kim&emailDomain=example.com
```

### Create example

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
H2
```

API 입력 모델과 DB 도메인을 분리하고, 조회되지 않는 사용자는 `404`, 이메일 중복은 `409`, validation 오류는 `400`으로 정규화합니다.

## Run locally

Requirements: Java 17+, Maven 3.9+

```bash
mvn spring-boot:run
```

기본 DB는 `jdbc:h2:mem:userdb`이며 실행 시 `schema.sql`, `data.sql`로 초기화됩니다.

## Test

```bash
mvn test
```

MockMvc 통합 테스트에서 조회, 생성, validation, 검색, Excel export를 검증합니다. 동일 명령을 GitHub Actions에서도 실행합니다.

## Tech stack

- Java 17
- Spring Boot 3.3
- Spring MVC / Bean Validation
- MyBatis 3
- H2
- Apache POI
- JUnit 5 / MockMvc
- GitHub Actions

## Engineering decisions

### Why MyBatis

기존 학습 프로젝트에서 SQL Mapper를 직접 다룬 경험을 유지하면서, SQL과 Java 계층의 책임을 명확히 보여주기 위해 MyBatis를 사용했습니다.

### Why H2

포트폴리오 검토자가 외부 DB를 설치하지 않고 바로 실행할 수 있도록 기본 프로필은 H2를 사용합니다. 실제 서비스라면 datasource 설정을 환경변수 기반 PostgreSQL/MySQL로 분리할 수 있습니다.

### Why Excel export is a separate service

HTTP 응답 처리와 문서 생성 책임을 분리해 Controller가 비대해지지 않도록 했습니다. Excel 생성 로직은 독립적으로 테스트하거나 다른 전달 채널에서 재사용할 수 있습니다.

## Future improvements

- pagination / sort
- OpenAPI 문서
- Testcontainers 기반 실제 DB 통합 테스트
- optimistic locking
- 인증/권한이 필요한 별도 서비스와 연동
