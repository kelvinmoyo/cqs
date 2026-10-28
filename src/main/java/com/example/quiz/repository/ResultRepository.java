package com.example.quiz.repository;

import com.example.quiz.model.QuizResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ResultRepository {
    private static final RowMapper<QuizResult> ROW_MAPPER = (row, index) -> new QuizResult(
            row.getLong("id"), row.getString("student_name"), row.getInt("score"), row.getInt("total"),
            LocalDateTime.parse(row.getString("taken_at")));

    private final JdbcTemplate jdbcTemplate;

    public ResultRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public QuizResult create(String studentName, int score, int total) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String takenAt = LocalDateTime.now().toString();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO results (student_name, score, total, taken_at) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, studentName);
            statement.setInt(2, score);
            statement.setInt(3, total);
            statement.setString(4, takenAt);
            return statement;
        }, keyHolder);
        return new QuizResult(keyHolder.getKey().longValue(), studentName, score, total,
                LocalDateTime.parse(takenAt));
    }

    public List<QuizResult> findAllNewestFirst() {
        return jdbcTemplate.query("SELECT * FROM results ORDER BY taken_at DESC, id DESC", ROW_MAPPER);
    }
}