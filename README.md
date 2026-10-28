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

Spring Boot REST API, Spring JDBC (`JdbcTemplate`), SQLite, Docker, GitHub Actions, GitHub Pages frontend, Swagger/OpenAPI.

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

### Render API

1. Create a new Render Web Service from this GitHub repository.
2. Select **Docker** as the runtime and the free instance plan.
3. Deploy the service; the included Dockerfile listens on Render's `PORT`.
4. Copy the generated service URL into `API_BASE_URL` at the top of `docs/app.js`, commit, and push.

### GitHub Pages frontend

1. In the repository, open **Settings** → **Pages**.
2. Under **Build and deployment**, choose **Deploy from a branch**.
3. Select branch **main**, folder **/docs**, then tap **Save**.
4. After the deployment completes, open `https://kelvinmoyo.github.io/cqs/`.

Live links: [Quiz frontend](https://kelvinmoyo.github.io/cqs/) · [API Swagger UI](https://your-render-service.onrender.com/swagger-ui.html) (replace with the URL assigned by Render)

The first request to Render's free tier can take 30–60 seconds while the service starts. The frontend begins in Demo mode with sample questions and switches to live data when the API responds. SQLite storage on Render's free tier is ephemeral; saved results reset after a restart or redeploy.

## Future Enhancements

- Authentication and roles
- Analytics
- Leaderboard
- Excel/PDF export
- Cloud database (Postgres)
- Subject-wise quizzes
- Mobile app
