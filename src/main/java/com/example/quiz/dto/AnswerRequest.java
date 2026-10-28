package com.example.quiz.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AnswerRequest(
        @NotNull @Min(1) Long questionId,
        @Pattern(regexp = "[ABCD]") String selected) {
}