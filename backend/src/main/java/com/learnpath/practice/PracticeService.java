package com.learnpath.practice;

import com.learnpath.practice.PracticeDtos.AnswerResult;
import com.learnpath.practice.PracticeDtos.OptionView;
import com.learnpath.practice.PracticeDtos.PracticeStats;
import com.learnpath.practice.PracticeDtos.QuestionView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PracticeService {

    private final PracticeQuestionRepository questionRepository;
    private final PracticeAttemptRepository attemptRepository;

    public PracticeService(PracticeQuestionRepository questionRepository, PracticeAttemptRepository attemptRepository) {
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
    }

    @Transactional(readOnly = true)
    public List<QuestionView> listQuestions(Long userId, String subject) {
        List<PracticeQuestion> questions = subject == null || subject.isBlank()
                ? questionRepository.findAllByOrderByIdAsc()
                : questionRepository.findBySubjectOrderByIdAsc(subject.trim());
        Set<Long> answeredQuestionIds = attemptRepository.findByUserIdOrderByAnsweredAtDesc(userId).stream()
                .map(PracticeAttempt::getQuestionId)
                .collect(Collectors.toSet());
        return questions.stream()
                .map(question -> toView(question, answeredQuestionIds.contains(question.getId())))
                .toList();
    }

    @Transactional
    public AnswerResult submit(Long userId, Long questionId, String selectedOption) {
        PracticeQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("练习题不存在"));
        String normalizedOption = selectedOption.toUpperCase(Locale.ROOT);
        boolean correct = question.isCorrect(normalizedOption);
        int pointsEarned = correct ? question.getPoints() : 0;
        attemptRepository.save(new PracticeAttempt(userId, questionId, normalizedOption, correct, pointsEarned));
        return new AnswerResult(
                questionId,
                normalizedOption,
                question.getCorrectOption(),
                correct,
                question.getExplanation(),
                pointsEarned,
                stats(userId));
    }

    @Transactional(readOnly = true)
    public PracticeStats stats(Long userId) {
        long total = attemptRepository.countByUserId(userId);
        long correct = attemptRepository.countByUserIdAndCorrectTrue(userId);
        int totalPoints = attemptRepository.findByUserIdOrderByAnsweredAtDesc(userId).stream()
                .mapToInt(PracticeAttempt::getPointsEarned)
                .sum();
        int accuracy = total == 0 ? 0 : (int) Math.round(correct * 100.0 / total);
        return new PracticeStats(total, correct, accuracy, totalPoints);
    }

    private QuestionView toView(PracticeQuestion question, boolean answered) {
        return new QuestionView(
                question.getId(),
                question.getSubject(),
                question.getPrompt(),
                List.of(
                        new OptionView("A", question.getOptionA()),
                        new OptionView("B", question.getOptionB()),
                        new OptionView("C", question.getOptionC()),
                        new OptionView("D", question.getOptionD())),
                question.getDifficulty(),
                question.getPoints(),
                answered);
    }
}
