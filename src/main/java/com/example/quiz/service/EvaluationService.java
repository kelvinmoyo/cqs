package com.example.quiz.service;

import com.example.quiz.dto.AnswerRequest;
import com.example.quiz.dto.QuestionFeedback;
import com.example.quiz.dto.QuizSubmitRequest;
import com.example.quiz.dto.QuizSubmitResponse;
import com.example.quiz.model.Question;
import com.example.quiz.repository.QuestionRepository;
import com.example.quiz.repository.ResultRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EvaluationService {
    private final QuestionRepository questionRepository;
    private final ResultRepository resultRepository;

    public EvaluationService(QuestionRepository questionRepository, ResultRepository resultRepository) {
        this.questionRepository = questionRepository;
        this.resultRepository = resultRepository;
    }

    public QuizSubmitResponse submit(QuizSubmitRequest request) {
        List<QuestionFeedback> feedback = new ArrayList<>();
        int score = 0;
        for (AnswerRequest answer : request.answers()) {
            Question question = questionRepository.findById(answer.questionId())
                    .orElseThrow(() -> new IllegalArgumentException("Unknown question: " + answer.questionId()));
            boolean correct = question.correctOption().equals(answer.selected());
            if (correct) {
                score++;
            }
            feedback.add(new QuestionFeedback(question.id(), correct, question.correctOption()));
        }
        int total = request.answers().size();
        resultRepository.create(request.studentName().trim(), score, total);
        return new QuizSubmitResponse(score, total, score * 100.0 / total, List.copyOf(feedback));
    }
}