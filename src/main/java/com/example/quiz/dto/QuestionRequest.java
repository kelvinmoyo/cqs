package com.example.quiz.dto;

import com.example.quiz.model.Question;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record QuestionRequest(
        @NotBlank String text,
        @NotBlank String optionA,
        @NotBlank String optionB,
        @NotBlank String optionC,
        @NotBlank String optionD,
        @NotBlank @Pattern(regexp = "[ABCD]") String correctOption) {

    public Question toQuestion(Long id) {
        return new Question(id, text, optionA, optionB, optionC, optionD, correctOption);
    }
}