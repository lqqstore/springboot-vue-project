# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Dormitory Management System (DMS) — 前后端分离的宿舍管理系统。用户认证、楼栋/房间管理、学生住宿分配、报修管理、公告通知。

## Commands

### Backend (`server/`)

```bash
# 启动（开发）
mvn -q -DskipTests spring-boot:run              # 在 server/ 目录下执行，端口 8080

# 构建
mvn clean package -DskipTests                    # 产出 jar 在 server/target/

# 测试
mvn test                                         # 运行全部测试
mvn test -Dtest=AuthServiceTest                  # 运行单个测试类
```

### Frontend (`web/`)

```bash
npm install                                      # 安装依赖
npm run dev                                      # 启动开发服务器，端口 5173
npm run build                                    # 生产构建，产出 web/dist/
```

### Database

1. 创建数据库 `dms`
2. 执行 `docs/init.sql` 初始化表结构
3. 手动插入管理员账号（密码需 bcrypt 哈希），参考 README.md 中的说明

## Architecture

### Backend (`server/`)

**技术栈**: Java 17, Spring Boot 3.2, MyBatis-Plus 3.5, Sa-Token, Knife4j (Swagger), MySQL 8.0, Hutool, Lombok

**包结构**: `com.dms` 下按层级 + 模块组织：

- `common/` — 全局组件：`Result<T>` 统一响应体（code/msg/data，成功 code=0）、`GlobalExceptionHandler` 统一异常处理
- `config/` — SaTokenConfig（拦截器，除 `/auth/login` 外所有接口需登录）、MybatisPlusConfig、Knife4jConfig
- `modules/{auth,dorm,notice,repair,student}/` — 每个模块内含 controller / service / mapper / entity / dto

**分层约定**：
- Controller 接收 DTO（`@Valid` 校验），返回 `Result<T>`
- Service 接口 + impl 实现
- Mapper 继承 MyBatis-Plus `BaseMapper<T>`
- Entity 用 `@TableName` 映射表，`@TableId(type = IdType.AUTO)` 自增主键
- `DormRoomVO` 是多表查询结果 VO（关联楼栋名称、值日生信息）

**认证流程**：Sa-Token 管理。登录成功调用 `StpUtil.login(userId)`，返回 tokenValue。前端放 `Authorization` header，拦截器用 `StpUtil.checkLogin()` 校验。角色字段 `role`（ADMIN/dorm_admin）存在 `sys_user` 表。

**密码**: bcrypt 哈希（Hutool `BCrypt.hashpw` / `BCrypt.checkpw`），`payload.json` 中明文 admin/admin123456 用于开发测试。

### Frontend (`web/`)

**技术栈**: Vue 3.4, Vite 5, TypeScript 5, Element Plus 2.7, Pinia 2.2, Vue Router 4, Axios

**目录结构**:

```
web/src/
├── api/           # 按模块拆分的 API 调用函数（dorm.ts, student.ts, notice.ts, repair.ts）
├── layout/        # 布局组件（侧边栏 + header + breadcrumb + 内容区）
├── router/        # 路由配置，/login 公开，其余需 auth
├── stores/        # Pinia store（user.ts — token + userInfo）
├── styles/        # theme.css — CSS 自定义属性，支持 prefers-color-scheme 深色模式
├── utils/         # request.ts — Axios 实例，baseURL=/api，自动挂载 token，401 时清理并跳转登录
└── views/         # 按模块的页面组件（login/, dorm/, notice/, repair/, student/）
```

**关键约定**：
- 前端所有请求前缀 `/api`，Vite dev server proxy 将 `/api` 转发到 `http://localhost:8080` 并去掉 `/api` 前缀
- Token 存 `localStorage` key `dms_token`，用户信息存 `dms_user`
- 路由守卫：`router.beforeEach` 检查 token，无 token 跳 `/login`，已登录不能访问 `/login`
- 侧边栏使用 `el-menu` 的 `router` 模式，路径映射到 menu item 的 `index`
- 请求成功判断 `res.data.code === 0`

**主题**：`theme.css` 定义 `--app-*` CSS 变量，通过 `@media (prefers-color-scheme: dark)` 切换深浅色，同时覆盖 Element Plus 的 CSS 变量实现完整的深色模式。

### API 接口一览

| 前缀 | 模块 | 说明 |
|------|------|------|
| `/auth` | 认证 | login, logout, current-user |
| `/dorm/buildings` | 楼栋 | CRUD + list |
| `/dorm/rooms` | 房间 | 分页查询（含值日生联表）、可用房间、CRUD |
| `/notice` | 公告 | CRUD + list |
| `/repair` | 报修 | CRUD + list + 按学生查询 |
| `/student` | 学生 | CRUD + list + 分配/移除宿舍 + 按房间查学生 |

启动后端后可访问 `http://localhost:8080/doc.html` 查看 Knife4j API 文档。
