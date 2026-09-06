# 知途 LearnPath

面向大一计算机学生的项目式学习平台毕业设计，围绕“做出属于自己的第一个网站”组织学习。当前版本包含技术栈入门说明、个性化建站路线、12 门课程、83 个完整章节、60 项配套资源、96 道在线练习题、互动课程、三种趣味闯关、项目上线检查与可持久化学习进度，并提供基于 Redis 会话的 Java API。

学生可以先选择个人作品集、兴趣博客或校园信息站，再选择 Vue 或原生前端、Spring Boot 或 FastAPI，以及 MySQL 或 SQLite。系统会把第一张 HTML 页面、视觉样式、JavaScript 交互、框架、发布、后端、数据库和最终交付检查组成一条可完成的学习路线。

## 项目结构

```text
study/
├── frontend/       Vue 3 + TypeScript + Vite + Vue Router + Pinia
├── backend/        Java 21 + Spring Boot + Spring Data JPA + Redis
└── compose.yaml    MySQL 与 Redis 本地环境
```

## 本地运行

1. 启动 MySQL 和 Redis：有 Docker 时执行 `docker compose up -d`；当前 Windows 环境可使用系统 MySQL 服务，并执行 `./scripts/start-redis.ps1` 启动项目本地 Redis 兼容服务
2. 启动后端：进入 `backend` 后执行 `./mvnw spring-boot:run`
3. 启动前端：进入 `frontend` 后执行 `npm run dev`

前端默认地址为 `http://localhost:5173`，后端默认地址为 `http://localhost:8080`。

默认数据库为 MySQL 8，数据库名、账号和密码分别为 `learnpath`、`learnpath`、`learnpath123`。如需使用其他凭证，请复制 `.env.example` 并通过环境变量覆盖；不要将真实密码提交到 Git。

首次安装时，请使用 MySQL 管理员账号执行 `scripts/init-mysql.sql`。脚本只创建项目数据库和最小权限项目账号，不包含管理员凭证。

项目本地 Redis 基于 Memurai Developer，仅用于开发与测试；执行 `./scripts/stop-redis.ps1` 可安全停止并保存缓存数据。

## 项目检查

- 前端完整检查：进入 `frontend` 后执行 `npm run check`
- 后端测试：进入 `backend` 后执行 `./mvnw test`
- 服务状态：启动后访问 `GET /api/public/status`

`npm run check` 会检查 58 个重点章节的图文映射、网络与数据库教程内容，然后执行 TypeScript 类型检查和生产构建。后端测试会检查课程、资源、全部章节正文和 12 个练习科目。

## DataGrip 连接

在 DataGrip 中新建 MySQL 数据源，填写 Host `127.0.0.1`、Port `3306`、Database `learnpath`、User `learnpath`、Password `learnpath123`。测试连接成功后，在 Schemas 中勾选 `learnpath`，即可查看课程、外部资源、学习任务、学习时长、用户、学习进度、练习题与答题记录。

## 当前接口

- `POST /api/auth/login`：按学生、教师或管理员身份登录
- `GET /api/auth/me`：读取当前登录用户
- `POST /api/auth/logout`：退出并清理 Redis 会话
- `GET /api/dashboard`：读取学习焦点、今日任务、本周趋势与个性化推荐
- `POST /api/dashboard/tasks/{id}/toggle`：切换今日任务完成状态
- `GET /api/courses`：读取课程列表，支持 `keyword` 与 `category` 查询参数
- `GET /api/courses/{id}`：读取课程详情、章节状态与分类学习资源；每门演示课程包含 5 项资源和推荐学习路线
- `POST /api/courses/{id}/progress`：更新当前学生的课程进度
- `GET /api/practice/questions`：读取练习题，可按 `subject` 筛选
- `POST /api/practice/questions/{id}/submit`：提交答案并获取判分解析
- `GET /api/practice/stats`：读取当前学生的累计答题统计

## 演示账号

| 身份 | 账号 | 密码 |
|---|---|---|
| 学生 | 20240001 | 123456 |
| 教师 | T10001 | 123456 |
| 管理员 | admin | 123456 |

演示数据仅用于本地开发。正式部署时应关闭自动初始化并更换所有默认密码。
