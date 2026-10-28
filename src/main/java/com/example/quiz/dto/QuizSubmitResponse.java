package com.example.quiz.dto;

import java.util.List;

public record QuizSubmitResponse(int score, int total, double percentage, List<QuestionFeedback> feedback) {
}