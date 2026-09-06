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
                knowledgeAnalyses(course.getTitle(), chapter, keyPoints),
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

    private List<KnowledgeAnalysisView> knowledgeAnalyses(String courseTitle, Chapter chapter, List<String> points) {
        BeginnerLessonCatalog.Guide guide = BeginnerLessonCatalog.guideFor(chapter.getTitle());
        java.util.ArrayList<KnowledgeAnalysisView> analyses = new java.util.ArrayList<>();
        for (int index = 0; index < points.size(); index++) {
            String point = points.get(index);
            analyses.add(new KnowledgeAnalysisView(
                    "analysis-" + (index + 1), conceptTitle(point), category(index),
                    point, plainExplanation(courseTitle, index, point, guide), whyItMatters(courseTitle, index, point, guide),
                    diagramSteps(point), relatedExample(index, guide, chapter.getPracticeTask(), point),
                    commonMistake(courseTitle, index), checkQuestion(courseTitle, index, point)));
        }
        return analyses;
    }

    private String plainExplanation(String courseTitle, int index, String point, BeginnerLessonCatalog.Guide guide) {
        List<String> parts = clauses(point);
        String first = parts.isEmpty() ? point : parts.get(0);
        String rest = parts.size() < 2 ? "把它放进本章的最小示例中观察输入和结果。" : String.join("；", parts.subList(1, parts.size()));
        String learningAction = domainLearningAction(courseTitle);
        return switch (index % 3) {
            case 0 -> "先不用背名词。这里首先要认清的是“" + first + "”。" + learningAction + "，再观察：" + rest + "；能够指出它在案例中的具体位置，就完成了第一遍理解。";
            case 1 -> "这一点讲的是方法怎样运作。先借助生活类比建立直觉，再回到准确规则：" + first + "；" + rest + "。接着" + learningAction + "，逐步核对规则是否真的产生了示例结果。";
            default -> "这一点用来判断什么时候能用、什么时候会出错。核心观察是“" + first + "”，随后检查“" + rest + "”。请" + domainBoundaryAction(courseTitle) + "，不要只看最顺利的一次结果。";
        };
    }

    private String whyItMatters(String courseTitle, int index, String point, BeginnerLessonCatalog.Guide guide) {
        String consequence = switch (courseTitle) {
            case "数据结构与算法" -> "代码在几条数据上能够运行，换成大规模、重复值或极端结构后却变慢或出错";
            case "大学英语进阶" -> "会出现每个单词都认识，却抓不住句子功能、文章观点或听众真正需要的信息";
            case "Python 数据分析基础" -> "代码可能暂时跑通，但换一份数据就得到错误结果或直接报错";
            case "软件工程与 Git 协作" -> "团队成员会对需求、提交历史或交付状态产生不同理解";
            case "人工智能导论" -> "模型指标看起来很好，却可能学错目标、泄露答案或伤害特定群体";
            case "HTML 与 CSS 网页设计" -> "页面在当前屏幕看起来正常，换成手机、键盘操作或不同内容后就溢出或难以使用";
            case "JavaScript 网页交互" -> "按钮在顺利路径上能响应，遇到重复点击、空数据或请求失败时界面就失去同步";
            case "Vue 3 前端开发" -> "组件暂时显示正确，状态来源一多就出现数据不同步、职责混乱或难以复用";
            case "FastAPI 后端开发" -> "接口能返回示例数据，遇到非法参数、数据库失败或跨域访问时却无法提供稳定契约";
            case "数据库原理" -> "查询结果、数据约束或并发更新会在真实数据量下出现错误";
            case "计算机网络" -> "只会背协议名，却无法根据报文和现象定位通信失败的位置";
            case "Java Web 应用开发" -> "接口在正常请求下能运行，遇到无效输入、越权或依赖故障就失去控制";
            default -> "答案可能碰巧正确，但数据规模、顺序或边界一变化就会失效";
        };
        return switch (index % 3) {
            case 0 -> "它决定你能否看懂后面的步骤。若一开始认错处理对象，后续即使记住公式、句型或命令，也会用在错误的问题上。";
            case 1 -> "它解释结果为什么出现，而不是只给出答案。否则" + consequence + "。";
            default -> "真实任务不会只给最顺利的输入。把这个知识点放回本章案例检查，可以提前发现限制、代价和不适用条件。";
        };
    }

    private String commonMistake(String courseTitle, int index) {
        String[] mistakes = switch (courseTitle) {
            case "数据结构与算法" -> new String[]{
                    "只记住结构名称，没有画出一次插入、删除或遍历后的状态。修正：用五到八个元素逐步记录每次变化。",
                    "看到代码运行就认为算法正确，没有写循环不变量或复杂度。修正：说明每一步保持什么条件，并统计关键操作次数。",
                    "只测试随机正常输入。修正：补上空结构、重复值、有序或逆序数据以及极端规模中的至少两类。"};
            case "大学英语进阶" -> new String[]{
                    "逐词翻译后就停下，没有标出句子或段落在完成什么沟通任务。修正：圈出主旨句、连接词和证据，再用一句中文概括功能。",
                    "机械套用句型，却没有替换场景、对象和语气。修正：保留结构，至少替换两项信息并朗读检查是否自然。",
                    "只检查语法，不检查观点是否有证据、内容是否回应任务。修正：先验收意思与结构，最后再改语言。"};
            case "Python 数据分析基础" -> new String[]{
                    "只复制代码，没有观察变量类型、形状和中间结果。修正：每执行一步就打印一个最小结果。",
                    "示例数据能运行就认为完成，没有检查缺失值、空表或类型错误。修正：主动构造一个异常输入。",
                    "图表或数字已经生成，却没有核对单位、口径和数据行数。修正：把代码结果与手算的小样本比较。"};
            case "软件工程与 Git 协作" -> new String[]{
                    "只记命令，不画工作区、暂存区、提交和分支之间的变化。修正：每条命令后查看状态或图形历史。",
                    "冲突出现时直接选择一侧，丢掉另一侧意图。修正：先理解两边目标，再写出同时满足需求的最终版本。",
                    "流程做完却没有测试证据和可追踪说明。修正：让每个需求都能对应到提交、评审和测试结果。"};
            case "人工智能导论" -> new String[]{
                    "只看模型名称，没有先写清输入、输出和成功标准。修正：先用一条具体样本描述完整任务。",
                    "把训练集上的高分当作真实能力。修正：保留独立测试集，并检查数据泄漏。",
                    "只看平均指标，忽略失败样本与群体差异。修正：分类型、分人群查看错误。"};
            case "数据库原理" -> new String[]{
                    "只画一张表，没有标主键、外键和业务约束。修正：为每个编号写清唯一性和引用方向。",
                    "SQL 能返回结果就认为正确，未检查重复行、NULL 和连接范围。修正：用三到五行小数据手算预期结果。",
                    "只测单用户正常操作，忽略事务中断和并发更新。修正：加入失败回滚与两个会话的实验。"};
            case "计算机网络" -> new String[]{
                    "把协议名称背成清单，却说不出它处理的数据单元和所在位置。修正：沿一次网页访问逐层标注。",
                    "把相邻设备通信和跨网络转发混在一起。修正：分别画 MAC 帧的下一跳与 IP 包的最终目的。",
                    "只看成功抓包，不分析超时、丢包和错误响应。修正：人为制造一个失败并定位发生层次。"};
            case "Java Web 应用开发" -> new String[]{
                    "只看注解名称，没有沿一次请求追踪到控制器、服务和数据库。修正：画出每层输入与输出。",
                    "接口正常返回 200 就认为完成。修正：补测无效参数、未登录、越权、404 和依赖失败。",
                    "把前端隐藏按钮当成权限控制。修正：在后端为每个受保护请求重新验证身份和资源归属。"};
            case "HTML 与 CSS 网页设计" -> new String[]{
                    "只按视觉大小选择 HTML 标签，忽略内容结构。修正：先写标题、导航、正文和表单的语义，再用 CSS 调整外观。",
                    "靠不断增加优先级解决样式冲突。修正：在开发者工具中找出命中规则，缩短选择器并统一组件边界。",
                    "只在自己的电脑宽度检查页面。修正：至少验证 320px、平板和桌面宽度，以及键盘焦点和文字放大。"};
            case "JavaScript 网页交互" -> new String[]{
                    "修改了数据却没有统一更新界面状态。修正：明确状态来源，并让渲染逻辑只根据这份状态执行。",
                    "只处理请求成功。修正：同时实现 loading、empty、error、success，并防止重复提交。",
                    "把用户输入直接交给 innerHTML。修正：普通文字使用 textContent，确需 HTML 时先进行可信的清洗与约束。"};
            case "Vue 3 前端开发" -> new String[]{
                    "直接修改 Props 或维护两份相同状态。修正：保持单向数据流，由拥有状态的组件负责更新。",
                    "把所有逻辑都放进一个组件。修正：按独立职责拆分组件和组合函数，并写清输入输出。",
                    "只验证首次渲染。修正：补测路由切换、接口等待与失败、列表增删以及组件卸载后的清理。"};
            case "FastAPI 后端开发" -> new String[]{
                    "路由函数直接完成校验、业务和数据库操作。修正：分开请求模型、服务和数据访问职责。",
                    "只看自动文档能否调用成功。修正：测试非法参数、资源不存在、冲突、未授权和数据库回滚。",
                    "返回 ORM 对象却没有明确响应模型。修正：用 Pydantic 固定对外字段，避免内部数据意外暴露。"};
            default -> new String[]{
                    "只背结论，没有在一组具体数据上演示变化。修正：把输入、每一步处理和输出写在同一张纸上。",
                    "直接套步骤，却没有检查数据是否满足前提。修正：每一步都写一句“因为……所以……”。",
                    "只测试正常数据。修正：再加入空数据、重复值、极端规模或失败操作中的至少一种。"};
        };
        return mistakes[index % 3];
    }

    private String domainLearningAction(String courseTitle) {
        return switch (courseTitle) {
            case "数据结构与算法" -> "在一组小数据上画出结构变化并统计关键操作次数";
            case "大学英语进阶" -> "直接在示例句或段落中圈出关键词与功能句";
            case "Python 数据分析基础" -> "在解释器中运行最小代码并查看变量的实际值";
            case "软件工程与 Git 协作" -> "画出提交、分支或流水线在操作前后的变化";
            case "人工智能导论" -> "先写清输入、模型处理和输出，再放入一条具体样本";
            case "数据库原理" -> "用三到五行小表格演示查询或更新前后的变化";
            case "计算机网络" -> "沿一条真实报文标出它经过的设备、协议和字段";
            case "Java Web 应用开发" -> "沿一次 HTTP 请求追踪控制器、服务与数据层";
            case "HTML 与 CSS 网页设计" -> "在浏览器开发者工具中核对元素结构、命中样式与盒模型";
            case "JavaScript 网页交互" -> "在控制台逐步查看事件、状态与 DOM 更新前后的值";
            case "Vue 3 前端开发" -> "从响应式状态开始追踪组件渲染、Props 与事件流向";
            case "FastAPI 后端开发" -> "用一条请求核对路径参数、请求模型、业务处理与响应正文";
            default -> "把示例数据逐项写出来并画出变化前后的状态";
        };
    }

    private String domainBoundaryAction(String courseTitle) {
        return switch (courseTitle) {
            case "数据结构与算法" -> "再测试空结构、重复值、极端形态或更大数据规模中的一种";
            case "大学英语进阶" -> "换一个主题或听众重写示例，并检查表达是否仍自然";
            case "Python 数据分析基础" -> "再运行空数据、缺失值或错误类型中的一种";
            case "软件工程与 Git 协作" -> "模拟一次需求变化、合并冲突或流水线失败";
            case "人工智能导论" -> "检查一条失败样本和一个群体差异";
            case "数据库原理" -> "加入重复值、NULL、事务失败或并发操作中的一种";
            case "计算机网络" -> "比较一次成功报文与一次超时或错误报文";
            case "Java Web 应用开发" -> "补测无效输入、未登录、越权或依赖故障中的一种";
            case "HTML 与 CSS 网页设计" -> "切换到窄屏、键盘操作或更长内容，检查结构是否仍然可用";
            case "JavaScript 网页交互" -> "加入重复点击、空数据、慢请求或失败响应中的一种";
            case "Vue 3 前端开发" -> "切换路由、改变父组件输入或让接口失败，检查状态是否仍一致";
            case "FastAPI 后端开发" -> "补测错误类型、缺少字段、资源不存在或数据库异常中的一种";
            default -> "换一组数据，并加入空值、重复值或极端规模中的一种";
        };
    }

    private String checkQuestion(String courseTitle, int index, String point) {
        return switch (index % 3) {
            case 0 -> "你能不看原文，用本课程中的一个具体对象解释“" + conceptTitle(point) + "”吗？";
            case 1 -> "如果把案例中的数据换一组，你能按图中的顺序重新做一遍吗？";
            default -> "你能按本课程的验收方式，举出一个不适用或容易出错的情况吗？";
        };
    }

    private List<DiagramStepView> diagramSteps(String point) {
        List<String> parts = clauses(point);
        if (parts.size() == 1) {
            return List.of(
                    new DiagramStepView("核心概念", conceptTitle(point)),
                    new DiagramStepView("准确结论", point));
        }
        String[] labels = switch (parts.size()) {
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

    private String relatedExample(int index, BeginnerLessonCatalog.Guide guide, String practiceTask, String point) {
        String focus = conceptTitle(point);
        return switch (index % 3) {
            case 0 -> guide.analogy() + " 对应知识点：“" + focus + "”。";
            case 1 -> guide.example() + " 这里重点观察：“" + focus + "”。";
            default -> practiceTask + " 完成时必须用“" + focus + "”核对结果。";
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
