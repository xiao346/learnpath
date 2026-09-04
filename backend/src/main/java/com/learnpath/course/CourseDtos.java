package com.learnpath.course;

import jakarta.validation.constraints.Min;

import java.time.Instant;
import java.util.List;

public final class CourseDtos {

    private CourseDtos() {
    }

    public record CourseSummary(
            Long id,
            String title,
            String subtitle,
            String category,
            String teacherName,
            String difficulty,
            int durationMinutes,
            int totalLessons,
            int completedLessons,
            int progressPercent,
            String accent,
            String icon
    ) {
    }

    public record CourseDetail(
            Long id,
            String title,
            String subtitle,
            String category,
            String teacherName,
            String description,
            String difficulty,
            int durationMinutes,
            int totalLessons,
            int completedLessons,
            int progressPercent,
            String accent,
            String icon,
            Instant lastStudiedAt,
            List<ChapterView> chapters,
            List<ResourceView> resources
    ) {
    }

    public record ChapterView(Long id, String title, int orderIndex, int durationMinutes, boolean completed) {
    }

    public record ResourceView(
            Long id,
            String title,
            String provider,
            String resourceType,
            String description,
            String url
    ) {
    }

    public record UpdateProgressRequest(@Min(value = 0, message = "完成课时不能小于0") int completedLessons) {
    }

    public record ProgressView(Long courseId, int completedLessons, int totalLessons, int progressPercent, Instant lastStudiedAt) {
    }
}
