package com.learnpath.practice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public final class PracticeDtos {

    private PracticeDtos() {
    }

    public record OptionView(String key, String text) {
    }

    public record QuestionView(
            Long id,
            String subject,
            String prompt,
            List<OptionView> options,
            String difficulty,
            int points,
            boolean answered
    ) {
    }

    public record SubmitAnswerRequest(
            @NotBlank(message = "请选择答案")
            @Pattern(regexp = "(?i)[A-D]", message = "答案必须是 A、B、C 或 D") String selectedOption
    ) {
    }

    public record AnswerResult(
            Long questionId,
            String selectedOption,
            String correctOption,
            boolean correct,
            String explanation,
            int pointsEarned,
            PracticeStats stats
    ) {
    }

    public record PracticeStats(long totalAnswered, long correctAnswers, int accuracyPercent, int totalPoints) {
    }
}
