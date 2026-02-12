# CodeBlog Cloud 微服务架构平台

基于 Spring Boot 3 + Spring Cloud 的现代化微服务架构平台，采用模块化设计，提供企业级微服务解决方案。

## 🏗️ 技术架构概览

### 核心框架版本

| 组件类别 | 框架名称 | 版本号 | 用途说明 |
|---------|---------|--------|----------|
| **核心框架** | Java | 17 | 运行环境 |
| **核心框架** | Spring Boot | 3.5.9 | 微服务基础框架 |
| **核心框架** | Spring Cloud | 2025.0.0 | 微服务治理框架 |
| **核心框架** | Spring Cloud Alibaba | 2023.0.3.3 | 阿里云微服务组件 |

### 安全认证框架

| 框架名称 | 版本号 | 主要功能 |
|---------|--------|----------|
| Sa-Token | 1.44.0 | 权限认证、会话管理、路由拦截 |

### 数据访问框架

| 框架名称 | 版本号 | 主要功能 |
|---------|--------|----------|
| MyBatis-Flex | 1.9.3 | ORM 框架，数据库访问 |
| Druid | 1.2.27 | 数据库连接池 |
| Redisson | 3.52.0 | Redis 客户端，分布式锁 |

### RPC 通信框架

| 框架名称 | 版本号 | 主要功能 |
|---------|--------|----------|
| OpenFeign | 4.2.0 | 声明式 HTTP 客户端 |

### 网关组件

| 框架名称 | 版本号 | 主要功能 |
|---------|--------|----------|
| Spring Cloud Gateway | 4.2.0 | API 网关，路由转发 |

### 工具类库

| 框架名称 | 版本号 | 主要功能 |
|---------|--------|----------|
| Hutool | 5.8.42 | Java 工具类集合 |
| Lombok | 1.18.42 | 简化 Java 代码 |
| MapStruct | 1.6.3 | 对象映射转换 |
| FastExcel | 1.3.0 | Excel 处理 |
| Apache Velocity | 2.4.1 | 模板引擎 |

### 文档与监控

| 框架名称 | 版本号 | 主要功能 |
|---------|--------|----------|
| Knife4j | 4.5.0 | Swagger 增强 UI |
| SpringDoc | 2.8.14 | OpenAPI 3 文档生成 |

### 构建与部署

| 工具名称 | 版本 | 主要功能 |
|---------|------|----------|
| Maven | 3.9+ | 项目构建管理 |
| Flatten Maven Plugin | 1.7.2 | 版本扁平化处理 |

## 📁 项目模块结构

```
codeblog-cloud/
├── codebolog-dependencies/     # 依赖版本管理中心
├── codeblog-framework/         # 服务组件封装
│   ├── codeblog-spring-boot-starter-banner/    # 启动 Banner 组件
│   ├── codeblog-spring-boot-starter-mybatis/   # 数据库访问组件
│   ├── codeblog-spring-boot-starter-redis/     # Redis 缓存组件
│   ├── codeblog-spring-boot-starter-security/  # 安全认证组件
│   ├── codeblog-spring-boot-starter-web/       # Web 基础组件
│   ├── codeblog-spring-boot-starter-excel/     # Excel 处理组件
│   ├── codeblog-spring-boot-starter-file/      # 文件上传组件
│   ├── codeblog-spring-boot-starter-rpc/       # RPC 调用组件
│   └── codeblog-common/                        # 公共组件
│       ├── codeblog-common-core/               # 核心公共组件
│       └── codeblog-common-util/               # 工具类组件
└── codeblog-gateway/           # API 网关服务
```

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.9+
- Redis 6.0+

### 构建项目
```bash
# 克隆项目
git clone <repository-url>

# 进入项目目录
cd codeblog-cloud

# 清理并构建
mvn clean install -DskipTests
```

## 📖 文档说明

本 README 仅展示项目当前使用的技术框架及版本信息，完整使用文档将在后续完善。

## 🔄 版本管理

项目采用统一的版本管理策略：
- 主版本号：`${revision}` (当前为 2026.02)
- 所有子模块继承父项目版本
- 通过 `codebolog-dependencies` 模块统一管理第三方依赖版本