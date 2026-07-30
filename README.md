# How to start the project as a developer?

If you inspect `main/resources/application.properties` you can see that there are some variables used in the application. They are taken from environmental variables, but some of them have default values. Those that don't have default values have to be set by a developer. These are:
- `SPRING_FLYWAY_USER` - database username for flyway (most likely sa in develobpemnt environment)
- `SPRING_FLYWAY_PASSWORD` - database password for flyway
- `SPRING_DATASOURCE_PASSWORD` - database password
- `SPRING_DATASOURCE_USER` - database username for flyway (most likely sa in develobpemnt environment)
- `SPRING_DATASOURCE_URL` - database url (a connection string to your database)
- `ADMIN_USER_PASSWORD` - password for admin user that will be created on first application startup
- `ALLOWED_ORIGINS` - a comma-separated list of allowed origins for CORS (e.g. `https://*.example.com,http://localhost:*`). If not set, the application will allow all origins by default (`*`),
- `ENABLE_SWAGGER` - swagger is disabled by default. Set as true to access swagger documentation.
- `JWT_KEY` - key for jwt tokens generation
- `JWT_EXPIRATION` - days of jwt token lifetime

Optional (defaults shown in `application.properties`):
- `SPRING_DATA_MONGODB_URI` - MongoDB connection string. Default `mongodb://localhost:27017/survey`.
- `SPRING_DATA_MONGODB_DATABASE` - MongoDB logical database name. Default `survey`.

The best idea is to configure your IDE, so that it sets those variables always, when you run the application. 

## IntelliJ Idea instruction

To configure your IntelliJ Idea to the following:
- On the top mane open `Run` context menu
- Go to `Debug` -> `Edit configurations` -> `Edit environmental variables`
- Add proper variables with values and save changes

# Data stores

Two data stores back this service:

- **MS SQL Server (primary)** — every transactional entity (identities,
  surveys, questions, options, participations, question answers,
  sensor/localization data, research area, phone numbers). Managed by
  Flyway; migrations live in `src/main/resources/db/migration/`.
- **MongoDB (secondary, response documents)** — every submitted survey
  response is mirrored to the `surveyResponseDocuments` collection as a
  denormalized JSON document, written via a Spring
  `@TransactionalEventListener(AFTER_COMMIT)`, so a Mongo failure can
  never roll back the primary SQL write. Used by the admin
  "Response documents" tab.

Locally both containers are managed by `../scripts/dev-up.ps1` (see
`../scripts/README.md`). For a bare-hands run:

```bash
docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=Str0ng!Passw0rd" \
  -p 1433:1433 --name geosenesm-mssql -d mcr.microsoft.com/mssql/server:2022-latest

docker run -p 27017:27017 --name geosenesm-mongo -d mongo:7.0
```

# Response documents API (ADMIN-only)

Endpoints exposed by `SurveyResponsesController` on top of the Mongo
collection:

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/surveyresponses/documents` | Paginated list. Filters (all optional): `surveyId`, `respondentId`, `dateFrom`, `dateTo` (ISO-8601 UTC, seconds precision). Pagination: `page` (default 0), `size` (default 20, capped 200). |
| `GET` | `/api/surveyresponses/documents/{participationId}/download` | Single document as JSON with `Content-Disposition: attachment; filename="survey-response-<id>.json"`. |
| `GET` | `/api/surveyresponses/documents/export` | Streams a ZIP archive of every document matching the same filters. One JSON entry per response, filename `survey-response-<participationId>.json`. |

The document schema (only the fields relevant to each answer are
persisted — null / empty properties are omitted):

```json
{
  "participationId": "<uuid>", "surveyId": "<uuid>", "surveyName": "…",
  "respondentId":    "<uuid>", "respondentUsername": "…",
  "participationDate": "2026-07-12T17:27:55Z",
  "surveyStartDate":   "2026-07-12T17:26:55Z",
  "surveyFinishDate":  "2026-07-12T17:27:55Z",
  "answers": [
    { "questionId": "…", "questionContent": "How do you feel today?",
      "questionType": "single_choice",
      "selectedOptions": [{ "optionId": "…", "label": "Great" }] },
    { "questionId": "…", "questionContent": "Comfort?",
      "questionType": "linear_scale", "numericAnswer": 3 }
  ],
  "sensorData": { "dateTime": "…", "temperature": 21.5, "humidity": 45.0 },
  "persistedAt": "2026-07-12T17:27:55Z"
}
```

# Documentation

- Remember to set `ENABLE_SWAGGER` environmental variable as true.
- Run this API
- Go to http://[host]:[port]/swagger-ui.html

# Build docker image

You are able to build a docker image with this application simply by runnning 

```bash
docker build -t your_image_name:your_tag .
```

The interenal docker image port, the applicatoin is listening on, is `8080`.