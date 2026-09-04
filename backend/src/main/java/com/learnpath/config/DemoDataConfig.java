package com.learnpath.config;

import com.learnpath.course.Course;
import com.learnpath.course.ChapterContentCatalog;
import com.learnpath.course.CourseRepository;
import com.learnpath.course.CourseResource;
import com.learnpath.course.CourseResourceRepository;
import com.learnpath.course.LearningProgress;
import com.learnpath.course.LearningProgressRepository;
import com.learnpath.dashboard.StudySession;
import com.learnpath.dashboard.StudySessionRepository;
import com.learnpath.dashboard.StudyTask;
import com.learnpath.dashboard.StudyTaskRepository;
import com.learnpath.practice.PracticeQuestion;
import com.learnpath.practice.ExpandedQuestionCatalog;
import com.learnpath.practice.PracticeQuestionRepository;
import com.learnpath.user.User;
import com.learnpath.user.UserRepository;
import com.learnpath.user.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Configuration
public class DemoDataConfig {

    @Bean
    CommandLineRunner seedDemoData(
            UserRepository userRepository,
            CourseRepository courseRepository,
            CourseResourceRepository resourceRepository,
            LearningProgressRepository progressRepository,
            PracticeQuestionRepository questionRepository,
            StudyTaskRepository taskRepository,
            StudySessionRepository sessionRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new User("20240001", passwordEncoder.encode("123456"), "林知夏", UserRole.STUDENT));
                userRepository.save(new User("T10001", passwordEncoder.encode("123456"), "陈老师", UserRole.TEACHER));
                userRepository.save(new User("admin", passwordEncoder.encode("123456"), "系统管理员", UserRole.ADMIN));
            }

            if (courseRepository.count() == 0) {
                Course dataStructure = new Course(
                        "数据结构与算法", "从线性表到图算法，建立计算思维", "计算机基础", "陈老师",
                        "通过可视化案例和逐步练习掌握数据结构的核心概念，能够分析常见算法的时间与空间复杂度。",
                        "进阶", 420, "violet", "⌘")
                        .addChapter("算法与复杂度入门", 1, 36)
                        .addChapter("线性表与顺序存储", 2, 42)
                        .addChapter("栈、队列与应用", 3, 48)
                        .addChapter("二叉树与遍历算法", 4, 55)
                        .addChapter("图的表示与遍历", 5, 58)
                        .addChapter("查找算法", 6, 49)
                        .addChapter("排序算法", 7, 62)
                        .addChapter("综合实践与复盘", 8, 70);

                Course database = new Course(
                        "数据库原理", "理解数据模型，设计可靠的信息系统", "计算机基础", "周老师",
                        "从关系模型和 SQL 出发，完成数据库设计、事务控制、索引优化与综合课程实践。",
                        "中级", 360, "blue", "▦")
                        .addChapter("数据库系统概述", 1, 38)
                        .addChapter("关系模型与关系代数", 2, 52)
                        .addChapter("SQL 数据查询", 3, 58)
                        .addChapter("数据库完整性", 4, 42)
                        .addChapter("关系数据库设计", 5, 61)
                        .addChapter("事务与并发控制", 6, 55)
                        .addChapter("索引优化与课程实践", 7, 54);

                Course javaWeb = new Course(
                        "Java Web 应用开发", "用 Spring Boot 构建现代 Web 服务", "专业核心", "李老师",
                        "围绕真实项目学习 REST API、分层架构、数据访问、安全认证和应用部署。",
                        "进阶", 510, "cyan", "</>")
                        .addChapter("现代 Web 开发概览", 1, 35)
                        .addChapter("Spring Boot 快速入门", 2, 48)
                        .addChapter("RESTful API 设计", 3, 55)
                        .addChapter("数据持久化", 4, 62)
                        .addChapter("参数校验与异常处理", 5, 51)
                        .addChapter("身份认证与权限控制", 6, 69)
                        .addChapter("Redis 缓存实践", 7, 58)
                        .addChapter("接口测试", 8, 54)
                        .addChapter("部署与综合实战", 9, 78);

                Course network = new Course(
                        "计算机网络", "从数据传输到互联网应用", "计算机基础", "王老师",
                        "以分层模型为主线理解网络协议、路由交换、传输控制及常用应用层协议。",
                        "中级", 400, "indigo", "⌁")
                        .addChapter("网络体系结构", 1, 42)
                        .addChapter("物理层基础", 2, 45)
                        .addChapter("数据链路层", 3, 52)
                        .addChapter("网络层与 IP", 4, 62)
                        .addChapter("路由选择", 5, 56)
                        .addChapter("TCP 与 UDP", 6, 58)
                        .addChapter("应用层协议", 7, 48)
                        .addChapter("网络安全基础", 8, 37);

                Course english = new Course(
                        "大学英语进阶", "在真实语境中提升表达与阅读", "通识课程", "Sarah",
                        "通过主题阅读、学术词汇与情境表达训练，提升综合语言应用能力。",
                        "中级", 300, "pink", "A+")
                        .addChapter("Campus Life", 1, 42)
                        .addChapter("Technology and Society", 2, 48)
                        .addChapter("Academic Reading", 3, 55)
                        .addChapter("Presentation Skills", 4, 50)
                        .addChapter("Critical Writing", 5, 58)
                        .addChapter("Final Project", 6, 47);

                Course ai = new Course(
                        "人工智能导论", "认识机器学习与智能系统", "前沿拓展", "赵老师",
                        "用直观案例了解人工智能的发展、搜索方法、机器学习基础和负责任的 AI。",
                        "入门", 330, "orange", "AI")
                        .addChapter("人工智能的过去与现在", 1, 38)
                        .addChapter("问题求解与搜索", 2, 46)
                        .addChapter("知识表示", 3, 42)
                        .addChapter("机器学习基础", 4, 58)
                        .addChapter("神经网络初步", 5, 55)
                        .addChapter("自然语言处理", 6, 47)
                        .addChapter("负责任的人工智能", 7, 44);

                courseRepository.save(dataStructure);
                courseRepository.save(database);
                courseRepository.save(javaWeb);
                courseRepository.save(network);
                courseRepository.save(english);
                courseRepository.save(ai);

                User student = userRepository.findByAccountAndRole("20240001", UserRole.STUDENT).orElseThrow();
                progressRepository.save(new LearningProgress(student.getId(), dataStructure.getId(), 3));
                progressRepository.save(new LearningProgress(student.getId(), database.getId(), 2));
                progressRepository.save(new LearningProgress(student.getId(), english.getId(), 4));
                progressRepository.save(new LearningProgress(student.getId(), ai.getId(), 1));
            }

            Course python = courseRepository.findByTitle("Python 数据分析基础").orElseGet(() -> courseRepository.save(
                    new Course("Python 数据分析基础", "用 Python 处理、分析与呈现数据", "专业拓展", "刘老师",
                            "从 Python 语法出发，学习数据清洗、统计分析和可视化，完成一个可复现的数据分析项目。",
                            "入门", 345, "green", "Py")
                            .addChapter("Python 与开发环境", 1, 35)
                            .addChapter("数据类型与控制流程", 2, 44)
                            .addChapter("函数与模块", 3, 46)
                            .addChapter("NumPy 数组计算", 4, 52)
                            .addChapter("Pandas 数据处理", 5, 61)
                            .addChapter("数据可视化", 6, 49)
                            .addChapter("综合分析项目", 7, 58)));

            Course git = courseRepository.findByTitle("软件工程与 Git 协作").orElseGet(() -> courseRepository.save(
                    new Course("软件工程与 Git 协作", "把规范开发流程应用到团队项目", "专业核心", "孙老师",
                            "围绕需求、分支、评审、测试与交付，掌握可追溯的软件工程协作方法。",
                            "中级", 315, "teal", "Git")
                            .addChapter("软件过程与需求", 1, 44)
                            .addChapter("Git 核心模型", 2, 48)
                            .addChapter("分支与合并", 3, 55)
                            .addChapter("代码评审与冲突处理", 4, 58)
                            .addChapter("自动化测试与持续集成", 5, 57)
                            .addChapter("版本发布实践", 6, 53)));

            seedChapterContent(courseRepository);
            synchronizeChineseResources(courseRepository, resourceRepository);

            if (questionRepository.count() == 0) {
                questionRepository.save(new PracticeQuestion(
                        "数据结构", "在二叉树的前序遍历中，节点的访问顺序是？",
                        "左子树 → 根节点 → 右子树", "根节点 → 左子树 → 右子树",
                        "左子树 → 右子树 → 根节点", "根节点 → 右子树 → 左子树",
                        "B", "前序遍历遵循“根—左—右”的顺序，先访问根节点，再递归遍历左右子树。", "基础", 10));
                questionRepository.save(new PracticeQuestion(
                        "数据库", "下列哪一项最能体现数据库事务的原子性？",
                        "事务中的操作全部成功或全部回滚", "多个事务可以同时执行",
                        "提交后的数据不会丢失", "事务只能看到已提交的数据",
                        "A", "原子性保证事务是不可分割的工作单元，其中的操作要么全部完成，要么全部不发生。", "基础", 10));
                questionRepository.save(new PracticeQuestion(
                        "Java Web", "RESTful API 中，更新一个已知资源的完整表示通常使用哪个 HTTP 方法？",
                        "GET", "POST", "PUT", "DELETE",
                        "C", "PUT 通常用于以请求中的完整表示创建或替换指定 URI 对应的资源。", "基础", 10));
                questionRepository.save(new PracticeQuestion(
                        "计算机网络", "TCP 建立连接时使用的经典过程是？",
                        "一次握手", "两次握手", "三次握手", "四次握手",
                        "C", "TCP 通过 SYN、SYN-ACK、ACK 三个步骤确认双方收发能力并建立连接。", "基础", 10));
                questionRepository.save(new PracticeQuestion(
                        "数据结构", "使用邻接表表示含 V 个顶点、E 条边的图，遍历全部邻接关系的时间复杂度通常是？",
                        "O(V)", "O(E)", "O(V + E)", "O(V × E)",
                        "C", "需要访问每个顶点并扫描每条边，因此邻接表遍历的时间复杂度为 O(V + E)。", "进阶", 15));
                questionRepository.save(new PracticeQuestion(
                        "人工智能", "监督学习与无监督学习最核心的区别是？",
                        "是否使用神经网络", "训练数据是否包含目标标签",
                        "是否需要大量数据", "模型是否能够上线部署",
                        "B", "监督学习使用带标签样本学习输入到目标的映射，无监督学习则从无标签数据中发现结构。", "基础", 10));
            }

            seedAdditionalQuestions(questionRepository);
            seedExpandedQuestions(questionRepository);

            User student = userRepository.findByAccountAndRole("20240001", UserRole.STUDENT).orElseThrow();
            seedDashboardData(student.getId(), taskRepository, sessionRepository);
        };
    }

    private static void seedChapterContent(CourseRepository courses) {
        courses.findAllByPublishedTrueOrderByIdAsc().forEach(course -> {
            course.getChapters().forEach(chapter -> {
                ChapterContentCatalog.Content content = ChapterContentCatalog.contentFor(chapter.getTitle());
                chapter.updateLesson(content.overview(), content.objectivesText(), content.keyPointsText(), content.practiceTask());
            });
            courses.save(course);
        });
    }

    private static void synchronizeChineseResources(CourseRepository courses, CourseResourceRepository resources) {
        courses.findAllByPublishedTrueOrderByIdAsc().forEach(course -> {
            resources.deleteAll(resources.findByCourseIdOrderBySortOrderAsc(course.getId()));
            List<ResourceSeed> seeds = chineseResources(course.getTitle());
            for (int index = 0; index < seeds.size(); index++) {
                ResourceSeed seed = seeds.get(index);
                resources.save(new CourseResource(course.getId(), seed.title(), seed.provider(), seed.type(),
                        seed.description(), seed.url(), index + 1));
            }
        });
    }

    private static List<ResourceSeed> chineseResources(String courseTitle) {
        return switch (courseTitle) {
            case "数据结构与算法" -> List.of(
                    resource("Hello 算法中文教程", "Hello 算法", "中文教材", "动画图解配合可运行代码，系统学习复杂度、数组、链表、树、图、查找和排序。", "https://www.hello-algo.com/"),
                    resource("OI Wiki 数据结构", "OI Wiki", "中文手册", "由中文社区维护的数据结构与算法知识库，适合按章节查漏补缺。", "https://oi-wiki.org/ds/"),
                    resource("VisuAlgo 中文可视化", "VisuAlgo", "交互演示", "通过中文界面逐步观察排序、树和图算法中每一次状态变化。", "https://visualgo.net/zh"),
                    resource("LeetCode 学习计划", "力扣中国", "在线练习", "按学习计划完成数组、链表、二叉树和图等专题练习，并获得即时判题。", "https://leetcode.cn/studyplan/"),
                    resource("洛谷题单广场", "洛谷", "题单练习", "使用中文题面和在线评测巩固算法实现，建议从官方精选题单逐级训练。", "https://www.luogu.com.cn/training/list"));
            case "数据库原理" -> List.of(
                    resource("MySQL 中文教程", "菜鸟教程", "中文教程", "从数据库创建、SQL 查询到事务、索引与管理的中文入门教程。", "https://www.runoob.com/mysql/mysql-tutorial.html"),
                    resource("SQL 中文教程", "W3School 中文网", "中文教程", "用短小示例学习 SELECT、连接、聚合、约束和数据修改语句。", "https://www.w3school.com.cn/sql/index.asp"),
                    resource("SQLZoo 中文练习", "SQLZoo", "在线练习", "在浏览器中按中文题目直接编写 SQL，覆盖查询、连接、聚合和子查询。", "https://sqlzoo.net/wiki/SQL_Tutorial/zh"),
                    resource("SQL 教程", "廖雪峰的官方网站", "中文课程", "从关系模型开始，用连续实例讲解查询、事务和数据库设计。", "https://liaoxuefeng.com/books/sql/introduction/index.html"),
                    resource("MySQL 索引与优化", "小林 coding", "图解专题", "通过中文图解理解 B+ 树、索引失效、执行计划和常见优化方法。", "https://xiaolincoding.com/mysql/"));
            case "Java Web 应用开发" -> List.of(
                    resource("Spring Boot 中文文档", "Spring 中文文档", "中文文档", "完整覆盖自动配置、Web、数据访问、测试、部署和生产特性。", "https://springdoc.cn/spring-boot/"),
                    resource("Spring Boot 中文教程", "Spring 中文文档", "项目教程", "从创建工程到开发 REST 接口，适合跟随课程逐步搭建项目。", "https://springdoc.cn/spring-boot-and-spring-data-jpa/"),
                    resource("Spring Data JPA 中文文档", "Spring 中文文档", "中文文档", "学习实体映射、Repository 查询、事务、分页和审计。", "https://springdoc.cn/spring-data-jpa/"),
                    resource("HTTP 中文指南", "MDN Web Docs", "中文指南", "系统理解 HTTP 消息、方法、状态码、缓存、认证与连接管理。", "https://developer.mozilla.org/zh-CN/docs/Web/HTTP"),
                    resource("Spring Security 中文文档", "Spring 中文文档", "安全专题", "学习认证、授权、密码存储、过滤器链和 Web 安全配置。", "https://springdoc.cn/spring-security/"));
            case "计算机网络" -> List.of(
                    resource("图解网络", "小林 coding", "中文图解", "用大量中文图解串联 TCP/IP、HTTP、网络层、传输层与抓包分析。", "https://xiaolincoding.com/network/"),
                    resource("HTTP 中文指南", "MDN Web Docs", "中文指南", "从请求响应、连接管理到缓存和认证，理解 Web 通信全过程。", "https://developer.mozilla.org/zh-CN/docs/Web/HTTP"),
                    resource("TCP/IP 中文教程", "菜鸟教程", "中文教程", "按网络模型、IP、TCP、UDP、DNS 等主题学习基础协议。", "https://www.runoob.com/tcpip/tcpip-tutorial.html"),
                    resource("网络指北图解教程", "编程指北", "中文图解", "用手绘图和中文讲解掌握网络分层、TCP、UDP、HTTP、IP 与实战排障。", "https://csguide.cn/network/"),
                    resource("Wireshark 中文教程", "Wireshark 中文站", "实验手册", "学习抓包、显示过滤器、TCP 会话追踪和常见协议字段分析。", "https://www.wireshark.org.cn/"));
            case "大学英语进阶" -> List.of(
                    resource("学术英语课程", "U校园·UMOOCs", "中文课程", "中文平台上的大学学术英语课程，包含主题单元、阅读、听说与写作任务。", "https://moocs.unipus.cn/course/7971"),
                    resource("中国大学 MOOC 外语课程", "中国大学 MOOC", "中文课程", "汇集高校大学英语、学术英语和英语写作课程，可按目标选择完整课程。", "https://www.icourse163.org/channel/2001.htm"),
                    resource("英语点津", "中国日报网", "双语阅读", "用中文讲解时事英语、词汇表达和文化背景，适合积累真实语料。", "https://language.chinadaily.com.cn/"),
                    resource("可可英语学习网", "可可英语", "听读训练", "提供中文导航的听力、口语、阅读和词汇训练材料。", "https://www.kekenet.com/"),
                    resource("沪江英语", "沪江网校", "学习专题", "中文讲解英语语法、写作、听力和考试技巧，适合按薄弱点专项学习。", "https://www.hjenglish.com/"));
            case "人工智能导论" -> List.of(
                    resource("机器学习速成课程中文版", "Google for Developers", "中文课程", "用中文模块、可视化和练习掌握机器学习核心概念。", "https://developers.google.com/machine-learning/crash-course?hl=zh-cn"),
                    resource("机器学习简介中文版", "Google for Developers", "中文入门", "解释监督学习、无监督学习、模型训练与评估的基本框架。", "https://developers.google.com/machine-learning/intro-to-ml?hl=zh-cn"),
                    resource("TensorFlow 中文学习中心", "TensorFlow", "中文课程", "面向初学者和进阶学习者的中文教程、模型与学习路径。", "https://tensorflow.google.cn/learn?hl=zh-cn"),
                    resource("TensorFlow 中文教程", "TensorFlow", "代码教程", "可直接在 Colab 运行的分类、回归、神经网络和文本处理中文教程。", "https://tensorflow.google.cn/tutorials?hl=zh-cn"),
                    resource("负责任的 AI 中文指南", "Google for Developers", "责任实践", "学习公平性、透明度、隐私、安全和问责等 AI 风险控制原则。", "https://developers.google.com/machine-learning/responsible-ai?hl=zh-cn"));
            case "Python 数据分析基础" -> List.of(
                    resource("Python 官方中文教程", "Python 软件基金会", "官方中文", "覆盖语法、控制流程、数据结构、函数、模块、异常与类。", "https://docs.python.org/zh-cn/3/tutorial/"),
                    resource("Python 标准库中文版", "Python 软件基金会", "官方中文", "按模块查阅文件、日期、数据结构、并发与网络等标准能力。", "https://docs.python.org/zh-cn/3/library/"),
                    resource("Python HOWTO 中文专题", "Python 软件基金会", "官方中文", "通过正则、日志、函数式编程等专题指南深化实战能力。", "https://docs.python.org/zh-cn/3/howto/"),
                    resource("NumPy 中文网", "NumPy 中文社区", "中文教程", "通过中文示例学习数组、索引、广播、统计和线性代数。", "https://www.numpy.org.cn/"),
                    resource("Pandas 中文教程", "Pandas 中文社区", "中文教程", "围绕 DataFrame 完成读取、清洗、筛选、连接、分组和时间序列处理。", "https://www.pypandas.cn/"));
            case "软件工程与 Git 协作" -> List.of(
                    resource("Pro Git 中文版", "Git SCM", "中文图书", "完整学习 Git 基础、分支、远程协作、工具与内部原理。", "https://git-scm.com/book/zh/v2"),
                    resource("GitHub 入门中文教程", "GitHub Docs", "官方中文", "用官方中文教程完成仓库、分支、提交和拉取请求基础操作。", "https://docs.github.com/zh/get-started/using-github/hello-world"),
                    resource("GitHub Flow 中文指南", "GitHub Docs", "协作规范", "按分支、提交、拉取请求、评审和合并的轻量流程协作。", "https://docs.github.com/zh/get-started/using-github/github-flow"),
                    resource("GitHub Actions 中文入门", "GitHub Docs", "持续集成", "创建自动构建与测试工作流，理解事件、作业、步骤和运行器。", "https://docs.github.com/zh/actions/get-started/quickstart"),
                    resource("约定式提交中文版", "Conventional Commits", "提交规范", "使用 feat、fix 等结构化提交信息建立清晰可追踪的版本历史。", "https://www.conventionalcommits.org/zh-hans/v1.0.0/"));
            default -> throw new IllegalArgumentException("缺少中文资源清单：" + courseTitle);
        };
    }

    private static ResourceSeed resource(String title, String provider, String type, String description, String url) {
        return new ResourceSeed(title, provider, type, description, url);
    }

    private record ResourceSeed(String title, String provider, String type, String description, String url) {
    }

    private static void seedAdditionalQuestions(PracticeQuestionRepository questions) {
        addQuestion(questions, new PracticeQuestion("数据库", "为经常出现在 WHERE 条件中的列建立索引，主要目的是？",
                "减少磁盘空间", "加快数据检索", "自动消除重复数据", "保证事务原子性",
                "B", "索引提供额外的数据访问路径，能减少查询需要扫描的数据量。", "基础", 10));
        addQuestion(questions, new PracticeQuestion("数据库", "事务隔离级别中，能够避免脏读的最低级别是？",
                "READ UNCOMMITTED", "READ COMMITTED", "REPEATABLE READ", "SERIALIZABLE",
                "B", "READ COMMITTED 只允许读取其他事务已提交的数据，因此可以避免脏读。", "进阶", 15));
        addQuestion(questions, new PracticeQuestion("Java Web", "HTTP 状态码 201 表示什么？",
                "请求成功且创建了资源", "请求无内容返回", "客户端未授权", "服务器内部错误",
                "A", "201 Created 表示请求已成功，并且通常创建了一个新资源。", "基础", 10));
        addQuestion(questions, new PracticeQuestion("Java Web", "Spring 中常用哪个注解把类声明为 REST 控制器？",
                "@Service", "@Repository", "@RestController", "@Configuration",
                "C", "@RestController 组合了控制器语义与响应体序列化能力。", "基础", 10));
        addQuestion(questions, new PracticeQuestion("计算机网络", "负责把域名解析为 IP 地址的应用层协议是？",
                "DNS", "DHCP", "SMTP", "FTP", "A", "DNS 将易读的域名映射到网络通信使用的 IP 地址。", "基础", 10));
        addQuestion(questions, new PracticeQuestion("计算机网络", "路由器转发 IP 数据报时主要依据的是？",
                "源端口号", "目的 IP 地址", "MAC 地址长度", "应用名称",
                "B", "路由器根据目的 IP 地址查询路由表并选择下一跳。", "基础", 10));
        addQuestion(questions, new PracticeQuestion("大学英语", "学术段落中的 topic sentence 通常承担什么作用？",
                "列出全部参考文献", "概括段落中心观点", "替代段落结论", "解释单词发音",
                "B", "主题句提示该段落要展开的中心观点，并帮助读者理解组织结构。", "基础", 10));
        addQuestion(questions, new PracticeQuestion("人工智能", "分类模型的混淆矩阵主要用于？",
                "展示预测类别与真实类别的对应关系", "计算训练时长", "存储模型参数", "生成新的训练样本",
                "A", "混淆矩阵按真实类别与预测类别汇总结果，是分析分类错误的重要工具。", "进阶", 15));
        addQuestion(questions, new PracticeQuestion("Python", "Python 中用于定义函数的关键字是？",
                "func", "def", "lambda only", "function", "B", "def 后跟函数名和参数列表，可定义具名函数。", "基础", 10));
        addQuestion(questions, new PracticeQuestion("Python", "Pandas 中通常使用哪种结构表示二维表格数据？",
                "Series", "DataFrame", "Tuple", "Set", "B", "DataFrame 是带行列标签的二维数据结构。", "基础", 10));
        addQuestion(questions, new PracticeQuestion("软件工程", "Git 中创建新提交前，通常先用哪个命令把更改加入暂存区？",
                "git add", "git clone", "git fetch", "git tag", "A", "git add 将选定的工作区更改加入暂存区，供下一次提交使用。", "基础", 10));
        addQuestion(questions, new PracticeQuestion("软件工程", "代码评审最直接的价值是？",
                "自动替代所有测试", "在合并前发现问题并共享知识", "删除提交历史", "确保永不发生冲突",
                "B", "评审能在变更合入前发现缺陷、讨论设计并促进团队知识共享。", "基础", 10));
    }

    private static void seedExpandedQuestions(PracticeQuestionRepository questions) {
        ExpandedQuestionCatalog.questions().forEach(question -> addQuestion(questions, question));
    }

    private static void addQuestion(PracticeQuestionRepository questions, PracticeQuestion question) {
        if (!questions.existsByPrompt(question.getPrompt())) {
            questions.save(question);
        }
    }

    private static void seedDashboardData(Long userId, StudyTaskRepository tasks,
                                          StudySessionRepository sessions) {
        LocalDate today = LocalDate.now();
        if (!tasks.existsByUserIdAndTaskDate(userId, today)) {
            tasks.save(new StudyTask(userId, "完成算法章节练习", "数据结构", 25, 15, today, true));
            tasks.save(new StudyTask(userId, "阅读 MySQL 索引章节", "数据库", 30, 20, today, false));
            tasks.save(new StudyTask(userId, "完成 REST 接口练习", "Java Web", 35, 20, today, false));
            tasks.save(new StudyTask(userId, "复习 TCP 三次握手", "计算机网络", 20, 15, today, true));
            tasks.save(new StudyTask(userId, "订正今日错题", "在线练习", 15, 10, today, false));
        }

        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate rangeStart = monday.minusWeeks(1);
        if (!sessions.existsByUserIdAndStudyDateBetween(userId, rangeStart, today)) {
            int[] previousWeek = {72, 88, 45, 96, 64, 35, 42};
            int[] currentWeek = {95, 76, 110, 84, 68, 122, 54};
            for (int index = 0; index < previousWeek.length; index++) {
                sessions.save(new StudySession(userId, monday.minusWeeks(1).plusDays(index),
                        previousWeek[index], "课程学习"));
            }
            int daysThisWeek = Math.min(today.getDayOfWeek().getValue(), 7);
            for (int index = 0; index < daysThisWeek; index++) {
                sessions.save(new StudySession(userId, monday.plusDays(index),
                        currentWeek[index], index % 2 == 0 ? "课程学习" : "在线练习"));
            }
        }
    }
}
