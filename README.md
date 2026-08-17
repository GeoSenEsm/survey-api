# survey-api

REST backend for the GeoSenEsm platform. Serves the Angular admin panel and
the Flutter respondent app over a shared JWT-authenticated `/api/**`
contract.


|                 |                                                                                                                                         |
| --------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| Stack           | Java 17, Spring Boot 3.2.3, Spring Security + JWT, Spring Data JPA, Flyway, Spring Data MongoDB, Springdoc/Swagger, Lombok, ModelMapper |
| Default port    | `8080`                                                                                                                                  |
| Primary store   | MS SQL Server                                                                                                                           |
| Secondary store | MongoDB (`surveyResponseDocuments`)                                                                                                     |
| Sibling clients | `survey-admin-panel`, `mobile-app`                                                                                                      |


---

## Repository contents


| Path                                        | Purpose                                                              |
| ------------------------------------------- | -------------------------------------------------------------------- |
| `src/main/java/com/survey/api/`             | Controllers, security, configuration, validation, exception handlers |
| `src/main/java/com/survey/application/`     | Application services, DTOs, event listeners                          |
| `src/main/java/com/survey/domain/`          | JPA entities, repositories, enums                                    |
| `src/main/java/com/survey/infrastructure/`  | Mongo documents, Mongo repositories, Mongo config                    |
| `src/main/resources/db/migration/`          | Flyway migrations (`V*.sql` + paired `U*.sql` revert scripts)        |
| `src/main/resources/application.properties` | Defaults and env-var bindings                                        |
| `src/test/java/`                            | Unit / integration tests (Testcontainers SQL Server)                 |
| `Dockerfile`                                | Production image (listens on `8080`)                                 |
| `pom.xml` / `mvnw`                          | Maven build                                                          |




### Package layout

```
com.survey/
├── api/             HTTP boundary — controllers, security, validation
├── application/     Use cases, DTOs, transactional services, domain events
├── domain/          JPA entities + Spring Data JPA repositories
└── infrastructure/  MongoDB documents + repositories (response documents)
```

Dependencies flow inward (`api` → `application` → `domain`). Controllers
return DTOs only — never JPA entities. Mongo writes happen after the SQL
transaction commits via `@TransactionalEventListener(AFTER_COMMIT)`.

Sensor readings use a dynamic parameter/value model. Global configuration is
served from `/api/surveysettings/sensordata`; mobile respondents read their
filtered setup from `/api/surveysettings/sensordata/mobile`. A survey
submission carries a list of sensor readings — one `sensor_data` row plus its
`sensor_data_parameter_value` rows per connected sensor type, since a
respondent can have more than one sensor assigned at once — mirrored into
response documents as a `sensorData` array of `{source, values}` entries.

---



## Local development



### Required environment variables

Set these in your IDE run configuration (or shell). Values without defaults
must be provided:


| Variable                     | Purpose                                                                                                       |
| ---------------------------- | ------------------------------------------------------------------------------------------------------------- |
| `SPRING_DATASOURCE_URL`      | JDBC URL, e.g. `jdbc:sqlserver://localhost:1433;databaseName=survey;encrypt=true;trustServerCertificate=true` |
| `SPRING_DATASOURCE_USER`     | DB user (typically `sa` in development)                                                                       |
| `SPRING_DATASOURCE_PASSWORD` | DB password                                                                                                   |
| `SPRING_FLYWAY_USER`         | Flyway user (usually same as datasource user)                                                                 |
| `SPRING_FLYWAY_PASSWORD`     | Flyway password                                                                                               |
| `ADMIN_USER_PASSWORD`        | Password for the admin account created on first startup                                                       |
| `JWT_KEY`                    | HMAC signing key for JWTs                                                                                     |
| `JWT_EXPIRATION`             | Token lifetime in days                                                                                        |
| `ALLOWED_ORIGINS`            | Comma-separated CORS origins (e.g. `https://*.example.com,http://localhost:*`). Defaults to `*`               |
| `ENABLE_SWAGGER`             | Set `true` to enable `/swagger-ui.html` (disabled by default)                                                 |


Optional (defaults in `application.properties`):


| Variable                       | Default                            |
| ------------------------------ | ---------------------------------- |
| `SPRING_DATA_MONGODB_URI`      | `mongodb://localhost:27017/survey` |
| `SPRING_DATA_MONGODB_DATABASE` | `survey`                           |


Local MongoDB is started **without authentication** for development
convenience. Do not expose port `27017` on a shared or public network
without credentials in the URI.

### IntelliJ IDEA

1. **Run → Edit Configurations…**
2. Select the Spring Boot run configuration.
3. Open **Environment variables** and add the required keys above.
4. Apply and run / debug.

### Manual database containers

```bash
docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=Str0ng!Passw0rd" \
  -p 1433:1433 --name geosenesm-mssql -d mcr.microsoft.com/mssql/server:2022-latest

docker run -p 27017:27017 --name geosenesm-mongo -d mongo:7.0
```



### Run the API

```bash
./mvnw.cmd spring-boot:run    # Windows
./mvnw spring-boot:run        # macOS / Linux
```

- Health: `http://localhost:8080/actuator/health`
- Swagger (when enabled): `http://localhost:8080/swagger-ui.html`

---



## Data stores


| Store             | Role                                                                                                                                                                                                                   |
| ----------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **MS SQL Server** | Source of truth for identities, surveys, questions, options, participations, answers, sensor/localization data, research area, phone numbers, per-survey phone notification rules (`survey_notification`), and study-wide `survey_settings`. Schema managed by Flyway under `src/main/resources/db/migration/`.       |
| **MongoDB**       | Denormalized snapshot of each submitted survey response (`surveyResponseDocuments`). Written after SQL commit so a Mongo failure never rolls back the primary write. Consumed by the admin **Response Documents** tab. Includes `localDate`/`localTime` (respondent wall clock) alongside UTC `participationDate`. |
| **Timezones**     | Each respondent has an IANA `time_zone` (default `UTC`). Mobile sends it on login; the API recalculates `survey_participation.local_date`/`local_time` and Mongo local fields. Study time slots are wall-clock schedules interpreted in that timezone. Result filters and daily completion use local columns. |


---

## Response documents 

Document shape (null / empty answer fields are omitted):

```json
{
  "participationId": "<uuid>",
  "surveyId": "<uuid>",
  "surveyName": "…",
  "respondentId": "<uuid>",
  "respondentUsername": "…",
  "participationDate": "2026-07-12T17:27:55Z",
  "surveyStartDate": "2026-07-12T17:26:55Z",
  "surveyFinishDate": "2026-07-12T17:27:55Z",
  "answers": [
    {
      "questionId": "…",
      "questionContent": "How do you feel today?",
      "questionType": "single_choice",
      "selectedOptions": [{ "optionId": "…", "label": "Great" }]
    },
    {
      "questionId": "…",
      "questionContent": "Comfort?",
      "questionType": "linear_scale",
      "numericAnswer": 3
    }
  ],
  "sensorData": [
    {
      "dateTime": "2026-07-12T17:27:00Z",
      "source": "xiaomi",
      "values": [
        { "parameterCode": "temperature", "value": "21.5" },
        { "parameterCode": "humidity", "value": "45.0" }
      ]
    }
  ],
  "persistedAt": "2026-07-12T17:27:55Z"
}
```

---

## API documentation (Swagger)

1. Set `ENABLE_SWAGGER=true`.
2. Start the API.
3. Open `http://<host>:<port>/swagger-ui.html`.

---

## Docker image

```bash
docker build -t survey-api:<tag> .
docker run -p 8080:8080 --env-file .env survey-api:<tag>
```

The process listens on port `8080` inside the container.