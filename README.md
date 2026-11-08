# Quiz Management System

[![CI](https://github.com/kelvinmoyo/cqs/actions/workflows/ci.yml/badge.svg)](https://github.com/kelvinmoyo/cqs/actions/workflows/ci.yml)

Java 17 quiz application with a Spring Boot API, SQLite storage, and a static web frontend.

**Live demo:** [Take the quiz](https://cqs-tau.vercel.app/)

## Features

- Randomized multiple-choice quizzes with immediate scoring and feedback.
- Question management and saved results through the REST API.
- Browser-based sample quiz that works without a backend.
- Optional Swing desktop client.

## Run Locally

Requires Java 17 and Maven 3.9+.

```sh
mvn spring-boot:run
```

The API runs at `http://localhost:8080`; Swagger UI is at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). The default SQLite file is `quiz.db`; set `DB_PATH` to change it. To connect the frontend to a local API, set `API_BASE_URL` in `docs/app.js` to `http://localhost:8080`.

Run tests with:

```sh
mvn test
```

Launch the desktop client in a graphical environment with:

```sh
mvn -q compile exec:java -Dexec.mainClass=com.example.quiz.desktop.QuizApp
```

## API Endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/health` | Service health status |
| `GET`, `POST`, `PUT`, `DELETE` | `/api/admin/questions` | Manage questions |
| `GET` | `/api/questions?count=10` | Get a randomized public question set |
| `POST` | `/api/quiz/submit` | Evaluate and save an attempt |
| `GET` | `/api/results` | List saved results, newest first |

## Deployment

The static frontend is configured for Vercel in `vercel.json`. Vercel serves the sample quiz; live questions and persistent results require a separately hosted API and database.

## Future Enhancements

- Authentication and roles
- Analytics
- Leaderboard
- Excel/PDF export
- Cloud database (Postgres)
- Subject-wise quizzes
- Mobile app
