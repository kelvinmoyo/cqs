package com.example.quiz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class QuizApiTests {
    private static final Path DATABASE = createTemporaryDatabase();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + DATABASE);
    }

    @Test
    void studentQuestionsDoNotExposeCorrectAnswers() throws Exception {
        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].optionA").exists())
                .andExpect(jsonPath("$[0].correctOption").doesNotExist())
                .andExpect(jsonPath("$[0].correct_option").doesNotExist());
    }

    @Test
    void questionCountIsRandomizedAndCappedAtAvailableQuestions() throws Exception {
        mockMvc.perform(get("/api/questions").param("count", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
        mockMvc.perform(get("/api/questions").param("count", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(15));
    }

    @Test
    void allCorrectAnswersReceiveFullMarks() throws Exception {
        List<JsonNode> questions = adminQuestions();
        ArrayNode answers = objectMapper.createArrayNode();
        for (JsonNode question : questions) {
            answers.add(answer(question, question.get("correctOption").asText()));
        }

        mockMvc.perform(post("/api/quiz/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submission("Full Marks", answers)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(15))
                .andExpect(jsonPath("$.total").value(15))
                .andExpect(jsonPath("$.percentage").value(100.0));
    }

    @Test
    void wrongAndUnansweredResponsesCountAsIncorrect() throws Exception {
        List<JsonNode> questions = adminQuestions();
        JsonNode first = questions.get(0);
        JsonNode second = questions.get(1);
        String wrong = first.get("correctOption").asText().equals("A") ? "B" : "A";
        ArrayNode answers = objectMapper.createArrayNode()
                .add(answer(first, wrong))
                .add(answer(second, null));

        mockMvc.perform(post("/api/quiz/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submission("Partial", answers)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(0))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.feedback[0].correct").value(false))
                .andExpect(jsonPath("$.feedback[1].correct").value(false));
    }

    @Test
    void invalidSubmissionReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/quiz/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentName\":\" \",\"answers\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void submissionIsSavedAndListed() throws Exception {
        JsonNode question = adminQuestions().get(0);
        ArrayNode answers = objectMapper.createArrayNode().add(answer(question,
                question.get("correctOption").asText()));
        mockMvc.perform(post("/api/quiz/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submission("Listed Result", answers)))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/results"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode results = objectMapper.readTree(result.getResponse().getContentAsString());
        List<String> names = new ArrayList<>();
        results.forEach(row -> names.add(row.get("studentName").asText()));
        assertThat(names).contains("Listed Result");
    }

    private List<JsonNode> adminQuestions() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/questions"))
                .andExpect(status().isOk())
                .andReturn();
        List<JsonNode> questions = new ArrayList<>();
        objectMapper.readTree(result.getResponse().getContentAsString()).forEach(questions::add);
        return questions;
    }

    private ObjectNode answer(JsonNode question, String selected) {
        ObjectNode answer = objectMapper.createObjectNode();
        answer.put("questionId", question.get("id").asLong());
        if (selected == null) {
            answer.putNull("selected");
        } else {
            answer.put("selected", selected);
        }
        return answer;
    }

    private String submission(String name, ArrayNode answers) throws IOException {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("studentName", name);
        body.set("answers", answers);
        return objectMapper.writeValueAsString(body);
    }

    private static Path createTemporaryDatabase() {
        try {
            return Files.createTempFile("quiz-tests-", ".db");
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}