package com.example.quiz.desktop;

import com.example.quiz.config.QuestionSeeder;
import com.example.quiz.dto.AnswerRequest;
import com.example.quiz.dto.QuizSubmitRequest;
import com.example.quiz.dto.QuizSubmitResponse;
import com.example.quiz.model.Question;
import com.example.quiz.repository.QuestionRepository;
import com.example.quiz.repository.ResultRepository;
import com.example.quiz.service.EvaluationService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class QuizApp {
    private final JFrame frame = new JFrame("CQS Quiz");
    private final JTextField nameField = new JTextField(22);
    private final JTextArea questionText = new JTextArea(3, 32);
    private final JRadioButton[] options = {new JRadioButton(), new JRadioButton(),
            new JRadioButton(), new JRadioButton()};
    private final ButtonGroup optionGroup = new ButtonGroup();
    private final JButton nextButton = new JButton("Next");
    private final List<Question> questions;
    private final List<AnswerRequest> answers = new ArrayList<>();
    private final EvaluationService evaluationService;
    private int questionIndex;

    private QuizApp() {
        String databasePath = System.getenv().getOrDefault("DB_PATH", "./quiz.db");
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setUrl("jdbc:sqlite:" + databasePath);
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(dataSource);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        QuestionRepository questionRepository = new QuestionRepository(jdbcTemplate);
        new QuestionSeeder(questionRepository).run();
        questions = questionRepository.findRandom(10);
        evaluationService = new EvaluationService(questionRepository, new ResultRepository(jdbcTemplate));

        buildWindow();
        showQuestion();
    }

    private void buildWindow() {
        JPanel content = new JPanel(new BorderLayout(12, 12));
        JPanel identity = new JPanel(new BorderLayout(8, 8));
        identity.add(new JLabel("Student name"), BorderLayout.WEST);
        identity.add(nameField, BorderLayout.CENTER);
        content.add(identity, BorderLayout.NORTH);

        questionText.setEditable(false);
        questionText.setLineWrap(true);
        questionText.setWrapStyleWord(true);
        content.add(questionText, BorderLayout.CENTER);

        JPanel answersPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        for (JRadioButton option : options) {
            optionGroup.add(option);
            answersPanel.add(option);
        }
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(answersPanel, BorderLayout.CENTER);
        bottom.add(nextButton, BorderLayout.SOUTH);
        content.add(bottom, BorderLayout.SOUTH);

        nextButton.addActionListener(event -> advance());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(content);
        frame.setSize(520, 360);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    private void showQuestion() {
        Question question = questions.get(questionIndex);
        questionText.setText("Question " + (questionIndex + 1) + " of " + questions.size()
                + "\n\n" + question.text());
        String[] values = {question.optionA(), question.optionB(), question.optionC(), question.optionD()};
        for (int index = 0; index < options.length; index++) {
            options[index].setText((char) ('A' + index) + ". " + values[index]);
        }
        optionGroup.clearSelection();
        nextButton.setText(questionIndex == questions.size() - 1 ? "Submit" : "Next");
    }

    private void advance() {
        String selected = null;
        for (int index = 0; index < options.length; index++) {
            if (options[index].isSelected()) {
                selected = String.valueOf((char) ('A' + index));
                break;
            }
        }
        Question current = questions.get(questionIndex);
        answers.add(new AnswerRequest(current.id(), selected));
        if (questionIndex < questions.size() - 1) {
            questionIndex++;
            showQuestion();
            return;
        }
        submitQuiz();
    }

    private void submitQuiz() {
        String studentName = nameField.getText().trim();
        if (studentName.isEmpty()) {
            answers.remove(answers.size() - 1);
            questionIndex = questions.size() - 1;
            JOptionPane.showMessageDialog(frame, "Enter your name before submitting.",
                    "Name required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        QuizSubmitResponse result = evaluationService.submit(new QuizSubmitRequest(studentName, answers));
        JOptionPane.showMessageDialog(frame,
                "Score: " + result.score() + " / " + result.total() + " (" +
                        Math.round(result.percentage()) + "%)",
                "Quiz result", JOptionPane.INFORMATION_MESSAGE);
        frame.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new QuizApp();
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(null, exception.getMessage(),
                        "Unable to start quiz", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}