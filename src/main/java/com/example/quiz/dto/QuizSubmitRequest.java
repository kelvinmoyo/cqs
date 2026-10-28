package com.example.quiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record QuizSubmitRequest(
        @NotBlank String studentName,
        @NotEmpty List<@Valid AnswerRequest> answers) {
}