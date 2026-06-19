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
- 容器化部署：Docker Compose 可一键构建并启动基础依赖、后端微服务和前端 Nginx。

## Tech Stack

| Type | Stack |
| --- | --- |
| Runtime | Java 21 |
| Framework | Spring Boot 3.4.1, Spring Cloud 2024.0.0, Spring Cloud Alibaba 2023.0.3.3 |
| Gateway | Spring Cloud Gateway |
| Registry & Config | Nacos 2.2.3 |
| Database | MySQL, PostgreSQL / pgvector, MyBatis |
| Cache | Redis 7 |
| Message Queue | RabbitMQ 3.12 |
| RPC | OpenFeign |
| Realtime | WebSocket |
| Object Storage | Aliyun OSS |
| Build | Maven multi-module |
| Container | Docker, Docker Compose, Nginx |

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
├── mysql-init                 # MySQL 初始化脚本
├── postgres-init              # PostgreSQL / pgvector 初始化脚本
├── nacos-conf                 # Nacos 配置文件
├── Dockerfile                 # 后端微服务通用镜像构建文件
├── docker-compose.yml         # 基础依赖、后端微服务、前端 Nginx 编排
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
- Maven 3.9+
- Docker Desktop / Docker Engine
- 前端项目 `InsTalk-Frontend` 与本项目位于同级目录时，可通过 Compose 一起构建前端 Nginx 镜像。

## Quick Start

### 1. Clone

```bash
git clone https://github.com/luf-23/InsTalk-Cloud.git
cd InsTalk-Cloud
```

如需一起构建前端，请确保目录结构类似：

```text
InsTalk
├── InsTalk-Cloud
└── InsTalk-Frontend
```

### 2. Configure Docker Profile

本地开发默认读取各模块的 `application.yml`。

Docker 运行时通过 Compose 激活：

```yaml
SPRING_PROFILES_ACTIVE: docker
```

并读取各模块本地的 `application-docker.yml`，用于覆盖 Docker 环境里的连接地址，例如：

```yaml
spring:
  cloud:
    nacos:
      server-addr: nacos:8848
```

`application-docker.yml` 通常包含数据库账号、密码等本地配置，已被 `.gitignore` 忽略，不应提交到仓库。

### 3. One Command Startup

```bash
docker compose up -d --build
docker compose ps
```

Compose 会启动基础依赖、后端微服务和前端 Nginx：

| Component | URL |
| --- | --- |
| Frontend Nginx | `http://localhost` |
| MySQL | `localhost:3306` |
| Redis | `localhost:6379` |
| PostgreSQL / pgvector | `localhost:5432` |
| Nacos | `http://localhost:8848` |
| RabbitMQ | `localhost:5672` |
| RabbitMQ Management | `http://localhost:15672` |

前端 Nginx 会代理：

| Path | Proxy Target |
| --- | --- |
| `/api/` | `instalk-gateway:10010` |
| `/ws` | `instalk-gateway:10010` |

网关和业务微服务通过 Docker Compose 内部网络通信，不需要直接暴露到宿主机端口。

### 4. Local Build

```bash
mvn clean package -DskipTests
```

只构建单个服务及其依赖：

```bash
mvn -pl instalk-chat-service -am package -DskipTests
```

### 5. Local Run

Windows 可直接运行：

```bat
start-all.bat
```

或手动启动单个服务：

```bash
mvn -pl instalk-gateway -am package -DskipTests
java -jar instalk-gateway/target/instalk-gateway-1.0.0.jar
```

本地直接运行后端时，统一后端入口为：

```text
http://localhost:10010
```

## Multi Instance

业务微服务没有固定 `container_name`，可以通过 `--scale` 启动多个实例：

```powershell
docker compose up -d --build `
  --scale instalk-identity-service=2 `
  --scale instalk-social-service=2 `
  --scale instalk-chat-service=2 `
  --scale instalk-ai-service=2
```

多个实例会注册到 Nacos，网关通过 `lb://服务名` 进行负载均衡。

## Restart Policy

当前基础依赖的重启策略：

| Component | Restart |
| --- | --- |
| MySQL | `unless-stopped` |
| Redis | `unless-stopped` |
| PostgreSQL | `"no"` |
| Nacos | `"no"` |
| RabbitMQ | `"no"` |

如果容器已经创建过，可以直接更新现有容器策略：

```powershell
docker update --restart=unless-stopped local_mysql local_redis
docker update --restart=no local_postgres local_nacos local_rabbitmq
```

业务服务当前使用：

```yaml
restart: on-failure
```

## Configuration

建议将真实配置放在环境变量、Nacos 配置中心或 CI/CD Secret 中，避免把数据库密码、OSS Key、AI Key、邮箱授权码提交到仓库。

Docker 环境内常用服务地址：

| Component | Docker Host |
| --- | --- |
| MySQL | `mysql:3306` |
| Redis | `redis:6379` |
| PostgreSQL | `postgres:5432` |
| Nacos | `nacos:8848` |
| RabbitMQ | `rabbitmq:5672` |

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
# 启动或更新全部容器
docker compose up -d --build

# 只启动基础依赖
docker compose up -d mysql redis nacos rabbitmq postgres

# 查看容器状态
docker compose ps

# 查看网关日志
docker compose logs -f instalk-gateway

# 停止并删除容器
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
| Docker 构建失败 | Docker Desktop Linux engine 是否已启动，前端目录是否位于 `../InsTalk-Frontend` |
| 服务无法注册 | Nacos 是否启动，`8848/9848/9849` 是否可访问，账号密码是否正确 |
| 数据库连接失败 | MySQL 是否启动，`ins_talk` 是否存在，Docker 环境是否使用 `mysql:3306` |
| RabbitMQ 连接失败 | RabbitMQ 是否启动，用户、密码、vhost 是否与配置一致 |
| 网关返回 401 | 路径是否在白名单中，Token 是否有效 |
| Feign 调用失败 | 被调用服务是否已启动，并成功注册到 Nacos |
| 前端接口 404 | Nginx 是否启动，前端请求是否以 `/api/` 或 `/ws` 访问 |

## Related

- Frontend: [luf-23/InsTalk-Frontend](https://github.com/luf-23/InsTalk-Frontend)
