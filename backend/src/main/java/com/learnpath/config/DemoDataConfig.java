package com.learnpath.config;

import com.learnpath.course.Course;
import com.learnpath.course.CourseRepository;
import com.learnpath.course.LearningProgress;
import com.learnpath.course.LearningProgressRepository;
import com.learnpath.practice.PracticeQuestion;
import com.learnpath.practice.PracticeQuestionRepository;
import com.learnpath.user.User;
import com.learnpath.user.UserRepository;
import com.learnpath.user.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoDataConfig {

    @Bean
    CommandLineRunner seedDemoData(
            UserRepository userRepository,
            CourseRepository courseRepository,
            LearningProgressRepository progressRepository,
            PracticeQuestionRepository questionRepository,
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
        };
    }
}
