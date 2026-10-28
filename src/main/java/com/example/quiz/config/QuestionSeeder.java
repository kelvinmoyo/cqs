package com.example.quiz.config;

import com.example.quiz.model.Question;
import com.example.quiz.repository.QuestionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuestionSeeder implements CommandLineRunner {
    private final QuestionRepository questionRepository;

    public QuestionSeeder(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(String... args) {
        if (questionRepository.count() != 0) {
            return;
        }
        sampleQuestions().forEach(questionRepository::create);
    }

    private List<Question> sampleQuestions() {
        return List.of(
                q("What is the capital city of Japan?", "Seoul", "Tokyo", "Beijing", "Bangkok", "B"),
                q("Which planet is known as the Red Planet?", "Venus", "Jupiter", "Mars", "Mercury", "C"),
                q("How many sides does a hexagon have?", "Five", "Six", "Seven", "Eight", "B"),
                q("Which ocean is the largest?", "Atlantic", "Indian", "Arctic", "Pacific", "D"),
                q("What is the chemical symbol for gold?", "Ag", "Au", "Gd", "Go", "B"),
                q("Who wrote the play Romeo and Juliet?", "Charles Dickens", "Jane Austen", "William Shakespeare", "Mark Twain", "C"),
                q("Which gas do plants absorb from the atmosphere?", "Oxygen", "Carbon dioxide", "Nitrogen", "Hydrogen", "B"),
                q("What is the largest mammal on Earth?", "African elephant", "Blue whale", "Giraffe", "Orca", "B"),
                q("In which continent is the Sahara Desert?", "Asia", "South America", "Africa", "Australia", "C"),
                q("What is the freezing point of water in Celsius?", "0", "32", "100", "-10", "A"),
                q("Which keyword declares a class in Java?", "define", "class", "struct", "object", "B"),
                q("Which Java type stores a true or false value?", "int", "String", "boolean", "char", "C"),
                q("Which collection does not allow duplicate elements?", "List", "Queue", "Set", "ArrayList", "C"),
                q("What is the entry-point method signature in a Java application?", "public void start()", "public static void main(String[] args)", "static int run()", "void main()", "B"),
                q("Which company originally created the Java programming language?", "Microsoft", "Sun Microsystems", "IBM", "Apple", "B"));
    }

    private Question q(String text, String a, String b, String c, String d, String correct) {
        return new Question(null, text, a, b, c, d, correct);
    }
}