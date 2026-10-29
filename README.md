# CodeBlog Cloud 微服务架构平台

基于 Spring Boot 3 + Spring Cloud 的现代化微服务架构平台，采用模块化设计，提供企业级微服务解决方案，并配套 React + TypeScript 管理后台前端。

## 🏗️ 技术架构概览

### 版本兼容矩阵（重要）

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 运行环境 |
| Spring Boot | 3.5.16 | 微服务基础框架 |
| Spring Cloud | 2025.0.3 | 微服务治理框架 |
| Spring Cloud Alibaba | 2025.0.0.0 | 与 Spring Cloud 2025.0.x / Boot 3.5.x 严格配套 |

### 后端技术栈

| 类别 | 框架 | 版本 | 用途 |
|------|------|------|------|
| 安全认证 | Sa-Token | 1.46.0 | 登录认证、会话管理、网关统一鉴权（Redis 共享会话） |
| 数据访问 | MyBatis-Flex | 1.11.8 | ORM（逻辑删除 / 乐观锁 / SQL 审计） |
| 数据访问 | Druid | 1.2.28 | 数据库连接池 |
| 缓存 | Redisson + Spring Data Redis | 4.8.0 | 分布式缓存、锁 |
| RPC | OpenFeign | 随 Spring Cloud | 声明式 HTTP 客户端（认证头 / 链路透传） |
| 网关 | Spring Cloud Gateway | 随 Spring Cloud | 路由转发、统一鉴权、CORS |
| 注册/配置中心 | Nacos | 随 SCA 2025 | 服务发现 + spring.config.import 配置分层 |
| 流控熔断 | Sentinel | 1.8.9（随 SCA） | 规则 Nacos 持久化 + 网关流控（SCG v6x adapter） |
| 消息队列 | RocketMQ | 5.3.1（client 随 SCA BOM） | SCA Stream binder；登录事件收发，local 可降级（见架构文档 12.5） |
| 分布式事务 | Apache Seata | 2.5.0（随 SCA） | AT 模式就绪，跨服务写时启用 @GlobalTransactional |
| 文档 | Knife4j / SpringDoc | 4.5.0 / 2.8.x | OpenAPI 3 文档 |
| 工具 | Hutool / Lombok / MapStruct | 5.8.48 / 1.18.42 / 1.6.3 | 工具集 / 简化代码 / 对象映射 |
| Excel | FastExcel | 1.3.0 | Excel 导入导出 |

### 前端技术栈（codeblog-ui）

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | React + TypeScript | 19.3 / 5.9 |
| 构建 | Vite | 8.x |
| UI | Ant Design + @ant-design/icons | 6.x |
| 路由 | React Router | 8.x（懒加载 + 登录守卫） |
| 服务端状态 | TanStack React Query | 5.x |
| 客户端状态 | Zustand（持久化登录态） | 5.x |
| HTTP | Axios（Result 解包 / 401 拦截） | 1.x |

## 📁 项目结构

```
codeblog-cloud/
├── codeblog-dependencies/       # 依赖版本管理中心（bom）
├── codeblog-framework/          # 技术组件封装
│   ├── codeblog-spring-boot-starter-banner/     # 启动 Banner
│   ├── codeblog-spring-boot-starter-web/        # Web 基础（全局异常/Result/Jackson Long→String）
│   ├── codeblog-spring-boot-starter-mybatis/    # MyBatis-Flex 封装
│   ├── codeblog-spring-boot-starter-redis/      # Redis 封装（RedisTemplate + RedisCache）
│   ├── codeblog-spring-boot-starter-security/   # Sa-Token 封装（LoginUser/LoginHelper/拦截器）
│   ├── codeblog-spring-boot-starter-rpc/        # OpenFeign 封装（请求透传/日志级别）
│   ├── codeblog-spring-boot-starter-excel/      # Excel 工具库
│   ├── codeblog-spring-boot-starter-file/       # 文件工具库
│   └── codeblog-common/          # 公共组件（core: Result/错误码/分页模型, util: 上下文工具）
├── codeblog-api/                # 服务对外契约层（Feign 接口 + DTO，无实现）
│   └── codeblog-api-system/     # 系统管理服务契约
├── codeblog-modules/            # 业务服务聚合（每子模块 = 一个可独立部署服务）
│   └── codeblog-system/         # 系统管理服务（认证 + 用户管理，spring.application.name=codeblog-system）
├── codeblog-gateway/            # API 网关（Sa-Token Reactor 统一鉴权 + CORS）
├── codeblog-ui/                 # 管理后台前端（React + TS + AntD）
├── docker/docker-compose.yml    # 基础设施编排（MySQL + Redis + Nacos）
└── pom.xml
```

## 🚀 快速开始

### 环境要求

- JDK 17+、Maven 3.9+
- MySQL 8.x、Redis 7.x（本机安装或使用 `docker/docker-compose.yml`）
- Nacos 2.x（可选：使用 `local` profile 可跳过）
- Node.js 20+（前端）

### 1. 启动基础设施

方式 A —— Docker 一键编排（含数据库自动初始化）：

```bash
cd docker
docker compose up -d          # mysql + redis + nacos + sentinel-dashboard(8858) + seata-server(7091/8091) + rocketmq(9876/10911/8180)
```

> Sentinel 控制台默认账号为官方默认 sentinel/sentinel（可用环境变量 AUTH_PASSWORD 覆盖）；
> 持久化流控/熔断规则在 Nacos 控制台编辑 dataId `{service}-{ruleType}-rules`（模板见 `docker/nacos/`），dashboard 仅作监控视图与临时推送。

方式 B —— 本机服务：启动 MySQL 与 Redis，然后导入初始化 SQL：

```bash
mysql -uroot -p < codeblog-modules/codeblog-system/sql/init.sql
```

### 2. 构建后端

```bash
mvn clean install -DskipTests
```

### 3. 启动服务（二选一）

**本地联调（无需 Nacos，推荐开发时使用）：**

```bash
# 系统管理服务（端口 48081）
java -jar codeblog-modules/codeblog-system/target/codeblog-system-2026.02.jar --spring.profiles.active=local

# 网关（端口 9999，路由直连本机服务）
java -jar codeblog-gateway/target/codeblog-gateway-2026.02.jar --spring.profiles.active=local
```

**注册中心模式（默认 profile，需 Nacos 运行于 127.0.0.1:8848）：**

```bash
java -jar codeblog-modules/codeblog-system/target/codeblog-system-2026.02.jar
java -jar codeblog-gateway/target/codeblog-gateway-2026.02.jar
```

### 4. 启动前端

```bash
cd codeblog-ui
npm install
npm run dev          # http://localhost:5173（/system 代理到网关 9999）
```

### 5. 访问

- 管理后台：<http://localhost:5173>，默认账号 `admin / admin123`
- 系统管理服务 API 文档：<http://127.0.0.1:48081/doc.html>
- 网关入口：<http://127.0.0.1:9999>（`/system/**` → 系统管理服务）

## 🔐 认证与请求约定

- 登录：`POST /system/auth/login` 返回 `token`
- 后续请求携带请求头 `Authorization: Bearer <token>`（网关统一校验，Redis 共享会话）
- 响应统一为 `Result<T>`：`{ code, message, data, timestamp }`，`code=200` 成功，`401` 未登录，`403` 无权限（RBAC）
- 后端 `Long` 统一序列化为字符串，避免 JS 精度丢失（前端已适配）

## 🔧 组件配置速查

| 配置 | 说明 | 默认 |
|------|------|------|
| `codeblog.security.enabled` | 业务服务登录校验开关 | true |
| `codeblog.security.excludes` | 登录校验白名单（Ant 风格） | - |
| `codeblog.gateway.security.excludes` | 网关鉴权白名单 | 登录/文档/actuator |
| `codeblog.gateway.cors.*` | 网关跨域配置（阶段4 已收紧为显式来源清单） | localhost/127.0.0.1:5173 |
| `codeblog.mybatis.audit-console-enabled` | SQL 控制台审计（仅开发） | false |
| `codeblog.rpc.feign-log-level` | Feign 日志级别 | 关闭 |
| `codeblog.banner.enabled` | 启动 Banner 开关 | true |
| `spring.cloud.sentinel.enabled` | Sentinel 开关（local profile 降级为 false） | true |
| `spring.cloud.sentinel.transport.dashboard` | Sentinel 控制台地址 | 127.0.0.1:8858 |
| `spring.cloud.sentinel.datasource.*` | 规则数据源（Nacos 持久化，热更新） | flow/degrade/gw-flow |
| `seata.enabled` | Seata 客户端开关（local profile 降级为 false） | true |
| `spring.cloud.stream.rocketmq.binder.name-server` | RocketMQ NameServer 地址 | 127.0.0.1:9876 |
| `codeblog.mq.login-event.enabled` | 登录事件收发开关（local profile 降级为 false，零 MQ 连接） | true |
| `management.endpoints.web.exposure.include` | actuator 暴露端点（阶段4） | health,info |
| `spring.servlet.multipart.max-file-size` | 上传单文件上限（阶段4 显式化） | 10MB |
| `seata.tx-service-group` | 事务分组 | codeblog_tx_group |
| `spring.config.import` | Nacos 配置导入（仅非 local profile） | 共享+私有 dataId |
| `X-Trace-Id` | 链路追踪头：网关生成→服务 MDC→Feign 透传→响应头回写（阶段4） | - |

## 🔄 版本管理

- 主版本号：`${revision}`（当前 2026.02），所有子模块统一继承，flatten-maven-plugin 扁平化
- 第三方依赖版本统一在 `codeblog-dependencies/pom.xml` 管理，业务模块禁止硬编码版本
