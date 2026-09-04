package com.learnpath.course;

import com.learnpath.course.CourseDtos.ChapterView;
import com.learnpath.course.CourseDtos.ChapterLessonView;
import com.learnpath.course.CourseDtos.CourseDetail;
import com.learnpath.course.CourseDtos.CourseSummary;
import com.learnpath.course.CourseDtos.ProgressView;
import com.learnpath.course.CourseDtos.ResourceView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final LearningProgressRepository progressRepository;
    private final CourseResourceRepository resourceRepository;

    public CourseService(CourseRepository courseRepository, LearningProgressRepository progressRepository,
                         CourseResourceRepository resourceRepository) {
        this.courseRepository = courseRepository;
        this.progressRepository = progressRepository;
        this.resourceRepository = resourceRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseSummary> list(Long userId, String keyword, String category) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        String normalizedCategory = category == null ? "" : category.trim();
        List<Course> courses = courseRepository.findAllByPublishedTrueOrderByIdAsc().stream()
                .filter(course -> normalizedCategory.isBlank() || normalizedCategory.equals(course.getCategory()))
                .filter(course -> normalizedKeyword.isBlank()
                        || course.getTitle().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || course.getSubtitle().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || course.getTeacherName().toLowerCase(Locale.ROOT).contains(normalizedKeyword))
                .toList();

        Map<Long, LearningProgress> progressByCourse = progressRepository
                .findByUserIdAndCourseIdIn(userId, courses.stream().map(Course::getId).toList())
                .stream()
                .collect(Collectors.toMap(LearningProgress::getCourseId, Function.identity()));

        return courses.stream()
                .map(course -> toSummary(course, progressByCourse.get(course.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseDetail detail(Long userId, Long courseId) {
        Course course = courseRepository.findWithChaptersByIdAndPublishedTrue(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在或已下架"));
        LearningProgress progress = progressRepository.findByUserIdAndCourseId(userId, courseId).orElse(null);
        int completedLessons = completed(progress);
        List<ChapterView> chapters = course.getChapters().stream()
                .map(chapter -> new ChapterView(
                        chapter.getId(),
                        chapter.getTitle(),
                        chapter.getOrderIndex(),
                        chapter.getDurationMinutes(),
                        chapter.getOrderIndex() <= completedLessons))
                .toList();
        List<ResourceView> resources = resourceRepository.findByCourseIdOrderBySortOrderAsc(courseId).stream()
                .map(resource -> new ResourceView(
                        resource.getId(), resource.getTitle(), resource.getProvider(),
                        resource.getResourceType(), resource.getDescription(), resource.getUrl()))
                .toList();

        return new CourseDetail(
                course.getId(), course.getTitle(), course.getSubtitle(), course.getCategory(),
                course.getTeacherName(), course.getDescription(), course.getDifficulty(),
                course.getDurationMinutes(), course.getChapters().size(), completedLessons,
                percent(completedLessons, course.getChapters().size()), course.getAccent(), course.getIcon(),
                progress == null ? null : progress.getLastStudiedAt(), chapters, resources);
    }

    @Transactional(readOnly = true)
    public ChapterLessonView lesson(Long userId, Long courseId, Long chapterId) {
        Course course = courseRepository.findWithChaptersByIdAndPublishedTrue(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在或已下架"));
        List<Chapter> chapters = course.getChapters();
        int chapterPosition = -1;
        for (int index = 0; index < chapters.size(); index++) {
            if (chapters.get(index).getId().equals(chapterId)) {
                chapterPosition = index;
                break;
            }
        }
        if (chapterPosition < 0) {
            throw new IllegalArgumentException("章节不存在或不属于当前课程");
        }

        Chapter chapter = chapters.get(chapterPosition);
        int completedLessons = progressRepository.findByUserIdAndCourseId(userId, courseId)
                .map(LearningProgress::getCompletedLessons)
                .orElse(0);
        return new ChapterLessonView(
                course.getId(), course.getTitle(), chapter.getId(), chapter.getTitle(),
                chapter.getOrderIndex(), chapter.getDurationMinutes(), chapter.getOrderIndex() <= completedLessons,
                chapter.getOverview(), lines(chapter.getObjectives()), lines(chapter.getKeyPoints()),
                chapter.getPracticeTask(),
                chapterPosition == 0 ? null : chapters.get(chapterPosition - 1).getId(),
                chapterPosition == chapters.size() - 1 ? null : chapters.get(chapterPosition + 1).getId());
    }

    @Transactional
    public ProgressView updateProgress(Long userId, Long courseId, int completedLessons) {
        Course course = courseRepository.findWithChaptersByIdAndPublishedTrue(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在或已下架"));
        int totalLessons = course.getChapters().size();
        if (completedLessons > totalLessons) {
            throw new IllegalArgumentException("完成课时不能超过课程总课时");
        }

        LearningProgress progress = progressRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseGet(() -> new LearningProgress(userId, courseId, 0));
        progress.update(completedLessons);
        LearningProgress saved = progressRepository.save(progress);
        return new ProgressView(courseId, saved.getCompletedLessons(), totalLessons,
                percent(saved.getCompletedLessons(), totalLessons), saved.getLastStudiedAt());
    }

    private CourseSummary toSummary(Course course, LearningProgress progress) {
        int completedLessons = completed(progress);
        int totalLessons = course.getChapters().size();
        return new CourseSummary(
                course.getId(), course.getTitle(), course.getSubtitle(), course.getCategory(),
                course.getTeacherName(), course.getDifficulty(), course.getDurationMinutes(), totalLessons,
                resourceRepository.countByCourseId(course.getId()),
                completedLessons, percent(completedLessons, totalLessons), course.getAccent(), course.getIcon());
    }

    private int completed(LearningProgress progress) {
        return progress == null ? 0 : progress.getCompletedLessons();
    }

    private int percent(int completedLessons, int totalLessons) {
        return totalLessons == 0 ? 0 : Math.round((completedLessons * 100f) / totalLessons);
    }

    private List<String> lines(String value) {
        return value == null || value.isBlank() ? List.of() : value.lines().filter(line -> !line.isBlank()).toList();
    }
}
