# Dormitory Management System (DMS)

前后端分离的宿舍管理系统：用户认证、楼栋/房间管理、学生住宿分配、报修管理、公告通知。

## 技术栈

- Backend：Java 17 / Spring Boot 3.2+ / MyBatis-Plus 3.5+ / MySQL 8.0 / Sa-Token / Knife4j(Swagger) / Lombok / Hutool
- Frontend：Vue 3.4+ / Vite 5+ / TypeScript 5+ / Element Plus / Pinia / Vue Router 4 / Axios

## 数据库初始化

1. 创建数据库：`dms`
2. 执行脚本：`docs/init.sql`

## 初始化管理员账号（用于登录）

`/auth/login` 需要 `sys_user` 中存在账号数据；`password` 字段建议存储 bcrypt 哈希值。

你可以先插入一条账号（示例中的 `password` 请替换为你生成的 bcrypt 哈希字符串）：

```sql
INSERT INTO sys_user (username, password, role, status)
VALUES ('admin', '替换为bcrypt哈希', 'ADMIN', 1);
```

生成 bcrypt 哈希可参考（Java 片段）：

```java
cn.hutool.crypto.digest.BCrypt.hash("admin");
```

## 后端启动

1. 修改数据库配置：`server/src/main/resources/application.yml`
2. 启动命令（二选一）：
   - IDEA 直接运行 `com.dms.DmsApplication`
   - 或在 `server` 目录执行：
     ```bash
     mvn -q -DskipTests spring-boot:run
     ```

后端默认端口：`8080`

## 前端启动

1. 进入 `web` 目录安装依赖并启动：
   ```bash
   npm install
   npm run dev
   ```

前端默认端口：`5173`

## 接口约定

后端统一封装返回结构 `Result<T>`（字段：`code / msg / data`），并提供 RESTful 接口供前端调用。

## 构建与部署

### 后端构建

1. 进入 `server` 目录执行构建命令：
   ```bash
   mvn clean package -DskipTests
   ```

2. 构建完成后，在 `server/target` 目录生成可执行的 jar 文件：
   ```
   dormitory-management-system-0.0.1-SNAPSHOT.jar
   ```

### 前端构建

1. 进入 `web` 目录执行构建命令：
   ```bash
   npm run build
   ```

2. 构建完成后，在 `web/dist` 目录生成静态资源文件。

### 部署方案

#### 方案一：分离部署

1. **后端部署**：
   - 将构建好的 jar 文件上传到服务器
   - 执行命令启动服务：
     ```bash
     java -jar dormitory-management-system-0.0.1-SNAPSHOT.jar
     ```
   - 可使用 `systemd` 或 `pm2` 管理服务

2. **前端部署**：
   - 将 `web/dist` 目录下的静态资源文件上传到 Nginx 或其他静态资源服务器
   - 配置 Nginx 反向代理到后端 API：
     ```nginx
     server {
         listen 80;
         server_name your-domain.com;
         
         location / {
             root /path/to/web/dist;
             index index.html;
             try_files $uri $uri/ /index.html;
         }
         
         location /api {
             proxy_pass http://localhost:8080;
             proxy_set_header Host $host;
             proxy_set_header X-Real-IP $remote_addr;
             proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
         }
     }
     ```

#### 方案二：整合部署

1. 将前端构建产物复制到后端的 `src/main/resources/static` 目录
2. 重新构建后端：
   ```bash
   mvn clean package -DskipTests
   ```
3. 部署单个 jar 文件，前端和后端将通过同一个端口访问

### 环境配置

- **后端配置**：修改 `server/src/main/resources/application.yml` 文件中的数据库连接信息
- **前端配置**：修改 `web/vite.config.ts` 文件中的代理配置，或在构建时设置环境变量

### API 文档

启动后端服务后，可通过以下地址访问 API 文档：
```
http://localhost:8080/doc.html
```

### 系统测试

1. **功能测试**：使用 API 文档或前端界面测试各模块功能
2. **性能测试**：使用工具如 JMeter 测试系统性能
3. **安全测试**：检查系统安全性，包括认证、授权和输入验证

## 注意事项

1. 确保数据库服务正常运行，且已创建 `dms` 数据库
2. 首次部署时，执行 `docs/init.sql` 脚本初始化数据库结构
3. 生产环境中，建议修改默认的管理员密码
4. 定期备份数据库，防止数据丢失
5. 监控系统运行状态，及时处理异常情况

