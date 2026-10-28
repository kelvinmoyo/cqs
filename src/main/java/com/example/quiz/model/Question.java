package com.example.quiz.model;

public record Question(
        Long id,
        String text,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String correctOption) {
}