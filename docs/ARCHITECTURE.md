# codeblog-cloud 架构规划

> 版本：v0.3（2026-10-29）｜状态：执行中（阶段 3 已完成）
> 配套文档：[AGENTS.md](../AGENTS.md)（智能体/协作者工作规范）、[README.md](../README.md)（快速上手）
> 输入依据：2026-10-28 全栈架构评审报告（八板块逐项通读代码后得出）

## 1. 结论速览

1. **采纳模块重组方向**：新增 `codeblog-modules` 聚合目录承载全部业务服务，删除顶层 `codeblog-user`。与 RuoYi-Cloud（ruoyi-modules）、pig（pig-modules）同构。
2. **两处修正**：
   - `codeblog-modules` 必须是"聚合 POM + 每个服务一个可独立构建部署的 Maven 子模块"，不是一个装下所有业务的大模块；
   - "用户子服务 + 权限认证服务"**不拆成两个服务**，合并为一个 `codeblog-system` 系统管理服务（认证、用户、角色、菜单、字典、配置、操作日志）。
3. **同步新增 `codeblog-api` 契约层**（Feign 接口 + DTO 下沉），这是 modules 化后服务间调用的前置配套。
4. 博客核心业务（文章/分类/标签/评论）规划为 `codeblog-blog` 服务，二期启动。
5. 评审报告的 P0/P1 缺陷（RBAC 断链、可观测断链、缓存空心等）不与本次重组混做，按第 6 节路线图分阶段落地。

## 2. 目标总体架构

```
                        ┌─────────────────────────┐
                        │  codeblog-ui (React)     │ 5173
                        │  /system /blog 反向代理   │
                        └────────────┬────────────┘
                                     ▼
                        ┌─────────────────────────┐
                        │  codeblog-gateway  9999  │
                        │  路由 / CORS / Sa-Token   │
                        │  统一登录鉴权(Redis 会话)  │
                        │  traceId 注入(阶段4)      │
                        └─────┬───────────────┬───┘
                    /system/**│               │/blog/**
                    StripPrefix=1             StripPrefix=1
                              ▼               ▼
                ┌───────────────────┐  ┌───────────────────┐
                │  codeblog-system  │  │ codeblog-blog(二期) │
                │  认证/用户/角色/菜单 │  │ 文章/分类/标签/评论  │
                │  字典/配置/操作日志 │  │                   │
                └────────┬──────────┘  └───────────────────┘
                         │  Nacos 注册发现；OpenFeign 走 codeblog-api
          ┌──────────────┼───────────────────┐
          ▼              ▼                   ▼
       MySQL 8        Redis 7.4          对象存储(阶段5)
     sys_* 表族      Sa-Token 会话 /     local + MinIO 双实现
                     业务缓存 / 分布式锁
```

## 3. 目标模块结构

```
codeblog-cloud/
├── codeblog-dependencies/                # BOM：第三方版本唯一管理点（不变）
├── codeblog-framework/                   # 技术组件层，不含业务（不变，按阶段扩充）
│   ├── codeblog-common/
│   │   ├── codeblog-common-core          # Result / 错误码 / 分页 / BusinessException
│   │   └── codeblog-common-util          # 上下文工具（阶段4 改造为随 web starter 自动装配）
│   ├── codeblog-spring-boot-starter-web
│   ├── codeblog-spring-boot-starter-security
│   ├── codeblog-spring-boot-starter-mybatis
│   ├── codeblog-spring-boot-starter-redis
│   ├── codeblog-spring-boot-starter-rpc
│   ├── codeblog-spring-boot-starter-excel
│   ├── codeblog-spring-boot-starter-file
│   └── codeblog-spring-boot-starter-banner
├── codeblog-api/                         # ★新增：服务对外契约层
│   └── codeblog-api-system               # RemoteUserService(Feign) + UserDTO 等
├── codeblog-modules/                     # ★新增：业务服务聚合（每子模块 = 一个可部署服务）
│   ├── codeblog-system/                  # ★由 codeblog-user 迁移升级而来
│   └── codeblog-blog/                    # 二期
├── codeblog-gateway/                     # 网关（不变，路由按新服务名调整）
├── codeblog-ui/                          # 前端（不变，代理前缀调整）
└── docker/
```

**职责边界规则**：

| 层 | 允许依赖 | 禁止 |
|---|---|---|
| framework | framework 内部、第三方 | 依赖 api / modules；出现任何业务概念 |
| api | common-core、rpc starter（仅注解） | 依赖任何服务实现；含业务逻辑 |
| modules/system、modules/blog | framework、api、自己的表 | 互相依赖对方实现 jar |
| gateway | common-core、Sa-Token reactor | 依赖业务服务实现 |

## 4. codeblog-user → codeblog-modules/codeblog-system 迁移方案

**为什么叫 system 而不叫 user**：RBAC 落地后该服务将承载角色、菜单、字典、配置、操作日志，"用户服务"之名不再准确（RuoYi-Cloud `ruoyi-system`、yudao `module-system` 同构）。

**迁移后包结构**：

```
site.codeblog.system
├── controller/    # AuthController、UserController（阶段3 增 RoleController、MenuController…）
├── service/impl/
├── mapper/
├── entity/        # UserPO（阶段3 增 RolePO、MenuPO…）
├── convert/
├── vo/
└── sql/init.sql
```

**迁移步骤**（严格按序，全部在 IDEA 内操作以保留 git 历史与模块缓存正确性）：

1. 创建 `codeblog-modules`（packaging=pom，parent=codeblog-cloud）与 `codeblog-modules/codeblog-system`（parent=codeblog-modules）
2. IDEA `Refactor → Move` 将 codeblog-user 全部源码与资源迁入 codeblog-system；`Refactor → Rename` 包名 `site.codeblog.user` → `site.codeblog.system`；artifactId、模块描述同步
3. 配置切换（一次性破坏性变更，趁生态未扩散尽早承受）：
   - `spring.application.name`：`user-service` → `codeblog-system`
   - 网关路由：`Path=/user/** → lb://user-service` 改为 `Path=/system/** → lb://codeblog-system`
   - 网关鉴权白名单：`/user/auth/login` → `/system/auth/login`；服务白名单保持 `/auth/login` 不变
   - 前端 vite 代理：`/user` → `/system`（axios 侧若有 baseURL 常量同步）
4. 根 pom `<modules>` 移除 `codeblog-user`、新增 `codeblog-modules`；BOM 内部模块清单同步增删
5. 回归验证（登录 / 用户 CRUD / doc.html / 网关直连）通过后，删除旧模块残留目录并提交

**同阶段顺带（不引入新功能，仅结构调整）**：创建 `codeblog-api` 聚合与空壳 `codeblog-api-system`（仅 pom + 包骨架），阶段 5 接入第一个真实 Feign 接口。

## 5. 服务拆分原则

**为什么"权限认证"不独立成服务**（对 pig-auth 模式的取舍）：

- 本项目 token 模型是 Sa-Token + Redis 共享会话：网关与任意服务凭 Redis 即可校验登录态，不存在需要中心化签发/校验的认证服务；
- 登录强依赖用户数据（查账号、验状态、取角色权限），独立认证服务意味着每次登录多一跳 RPC，且用户/权限数据双写同步问题；
- pig 的 pig-auth 独立是 OAuth2 授权码与第三方登录流程所需；RuoYi-Cloud、yudao 均将 auth 与用户/角色/菜单合在 system 单服务，是更贴近本项目技术选型的参照。
- 重拆判据：出现 OAuth2 开放平台、短信/第三方登录聚合、认证流量独立扩缩容诉求时再拆。

**codeblog-blog 为什么够格独立**：业务域独立（内容 vs 系统管理）；读写模型不同（内容读多写少，缓存与搜索策略不同）；发布节奏可独立。反之字典、配置、操作日志这类"跟着系统管理走"的域不拆。

**新服务准入判据**（后续新增服务统一按此评估）：独立业务域 + 独立数据所有权 + 独立伸缩/发布诉求，三者占二才建新服务；否则先进 system。

## 6. 阶段路线图

| 阶段 | 主题 | 主要内容 | 验收标准 |
|---|---|---|---|
| 0 | 规划 | AGENTS.md、本架构文档 | 评审确认 |
| 1 | 模块重组 | modules/api 落地、user→system 迁移、网关/前端路由切换；Nacos dataId/服务名联动更新；sql 目录双轨规范（第 9 节）随迁移生效 | 全链路回归通过；`mvn clean install` 全绿 |
| 2 | 微服务治理组件 | Nacos 配置规范化（spring.config.import + shared-configs 白名单收敛）；Sentinel 接入（dashboard 进 compose、规则 Nacos 持久化、网关流控、Feign 熔断整合）；Seata 就绪（seata-server 进 compose、undo_log、数据源代理，暂无 @GlobalTransactional 用例）；local profile 三件套降级策略（详见第 12 节） | compose 全套：dashboard 可见服务、网关流控规则重启不丢；local 联调不受影响 |
| 3 | RBAC 权限 | sys_role / sys_menu / sys_user_role / sys_role_menu 表（增量脚本 `rbac_YYYYMMDD.sql`）+ sys_login_log 登录日志；登录填充 LoginUser.roles/permissions；DefaultStpInterfaceImpl 从会话读取（兑现注释）；管理接口加 @SaCheckPermission；前端菜单与按钮权限 | 普通用户调管理接口得 403；admin 正常管理 |
| 4 | 拦截链与可观测 | 网关 traceId GlobalFilter；MDC Filter + logback-spring.xml（含 traceId、文件滚动、logs/{service} 目录，见 12.4 L1）；ThreadContextHolder 填充/清理激活；SpringContextHolder 改自动装配；actuator；CORS 收紧；multipart 大小配置 | 日志全链含 traceId；/actuator/health 可用 |
| 5 | 数据与缓存 | BaseEntity + version 列落地；审计字段自动填充（Flex Insert/UpdateListener）；DuplicateKeyException 处理；CacheConstants + Spring Cache + LockTemplate | 并发创建用户返回友好错误；currentUser 缓存旁路生效 |
| 6 | 组件完善 | File 存储抽象（local + MinIO）+ sys_file；Excel 注解打通 FastExcel + 导入错误行聚合；Feign 容错（Sentinel fallbackFactory + ErrorDecoder，api 模块第一个真实接口）；@Log 操作日志切面 + sys_oper_log；错误码模块化 | 各组件有最小闭环 + 示例接口 |
| 7 | 日志收集与工程化 | Grafana Loki + Promtail + Grafana 观测栈进 compose（按 traceId 反查全链路日志，见 12.4 L3）；框架层单测；CI 流水线；@RepeatSubmit 评估；代码生成器决策（做或删 velocity 依赖） | Grafana 按 traceId 检出跨服务日志链；CI 全绿 |
| 8 | 博客业务 | codeblog-blog：文章/分类/标签/评论；api 模块真实跨服务调用（文章作者信息）；首个 @GlobalTransactional 场景（跨服务写，如发文的 system 侧联动）与回滚验证 | 博客前台可发文评论；分布式事务回滚验证通过 |

排期原则：阶段 1 是纯搬移不加功能；阶段 3-6 对应评审报告 P0/P1 清单；治理组件（阶段 2）与日志收集（阶段 7）为用户追加需求，设计见第 12 节、决策见 ADR-6~8。

**阶段收尾三件事（每个阶段完成时强制执行，缺一视为阶段未完成）**：

1. **SQL 基线合并**：当期增量脚本合并回 `init.sql` 全量基线（规则见第 9 节）；
2. **本文档回填**（见第 11 节）：阶段进度表补状态与完成日期；与计划的实现偏差记入回填记录（做了什么、为何偏离）；模块结构、横切约定、拦截链等章节若与实际代码不符，**以实际代码为准**更新对应章节；期间产生的新决策追加第 10 节 ADR；协作规范变化同步 AGENTS.md 与 README 配置速查表；
3. **打 git tag**：`phase-N-<主题>`（如 `phase-1-restructure`），作为阶段快照锚点。

## 7. 横切约定（各阶段实施时遵守）

- **认证与授权**：网关只做"登录校验"（白名单外一律验 token）；授权（@SaCheckPermission/@SaCheckRole）下沉到服务内；白名单两份语义（网关带前缀/服务不带）改动时必须双侧核对。
- **缓存**：key 统一 `codeblog:{service}:{domain}:{bizId}`，集中在 CacheConstants 定义；读多写少用 Spring Cache 注解，精细控制用 RedisCache；锁一律走 LockTemplate（watchdog + finally unlock）。
- **错误码**：GlobalErrorCode 只留 HTTP 语义与全局码；业务码按服务分段（system=1-000-xxx，blog=2-000-xxx），各服务维护 ErrorCodeConstants。
- **文件**：FileClient 抽象，local/minio 双实现，元数据统一落 sys_file，禁止把绝对路径存库。
- **可观测**：traceId 网关生成 → MDC → Feign 透传 → 响应头 X-Trace-Id 回写；日志 pattern 全服务统一。
- **接口文档**：每服务 knife4j /doc.html；阶段 6 评估网关聚合文档路由。
- **服务治理**：注册与配置中心 Nacos（shared-configs 共享配置收敛白名单）、流控熔断 Sentinel（规则 Nacos 持久化）、分布式事务 Seata（跨服务写才 @GlobalTransactional）——详见第 12 节。
- **日志**：应用日志统一 logback + traceId 落盘 `./logs/{service}/`，Grafana Loki 集中收集（阶段 7）；业务审计走 sys_oper_log / sys_login_log 表——详见第 12.4 节。

## 8. 拦截链设计蓝图（阶段 2-7 分批实施）

**现状结论（2026-10-28 评审）**：拦截链不符合预期、设计不完善——全链只有"登录校验"一环在运转（网关 SaReactorFilter + 服务 SaInterceptor），traceId、MDC、上下文填充、操作日志、防重、限流、锁全部缺位或为死代码。目标蓝图如下，"已具备"项保留，其余分阶段补齐。

### 8.1 网关侧（WebFlux，执行顺序自上而下）

| 序 | 组件 | 职责 | 状态 |
|---|---|---|---|
| G1 | TraceIdFilter（GlobalFilter，Ordered=-100） | 入口生成/透传 `X-Trace-Id`；注入转发请求头并回写响应头 | 阶段4 新增 |
| G2 | CorsWebFilter | 跨域；allowedOrigins 由 `*` 收紧为显式来源清单 | 已具备，阶段4 收紧 |
| G3 | SaReactorFilter | 白名单放行 + 登录校验（Redis 共享会话）+ 鉴权异常统一 JSON | 已具备 |
| G4 | Sentinel 网关流控（sentinel-spring-cloud-gateway-adapter） | 路由/API 分组维度限流，规则 Nacos 持久化 | 阶段2 接入（见第 12.2 节，ADR-6） |
| G5 | 统一 ErrorWebExceptionHandler | 非 Sa-Token 的网关异常也输出 Result JSON（现为 Spring 默认错误体） | 阶段4 新增 |

### 8.2 服务侧（Servlet）

| 序 | 组件 | 类型 | 职责 | 状态 |
|---|---|---|---|---|
| S1 | TraceMdcFilter | Filter（最先） | 读/生成 X-Trace-Id → MDC + 响应头回写；finally MDC.clear | 阶段4 新增 |
| S2 | SaInterceptor | MVC 拦截器 | 登录校验 + 白名单 | 已具备（阶段3 起配合注解鉴权） |
| S3 | ContextInterceptor | MVC 拦截器 | 登录校验通过后填充 ThreadContextHolder(userId/username)，afterCompletion 清理——激活 TTL 工具 | 阶段4 新增 |
| S4 | @SaCheckPermission / @SaCheckRole | AOP（sa-token-spring-aop，依赖已引入） | 接口级授权 | 阶段3 启用 |
| S5 | @Log 操作日志 | AOP | 管理写操作审计落 sys_oper_log | 阶段6 新增 |
| S6 | @RepeatSubmit | AOP | Redis setnx + 参数摘要防重复提交 | 阶段7 评估 |
| S7 | LockTemplate（编程式锁，watchdog） | 工具 | 分布式锁统一入口 | 阶段5 新增 |

配套修复（阶段4 一并完成）：SpringContextHolder 由 `@Component` 改为 AutoConfiguration 注册（当前包路径扫描不到、永不装配）；新增 logback-spring.xml 统一日志 pattern（含 traceId、文件滚动，详见 12.4 L1）；引入 actuator 暴露健康端点；multipart 大小显式配置。

链路追踪闭环：G1 生成 → 转发头 → S1 写 MDC → 日志输出 → rpc starter（已具备）Feign 透传 → 下游服务 S1 → 响应头回写 X-Trace-Id（前端报障可对号入座）。

### 8.3 参考实现清单（如需源码对照，按此精准下载，无需整仓 clone）

| 目标组件 | 参考项目 | 位置 |
|---|---|---|
| TraceFilter / MDC / 过滤器装配 | RuoYi-Cloud-Plus | ruoyi-common/ruoyi-common-web（FilterConfig）+ ruoyi-gateway 过滤器包 |
| 网关鉴权过滤器与异常统一 | RuoYi-Cloud-Plus | ruoyi-gateway 的 filter/AuthFilter.java |
| @Log 操作日志切面 | pig | pig-common/pig-common-log（LogAspect + @Log 注解） |
| 全局异常处理增强版 | yudao-cloud | yudao-framework/yudao-spring-boot-starter-web 的 exception 包 |

结论：以上均为 Spring MVC/WebFlux 与 Sa-Token 的标准机制，**当前无需下载源码即可按蓝图开工**；阶段 4 动工时若要逐行对照，再按清单拉取（gitee 镜像优先）。

## 9. SQL 脚本规范（随 system 模块开发即日生效）

目录：`codeblog-modules/codeblog-system/sql/`（迁移时携带现有 init.sql 入驻）。

**双轨制**：

```
sql/
├── init.sql                  # 全量基线：新环境唯一初始化入口；阶段收尾(打 tag 前)合并当期增量
├── user_20261028.sql         # 增量示例：业务域_YYYYMMDD
└── rbac_2026MMDD.sql         # 阶段3 的 RBAC 表脚本
```

- **全量基线 init.sql**：docker-compose 首启挂载目标保持不变（路径随迁移更新为 modules 下的 init.sql）；每阶段收尾把该阶段增量合并回基线，保证新环境一条路初始化。
- **增量脚本 `<业务域>_<YYYYMMDD>.sql`**：每次表结构或初始数据变更**必须随代码同一提交**产出；已存在环境按文件名顺序手工执行；同日多版本加两位序号（如 `rbac_20261102_02.sql`）。

**硬性规则**：

1. 脚本必须幂等：`CREATE TABLE IF NOT EXISTS`、`INSERT ... SELECT ... WHERE NOT EXISTS`（沿用 init.sql 现有风格）；禁止 DROP 现网表。
2. 脚本开头显式 `USE codeblog;`，与基线一致。
3. 业务域用小写英文单词：user / rbac / dict / config / operlog / loginlog / seata / file / blog。
4. 涉及表结构变更的提交若缺增量 SQL，视为不完整提交，禁止合入（已同步写入 AGENTS.md 供智能体遵守）。

## 10. 决策记录（ADR）

| 编号 | 决策 | 理由 | 日期 |
|---|---|---|---|
| ADR-1 | 采用 codeblog-modules 聚合目录 | RuoYi-Cloud / pig 同构，业务服务收口，根 pom 保持清爽 | 2026-10-28 |
| ADR-2 | 用户与权限认证合并为 codeblog-system 单服务 | Sa-Token 共享会话模式下独立认证服务无价值；登录强依赖用户表；RuoYi/yudao 同构 | 2026-10-28 |
| ADR-3 | 与 modules 同步引入 codeblog-api 契约层 | 防止服务间依赖实现 jar；Feign 接口与 DTO 需要中立归属 | 2026-10-28 |
| ADR-4 | 路由前缀 /user/** → /system/** 随重组一次切换 | 生态未扩散，越晚改成本越高 | 2026-10-28 |
| ADR-5 | 博客业务独立 codeblog-blog 服务，二期启动 | 业务域/读写模型/发布节奏独立 | 2026-10-28 |
| ADR-6 | 流控熔断采用 Sentinel，规则持久化到 Nacos；网关限流用 Sentinel 网关流控替代 Gateway RequestRateLimiter | SCA 生态原生配套；规则重启不丢；与 Feign 熔断同栈 | 2026-10-28 |
| ADR-7 | Seata 就绪先行（AT 模式 + undo_log + compose 部署），@GlobalTransactional 待真实跨服务写场景（阶段 8 首验）再启用；单服务多表写一律 @Transactional | 满足"system 服务开发时接入"的要求，同时避免全局事务锁被滥用放大 | 2026-10-28 |
| ADR-8 | 日志收集选 Grafana Loki + Promtail + Grafana，EFK 为数据量增大后备选；APM（SkyWalking/Zipkin）二期可选 | 当前规模资源占用最小；traceId + Loki 过滤已构成排障闭环 | 2026-10-28 |
| ADR-9 | RocketMQ 接入采用 SCA Stream binder（版本随 SCA BOM = client 5.3.1），不引入 Apache rocketmq-spring-boot-starter 双体系；不建独立 mq starter（等第二个服务出现再抽）；broker 宿主机直连用 brokerIP1=127.0.0.1 | 与 Sentinel/Seata 同一 SCA 版本治理体系；SC Stream 绑定模型解耦 broker 细节；Docker Desktop 容器 IP 宿主机不可达（同 Seata grouplist 结论） | 2026-10-29 |

## 11. 阶段进度与回填记录

> 本节由"阶段收尾回填"动作维护（见第 6 节）：每阶段完成时更新进度表，并追加一条回填记录。回填记录只写**与计划的偏差与关键事实**，不重复计划内容；无偏差则写"无偏差"。

### 进度表

| 阶段 | 状态 | 完成日期 | tag |
|---|---|---|---|
| 0 规划 | ✅ 已完成 | 2026-10-28 | - |
| 1 模块重组 | ✅ 已完成 | 2026-10-28 | phase-1-restructure |
| 2 微服务治理组件 | ✅ 已完成 | 2026-10-28 | phase-2-governance |
| 3 RBAC 权限 | ✅ 已完成 | 2026-10-29 | phase-3-rbac |
| 4 拦截链与可观测 | ⬜ 未开始 | - | - |
| 5 数据与缓存 | ⬜ 未开始 | - | - |
| 6 组件完善 | ⬜ 未开始 | - | - |
| 7 日志收集与工程化 | ⬜ 未开始 | - | - |
| 8 博客业务 | ⬜ 未开始 | - | - |

### 回填记录

#### 阶段 0 · 规划（2026-10-28）

- 产出：本文档 v0.1（含第 8 节拦截链蓝图、第 9 节 SQL 双轨规范）、根目录 AGENTS.md（同日按 Mimosa 复查意见完成凭据整改：文档不再含明文口令，新增"安全红线"一节）
- 偏差：无（纯规划阶段）
- 决策：ADR-1 ~ ADR-5 于本阶段制定；SQL 双轨规范与回填机制系用户评审时追加需求，直接并入本版
- 2026-10-28 规划内修订：用户追加微服务治理组件（Nacos/Sentinel/Seata）与日志收集需求（第 12 节、ADR-6~8）；路线图由 0-7 扩为 0-8（新增阶段 2 治理组件接入，阶段 7 更名为日志收集与工程化，阶段 8 博客业务），全文阶段编号引用已同步顺延

#### 阶段 1 · 模块重组（2026-10-28）

- 产出：`codeblog-modules` 聚合 + `codeblog-system`（包名 `site.codeblog.system`，主类 `SystemApplication`，服务名 `codeblog-system`）；`codeblog-api` 聚合 + `codeblog-api-system` 空壳（pom + package-info，已入 BOM）；路由 `/user/**` → `/system/**` 全链切换（网关路由与白名单、前端 vite 代理与 api 封装、README/AGENTS.md/higress-guide 同步）；docker-compose init.sql 挂载路径更新为 modules 下并更名 `01_system.sql`；`.idea/encodings.xml` 路径同步
- 验证：`mvn clean install -DskipTests` 19 模块全绿；全链路回归 13/13 通过（登录、未登录 401、/me、分页、用户 CRUD 全流程、重复用户名拒绝 1004、doc.html 直连与经网关、旧前缀 `/user/**` 不再放行）
- 偏差与关键事实：
  - 迁移未在 IDEA 内 Refactor 完成，由智能体以 `git mv` + 逐文件编辑执行（git 重命名跟踪完整）；按已知坑 5 已清理 IDEA 外部存储缓存（IDEA 未运行时删除 `external_build_system` / `project-model-cache`），下次打开需等待 Maven 重新同步
  - 主类随包名一并更名 `UserApplication → SystemApplication`（计划未明说，属迁移一致性动作）
  - BOM 纳管 `codeblog-api-system` 但未纳管 `codeblog-system`（可部署服务不应被他方依赖，符合第 3 节边界规则）
  - Nacos 环境（如有）需手工新建 dataId `codeblog-system.yaml` 并迁移旧 `user-service.yaml` 配置；本地 local 联调不受影响，阶段 2 规范化时一并处理
  - 本阶段无表结构变更，`init.sql` 原样随迁移搬移（`codeblog-modules/codeblog-system/sql/`），无增量脚本

#### 阶段 2 · 微服务治理组件（2026-10-28）

- 产出：Nacos 规范化（去 bootstrap starter → `spring.config.import` 条件文档 + application-common.yaml 共享配置收敛白名单）；Sentinel 全链（system/gateway 依赖 + 规则 Nacos 持久化 dataId `{service}-{ruleType}-rules` + local 降级）；Seata 就绪（client 2.5.0 + seata-server 进 compose + undo_log 双轨 SQL）；compose 新增 sentinel-dashboard(8858) 与 seata-server(7091/8091)
- 验收实测：nacos 服务发现两服务注册、网关 lb 路由登录 200、dashboard `/app/names.json` 可见 codeblog-system/codeblog-gateway、/user/page 限 2 QPS 规则连打 10 次 4×200+6×429 且**服务重启后规则仍生效**（Nacos 重载）、Seata TM/RM 注册成功 + 数据源 AT 代理自动装配、local profile 回归 13/13 且 Nacos 客户端零启动
- 偏差与关键事实（均已同步 12.2/12.3 节）：
  - Seata 版本核验（开工首日实测项）结论：SCA 2025.0.0.0 的 seata starter 直依赖 `org.apache.seata:seata-spring-boot-starter:2.5.0`，与 Boot 3.5/SC 2025 配套成立；seata-server 镜像 `apache/seata-server:2.5.0` 经挂载 application.yml 注册进 Nacos
  - `spring.config.import` 是跨文档追加语义：profile 文档**无法覆盖**主文档的 import，local 隔离必须用 `on-profile: "!local"` 条件文档承载 nacos import（否则 local 下 Nacos 客户端仍会启动重连）
  - SCA 的 sentinel-datasource 模块将 `sentinel-datasource-nacos` 标记 optional，业务 pom 必须显式引入，否则数据源 FactoryBean 构建失败导致启动失败
  - sentinel 网关适配 artifact 为 `spring-cloud-alibaba-sentinel-gateway`（SCA 2025 无 starter-gateway 命名），需与核心 starter 并用
  - dashboard 镜像选型 bladex/sentinel-dashboard（Sentinel 1.8.10 内核，官方未发布镜像）：仅监控/临时推送定位，持久化规则一律在 Nacos 编辑（ADR-6 工作流的落地形态）
  - seata 客户端开发联调走 grouplist 直连 127.0.0.1:8091（Docker Desktop 容器 IP 宿主机不可达，注册发现地址仅同网络部署可用）；生产切换 registry=nacos 已在配置注释预留
  - 运维注意：Windows curl 向 Nacos 发布含中文配置会被 ANSI(GBK) 转码导致服务端 YAML 解析失败——线上操作一律走 Nacos 控制台（本阶段验收用 ASCII 百分号编码绕过）

#### 阶段 3 · RBAC 权限（2026-10-29）

- 产出：RBAC 五表（增量脚本 `rbac_20261028.sql`，已随收尾并入 `init.sql` 基线）；登录时装配 `LoginUser.roles/permissions`（admin 角色权限为通配 `*`，无需逐菜单绑定）；`DefaultStpInterfaceImpl` 从会话读取（兑现注释）；全部管理接口挂 `@SaCheckPermission`；用户⇄角色、角色⇄菜单双向授权接口；前端角色管理页（含菜单授权抽屉）、用户角色列/分配弹窗、按钮级 `hasPerm` 与侧栏按权限过滤；登录日志记录（成败双写、旁路不影响主流程）+ 分页查询接口（`/login-log/page`，权限 `system:login-log:list`）
- 验收实测：回归 26 项全过——admin 登录会话含通配权限；未登录 401；common 用户调用户/角色/登录日志等管理接口均 403；admin 角色 CRUD 与授权闭环正常；删除内置 admin 角色被拒（1000006）；错误密码 1001 且失败登录落日志；登录日志查询接口 admin 200 / common 403 / 用户名过滤生效
- 偏差与关键事实：
  - 主体代码由前一开发会话完成（未提交），本会话补齐验收、登录日志查询接口与收尾三件套
  - 登录日志按 12.4 L2 交付了查询接口，但管理端页面未做，随阶段 6 日志查询页统一开放（菜单种子 id=500 已预留 path `/system/login-logs`）；菜单管理页同理仅树查询（计划内）
  - 权限为登录会话快照模型：角色/授权变更后需重新登录生效（`/auth/me` 每次进后台重查，前端菜单/按钮即时反映，后端注解鉴权仍读会话）
  - 已知限制：`uk_role_key(role_key, del_flag)` 下同一 role_key 软删两次会撞唯一键（del_flag 仅 0/1），阶段 5 BaseEntity/审计落地时一并评估（如 del_flag 改存 id 或删除时间戳）
  - 移除了无调用方的 `RoleService.getByRoleKey`（死代码）

#### 2026-10-29 · RocketMQ 追加接入（用户需求，阶段 2 治理组件体系的延伸）

- 产出：SCA Stream binder 接入（依赖随 SCA BOM，无新增版本声明）+ compose 三件套（namesrv/broker/dashboard）+ 登录事件收发用例 + local 降级开关（详见 12.5 节与 ADR-9）
- 验收实测：登录请求 → 消费者收到登录事件（闭环通）；local 默认启动零 MQ 连接；broker 注册地址 127.0.0.1:10911 宿主机可达
- 偏差与关键事实：
  - dashboard 镜像（apache/rocketmq-dashboard:1.1.0）因 Docker Hub 网络超时暂未拉起，compose 定义已就绪，网络恢复后 `docker compose up -d rocketmq-dashboard` 即可
  - broker store 卷问题与 brokerIP1 问题均为实测踩坑，结论写入 12.5，compose/broker.conf 内有注释

## 12. 微服务治理组件与日志收集（阶段 2 / 7 实施依据；2026-10-28 追加；12.5 RocketMQ 为 2026-10-29 追加）

> 本节回应两条追加需求：①system 子服务开发时接入 Nacos / Sentinel / Seata；②系统与业务日志的收集方案。

### 12.1 Nacos —— 从"已接入"到"规范化"

现状：discovery + config 已接入（bootstrap 兼容模式）。阶段 2 目标：

1. 迁移 `spring-cloud-starter-bootstrap` → `spring.config.import: nacos:`（SCA 推荐姿势，减一个 starter）；
2. 配置分层：`{spring.application.name}.yaml`（私有）+ `application-common.yaml`（shared-configs 共享）——**网关与业务服务的两份鉴权白名单借此收敛为一份共享配置**，消除"带路由前缀/不带前缀"双维护的错配风险（各侧本地仍保留兜底白名单）；
3. namespace 按环境划分（dev / test / prod），group 默认 DEFAULT_GROUP；dataId = spring.application.name（阶段 1 服务改名时同步更新）；
4. local profile 维持免 Nacos 联调（既定原则不动摇）。

### 12.2 Sentinel —— 流控 / 熔断 / 网关限流

| 事项 | 方案 |
|---|---|
| 依赖 | `spring-cloud-starter-alibaba-sentinel`（业务服务）+ `spring-cloud-alibaba-sentinel-gateway`（网关，SCA 2025 无 starter-gateway 命名）；**必须显式引入 `sentinel-datasource-nacos`**（SCA datasource 模块将其标记 optional）；Feign 整合 `feign.sentinel.enabled=true` 阶段 6 落地 |
| Dashboard | bladex/sentinel-dashboard 镜像（Sentinel 1.8.10 内核，官方不发布镜像）已进 compose；**定位为监控视图 + 临时推送**，持久化规则不经过 dashboard（其推送仅到客户端内存） |
| 规则持久化 | `sentinel-datasource-nacos`：dataId 约定 `{service}-{ruleType}-rules`（flow/degrade/gw-flow），在 **Nacos 控制台**编辑，服务监听热加载、重启自动重载——2026-10-28 实测通过（限流规则重启后仍生效） |
| 网关流控 | `sentinel-spring-cloud-gateway-v6x-adapter`（随 SCA 2025，配套 SCG 4.3）：路由/API 分组维度，dataId `codeblog-gateway-gwflow-rules` |
| Feign 熔断 | fallbackFactory 定义在 codeblog-api 模块，与阶段 6 Feign 容错（ErrorDecoder 错误翻译）合并落地 |
| local 降级 | `spring.cloud.sentinel.enabled=false`：不接 dashboard、不加载规则（无规则即直通），本地联调零外部依赖 |

### 12.3 Seata —— 就绪先行，用例后置（ADR-7）

| 事项 | 方案 |
|---|---|
| 版本核验 | **已实测通过（2026-10-28）**：SCA 2025.0.0.0 的 `spring-cloud-starter-alibaba-seata` 直依赖 `org.apache.seata:seata-spring-boot-starter:2.5.0`（Apache Seata 2.5.0，按 Boot 3.5 配套），与本项目 Boot 3.5.16/SC 2025.0.3 组合启动、TM/RM 注册、AT 数据源代理均正常，无需排除冲突依赖 |
| 服务端 | seata-server 进 docker-compose（`apache/seata-server:2.5.0`，standalone；经挂载 `docker/seata/application.yml` 注册进 Nacos SEATA_GROUP；存储 file 模式起步，生产切 db）。注意：镜像默认 application.yml 的 `spring.main.web-application-type: none` 必须保留，去掉会启动失败 |
| 客户端 | 各业务库建 `undo_log` 表（增量脚本 `seata_20261028.sql`，已并入 init.sql 基线）；AT 模式数据源自动代理（实测 `Auto proxy data source 'dataSource' by 'AT' mode`）。**开发联调用 grouplist 直连 127.0.0.1:8091**（Docker Desktop 容器 IP 宿主机不可达），生产同网络部署切换 registry=nacos（配置已预留注释） |
| 使用纪律 | 单服务多表写**一律 @Transactional，禁止滥用 @GlobalTransactional**（无跨库语义时只会放大锁与回滚开销）；跨服务写场景由发起方标注 @GlobalTransactional；TCC/SAGA 待真实长事务场景再评估 |
| local 降级 | `seata.enabled=false`（不连 seata-server、关闭数据源代理） |
| 首个用例 | 阶段 8 博客业务的跨服务写（如发文的 system 侧联动），并完成故意回滚验证 |

### 12.4 日志收集 —— 三层设计

| 层 | 内容 | 落点 |
|---|---|---|
| L1 应用日志规范 | logback-spring.xml 统一：pattern 含 traceId、按天滚动（app-%d.log 保留 30 天）、error 单独文件、目录 `./logs/{service}/`；web starter 内置默认 + 各服务薄覆盖 | 阶段 4（与 MDC 同批落地） |
| L2 业务审计日志 | `@Log` 切面 → `sys_oper_log`（操作人/模块/动作/参数/结果/IP/耗时）；`sys_login_log` 登录成败记录；均提供管理端查询接口 | 登录日志随阶段 3；操作日志阶段 6 |
| L3 集中收集 | **Grafana Loki + Promtail + Grafana** 进 docker-compose：Promtail 采集 `./logs/` 按 {service}/{env} 打标签；Grafana 预置视图——按 traceId 反查全链路日志、按服务/级别/时间过滤（ADR-8）。EFK 为数据量增大后备选；APM（SkyWalking / Micrometer Tracing + Zipkin）二期可选，一期以自研 traceId + Loki 满足排障闭环 | 阶段 7 |

### 12.5 RocketMQ —— 消息队列（2026-10-29 追加接入，ADR-9）

| 事项 | 方案 |
|---|---|
| 依赖 | `spring-cloud-starter-stream-rocketmq`（SCA Stream binder，版本随 SCA BOM，内含 rocketmq-client 5.3.1）；不引入 Apache rocketmq-spring-boot-starter，避免双体系 |
| 选型理由 | 与 Sentinel/Seata 同随 SCA BOM 版本治理；Spring Cloud Stream 绑定模型解耦 broker 细节；仅一个业务服务，暂不抽独立 mq starter（第二个服务出现再抽 codeblog-spring-boot-starter-mq） |
| 服务端 | compose 三件套：namesrv(9876) + broker(10909/10911) + dashboard(8180)；镜像 `apache/rocketmq:5.3.1` 与客户端严格配套；broker 开发环境 autoCreateTopicEnable=true、异步刷盘 |
| 编程模型 | 生产者 `StreamBridge.send("loginEvent-out-0", msg)`；消费者 `@Bean Consumer<LoginEventMessage> loginEvent()` → 绑定 `loginEvent-in-0`（destination `codeblog-login-event`，group `codeblog-system`） |
| 首个用例 | 登录事件：成败双向发布、fire-and-forget 旁路容错（MQ 故障不影响登录）、消费侧日志落地；后续风控/通知/审计挂点 |
| local 降级 | `codeblog.mq.login-event.enabled=false`（application-local.yml）：条件注解使消费者与发布器 bean 均不注册 → 无消费绑定、零 MQ 连接（实测 local 启动无任何 rocketmq 连接日志） |
| 验证 | 2026-10-29 实测：登录 → StreamBridge 发送 → broker → 消费者收到「收到登录事件：username=admin, success=true」；local 默认启动零连接 |

**两个实际踩过的坑（均已注释在 compose/broker.conf 内）**：

1. **brokerIP1 必须显式指定 `127.0.0.1`**：默认注册容器 IP，Docker Desktop 下宿主机不可达（同 Seata grouplist 直连理由）；代价是 compose 内部容器互访 broker 受限（dashboard 部分页签可能异常）。
2. **开发编排不挂 broker store 卷**：命名卷以 root 属主初始化，镜像内 broker 用户（uid 3000）不可写 → broker 静默失败、退出码 255，且 shutdown 路径 NPE 掩盖真实原因；生产挂卷需 `chown 3000:3000`。开发环境存储随容器（重建即清空，与 Seata file 模式同定位）。
