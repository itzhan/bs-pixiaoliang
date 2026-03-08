# 智能停车场管理系统

基于 Spring Boot + Vue 3 + MySQL 的智能停车场管理系统，包含后端 API、管理端和用户端三个子项目。

## 项目结构

```
├── parking-backend/     # Spring Boot 3 后端 (端口 8088)
├── parking-admin/       # Vue 3 + Ant Design Vue 管理端 (端口 5173)
├── parking-frontend/    # Vue 3 + Naive UI 用户端 (端口 5174)
├── start.sh / start.bat # 一键启动脚本
├── stop.sh / stop.bat   # 一键停止脚本
└── API-DOC.md           # API 接口文档
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2 + MyBatis-Plus 3.5 |
| 安全认证 | Spring Security + JWT + Redis |
| 数据库 | MySQL 8.0 + utf8mb4 |
| 管理端 | Vue 3 + TypeScript + Ant Design Vue 4 |
| 用户端 | Vue 3 + TypeScript + Naive UI |
| 构建工具 | Maven (后端) + Vite (前端) |

## 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Redis 6.0+
- Node.js 18+ + npm

## 快速启动

### 方式一：一键启动（推荐）

```bash
# Mac/Linux
chmod +x start.sh stop.sh
./start.sh

# Windows
start.bat
```

脚本会自动：
1. 检查环境依赖
2. 创建数据库并导入数据
3. 安装项目依赖
4. 启动所有服务

### 方式二：手动启动

#### 1. 创建数据库

```sql
CREATE DATABASE smart_parking DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

导入表结构和测试数据：
```bash
mysql -uroot -proot --default-character-set=utf8mb4 < parking-backend/src/main/resources/sql/init.sql
mysql -uroot -proot --default-character-set=utf8mb4 < parking-backend/src/main/resources/sql/data.sql
```

#### 2. 修改后端配置

编辑 `parking-backend/src/main/resources/application.yml`，修改数据库和 Redis 连接信息。

#### 3. 启动后端

```bash
cd parking-backend
mvn spring-boot:run
```

#### 4. 启动管理端

```bash
cd parking-admin
npm install
npm run dev
```

#### 5. 启动用户端

```bash
cd parking-frontend
npm install
npm run dev
```

## 访问地址

| 服务 | 地址 |
|------|------|
| 后端 API | http://localhost:8088 |
| 管理端 | http://localhost:5173 |
| 用户端 | http://localhost:5174 |

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 操作员 | operator1 | admin123 |
| 普通用户 | user1 ~ user10 | 123456 |

## 功能模块

### 管理端功能

- **数据看板**：用户数、车位数、订单数、收入统计、占用率、营收趋势、时段统计
- **停车管理**：停车区域管理、车位管理、预约管理、停车订单管理、出入日志查询
- **计费管理**：计费规则配置、支付记录查询
- **用户管理**：用户列表、车辆管理、黑名单管理
- **内容管理**：公告发布、评价管理
- **系统管理**：系统设置、审计日志

### 用户端功能

- **首页**：停车场概览、空位统计、最新公告
- **停车位查询**：按区域查看空闲车位、在线预约
- **我的预约**：查看/取消预约
- **我的订单**：停车记录、在线支付
- **我的车辆**：车辆绑定与管理
- **我的支付**：支付记录查询
- **我的评价**：评价停车体验
- **我的消息**：系统通知、订单消息
- **个人中心**：资料编辑、密码修改

## 停止服务

```bash
# Mac/Linux
./stop.sh

# Windows
stop.bat
```

## 接口文档

详见 [API-DOC.md](API-DOC.md)
