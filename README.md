# Quiz Management System

[![CI](https://github.com/kelvinmoyo/cqs/actions/workflows/ci.yml/badge.svg)](https://github.com/kelvinmoyo/cqs/actions/workflows/ci.yml)

A course quiz application with a Java 17 Spring Boot REST API, SQLite storage, and a static web frontend. It provides question administration, randomized student quizzes, automatic evaluation, and saved results.

## Objectives

- Manage multiple-choice questions through an administrative API.
- Let students take a quiz and receive immediate per-question feedback.
- Save and review quiz results.
- Provide a responsive frontend that remains usable while a free-tier server wakes.

## Modules

- **Question Management:** CRUD endpoints for questions and answer options.
- **Student:** Random question sets with correct answers omitted.
- **Evaluation:** Scores submitted answers and returns correct options.
- **Result Management:** Persists attempts and lists newest results first.
- **Database:** Creates the SQLite schema and seeds 15 sample questions on an empty database.

## Tech

Spring Boot REST API, Spring JDBC (`JdbcTemplate`), SQLite, Docker, GitHub Actions, Vercel frontend, Swagger/OpenAPI.

## Run Locally

Requirements: Java 17 and Maven 3.9+.

```sh
mvn spring-boot:run
```

The API uses `./quiz.db` by default. Set `DB_PATH` to choose another SQLite file. The API listens on `http://localhost:8080`; Swagger UI is at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). Open [docs/index.html](docs/index.html) for the standalone demo frontend. Set its `API_BASE_URL` in `docs/app.js` to your running API to use live mode.

Run the test suite and package the application:

```sh
mvn -B clean package
```

## Desktop Version

The optional Swing client uses the same SQLite file and quiz evaluation service. Launch it in a graphical Java 17 environment with:

```sh
mvn -q compile exec:java -Dexec.mainClass=com.example.quiz.desktop.QuizApp
```

Set `DB_PATH` to use a different database file. The desktop UI is compiled by CI but is not launched in the headless Codespace.

## API Endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/health` | Service health status |
| `GET` | `/api/admin/questions` | List all questions including answers |
| `POST` | `/api/admin/questions` | Create a question |
| `PUT` | `/api/admin/questions/{id}` | Update a question |
| `DELETE` | `/api/admin/questions/{id}` | Delete a question |
| `GET` | `/api/questions?count=10` | Get a randomized public question set |
| `POST` | `/api/quiz/submit` | Evaluate and save an attempt |
| `GET` | `/api/results` | List saved results, newest first |

Submit example:

```json
{
	"studentName": "Sam Student",
	"answers": [
		{ "questionId": 1, "selected": "B" },
		{ "questionId": 2, "selected": null }
	]
}
```

## Deployment

### Vercel demo

1. Import this GitHub repository in Vercel and deploy from the repository root. `vercel.json` serves the static site from `docs/`.
2. Open the Vercel deployment URL. The quiz runs with bundled sample questions and keeps recent results in page memory; it does not require a separate API service.

Vercel does not run this Java Spring Boot API. To use live questions and shared, persistent results, deploy the API and database separately, then set `API_BASE_URL` in `docs/app.js` to that service. The included SQLite database is local storage and is not suitable for persistent serverless deployment.

## Future Enhancements

- Authentication and roles
- Analytics
- Leaderboard
- Excel/PDF export
- Cloud database (Postgres)
- Subject-wise quizzes
- Mobile app
