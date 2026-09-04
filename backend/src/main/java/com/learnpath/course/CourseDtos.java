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
            long resourceCount,
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

    public record ChapterLessonView(
            Long courseId,
            String courseTitle,
            Long chapterId,
            String chapterTitle,
            int orderIndex,
            int durationMinutes,
            boolean completed,
            String overview,
            String beginnerIntro,
            String beginnerAnalogy,
            List<String> beginnerWalkthrough,
            List<String> objectives,
            List<String> keyPoints,
            WorkedExampleView workedExample,
            List<LearningStepView> learningPath,
            List<KnowledgeAnalysisView> knowledgeAnalyses,
            List<StudySectionView> studySections,
            List<String> selfCheckQuestions,
            String practiceTask,
            Long previousChapterId,
            Long nextChapterId
    ) {
    }

    public record WorkedExampleView(
            String title,
            String scenario,
            List<WorkedExampleStepView> steps,
            String result,
            String tryIt
    ) {
    }

    public record WorkedExampleStepView(String label, String action, String explanation) {
    }

    public record LearningStepView(
            String id,
            String stage,
            String title,
            String detail
    ) {
    }

    public record KnowledgeAnalysisView(
            String id,
            String title,
            String category,
            String plainExplanation,
            String whyItMatters,
            List<DiagramStepView> diagram,
            String example,
            String commonMistake,
            String quickCheck
    ) {
    }

    public record DiagramStepView(String label, String content) {
    }

    public record StudySectionView(String title, String summary, List<String> points) {
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
