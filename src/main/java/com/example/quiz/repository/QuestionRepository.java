package com.example.quiz.repository;

import com.example.quiz.model.Question;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class QuestionRepository {
    private static final RowMapper<Question> ROW_MAPPER = (row, index) -> new Question(
            row.getLong("id"), row.getString("text"), row.getString("option_a"),
            row.getString("option_b"), row.getString("option_c"), row.getString("option_d"),
            row.getString("correct_option"));

    private final JdbcTemplate jdbcTemplate;

    public QuestionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Question> findAll() {
        return jdbcTemplate.query("SELECT * FROM questions ORDER BY id", ROW_MAPPER);
    }

    public Optional<Question> findById(long id) {
        return jdbcTemplate.query("SELECT * FROM questions WHERE id = ?", ROW_MAPPER, id)
                .stream().findFirst();
    }

    public List<Question> findRandom(int count) {
        return jdbcTemplate.query("SELECT * FROM questions ORDER BY RANDOM() LIMIT ?", ROW_MAPPER, count);
    }

    public int count() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM questions", Integer.class);
    }

    public Question create(Question question) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO questions (text, option_a, option_b, option_c, option_d, correct_option) " +
                            "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            bind(statement, question);
            return statement;
        }, keyHolder);
        return findById(keyHolder.getKey().longValue()).orElseThrow();
    }

    public Optional<Question> update(long id, Question question) {
        int updated = jdbcTemplate.update(
                "UPDATE questions SET text = ?, option_a = ?, option_b = ?, option_c = ?, option_d = ?, " +
                        "correct_option = ? WHERE id = ?",
                question.text(), question.optionA(), question.optionB(), question.optionC(),
                question.optionD(), question.correctOption(), id);
        return updated == 0 ? Optional.empty() : findById(id);
    }

    public boolean delete(long id) {
        return jdbcTemplate.update("DELETE FROM questions WHERE id = ?", id) > 0;
    }

    private static void bind(PreparedStatement statement, Question question) throws java.sql.SQLException {
        statement.setString(1, question.text());
        statement.setString(2, question.optionA());
        statement.setString(3, question.optionB());
        statement.setString(4, question.optionC());
        statement.setString(5, question.optionD());
        statement.setString(6, question.correctOption());
    }
}