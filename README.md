# InsTalk Cloud

<p align="center">
  <strong>InsTalk 即时通讯系统后端</strong>
</p>

<p align="center">
  <a href="https://www.oracle.com/java/"><img alt="Java" src="https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white"></a>
  <a href="https://spring.io/projects/spring-boot"><img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.4.1-6DB33F?logo=springboot&logoColor=white"></a>
  <a href="https://spring.io/projects/spring-cloud"><img alt="Spring Cloud" src="https://img.shields.io/badge/Spring%20Cloud-2024.0.0-6DB33F?logo=spring&logoColor=white"></a>
  <a href="https://github.com/luf-23/InsTalk-Frontend"><img alt="Frontend" src="https://img.shields.io/badge/Frontend-InsTalk--Frontend-blue?logo=github"></a>
</p>

InsTalk Cloud 是一个基于 Spring Cloud 的即时通讯后端项目，采用多模块微服务架构，提供用户认证、好友关系、群聊、消息收发、WebSocket 推送、AI 对话配置、OSS 上传凭证等能力。

前端项目详见：[luf-23/InsTalk-Frontend](https://github.com/luf-23/InsTalk-Frontend)

## Features

- 统一网关入口：基于 Spring Cloud Gateway 做路由转发和鉴权过滤。
- 微服务拆分：用户认证、社交关系、聊天消息、AI 配置独立成服务。
- 实时通信：聊天服务提供 WebSocket 连接和消息推送。
- 异步消息：RabbitMQ 负责消息投递、消费和死信处理。
- 服务治理：Nacos 用于服务注册和配置管理。
- 公共能力沉淀：DTO/VO、Feign API、Token、Redis、SMTP 等能力集中维护。

## Tech Stack

| Type | Stack |
| --- | --- |
| Runtime | Java 21 |
| Framework | Spring Boot 3.4.1, Spring Cloud 2024.0.0, Spring Cloud Alibaba 2023.0.3.3 |
| Gateway | Spring Cloud Gateway |
| Registry & Config | Nacos 2.2.3 |
| Database | MySQL 5.7, MyBatis |
| Cache | Redis 7 |
| Message Queue | RabbitMQ 3.12 |
| RPC | OpenFeign |
| Realtime | WebSocket |
| Object Storage | Aliyun OSS |
| Build | Maven multi-module |

## Architecture

```text
InsTalk-Cloud
├── instalk-common             # 公共模型、Feign API、工具类、拦截器
├── instalk-infrastructure     # Redis、RabbitMQ、SMTP 等基础设施实现
├── instalk-gateway            # API 网关，统一鉴权和路由
├── instalk-identity-service   # 用户、认证、OSS
├── instalk-social-service     # 好友关系、群组
├── instalk-ai-service         # AI 配置、AI 对话
├── instalk-chat-service       # 消息、WebSocket、MQ 消费生产
├── docker-compose.yml         # 本地中间件编排
├── .env.example               # 本地环境变量示例
├── start-all.bat              # Windows 一键构建并启动全部服务
└── start-one.bat              # Windows 单服务启动示例
```

## Services

| Module | Service Name | Port | Responsibility |
| --- | --- | ---: | --- |
| `instalk-gateway` | `instalk-gateway` | `10010` | API 网关、路由、鉴权 |
| `instalk-identity-service` | `instalk-identity-service` | `8081` | 用户、登录注册、Token、OSS |
| `instalk-social-service` | `instalk-social-service` | `8083` | 好友、群组 |
| `instalk-ai-service` | `instalk-ai-service` | `8084` | AI 配置、AI 对话 |
| `instalk-chat-service` | `instalk-chat-service` | `8085` | 聊天消息、WebSocket、RabbitMQ |

## Gateway Routes

| Path | Target Service |
| --- | --- |
| `/auth/**` | `instalk-identity-service` |
| `/user/**` | `instalk-identity-service` |
| `/oss/**` | `instalk-identity-service` |
| `/friendship/**` | `instalk-social-service` |
| `/group/**` | `instalk-social-service` |
| `/message/**` | `instalk-chat-service` |
| `/ws/**` | `instalk-chat-service` |
| `/ai/**` | `instalk-ai-service` |

## Requirements

- JDK 21
- Maven 3.9+，或使用项目内 Maven Wrapper
- Docker Desktop / Docker Engine
- MySQL 客户端工具，可选但推荐

## Quick Start

### 1. Clone

```bash
git clone https://github.com/luf-23/InsTalk-Cloud.git
cd InsTalk-Cloud
```

### 2. Configure Environment

```bash
cp .env.example .env
```

根据本地环境修改 `.env` 中的密码、Token、OSS、AI、邮箱等配置。`.env` 只用于本地，不要提交真实密钥。

### 3. Start Middleware

```bash
docker compose up -d
docker compose ps
```

本地中间件：

| Component | URL |
| --- | --- |
| MySQL | `localhost:3306` |
| Redis | `localhost:6379` |
| Nacos | `http://localhost:8848` |
| RabbitMQ | `localhost:5672` |
| RabbitMQ Management | `http://localhost:15672` |

### 4. Prepare Database

业务服务默认使用 `ins_talk` 数据库：

```sql
CREATE DATABASE IF NOT EXISTS ins_talk
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

如果使用 Nacos MySQL 持久化，也需要提前准备 `.env` 中配置的 `MYSQL_SERVICE_DB_NAME`、`MYSQL_SERVICE_USER` 和 `MYSQL_SERVICE_PASSWORD`。

### 5. Build

```bash
mvn clean package -DskipTests
```

### 6. Run

Windows 可直接运行：

```bat
start-all.bat
```

或手动启动单个服务：

```bash
mvn -pl instalk-gateway -am package -DskipTests
java -jar instalk-gateway/target/instalk-gateway-1.0.0.jar
```

服务启动后，统一后端入口为：

```text
http://localhost:10010
```

## Configuration

`.env.example` 已整理常用配置项：

| Group | Variables |
| --- | --- |
| MySQL | `MYSQL_ROOT_PASSWORD`, `SPRING_DATASOURCE_*` |
| Nacos | `MYSQL_SERVICE_*`, `NACOS_AUTH_*`, `SPRING_CLOUD_NACOS_*` |
| RabbitMQ | `RABBITMQ_DEFAULT_*`, `SPRING_RABBITMQ_*` |
| Mail | `SPRING_MAIL_*` |
| OSS | `ALIYUN_OSS_*` |
| AI | `AI_KEY`, `AI_URL` |

建议将真实配置放在环境变量、Nacos 配置中心或 CI/CD Secret 中，避免把数据库密码、OSS Key、AI Key、邮箱授权码提交到仓库。

## Authentication

网关通过 `AuthorizeFilter` 统一处理鉴权。默认白名单：

```yaml
gateway:
  auth:
    white-list:
      - /auth/login
      - /auth/register
      - /auth/captcha
      - /auth/refresh
      - /ws
```

除白名单外的接口通常需要携带有效 Token。

## Development

常用命令：

```bash
# 启动本地中间件
docker compose up -d

# 查看中间件状态
docker compose ps

# 停止中间件
docker compose down

# 打包全部模块
mvn clean package -DskipTests

# 只打包某个服务及其依赖
mvn -pl instalk-chat-service -am package -DskipTests

# 运行测试
mvn test
```

开发约定：

- 跨服务 DTO、VO、Feign API 优先放在 `instalk-common`。
- Redis、RabbitMQ、SMTP 等通用基础设施实现优先放在 `instalk-infrastructure`。
- WebSocket 和消息投递逻辑集中在 `instalk-chat-service`。
- 修改公共模块后，需要重新打包依赖它的业务服务。
- MyBatis 已开启 `map-underscore-to-camel-case: true`。

## Troubleshooting

| Problem | Check |
| --- | --- |
| 服务无法注册 | Nacos 是否启动，`8848/9848/9849` 是否可访问，账号密码是否正确 |
| 数据库连接失败 | MySQL 是否启动，`ins_talk` 是否存在，账号密码是否匹配 |
| RabbitMQ 连接失败 | RabbitMQ 是否启动，用户、密码、vhost 是否与配置一致 |
| 网关返回 401 | 路径是否在白名单中，Token 是否有效 |
| Feign 调用失败 | 被调用服务是否已启动，并成功注册到 Nacos |

## Related

- Frontend: [luf-23/InsTalk-Frontend](https://github.com/luf-23/InsTalk-Frontend)
