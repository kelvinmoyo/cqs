package com.example.quiz.service;

import com.example.quiz.dto.PublicQuestion;
import com.example.quiz.model.Question;
import com.example.quiz.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    public Question create(Question question) {
        return questionRepository.create(question);
    }

    public Question update(long id, Question question) {
        return questionRepository.update(id, question).orElseThrow(() -> new ResourceNotFoundException("Question", id));
    }

    public void delete(long id) {
        if (!questionRepository.delete(id)) {
            throw new ResourceNotFoundException("Question", id);
        }
    }

    public List<PublicQuestion> getRandomPublicQuestions(int requestedCount) {
        int count = Math.min(requestedCount, questionRepository.count());
        return questionRepository.findRandom(count).stream().map(PublicQuestion::from).toList();
    }
}