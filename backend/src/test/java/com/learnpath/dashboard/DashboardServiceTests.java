package com.learnpath.dashboard;

import com.learnpath.course.CourseRepository;
import com.learnpath.course.LearningProgressRepository;
import com.learnpath.practice.PracticeAttemptRepository;
import com.learnpath.practice.PracticeQuestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTests {

    @Mock private CourseRepository courseRepository;
    @Mock private LearningProgressRepository progressRepository;
    @Mock private StudyTaskRepository taskRepository;
    @Mock private StudySessionRepository sessionRepository;
    @Mock private PracticeAttemptRepository attemptRepository;
    @Mock private PracticeQuestionRepository questionRepository;

    @InjectMocks private DashboardService dashboardService;

    @Test
    void toggleUnknownTaskFailsWithoutChangingAnotherUsersData() {
        when(taskRepository.findByIdAndUserId(42L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardService.toggleTask(7L, 42L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("学习任务不存在");
    }
}
