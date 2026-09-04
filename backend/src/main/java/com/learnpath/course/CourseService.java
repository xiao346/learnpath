package com.learnpath.course;

import com.learnpath.course.CourseDtos.ChapterView;
import com.learnpath.course.CourseDtos.ChapterLessonView;
import com.learnpath.course.CourseDtos.CourseDetail;
import com.learnpath.course.CourseDtos.CourseSummary;
import com.learnpath.course.CourseDtos.ProgressView;
import com.learnpath.course.CourseDtos.ResourceView;
import com.learnpath.course.CourseDtos.KnowledgeEdgeView;
import com.learnpath.course.CourseDtos.KnowledgeNodeView;
import com.learnpath.course.CourseDtos.StudySectionView;
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
        List<String> keyPoints = lines(chapter.getKeyPoints());
        int completedLessons = progressRepository.findByUserIdAndCourseId(userId, courseId)
                .map(LearningProgress::getCompletedLessons)
                .orElse(0);
        return new ChapterLessonView(
                course.getId(), course.getTitle(), chapter.getId(), chapter.getTitle(),
                chapter.getOrderIndex(), chapter.getDurationMinutes(), chapter.getOrderIndex() <= completedLessons,
                chapter.getOverview(), lines(chapter.getObjectives()), keyPoints,
                knowledgeNodes(chapter, keyPoints), knowledgeEdges(keyPoints.size()),
                studySections(chapter, keyPoints), selfCheckQuestions(chapter, keyPoints),
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

    private List<KnowledgeNodeView> knowledgeNodes(Chapter chapter, List<String> points) {
        java.util.ArrayList<KnowledgeNodeView> nodes = new java.util.ArrayList<>();
        nodes.add(new KnowledgeNodeView("root", chapter.getTitle(), "核心主题", chapter.getOverview()));
        for (int index = 0; index < points.size(); index++) {
            String point = points.get(index);
            nodes.add(new KnowledgeNodeView("point-" + (index + 1), shortLabel(point),
                    switch (index % 3) {
                        case 0 -> "概念基础";
                        case 1 -> "原理方法";
                        default -> "应用边界";
                    }, point));
        }
        nodes.add(new KnowledgeNodeView("practice", "实践验证", "学习产出", chapter.getPracticeTask()));
        return nodes;
    }

    private List<KnowledgeEdgeView> knowledgeEdges(int pointCount) {
        java.util.ArrayList<KnowledgeEdgeView> edges = new java.util.ArrayList<>();
        for (int index = 1; index <= pointCount; index++) {
            edges.add(new KnowledgeEdgeView("root", "point-" + index,
                    index <= 2 ? "包含" : index <= 4 ? "推导" : "迁移"));
        }
        if (pointCount > 0) {
            edges.add(new KnowledgeEdgeView("point-" + pointCount, "practice", "验证"));
        }
        return edges;
    }

    private List<StudySectionView> studySections(Chapter chapter, List<String> points) {
        return List.of(
                new StudySectionView("一、概念与定义", "先建立准确术语和边界，避免只记结论。", slice(points, 0, 2)),
                new StudySectionView("二、原理与方法", "理解知识如何运作、为何有效，以及选择方法时的依据。", slice(points, 2, 4)),
                new StudySectionView("三、应用与辨析", "把知识迁移到真实问题，并识别限制条件与常见误区。", slice(points, 4, 6)),
                new StudySectionView("四、实践与复盘", "完成可检查的学习产出，再根据结果回到薄弱知识节点。",
                        List.of(chapter.getPracticeTask(), "复盘时写下：采用了什么方法、为什么这样选择、结果如何验证、下一次怎样改进。")));
    }

    private List<String> selfCheckQuestions(Chapter chapter, List<String> points) {
        String first = points.isEmpty() ? chapter.getTitle() : shortLabel(points.get(0));
        String second = points.size() < 2 ? chapter.getTitle() : shortLabel(points.get(1));
        return List.of(
                "你能不用查看资料，用自己的话解释“" + chapter.getTitle() + "”解决的核心问题吗？",
                "“" + first + "”与“" + second + "”之间有什么联系或区别？",
                "在哪些条件下本节方法不再适用，应该改用什么思路？",
                "你能独立完成动手任务，并用结果证明实现或推理正确吗？");
    }

    private List<String> slice(List<String> points, int start, int end) {
        if (start >= points.size()) return List.of("结合本节实践任务补充一个自己的例子，并说明适用条件。");
        return points.subList(start, Math.min(end, points.size()));
    }

    private String shortLabel(String point) {
        String normalized = point.replaceFirst("^[^：]{1,10}：", "");
        int stop = normalized.length();
        for (String mark : List.of("，", "；", "。")) {
            int index = normalized.indexOf(mark);
            if (index >= 0) stop = Math.min(stop, index);
        }
        String label = normalized.substring(0, stop).trim();
        return label.length() <= 16 ? label : label.substring(0, 16) + "…";
    }
}
