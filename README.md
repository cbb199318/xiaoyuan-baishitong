# 校园百事通

校园百事通项目，包含：

- `server/`：Spring Boot 通用后端
- `mobile/`：uni-app 用户端
- `admin/`：Vue 3 管理后台

## 技术栈

- 后端：Spring Boot 3、Spring Security、JWT、MyBatis-Plus、MySQL 8、WebSocket
- 用户端：uni-app、Vue 3、Pinia、uView 风格基础组件封装
- 管理端：Vue 3、Vite、Element Plus、Pinia、Vue Router

## 启动说明

### 1. 数据库

创建 `MySQL 8` 数据库：

```sql
CREATE DATABASE campus_platform
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
```

执行初始化脚本：

```bash
mysql -u root -p campus_platform < server/sql/init.sql
```

### 2. 后端

```bash
cd server
./mvnw spring-boot:run
```

默认访问端口：`9001`

### 3. 用户端

```bash
cd mobile
pnpm install
pnpm dev:h5
```

默认访问端口：`9003`

### 4. 管理端

```bash
cd admin
npm install
npm run dev
```

默认访问端口：`9002`
