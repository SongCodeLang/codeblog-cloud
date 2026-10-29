# AGENTS.md — codeblog-cloud 智能体工作指南

> 本文件是所有 AI 编码智能体（ZCode / Codex / Claude 等）在本仓库工作的强制上下文，修改代码前必须先通读。
> 架构与路线图见 `docs/ARCHITECTURE.md`；快速上手见 `README.md`。

## 项目定位

基于 Spring Boot 3.5 + Spring Cloud 2025 的微服务脚手架 + 博客平台后端，配套 React + TS 管理后台（codeblog-ui）。
当前阶段：阶段 1（模块重组）、阶段 2（治理组件）、阶段 3（RBAC 权限）、阶段 4（拦截链与可观测）已完成，下一步为阶段 5 数据与缓存；实施前先读架构文档的阶段路线图。

## 技术栈与版本矩阵（锁定，禁止随意升级）

| 组件 | 版本 | 备注 |
|---|---|---|
| JDK | 17 | |
| Spring Boot | 3.5.16 | 不走 Boot 4（JDK21 + Knife4j 不适配） |
| Spring Cloud | 2025.0.3 | |
| Spring Cloud Alibaba | 2025.0.0.0 | 与 SC 2025 / Boot 3.5 严格配套，SCA 2023.x 不兼容 |
| Sa-Token | 1.46.0 | 网关 Reactor 版 + 服务 Servlet 版 + Redis 共享会话 |
| MyBatis-Flex | 1.11.8 | 逻辑删除 del_flag / 乐观锁 version |
| Redisson | 4.8.0 | |
| Druid | 1.2.28 | |
| Knife4j / SpringDoc | 4.5.0 / 2.8.x | |
| FastExcel | 1.3.0 | cn.idev.excel |

规则：第三方版本只允许在 `codeblog-dependencies/pom.xml`（BOM）中声明，业务与 starter 模块 pom 禁止写版本号。升级任何一个框架前必须先核对兼容矩阵。

微服务治理四件套：Nacos（注册/配置中心）、Sentinel（流控熔断，规则持久化到 Nacos）、Seata（分布式事务，AT 模式）、RocketMQ（消息队列，SCA Stream binder）。**local profile 降级约定**：Sentinel 不接 dashboard、Seata 关闭数据源代理、RocketMQ 关闭登录事件收发（零 MQ 连接），保持本地免外部依赖联调。事务纪律：单服务多表写用 @Transactional，跨服务写才由发起方标注 @GlobalTransactional，禁止滥用。治理设计详见 `docs/ARCHITECTURE.md` 第 12 节。

## 构建与运行

以下为本机（Windows）开发环境专用路径，换机请自行替换：

- Maven：`D:\software\maven\apache-maven-3.9.16\bin\mvn.cmd -s D:\software\maven\settings.xml`（必须带 `-s`，阿里云镜像）
- 全量构建：`mvn clean install -DskipTests`（项目未继承 spring-boot-starter-parent，repackage 已在各服务 pom 显式配置，勿删）

本地联调（免 Nacos）：

1. 起 MySQL 8.4 与 Redis 7.4（ServBay 或 `docker/docker-compose.yml`；数据库口令以 docker-compose 环境变量或本机 ServBay 配置为准，禁止在任何文档中明文记录）
2. 首次导入初始化 SQL（`codeblog-modules/codeblog-system/sql/init.sql`）
3. 启动服务，均加 `--spring.profiles.active=local`：
   - 系统服务 codeblog-modules/codeblog-system：端口 48081
   - 网关 codeblog-gateway：端口 9999
4. 前端：`cd codeblog-ui && npm install && npm run dev`（5173，/system 代理到网关）
5. 默认演示账号：见初始化 SQL 脚本内的注释说明（`codeblog-modules/codeblog-system/sql/init.sql`），不在此明文记录

## 模块地图（重组细节见 docs/ARCHITECTURE.md）

```
codeblog-dependencies/   BOM，第三方版本唯一管理点
codeblog-framework/      技术组件层（starter + common），不含任何业务代码
codeblog-api/            服务对外契约：Feign 接口 + DTO，无实现
  codeblog-api-system/   系统服务契约（首个真实接口随阶段 6 落地）
codeblog-modules/        业务服务聚合，每个子模块 = 一个可独立部署服务
  codeblog-system/       系统管理服务（认证/用户，角色/菜单/字典/配置/日志随阶段演进）
  codeblog-blog/         （二期）博客业务：文章/分类/标签/评论
codeblog-gateway/        网关：路由 / CORS / Sa-Token 统一登录鉴权
codeblog-ui/             React 管理后台
docker/                  基础设施编排（mysql/redis/nacos/sentinel-dashboard/seata-server 已就绪；阶段7 增 loki/promtail/grafana）
```

依赖方向铁律：framework 不得依赖 api/modules；api 不得依赖 modules 实现；modules 之间禁止互相依赖实现 jar，跨服务调用走 Feign + codeblog-api。

## 代码规范

- 响应统一 `Result<T>`（HTTP 恒 200，业务码在 code 字段，200 成功 / 401 未登录）；分页用 `PageParam`/`PageResult`（pageSize ≤ 100）
- 业务异常抛 `BusinessException` + 错误码；`GlobalErrorCode` 只放 HTTP 语义码与全局码，业务错误码放各服务的 ErrorCodeConstants（规划中）
- 分层：Controller（VO + 校验注解）→ Service → Mapper；PO 不出服务边界；对象转换一律 MapStruct（参考 UserConvert）
- 新增 starter Bean 的三件套：`@AutoConfiguration` + `META-INF/spring/...AutoConfiguration.imports` 注册 + `@ConditionalOnMissingBean` 留覆盖点；对外环境依赖（servlet/web/驱动）用 `provided`/`optional`
- 配置前缀统一 `codeblog.*`；新增配置项必须同步更新 README 的配置速查表
- 数据库约定：逻辑删除 `del_flag`（0/1），乐观锁列 `version`（表里有该列才生效），审计字段 create_time/update_time/create_by/update_by（规划自动填充）
- SQL 脚本规范：业务服务的 `sql/` 目录双轨制——全量基线 `init.sql`（阶段收尾刷新，新环境/docker 首启唯一入口）+ 增量脚本 `业务域_YYYYMMDD.sql`（表结构或初始数据变更必须随代码同一提交产出，同日多版本加 `_02` 序号）；脚本一律幂等（`IF NOT EXISTS` / `WHERE NOT EXISTS`），开头 `USE codeblog;`，禁止 DROP 现网表；缺增量 SQL 的结构变更视为不完整提交

## 已知坑（改代码前必读，均实际踩过）

1. **Redisson 4.x 会自动注册 `redisTemplate`/`stringRedisTemplate`** —— 自定义模板必须用独立 bean 名 `codeblogRedisTemplate`，禁止改回默认名，否则启动冲突
2. **Sa-Token 1.46 Jackson 序列化有全局类型白名单** —— 会话中存自定义对象必须 `SaJsonStrategy.instance.registerAllowType(X.class)`（见 SecurityAutoConfiguration 静态块）
3. **token 约定 `Authorization: Bearer <token>`** —— sa-token 的 token-name/token-prefix 配置在网关与所有业务服务必须完全一致
4. **鉴权白名单双份维护** —— 网关白名单是路由前缀路径（如 `/system/auth/login`），业务服务白名单是 StripPrefix 后路径（如 `/auth/login`），改一处必须评估另一处
5. **模块/目录重命名必须在 IDEA 内 Refactor → Rename/Move 完成** —— 在 IDEA 外重命名 Maven 目录会残留外部存储模块缓存（Project 视图出现 `name[name]`），修复需关 IDEA 删缓存
6. **未继承 spring-boot-starter-parent** —— 新增可执行服务模块时，spring-boot-maven-plugin 必须显式 `<execution>repackage</execution>`
7. `.flattened-pom.xml` 已被 git 忽略，勿提交
8. **Spring Data Redis 3.5 的 SerializationPair 在 RedisSerializationContext 嵌套**（非 RedisSerializer 内部）；且缓存值含 LocalDateTime 时 ObjectMapper 必须注册 jackson-datatype-jsr310，否则写 Redis 报 "Java 8 date/time type not supported"（redis starter 的 buildJsonObjectMapper 已统一处理，勿另建序列化器）
9. **实体继承 BaseEntity 后，表必须有全部审计列**（create_by/create_time/update_by/update_time）+ 对应 version 列（若 PO 声明 @Column(version=true)），否则 BadSqlGrammar——新表设计默认带全审计列，参考 sys_role
10. **含 del_flag 的唯一键在同 key 二次软删时必撞键**（如 uk(item, del_flag)：删→重建→再删，第二次软删与墓碑冲突报 DuplicateKey）——新表禁止把 del_flag 放进唯一键，唯一性用服务层查重（字典表已按此落地，RuoYi 同款无 uk）；sys_user/sys_role 的存量 uk 按已知限制保留

## 安全红线

- 禁止读取、展示或向任何外部服务上传凭据类文件（Maven `settings.xml`、`.env`、`*.pem`/`*.jks`/`*.p12`、token 与密码文件）；构建命令只允许引用其路径
- 禁止把口令、密钥、token 写入仓库的任何新增或修改文件（含文档与注释）；开发环境口令只存在于 docker-compose 环境变量、本机配置或 SQL 初始化脚本等既有位置
- 任何对外发送操作（git push、发布构件到远程仓库、把仓库内容提交给外部服务或站点）必须逐次征得用户明确确认

## Git 约定

- 提交信息：中文 conventional 风格，如 `feat(系统管理): 新增角色分页查询`、`fix(网关): 修正鉴权白名单`
- 用户说"提交"仅指 git commit；**未经明确要求禁止 push**（对外发送规则见"安全红线"）
- 默认分支 master

## 当前待办与阶段纪律

- 落地排期见 `docs/ARCHITECTURE.md` 第 6 节阶段路线图，动工前先对照所在阶段的验收标准
- **阶段收尾强制动作**（每完成一个阶段，缺一视为阶段未完成）：① 当期增量 SQL 合并回 `init.sql` 基线；② 回填 `docs/ARCHITECTURE.md` 第 11 节——进度表补状态/日期/tag，实现偏差写回填记录，文档章节与代码不一致处以代码为准更新，新决策追加 ADR，协作规范变化同步本文件与 README；③ 打 git tag（`phase-N-主题`）

评审报告（2026-10-28）确认的优先级：P0 = RBAC 权限体系断链、Excel 注解与 FastExcel 未打通、SpringContextHolder 未装配；P1 = 拦截链/可观测性、业务缓存、File 存储抽象等。
