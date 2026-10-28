package com.example.quiz.dto;

import com.example.quiz.model.Question;

public record PublicQuestion(
        Long id,
        String text,
        String optionA,
        String optionB,
        String optionC,
        String optionD) {

    public static PublicQuestion from(Question question) {
        return new PublicQuestion(question.id(), question.text(), question.optionA(), question.optionB(),
                question.optionC(), question.optionD());
    }
}