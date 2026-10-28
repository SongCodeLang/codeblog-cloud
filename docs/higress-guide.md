# Higress 网关调研与选型指南

> 调研日期：2026-10-28
> 背景：本项目当前网关为 Spring Cloud Gateway（`codeblog-gateway` 模块）。本文档整理 Higress 与 SCG 的对比结论、替换影响评估、部署与管理台集成方案，供后续决策"是否引入 Higress"时参考。

---

## 1. 本项目现状（对比基线）

| 项 | 现状 |
|----|------|
| 网关实现 | Spring Cloud Gateway（Spring Boot 3.5.16 + Spring Cloud 2025.0.3），WebFlux/Reactor 模型 |
| 统一鉴权 | 网关内 `SaReactorFilter` 校验 Sa-Token 会话（Redis 共享会话），白名单可配置 |
| 路由方式 | `application.yml` 声明 `lb://user-service`，经 Spring Cloud LoadBalancer 查 Nacos |
| 跨域 | 网关内 `CorsWebFilter` 统一处理 |
| 部署形态 | `java -jar` 单进程，与业务服务同构 |
| 认证约定 | `Authorization: Bearer <token>`，业务服务亦有服务级拦截兜底（security starter） |

---

## 2. Higress 是什么

- 阿里 2022 年开源的**云原生网关**，数据面基于 **Envoy（C++）**，控制面源自 **Istio**，已进入 CNCF 沙箱，当前大版本 2.x（截至调研时 2.1.8+）。
- 定位为"四合一"网关：**Ingress + 微服务网关 + 安全网关 + AI 网关**。
- 与 Nacos 深度集成，Spring Cloud 服务**零改造**接入（服务照常注册 Nacos，Higress 直接发现）。
- 起源动机：解决阿里内部 Tengine（Nginx）配置 reload 断线问题与 gRPC/Dubbo 负载均衡问题。

> **概念澄清**：Higress **不属于 Spring Cloud Alibaba 的 Maven starter 体系**（SCA 官方网关方案仍是 SCG）。它属于阿里外围开源生态，因与 Nacos 无缝集成而常与 SCA 并提。引入方式是**独立部署的网关产品**，不是加依赖。

## 3. 核心能力与卖点

| 能力 | 说明 |
|------|------|
| 配置热更新 | 控制台/Nacos 下发配置，xDS 生效，进程不重启（对比 Nginx reload 的断线重连风暴） |
| 性能 | 官方口径：较 Java 网关（SCG/Zuul）吞吐高约 2 倍；较 Nginx+Ingress+微服务网关三层架构省约 35% 资源；无 JVM 预热 |
| 插件机制 | Wasm 插件，支持 Go/Java/Rust/JS 等多语言编写，动态加载不重启 |
| Nginx 迁移兼容 | 支持 Nginx Ingress 注解平滑迁移 |
| AI 网关 | 100+ 大模型统一接入、Token 配额限流、语义缓存、多模型兜底重试、流量灰度、内容安全 |
| MCP 能力 | OpenAPI/数据库快速转换生成 MCP Server，构建私有 MCP 市场，支持 Key Auth/JWT 认证 |
| 控制台 | 自带 Console：路由、服务来源、插件、证书、可观测开箱即用 |

## 4. SCG vs Higress 对比总表

| 维度 | Spring Cloud Gateway | Higress |
|------|---------------------|---------|
| 本质 | Java 框架（写在你代码里的网关） | 独立网关产品（架在系统前面的网关） |
| 数据面 | JVM + Netty/WebFlux | Envoy（C++） |
| 扩展方式 | 编写 `GatewayFilter`/`GlobalFilter`（Java） | Wasm 插件（多语言），动态加载 |
| 配置变更 | 改 yml/代码 → 打包重启发布 | 控制台保存 → 热生效 |
| 服务发现 | LoadBalancer 桥接 Eureka/Consul/Nacos | 原生对接 Nacos / K8s Service / DNS / 静态 IP |
| 部署形态 | 随意的 jar 进程 | 偏容器化：K8s Helm 为主，另有 all-in-one Docker |
| 控制台 | 无（自建监控） | 自带 |
| AI 能力 | 无原生支持 | 原生（模型管理/Token 限流/MCP 市场） |
| 团队技能要求 | Spring 开发者即可 | 需要容器/K8s 与网关产品化运维经验 |

## 5. 若替换本项目网关：影响评估

### 5.1 必须解决的核心问题——认证体系重构

当前网关鉴权依赖 Sa-Token 的 **Redis 共享会话**（纯 Java 逻辑），Higress 插件无法直接复用。三个选项：

1. **ext-auth 插件回调**：自建鉴权服务，Higress 每请求回调校验（保留 Sa-Token 体系，多一跳内网调用）；
2. **认证下沉到业务服务**：本项目的 security starter 已具备服务级登录拦截（`codeblog.security.*`），去掉网关层校验即可，改动最小；代价是每个服务都要执行校验（性能开销小，Redis 读）；
3. **改用无状态令牌（JWT）**：Higress 可自行校验签名，彻底摆脱 Redis 会话；但需放弃 Sa-Token 会话能力（踢人下线、并发控制等需另行实现）。

### 5.2 其他迁移点

- 路由配置从 `application.yml` 迁到 Higress 控制台/Nacos；
- CORS、限流、灰度从写代码变成控制台插件配置；
- 网关代码（`codeblog-gateway` 模块）整体下线，退出构建与发布流水线；
- 后端服务**发布流程不变**（照常注册 Nacos）。

## 6. 部署架构：还需要 Nginx 吗？

拆开 Nginx 的两个角色：

**流量入口（反代/LB/TLS 终止）→ 不再需要**，Higress 完整接管。

**静态文件托管（codeblog-ui 的 dist 产物）→ 不归 Higress**，三种去处：

| 方案 | 说明 | 适用 |
|------|------|------|
| OSS/CDN + Higress 代理 API | 云上标准做法 | 生产/云环境（推荐） |
| `nginx:alpine` 容器挂 dist，Higress 路由到它 | Nginx 退化为纯静态服务容器，不再是流量入口 | 自建/K8s 环境 |
| 任意静态服务器 + Higress 前置 | 简单场景 | 临时/小规模 |

部署流程变化对照：

| 环节 | 现在（SCG） | 换 Higress 后 |
|------|------------|--------------|
| 网关部署 | `java -jar codeblog-gateway` | Helm（K8s）或 all-in-one Docker |
| 路由变更 | 改 yml → 打包 → 重启 | 控制台保存 → 热生效 |
| 证书管理 | 自行配置 | 控制台统一管理 |
| CI/CD | 网关参与流水线 | 网关退出流水线，后端流程不变 |

> **本机环境提醒**（调研时状态）：开发机 Docker daemon 未运行、Nacos 未安装（服务用 `local` profile 免 Nacos 联调）。若启动 Higress 评估，需先准备 Docker/K8s 环境。

## 7. 管理台与 codeblog-ui 的集成路径

Higress 自带独立 Console（开源、前后端分离），部署即有管理界面。若要**统一进 codeblog-ui 单一后台**，三条路径按成本递增：

### 路径 A：iframe 嵌入（最低成本）
codeblog-ui 加"网关管理"菜单，iframe 指向 Console 域名。
- 优点：几乎零开发
- 缺点：账号体系独立（Console 自有 admin），无法与 Sa-Token 登录态打通

### 路径 B：后端代理 Console 管理 API（推荐起步方案）
Console 的 Vue 前端即调用其后端 REST API 工作，可由 codeblog 后端新建"网关管理"模块做服务端代理，codeblog-ui 加对应菜单，权限纳入现有角色体系。
- 注意 1：该 API 面向 Console 自身前端，**未承诺版本间稳定**，升级 Higress 可能需适配
- 注意 2：网关配置为高危操作，务必限管理员角色 + 审计日志
- 可参考社区 fork [higress-console](https://github.com/higress-group/higress-console) 二开，以及[不部署 Console 时的 API 管理讨论](https://github.com/higress-group/higress/discussions/3137)

### 路径 C：K8s 模式直接管理 CRD（长期正统方案）
K8s 部署下，Higress 路由/插件即 Istio CRD（VirtualService/Gateway 等），管理后端经 K8s API（Java 可用 fabric8 client）声明式管理。稳定契约，适合生产级自研控制台。

**演进建议**：先用独立 Console → 需要统一入口时走 B → K8s 化后演进到 C。

## 8. 选型建议

**当前维持 SCG**，理由：

- 单团队、纯 Spring 技术栈，SCG 与现有代码/认证体系（Sa-Token Redis 会话）闭环，少一层基础设施；
- 本机/当前部署形态（jar 直跑）与 Higress 的容器化倾向不匹配；
- 现阶段无 AI 网关诉求。

**触发迁移评估的信号**（满足任一即值得重新评估）：

1. 网关层成为性能/资源瓶颈（高并发、GC 压力、需要更低延迟）；
2. 路由/限流/灰度策略变更频繁，热更新成为刚需；
3. 团队整体转向 K8s，网关可顺势产品化；
4. **项目要接入大模型/AI 能力**（模型代理、Token 计费限流、MCP 服务市场）——这是 Higress 相对 SCG 最大的差异化能力。

---

## 参考资料

- [时速云使用 Higress 替换 Nginx Ingress + Spring Cloud Gateway（官方博客）](https://higress.ai/blog/higress-gvr7dx_awbbpb_ue5yznvun850sra3)
- [Higress + Nacos 微服务网关最佳实践（官方博客）](https://higress.ai/blog/higress-gvr7dx_awbbpb_xdxim253zzta4bun)
- [对比 5 个开源网关项目，SaaS 企业统一网关架构（阿里云开发者社区）](https://developer.aliyun.com/article/1278440)
- [微服务网关怎么选：Spring Cloud Gateway vs Higress（腾讯云开发者社区）](https://developer.cloud.tencent.com/article/2703525)
- [深度解析微服务网关：APISIX、Higress 与 Spring Cloud Gateway](https://blog.csdn.net/awei0916/article/details/148176416)
- [阿里巴巴开源云原生网关 Higress](https://developer.aliyun.com/article/1081483)
- [Higress 开源后开发者最关心的 15 个问题](https://developer.aliyun.com/article/1081539)
- [网关多合一时代来临：Higress 引领架构革新](https://cloud.baidu.com/article/4473121)
- [应对 Nginx Ingress 退役的易混淆概念（2026-01）](https://www.cnblogs.com/alisystemsoftware/p/19446604)
- [Higress 助力企业构建私有 AI 网关和 MCP 市场](https://www.53ai.com/news/LargeLanguageModel/2025073078390.html)
- [Higress 2.1.8 版本发布说明（GitHub）](https://github.com/alibaba/higress/blob/main/release-notes/2.1.8/README_ZH.md)
- [Higress MCP 服务管理详解（阿里云开发者社区）](https://developer.aliyun.com/article/1670473)
- [不部署 higress-console 时如何配置管理（GitHub Discussion #3137）](https://github.com/higress-group/higress/discussions/3137)
