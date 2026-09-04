# 知途 LearnPath

面向大学生的智能学习平台毕业设计。当前基础版本包含蓝紫玻璃拟态登录页、学生学习工作台，以及基于 MySQL 和 Redis 会话的 Java 登录接口。

## 项目结构

```text
study/
├── frontend/       Vue 3 + TypeScript + Vite
├── backend/        Java 21 + Spring Boot + Spring Data JPA
└── compose.yaml    MySQL 与 Redis 本地环境
```

## 本地运行

1. 启动 MySQL 和 Redis：有 Docker 时执行 `docker compose up -d`；当前 Windows 环境也可执行 `./scripts/start-redis.ps1` 启动项目本地 Redis 兼容服务
2. 启动后端：进入 `backend` 后执行 `./mvnw spring-boot:run`
3. 启动前端：进入 `frontend` 后执行 `npm run dev`

前端默认地址为 `http://localhost:5173`，后端默认地址为 `http://localhost:8080`。

当前本机演示可用 `./mvnw spring-boot:run -Dspring-boot.run.profiles=demo` 启动，使用本地 H2 文件保存业务数据，并使用真实 Redis 保存登录会话。

如果电脑暂时没有 MySQL 或 Redis，也可用 `./mvnw spring-boot:run -Dspring-boot.run.profiles=preview` 启动纯预览环境。该模式使用临时内存数据，关闭后即清空；正式开发仍使用 MySQL 和 Redis。

项目本地 Redis 基于 Memurai Developer，仅用于开发与测试；执行 `./scripts/stop-redis.ps1` 可安全停止并保存缓存数据。

## 演示账号

| 身份 | 账号 | 密码 |
|---|---|---|
| 学生 | 20240001 | 123456 |
| 教师 | T10001 | 123456 |
| 管理员 | admin | 123456 |

演示数据仅用于本地开发。正式部署时应关闭自动初始化并更换所有默认密码。
