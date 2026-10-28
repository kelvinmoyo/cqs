package com.example.quiz.service;

import com.example.quiz.model.QuizResult;
import com.example.quiz.repository.ResultRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResultService {
    private final ResultRepository resultRepository;

    public ResultService(ResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    public List<QuizResult> findAll() {
        return resultRepository.findAllNewestFirst();
    }
}