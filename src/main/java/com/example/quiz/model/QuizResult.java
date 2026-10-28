package com.example.quiz.model;

import java.time.LocalDateTime;

public record QuizResult(
        Long id,
        String studentName,
        int score,
        int total,
        LocalDateTime takenAt) {
}