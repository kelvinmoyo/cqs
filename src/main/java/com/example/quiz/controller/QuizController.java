package com.example.quiz.controller;

import com.example.quiz.dto.QuizSubmitRequest;
import com.example.quiz.dto.QuizSubmitResponse;
import com.example.quiz.service.EvaluationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {
    private final EvaluationService evaluationService;

    public QuizController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping("/submit")
    public QuizSubmitResponse submit(@Valid @RequestBody QuizSubmitRequest request) {
        return evaluationService.submit(request);
    }
}