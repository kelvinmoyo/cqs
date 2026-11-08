# Internship Project Report

## Quiz Management System

**Submitted in partial fulfillment of the requirements for:**<br>
`[Degree / Diploma / Course Name]`

**Submitted by:** Kelvin Moyo<br>
**Organization:** InternshipStudio

**Live demo:** [https://cqs-tau.vercel.app/](https://cqs-tau.vercel.app/)<br>
**Source repository:** [https://github.com/kelvinmoyo/cqs](https://github.com/kelvinmoyo/cqs)

> Complete the bracketed fields, verify the declaration and certificate wording with your institution, and insert your own project images before submission.

---

## Certificate

This is to certify that **Kelvin Moyo** has completed the internship project titled **“Quiz Management System”** at **Internshipstudio**.

The report is submitted in partial fulfillment of the requirements for **`[Degree / Diploma / Course Name]`**.

**Authorized signature:** `[Name / Signature]`

## Declaration

I, **Kelvin Moyo**, declare that this report, titled **“Quiz Management System,”** describes my project work at **Internshipstudio**. Sources and third-party materials used in preparing this report are acknowledged in the references.

**Student signature:** `[Signature]`<br>

## Acknowledgement

I would like to thank **Internshipstudio** for the opportunity to work on this project. I also acknowledge the maintainers of the frameworks and tools listed in the references.

## Abstract

The Quiz Management System is a multiple-choice quiz application built with a Java 17 and Spring Boot REST API, SQLite persistence, and a static web frontend. The backend provides question administration, randomized public question retrieval, quiz evaluation, and result listing. Public question responses omit the correct answer, while submitted answers are evaluated by the service layer and stored as quiz results.

The web demo is deployed on Vercel and uses bundled sample questions, allowing a quiz to be completed without a separately hosted API. In this deployment, scores and recent attempts are held in browser memory and are not persistent. The Spring Boot API can be run locally with SQLite; a hosted API and persistent database are required for shared, persistent results.

**Keywords:** Quiz application, Spring Boot, REST API, SQLite, Java, web application

## Table of Contents

1. [Introduction](#1-introduction)
2. [Requirements and Tools](#2-requirements-and-tools)
3. [System Design](#3-system-design)
4. [Implementation](#4-implementation)
5. [Testing and Results](#5-testing-and-results)
6. [Deployment, Limitations, and Future Work](#6-deployment-limitations-and-future-work)
7. [Conclusion](#7-conclusion)
8. [References](#8-references)
9. [Appendix](#9-appendix)

## 1. Introduction

### 1.1 Background

Computer-based quizzes provide a practical way to present questions, collect answers, evaluate submissions, and return feedback. This project implements those core activities in a small application with a browser interface and a REST API.

### 1.2 Problem Statement

Manual quiz evaluation can take time and makes it harder to return consistent, immediate feedback. The project addresses this by providing randomized question sets, automatic scoring, answer-level feedback, and an API for maintaining questions and reviewing submitted results.

### 1.3 Objectives

- Provide an interactive multiple-choice quiz with a time limit and immediate feedback.
- Expose REST endpoints to create, read, update, and delete questions.
- Return randomized student questions without exposing their correct answers.
- Evaluate submissions and record results in SQLite when the API is used.
- Publish an accessible sample quiz through a static Vercel deployment.

### 1.4 Project Scope

The project includes a static browser frontend, a Spring Boot REST API, a SQLite schema, sample question seeding, and an optional Swing desktop client. Authentication, user roles, and a managed production database are outside the current scope.

## 2. Requirements and Tools

### 2.1 Functional Requirements

1. Administrators can list and maintain quiz questions through the API.
2. Students can request a randomized set of public questions.
3. The application can accept answers, calculate a score, and return per-question feedback.
4. The API can save quiz results and return them in newest-first order.
5. The static demo can run a sample quiz without a backend service.

### 2.2 Non-Functional Requirements

- Public question data must not expose the correct option.
- Request data is validated by the API before processing.
- The frontend should remain usable on common desktop and mobile screen sizes.
- The backend should be straightforward to run locally with Java and Maven.

### 2.3 Technology Stack

| Area | Technology |
| --- | --- |
| Backend | Java 17, Spring Boot 3.5 |
| API and validation | Spring Web, Jakarta Bean Validation |
| Data access | Spring JDBC (`JdbcTemplate`) |
| Database | SQLite |
| Frontend | HTML, CSS, JavaScript |
| API documentation | SpringDoc OpenAPI / Swagger UI |
| Build and tests | Maven, JUnit 5, Spring MockMvc |
| Static hosting | Vercel |

## 3. System Design

### 3.1 Architecture

The backend follows a layered structure. Controllers handle HTTP requests, services implement quiz and question operations, repositories access the SQLite database, and DTOs define public request and response data. The static frontend renders the quiz and communicates with the API when an API base URL is configured.

The current Vercel deployment is frontend-only: it loads bundled sample data and does not connect to the Spring API. Its sample score and recent-attempt list exist only in the current page session. Running the backend locally enables database-backed API behavior; public live API functionality requires a separately deployed backend and database.

**Figure 1. System architecture**<br>
`[Insert architecture diagram here]`

### 3.2 Data Design

The database contains two main tables:

| Table | Main fields | Purpose |
| --- | --- | --- |
| `questions` | ID, question text, four options, correct option | Stores quiz questions and answer keys. The correct option is restricted to A, B, C, or D. |
| `results` | ID, student name, score, total, submission time | Stores evaluated quiz attempts. |

On an empty database, the application seeds 15 sample questions. The public question endpoint maps database records to a public DTO that excludes the answer key.

**Figure 2. Database ER diagram**<br>
`[Insert database diagram here]`

### 3.3 Quiz Workflow

1. The student enters a name and begins a quiz.
2. The frontend displays a randomized set of up to 10 questions and starts a five-minute timer.
3. Answers are collected and submitted when the student finishes or time expires.
4. The evaluation service compares each answer with the stored correct option and builds feedback.
5. When using the API, the attempt is saved and can be retrieved through the results endpoint. In the Vercel sample mode, the result is calculated in the browser and is not saved persistently.

**Figure 3. Quiz workflow**<br>
`[Insert workflow diagram here]`

## 4. Implementation

### 4.1 Backend

The Spring Boot application exposes the following principal endpoints:

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/health` | Returns service health status. |
| `GET` | `/api/admin/questions` | Lists all questions, including answer keys. |
| `POST` | `/api/admin/questions` | Creates a question. |
| `PUT` | `/api/admin/questions/{id}` | Updates a question. |
| `DELETE` | `/api/admin/questions/{id}` | Deletes a question. |
| `GET` | `/api/questions?count=10` | Returns a randomized public question set. |
| `POST` | `/api/quiz/submit` | Evaluates and stores a quiz attempt. |
| `GET` | `/api/results` | Lists saved results, newest first. |

The question service caps the requested count at the number of available questions. The evaluation service compares selected options, calculates the score and percentage, creates per-question feedback, and asks the result repository to store the attempt. Validation errors are returned as client errors.

### 4.2 Frontend

The browser interface is a static HTML, CSS, and JavaScript application in the `docs/` directory. It supports a timed quiz, randomized questions, progress tracking, answer submission, score display, and review of correct answers. With the API base URL left empty, the page uses bundled sample data and stores recent results only in page memory.

**Figure 4. Quiz start screen**<br>
`[Insert screenshot here]`

**Figure 5. Quiz question screen**<br>
`[Insert screenshot here]`

**Figure 6. Score and feedback screen**<br>
`[Insert screenshot here]`

## 5. Testing and Results

The automated API test suite uses Spring Boot, MockMvc, JUnit 5, and a temporary SQLite database. It covers the following behaviors:

| Test area | Expected behavior |
| --- | --- |
| Public question privacy | Correct-answer fields are absent from student responses. |
| Randomization and count | Requested count is respected and capped at available questions. |
| Correct answers | A fully correct submission receives full marks. |
| Incorrect and unanswered responses | Incorrect or unanswered choices do not receive marks. |
| Request validation | Invalid submissions return HTTP 400. |
| Result persistence | Submitted attempts appear in the results endpoint. |

The project test suite contains six API tests and has passed with `mvn test` in the development environment. The browser demo can also be checked manually by completing a sample quiz and reviewing the displayed score and feedback.

**Figure 7. Automated test results**<br>
`[Insert test report screenshot here]`

## 6. Deployment, Limitations, and Future Work

### 6.1 Deployment

The live browser demo is available at [https://cqs-tau.vercel.app/](https://cqs-tau.vercel.app/). It is deployed as a static site from the `docs/` directory and currently runs in sample mode. The Spring Boot API can be started locally with `mvn spring-boot:run`; Swagger UI is available at `http://localhost:8080/swagger-ui.html` while the local service is running.

**Figure 8. Live application**<br>
`[Insert screenshot of the deployed application here]`

### 6.2 Limitations

- The public Vercel demo does not connect to a hosted Spring API.
- Sample-mode scores and recent attempts are not persisted across page reloads.
- The backend uses SQLite by default; a production deployment needs suitable persistent database hosting and operational configuration.
- Authentication and authorization for question administration are not implemented.

### 6.3 Future Work

- Deploy the API with a managed, persistent database.
- Add authentication and role-based access to administration endpoints.
- Add reporting, analytics, and export features.
- Expand question management and quiz configuration options.

## 7. Conclusion

The Quiz Management System demonstrates a full-stack approach to quiz delivery and evaluation. The backend separates request handling, business logic, and persistence, while the static frontend provides a timed quiz experience and immediate feedback. Automated tests cover key API behaviors, including answer privacy, scoring, validation, and result storage. The current public deployment is a sample-mode frontend; connecting it to a persistent hosted API and database is the main step required for a complete public client-server deployment.

## 8. References

1. Project source repository: [https://github.com/kelvinmoyo/cqs](https://github.com/kelvinmoyo/cqs).
2. Java documentation: [https://docs.oracle.com/en/java/](https://docs.oracle.com/en/java/).
3. Spring Boot reference documentation: [https://docs.spring.io/spring-boot/](https://docs.spring.io/spring-boot/).
4. SQLite documentation: [https://www.sqlite.org/docs.html](https://www.sqlite.org/docs.html).
5. Maven documentation: [https://maven.apache.org/guides/](https://maven.apache.org/guides/).

## 9. Appendix

### A. Running the Backend Locally

Requirements: Java 17 and Maven 3.9 or later.

```sh
mvn spring-boot:run
```

The API listens on port `8080` by default and creates/uses `quiz.db` in the working directory. Set `DB_PATH` to select another database file. Run automated tests with:

```sh
mvn test
```

### B. Additional Images or Screenshots

`[Insert additional image or screenshot here]`

`[Insert additional image or screenshot here]`