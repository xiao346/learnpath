package com.learnpath.course;

import com.learnpath.course.CourseDtos.ChapterView;
import com.learnpath.course.CourseDtos.ChapterLessonView;
import com.learnpath.course.CourseDtos.CourseDetail;
import com.learnpath.course.CourseDtos.CourseSummary;
import com.learnpath.course.CourseDtos.ProgressView;
import com.learnpath.course.CourseDtos.ResourceView;
import com.learnpath.course.CourseDtos.DiagramStepView;
import com.learnpath.course.CourseDtos.KnowledgeAnalysisView;
import com.learnpath.course.CourseDtos.LearningStepView;
import com.learnpath.course.CourseDtos.StudySectionView;
import com.learnpath.course.CourseDtos.WorkedExampleStepView;
import com.learnpath.course.CourseDtos.WorkedExampleView;
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
                chapter.getOverview(), chapter.getBeginnerIntro(), chapter.getBeginnerAnalogy(),
                lines(chapter.getBeginnerWalkthrough()), lines(chapter.getObjectives()), keyPoints,
                workedExample(chapter, keyPoints), learningPath(keyPoints),
                knowledgeAnalyses(chapter, keyPoints),
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

    private WorkedExampleView workedExample(Chapter chapter, List<String> points) {
        BeginnerLessonCatalog.Guide guide = BeginnerLessonCatalog.guideFor(chapter.getTitle());
        List<String> exampleParts = exampleParts(guide.example());
        return new WorkedExampleView(
                chapter.getTitle() + "完整跟做示例",
                guide.example(),
                List.of(
                        new WorkedExampleStepView(
                                "题目目标",
                                examplePart(exampleParts, 0, guide.intro()),
                                "先明确处理对象与目标。对应知识是：“" + pointAt(points, 0) + "”"),
                        new WorkedExampleStepView(
                                "实际操作 1",
                                examplePart(exampleParts, 1, guide.example()),
                                "这一操作为什么成立：" + pointAt(points, 1)),
                        new WorkedExampleStepView(
                                "实际操作 2",
                                examplePart(exampleParts, 2, "继续按同一规则处理，并把每一步的变化写下来。"),
                                "做这一步时要检查：" + pointAt(points, 2)),
                        new WorkedExampleStepView(
                                "比较结果",
                                examplePart(exampleParts, 3, "比较两种做法的步骤、结果或代价，写出哪一种更适合当前条件。"),
                                "不要只看答案，还要说明判断依据：" + pointAt(points, 3))),
                "本例最终要说明：“" + chapter.getBeginnerIntro() + "”现在应当能从例子中的每一步说出原因。",
                "变式示例（带提示）：" + chapter.getPracticeTask() + " 提示：先照上面四步写出目标、操作、结果，再用“"
                        + conceptTitle(pointAt(points, 5)) + "”检查边界。");
    }

    private List<String> exampleParts(String example) {
        return java.util.Arrays.stream(example.split("[：；。]"))
                .map(String::trim)
                .filter(part -> !part.isBlank())
                .toList();
    }

    private String examplePart(List<String> parts, int index, String fallback) {
        return index < parts.size() ? parts.get(index) : fallback;
    }

    private List<LearningStepView> learningPath(List<String> points) {
        String[] stages = {"先认识", "再理解", "会分析", "看进阶", "能迁移", "辨边界"};
        java.util.ArrayList<LearningStepView> path = new java.util.ArrayList<>();
        for (int index = 0; index < points.size(); index++) {
            String point = points.get(index);
            path.add(new LearningStepView(
                    "path-" + (index + 1), stages[Math.min(index, stages.length - 1)],
                    conceptTitle(point), point));
        }
        return path;
    }

    private List<KnowledgeAnalysisView> knowledgeAnalyses(Chapter chapter, List<String> points) {
        BeginnerLessonCatalog.Guide guide = BeginnerLessonCatalog.guideFor(chapter.getTitle());
        java.util.ArrayList<KnowledgeAnalysisView> analyses = new java.util.ArrayList<>();
        for (int index = 0; index < points.size(); index++) {
            String point = points.get(index);
            analyses.add(new KnowledgeAnalysisView(
                    "analysis-" + (index + 1), conceptTitle(point), category(index),
                    plainExplanation(point), whyItMatters(index), diagramSteps(point),
                    relatedExample(index, guide, chapter.getPracticeTask()),
                    commonMistake(point, index), quickCheck(point, index)));
        }
        return analyses;
    }

    private List<DiagramStepView> diagramSteps(String point) {
        List<String> parts = clauses(point);
        String[] labels = switch (parts.size()) {
            case 1 -> new String[]{"核心结论"};
            case 2 -> new String[]{"前提 / 对象", "规则 / 结果"};
            case 3 -> new String[]{"先看对象", "再看规则", "最后判断"};
            default -> new String[]{"对象", "条件", "处理", "边界"};
        };
        java.util.ArrayList<DiagramStepView> steps = new java.util.ArrayList<>();
        for (int index = 0; index < parts.size(); index++) {
            steps.add(new DiagramStepView(labels[Math.min(index, labels.length - 1)], parts.get(index)));
        }
        return steps;
    }

    private List<String> clauses(String point) {
        return java.util.Arrays.stream(point.split("[，；。]"))
                .map(String::trim)
                .filter(part -> !part.isBlank())
                .limit(4)
                .toList();
    }

    private String plainExplanation(String point) {
        List<String> parts = clauses(point);
        if (parts.size() == 1) {
            return "核心结论是：“" + parts.get(0) + "”。理解时要能指出它处理的对象、采用的动作以及得到的结果。";
        }
        String rest = String.join("；", parts.subList(1, parts.size()));
        return "这条知识分两层：先记住核心结论“" + parts.get(0) + "”；再理解它的条件、过程或边界——“" + rest
                + "”。两层必须一起使用，不能只背前半句。";
    }

    private String whyItMatters(int index) {
        return switch (index % 3) {
            case 0 -> "它负责回答“本章究竟在讨论什么”。这个概念没分清，后面的规则、步骤和例子都会混在一起。";
            case 1 -> "它负责回答“具体应该怎样做、为什么这样做”。做题或写代码时，这通常就是选择处理方法的依据。";
            default -> "它负责回答“什么时候可以用、什么时候会出错”。初学者最容易漏掉这一层，导致会背结论却不会判断场景。";
        };
    }

    private String relatedExample(int index, BeginnerLessonCatalog.Guide guide, String practiceTask) {
        return switch (index % 3) {
            case 0 -> "生活中的对应：" + guide.analogy();
            case 1 -> "放进完整案例：" + guide.example();
            default -> "换到真实任务：" + practiceTask;
        };
    }

    private String commonMistake(String point, int index) {
        String title = conceptTitle(point);
        if (point.matches(".*(不|不能|必须|只有|要求|适合|但|警惕).*")) {
            return "不要忽略原句里的限制词。“" + title + "”不是在任何条件下都成立，使用前要逐项核对前提和边界。";
        }
        return switch (index % 3) {
            case 0 -> "只记住“" + title + "”这个名称，却不能指出它对应的对象和作用。请回到上面的生活例子逐一对应。";
            case 1 -> "看到熟悉题目就直接套方法，却没有写出中间步骤。结果即使碰巧正确，也无法判断规则是否真正用对。";
            default -> "只验证正常情况，不检查空值、极端输入或条件变化。边界一变，原来的结论可能立即失效。";
        };
    }

    private String quickCheck(String point, int index) {
        String title = conceptTitle(point);
        return switch (index % 3) {
            case 0 -> "不用看原文，用一句话说明“" + title + "”是什么，并举出一个属于它的对象。";
            case 1 -> "如果让你向同学演示“" + title + "”，第一步、第二步和最终结果分别是什么？";
            default -> "给“" + title + "”换一个条件：它还成立吗？说出判断依据，而不是只回答“是”或“否”。";
        };
    }

    private String conceptTitle(String point) {
        List<String> parts = clauses(point);
        return parts.isEmpty() ? point : parts.get(0);
    }

    private String pointAt(List<String> points, int index) {
        return index < points.size() ? points.get(index) : "回到案例，写出这一阶段的输入、处理和结果。";
    }

    private String category(int index) {
        return switch (index % 3) {
            case 0 -> "概念与定义";
            case 1 -> "原理与方法";
            default -> "场景与边界";
        };
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
