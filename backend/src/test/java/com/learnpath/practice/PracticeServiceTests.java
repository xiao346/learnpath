package com.learnpath.practice;

import com.learnpath.practice.PracticeDtos.AnswerResult;
import com.learnpath.practice.PracticeDtos.PracticeStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PracticeServiceTests {

    @Mock
    private PracticeQuestionRepository questionRepository;

    @Mock
    private PracticeAttemptRepository attemptRepository;

    @InjectMocks
    private PracticeService practiceService;

    @Test
    void submitCorrectAnswerAwardsPointsAndReturnsUpdatedStats() {
        PracticeQuestion question = new PracticeQuestion(
                "数据结构", "前序遍历的顺序是？", "左根右", "根左右", "左右根", "根右左",
                "B", "前序遍历是根、左、右。", "基础", 10);
        when(questionRepository.findById(7L)).thenReturn(Optional.of(question));
        when(attemptRepository.countByUserId(3L)).thenReturn(1L);
        when(attemptRepository.countByUserIdAndCorrectTrue(3L)).thenReturn(1L);
        when(attemptRepository.findByUserIdOrderByAnsweredAtDesc(3L))
                .thenReturn(List.of(new PracticeAttempt(3L, 7L, "B", true, 10)));

        AnswerResult result = practiceService.submit(3L, 7L, "b");

        assertThat(result.correct()).isTrue();
        assertThat(result.selectedOption()).isEqualTo("B");
        assertThat(result.pointsEarned()).isEqualTo(10);
        assertThat(result.stats()).isEqualTo(new PracticeStats(1, 1, 100, 10));
        ArgumentCaptor<PracticeAttempt> captor = ArgumentCaptor.forClass(PracticeAttempt.class);
        verify(attemptRepository).save(captor.capture());
        assertThat(captor.getValue().isCorrect()).isTrue();
    }

    @Test
    void submitUnknownQuestionFailsWithHelpfulMessage() {
        when(questionRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> practiceService.submit(3L, 404L, "A"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("练习题不存在");
    }
}
