package com.learnpath.practice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "practice_question")
public class PracticeQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String subject;

    @Column(nullable = false, length = 500)
    private String prompt;

    @Column(nullable = false, length = 200)
    private String optionA;

    @Column(nullable = false, length = 200)
    private String optionB;

    @Column(nullable = false, length = 200)
    private String optionC;

    @Column(nullable = false, length = 200)
    private String optionD;

    @Column(nullable = false, length = 1)
    private String correctOption;

    @Column(nullable = false, length = 500)
    private String explanation;

    @Column(nullable = false, length = 20)
    private String difficulty;

    @Column(nullable = false)
    private int points;

    protected PracticeQuestion() {
    }

    public PracticeQuestion(String subject, String prompt, String optionA, String optionB,
                            String optionC, String optionD, String correctOption,
                            String explanation, String difficulty, int points) {
        this.subject = subject;
        this.prompt = prompt;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
        this.explanation = explanation;
        this.difficulty = difficulty;
        this.points = points;
    }

    public boolean isCorrect(String selectedOption) {
        return correctOption.equalsIgnoreCase(selectedOption);
    }

    public Long getId() { return id; }
    public String getSubject() { return subject; }
    public String getPrompt() { return prompt; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public String getCorrectOption() { return correctOption; }
    public String getExplanation() { return explanation; }
    public String getDifficulty() { return difficulty; }
    public int getPoints() { return points; }
}
