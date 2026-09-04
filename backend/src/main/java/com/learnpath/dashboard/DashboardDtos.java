package com.learnpath.dashboard;

import java.time.LocalDate;
import java.util.List;

public final class DashboardDtos {

    private DashboardDtos() {
    }

    public record FocusView(
            Long courseId,
            String courseTitle,
            String chapterTitle,
            int estimatedMinutes,
            int completedLessons,
            int totalLessons,
            int progressPercent
    ) {
    }

    public record TaskView(
            Long id,
            String title,
            String subject,
            int estimatedMinutes,
            int xpReward,
            boolean completed
    ) {
    }

    public record DayView(LocalDate date, String label, int minutes, boolean studied, boolean today) {
    }

    public record TrendView(
            int totalMinutes,
            int previousWeekMinutes,
            int changePercent,
            List<DayView> days
    ) {
    }

    public record RecommendationView(String title, String description, String route) {
    }

    public record DashboardView(
            FocusView focus,
            int streakDays,
            int tasksCompleted,
            int totalTasks,
            List<TaskView> tasks,
            TrendView trend,
            int weeklyGoalPercent,
            int weeklyRemainingMinutes,
            RecommendationView recommendation
    ) {
    }
}
