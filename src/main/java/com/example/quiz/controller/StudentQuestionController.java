package com.example.quiz.controller;

import com.example.quiz.dto.PublicQuestion;
import com.example.quiz.service.QuestionService;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/questions")
public class StudentQuestionController {
    private final QuestionService questionService;

    public StudentQuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public List<PublicQuestion> getQuestions(@RequestParam(defaultValue = "10") @Min(1) int count) {
        return questionService.getRandomPublicQuestions(count);
    }
}