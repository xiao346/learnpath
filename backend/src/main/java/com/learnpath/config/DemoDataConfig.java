package com.learnpath.config;

import com.learnpath.course.Course;
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

            seedResources(courseRepository, resourceRepository, python, git);

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

            User student = userRepository.findByAccountAndRole("20240001", UserRole.STUDENT).orElseThrow();
            seedDashboardData(student.getId(), taskRepository, sessionRepository);
        };
    }

    private static void seedResources(CourseRepository courses, CourseResourceRepository resources,
                                      Course python, Course git) {
        courses.findByTitle("人工智能导论").ifPresent(course -> resources.findByCourseIdAndUrl(
                course.getId(), "https://developers.google.com/machine-learning/resources/intro-responsible-ai")
                .ifPresent(resources::delete));
        courses.findByTitle("计算机网络").ifPresent(course -> resources.findByCourseIdAndUrl(
                course.getId(), "https://cs144.github.io/").ifPresent(resources::delete));
        addResource(courses, resources, "数据结构与算法", "OpenDSA 交互式教材", "Virginia Tech",
                "互动教材", "包含数据结构和算法讲解、可视化与练习的开放教材。",
                "https://opendsa-server.cs.vt.edu/", 1);
        addResource(courses, resources, "数据结构与算法", "VisuAlgo 算法可视化", "VisuAlgo",
                "可视化", "通过动画观察排序、树、图等算法的执行过程。",
                "https://visualgo.net/zh", 2);
        addResource(courses, resources, "数据库原理", "MySQL 8.0 参考手册", "Oracle MySQL",
                "官方文档", "查询 SQL、事务、索引和数据库管理的权威参考资料。",
                "https://dev.mysql.com/doc/refman/8.0/en/", 1);
        addResource(courses, resources, "Java Web 应用开发", "Spring REST 服务指南", "Spring",
                "官方教程", "从零构建并运行一个基于 Spring 的 RESTful Web 服务。",
                "https://spring.io/guides/gs/rest-service/", 1);
        addResource(courses, resources, "Java Web 应用开发", "HTTP 概述", "MDN Web Docs",
                "参考资料", "系统理解 HTTP 消息、方法、状态码与连接管理。",
                "https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Guides/Overview", 2);
        addResource(courses, resources, "计算机网络", "TCP 标准 RFC 9293", "RFC Editor",
                "协议标准", "TCP 协议当前规范，适合结合传输层章节查阅。",
                "https://www.rfc-editor.org/rfc/rfc9293.html", 1);
        addResource(courses, resources, "大学英语进阶", "Academic Writing", "Purdue OWL",
                "写作指南", "覆盖论证、段落组织和学术写作基本规范。",
                "https://owl.purdue.edu/owl/general_writing/academic_writing/index.html", 1);
        addResource(courses, resources, "人工智能导论", "机器学习速成课程", "Google for Developers",
                "互动课程", "通过模块、可视化和练习学习机器学习核心概念。",
                "https://developers.google.com/machine-learning/crash-course", 1);
        addResource(resources, python, "Python 官方教程", "Python Software Foundation", "官方教程",
                "中文版 Python 教程，覆盖语言基础、数据结构、模块和异常。",
                "https://docs.python.org/zh-cn/3/tutorial/", 1);
        addResource(resources, git, "Pro Git 中文版", "Git SCM", "在线图书",
                "完整讲解 Git 基础、分支、协作、工具与内部原理。",
                "https://git-scm.com/book/zh/v2", 1);

        addResource(courses, resources, "数据结构与算法", "MIT 6.006 算法导论", "MIT OpenCourseWare",
                "完整课程", "按课程顺序学习数据结构、算法设计与复杂度分析；包含视频、讲义、测验和编程作业。",
                "https://ocw.mit.edu/courses/6-006-introduction-to-algorithms-spring-2020/", 3);
        addResource(courses, resources, "数据结构与算法", "6.006 练习与作业", "MIT OpenCourseWare",
                "课后练习", "完成配套练习题与作业，用代码验证动态数组、排序、树、图和动态规划知识。",
                "https://ocw.mit.edu/courses/6-006-introduction-to-algorithms-spring-2020/pages/assignments/", 4);
        addResource(courses, resources, "数据结构与算法", "Algorithms for Competitive Programming", "CP-Algorithms",
                "专题手册", "以专题方式复习图算法、字符串、数论和数据结构，并参考可运行的实现思路。",
                "https://cp-algorithms.com/", 5);

        addResource(courses, resources, "数据库原理", "MySQL 入门教程", "Oracle MySQL",
                "入门教程", "从创建数据库和表开始练习查询、连接与常用 SQL，建议在 DataGrip 中同步操作。",
                "https://dev.mysql.com/doc/refman/8.0/en/tutorial.html", 2);
        addResource(courses, resources, "数据库原理", "SQL 语句参考", "Oracle MySQL",
                "语法手册", "按 SELECT、INSERT、UPDATE、DDL 分类查阅语法，并为每类语句编写一个示例。",
                "https://dev.mysql.com/doc/refman/8.0/en/sql-statements.html", 3);
        addResource(courses, resources, "数据库原理", "InnoDB 事务模型", "Oracle MySQL",
                "核心专题", "理解 ACID、自动提交、隔离级别和锁，使用两个连接复现实验现象。",
                "https://dev.mysql.com/doc/refman/8.0/en/innodb-transaction-model.html", 4);
        addResource(courses, resources, "数据库原理", "索引与查询优化", "Oracle MySQL",
                "实践专题", "学习 B-Tree 索引与联合索引，使用 EXPLAIN 比较加索引前后的执行计划。",
                "https://dev.mysql.com/doc/refman/8.0/en/optimization-indexes.html", 5);

        addResource(courses, resources, "Java Web 应用开发", "使用 JPA 访问数据", "Spring",
                "项目教程", "构建实体、仓库和数据访问层，把 REST 接口连接到关系数据库。",
                "https://spring.io/guides/gs/accessing-data-jpa/", 3);
        addResource(courses, resources, "Java Web 应用开发", "保护 Web 应用", "Spring",
                "安全实践", "完成登录、路由保护与权限控制，理解认证和授权在 Web 项目中的边界。",
                "https://spring.io/guides/gs/securing-web/", 4);
        addResource(courses, resources, "Java Web 应用开发", "测试 Web 层", "Spring",
                "测试实践", "使用 Spring Boot 测试工具验证控制器与应用上下文，为接口补齐自动化测试。",
                "https://spring.io/guides/gs/testing-web/", 5);

        addResource(courses, resources, "计算机网络", "IPv6 标准 RFC 8200", "RFC Editor",
                "协议标准", "结合网络层章节理解 IPv6 首部、扩展首部和数据包转发规则。",
                "https://www.rfc-editor.org/rfc/rfc8200.html", 2);
        addResource(courses, resources, "计算机网络", "HTTP Semantics RFC 9110", "RFC Editor",
                "协议标准", "查阅 HTTP 方法、状态码、缓存和内容协商的标准定义。",
                "https://www.rfc-editor.org/rfc/rfc9110.html", 3);
        addResource(courses, resources, "计算机网络", "Stanford CS144 计算机网络", "Stanford University",
                "完整课程", "通过 TCP/IP 课程材料与网络协议栈实验，把分层协议知识落实到代码。",
                "https://www.scs.stanford.edu/10au-cs144/", 4);
        addResource(courses, resources, "计算机网络", "HTTP 工作原理", "MDN Web Docs",
                "图解指南", "从客户端、代理、服务器和连接流程理解一次 Web 请求如何完成。",
                "https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Guides/Overview", 5);

        addResource(courses, resources, "大学英语进阶", "Paragraphs and Paragraphing", "Purdue OWL",
                "写作指南", "学习主题句、统一性和连贯性，按指南改写一个结构松散的段落。",
                "https://owl.purdue.edu/owl/general_writing/academic_writing/paragraphs_and_paragraphing/index.html", 2);
        addResource(courses, resources, "大学英语进阶", "Strong Thesis Statements", "Purdue OWL",
                "写作训练", "区分事实陈述与可论证观点，为学术短文写出清晰、具体的 thesis statement。",
                "https://owl.purdue.edu/owl/general_writing/the_writing_process/thesis_statement_tips.html", 3);
        addResource(courses, resources, "大学英语进阶", "B2 Writing", "British Council",
                "分级练习", "阅读范文、完成写作任务并对照反馈，训练邮件、报告、评论和议论文。",
                "https://learnenglish.britishcouncil.org/skills/writing/b2-writing", 4);
        addResource(courses, resources, "大学英语进阶", "Academic Phrasebank", "University of Manchester",
                "表达手册", "按引言、比较、因果和结论场景积累学术表达，并在写作中正确改写使用。",
                "https://www.phrasebank.manchester.ac.uk/", 5);

        addResource(courses, resources, "人工智能导论", "MIT 6.036 机器学习导论", "MIT OpenCourseWare",
                "完整课程", "系统学习表示、泛化、监督学习、神经网络和强化学习，并完成课程练习。",
                "https://ocw.mit.edu/courses/6-036-introduction-to-machine-learning-fall-2020/", 2);
        addResource(courses, resources, "人工智能导论", "TensorFlow 核心教程", "TensorFlow",
                "代码教程", "用可运行笔记本完成分类、回归和神经网络入门实验。",
                "https://www.tensorflow.org/tutorials", 3);
        addResource(courses, resources, "人工智能导论", "PyTorch Tutorials", "PyTorch",
                "代码教程", "从张量、自动微分到模型训练，独立实现并评估一个基础神经网络。",
                "https://pytorch.org/tutorials/", 4);
        addResource(courses, resources, "人工智能导论", "负责任的 AI 入门", "Google for Developers",
                "责任实践", "识别公平性、隐私、安全和问责风险，为一个 AI 场景完成风险检查清单。",
                "https://developers.google.com/machine-learning/guides/intro-responsible-ai", 5);

        addResource(resources, python, "NumPy 学习资源", "NumPy",
                "基础实践", "通过官方快速入门掌握数组、索引、广播和向量化计算。",
                "https://numpy.org/learn/", 2);
        addResource(resources, python, "pandas 入门教程", "pandas",
                "数据处理", "练习读取表格、筛选、清洗、分组聚合和重塑数据，形成分析工作流。",
                "https://pandas.pydata.org/docs/getting_started/index.html", 3);
        addResource(resources, python, "Matplotlib 教程", "Matplotlib",
                "可视化", "从基础图表到多子图和样式设置，把数据结论表达为清晰图形。",
                "https://matplotlib.org/stable/tutorials/index.html", 4);
        addResource(resources, python, "Try Jupyter", "Project Jupyter",
                "在线实验", "无需本地安装即可运行 Notebook，把代码、说明和图表整理成可复现报告。",
                "https://jupyter.org/try", 5);

        addResource(resources, git, "Git 官方入门教程", "Git SCM",
                "官方教程", "动手完成初始化、暂存、提交、分支与合并，理解工作区、暂存区和仓库。",
                "https://git-scm.com/docs/gittutorial", 2);
        addResource(resources, git, "GitHub Skills", "GitHub",
                "互动课程", "在真实仓库中练习 Pull Request、代码评审、冲突处理和 GitHub Actions。",
                "https://skills.github.com/", 3);
        addResource(resources, git, "GitHub Flow", "GitHub Docs",
                "协作规范", "按分支、提交、Pull Request、评审和合并的流程完成一次功能交付。",
                "https://docs.github.com/en/get-started/using-github/github-flow", 4);
        addResource(resources, git, "Conventional Commits", "Conventional Commits",
                "提交规范", "学习结构化提交信息，使用 feat、fix 等类型建立清晰可追踪的版本历史。",
                "https://www.conventionalcommits.org/zh-hans/v1.0.0/", 5);
    }

    private static void addResource(CourseRepository courses, CourseResourceRepository resources,
                                    String courseTitle, String title, String provider, String type,
                                    String description, String url, int order) {
        courses.findByTitle(courseTitle).ifPresent(course ->
                addResource(resources, course, title, provider, type, description, url, order));
    }

    private static void addResource(CourseResourceRepository resources, Course course,
                                    String title, String provider, String type,
                                    String description, String url, int order) {
        if (!resources.existsByCourseIdAndUrl(course.getId(), url)) {
            resources.save(new CourseResource(course.getId(), title, provider, type, description, url, order));
        }
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
