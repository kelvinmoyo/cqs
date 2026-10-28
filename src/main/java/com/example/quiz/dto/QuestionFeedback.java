package com.example.quiz.dto;

public record QuestionFeedback(long questionId, boolean correct, String correctOption) {
}