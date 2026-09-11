# 知途 LearnPath

刚上大一的你，是否也想做出自己的第一个网站项目，却面对 HTML、CSS、JavaScript、Vue、后端和数据库，不知道该从哪里开始？**知途 LearnPath** 是一个面向计算机新生的项目式学习平台：先帮你选定想做的网站和合适的技术路线，再陪你从第一行代码走到真实上线。

你不需要先学完所有知识才开始动手。选择个人作品集、兴趣博客或校园信息站后，每学到一个知识点，就把它用到同一个项目中：搭页面、做样式、加交互、接接口、存数据、发布网站，再根据真实访客的反馈完成修改。走完整条路线，你得到的不只是课程进度，而是一个真正能打开、能操作、能向别人展示的作品。

当前版本包含 12 门课程、83 个深度教学章节、60 项配套资源、96 道在线练习题、21 个趣味挑战、建站社区和可持久化学习进度。章节按“原理拆解—完整推演—图解过程—具体例子—常见误区—自测与实践”组织；建站阶段按“学习—应用—验证—记录”形成项目闭环。

## 核心建站流程

每条路线都从网页基础开始，按依赖顺序逐步开放：

```text
HTML 首页 → CSS 造型 → JavaScript 交互
          → [Vue 组件化]
          → [Spring Boot / FastAPI 接口]
          → [MySQL / SQLite 持久化]
          → 真实发布 → 上线验收
```

方括号中的阶段会根据技术选择自动加入或省略。发布始终排在所有开发阶段之后，确保学生发布的是完整版本。

| 阶段 | 项目成果 | 完成要求 |
|---|---|---|
| HTML 首页 | 站点介绍与第一条真实内容 | 内容在浏览器中正确出现 |
| CSS 造型 | 清楚的内容层级、卡片与手机布局 | 完成样式实验并写回项目 |
| JavaScript 交互 | 筛选、收藏与详情展开 | 验证重复操作和边界情况 |
| Vue 组件化 | 数据驱动的作品、文章或活动列表 | 完成 Vue 课程、项目实做和证据记录 |
| 后端接口 | 项目专属 REST API 与前端三种请求状态 | 完成对应后端课程、联调和证据记录 |
| 数据持久化 | 与项目内容匹配的数据表和 CRUD 链路 | 完成数据库课程与重启验证 |
| 真实发布 | 可公开访问的网站；全栈路线还需公开 API | 异地打开并走通核心流程 |
| 上线验收 | 访客反馈、修订记录与交付报告 | 全部检查通过后完成建站之旅 |

### 42 条有效路线

- 项目方向：个人作品集、兴趣博客、校园信息站
- 前端：原生 HTML/CSS/JavaScript 或 Vue 3
- 后端：暂不使用、Spring Boot 或 FastAPI
- 数据库：后端启用后可选择暂不使用、MySQL 或 SQLite

暂不使用后端时数据库会自动关闭，因此共有 `3 × 2 × (1 + 2 × 3) = 42` 条有效路线。三个项目方向分别使用作品、文章和活动作为贯穿各站的数据模型，任务、接口、表结构与最终验收会随项目变化。

### 进度与验收规则

- 普通课程达到 100% 只代表知识准备完成，不会直接完成项目阶段。
- Vue、后端和数据库阶段还需要在同一个网站中完成实做、测试边界情况，并提交不少于 20 字的项目证据。
- 已掌握的阶段可以按顺序跳过，最终上线验收不能跳过。
- 更换项目方向会重新验收全部阶段；更换技术时会重新验收受影响阶段及其后的发布流程。
- 路线变化后，旧项目证据、公开地址和毕业状态会随受影响阶段失效，避免沿用不匹配的成果。
- 全栈路线发布时必须同时保存网站地址和公开 API 健康检查地址；最终站会实际请求该接口。

## 项目结构

```text
study/
├── frontend/                       Vue 3 + TypeScript + Vite + Vue Router + Pinia
│   └── src/content/journeyPlan.ts  前端路线生成规则
├── backend/                        Java 21 + Spring Boot + Spring Data JPA + Redis
│   └── .../journey/JourneyPlan.java 后端阶段依赖与课程映射
├── scripts/                        MySQL、Redis 与内容维护脚本
└── compose.yaml                    MySQL 与 Redis 本地环境
```

## 本地运行

1. 启动 MySQL 和 Redis：有 Docker 时执行 `docker compose up -d`；Windows 也可使用系统 MySQL 服务，并执行 `.\scripts\start-redis.ps1` 启动项目本地 Redis 兼容服务。
2. 启动后端：进入 `backend`，Windows 执行 `.\mvnw.cmd spring-boot:run`，macOS/Linux 执行 `./mvnw spring-boot:run`。
3. 启动前端：进入 `frontend`，首次运行执行 `npm install`，然后执行 `npm run dev`。

前端默认地址为 `http://localhost:5173`，后端默认地址为 `http://localhost:8080`。

默认数据库为 MySQL 8，数据库名、账号和密码分别为 `learnpath`、`learnpath`、`learnpath123`。如需使用其他凭证，请复制 `.env.example` 并通过环境变量覆盖；不要将真实密码提交到 Git。

首次安装时，请使用 MySQL 管理员账号执行 `scripts/init-mysql.sql`。脚本只创建项目数据库和最小权限项目账号，不包含管理员凭证。

项目本地 Redis 基于 Memurai Developer，仅用于开发与测试；执行 `./scripts/stop-redis.ps1` 可安全停止并保存缓存数据。

## 项目检查

- 前端完整检查：进入 `frontend` 后执行 `npm run check`
- 后端测试：进入 `backend` 后执行 `.\mvnw.cmd test`（Windows）或 `./mvnw test`（macOS/Linux）
- 服务状态：启动后访问 `GET /api/public/status`

`npm run check` 会检查 58 个重点章节的图文映射、网络与数据库教程内容，然后执行 TypeScript 类型检查和生产构建。后端测试覆盖课程与练习服务、用户状态持久化、技术课与项目证据门槛，以及全部 42 条路线的阶段顺序。

## 数据存储

MySQL 保存用户、课程、章节、练习、学习任务、课程进度、建站路线、作品内容、样式选择、真实网站与 API 地址、阶段实做证据、完成或跳过记录、游戏成绩、社区分享、分享图片、点赞和评论，是业务数据的最终来源。Redis 缓存建站路线、游戏进度与社区信息流，并保存登录会话；缓存不可用时，核心业务仍可直接读写 MySQL。

建站相关数据位于 `web_journey` 和 `journey_stage_progress` 表，游戏成绩位于 `user_game_progress` 表，社区文字、图片、点赞和评论分别位于 `community_post`、`community_post_image`、`community_post_like`、`community_comment` 表。旧版浏览器中的建站数据会在用户首次进入新版页面时自动迁移到 MySQL，迁移成功后清理旧数据。浏览器只保留登录令牌，实际会话状态仍由服务端 Redis 管理。

## DataGrip 连接

在 DataGrip 中新建 MySQL 数据源，填写 Host `127.0.0.1`、Port `3306`、Database `learnpath`、User `learnpath`、Password `learnpath123`。测试连接成功后，在 Schemas 中勾选 `learnpath`，即可查看课程、外部资源、学习任务、学习时长、用户、学习进度、练习题与答题记录。

## 当前接口

- `POST /api/auth/login`：按学生或管理员身份登录
- `GET /api/auth/me`：读取当前登录用户
- `POST /api/auth/logout`：退出并清理 Redis 会话
- `GET /api/dashboard`：读取学习焦点、今日任务、本周趋势与个性化推荐
- `POST /api/dashboard/tasks/{id}/toggle`：切换今日任务完成状态
- `GET /api/courses`：读取课程列表，支持 `keyword` 与 `category` 查询参数
- `GET /api/courses/{id}`：读取课程详情、章节状态与分类学习资源；每门演示课程包含 5 项资源和推荐学习路线
- `POST /api/courses/{id}/progress`：更新当前学生的课程进度
- `GET /api/journey`：读取当前用户的建站路线、作品设置和阶段进度
- `PUT /api/journey`：保存网站主题与前端、后端、数据库路线
- `PUT /api/journey/first-page`：保存第一个页面的内容
- `PUT /api/journey/style`：保存作品的视觉样式
- `PUT /api/journey/deployment`：保存实际发布的网站地址；全栈路线同时保存公开 API 健康地址
- `PUT /api/journey/stages/{stageId}/evidence`：保存技术应用或访客反馈的项目证据
- `POST /api/journey/stages/{stageId}/complete`：完成建站阶段并刷新缓存
- `POST /api/journey/stages/{stageId}/skip`：把已掌握的阶段标记为跳过并继续路线
- `GET /api/games/progress`：读取累计游戏分数和已完成挑战
- `POST /api/games/challenges/{challengeId}/complete`：完成挑战；重复提交不会重复计分
- `GET /api/community/posts`：读取最新社区分享，可按建站历程或作品展示筛选
- `POST /api/community/posts`：以 multipart 表单发布建站历程或小网站作品，并可上传最多 3 张图片
- `POST /api/community/posts/{postId}/like`：点赞或取消点赞一条社区动态
- `POST /api/community/posts/{postId}/comments`：发表评论并刷新社区互动数据
- `GET /api/community/posts/{postId}/images/{imageId}`：读取社区分享图片
- `GET /api/practice/questions`：读取练习题，可按 `subject` 筛选
- `POST /api/practice/questions/{id}/submit`：提交答案并获取判分解析
- `GET /api/practice/stats`：读取当前学生的累计答题统计

## 演示账号

| 身份 | 账号 | 密码 |
|---|---|---|
| 学生 | 20240001 | 123456 |
| 管理员 | admin | 123456 |

演示数据仅用于本地开发。正式部署时应关闭自动初始化并更换所有默认密码。
