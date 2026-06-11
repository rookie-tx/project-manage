# ai-agent 项目规范

## 技术栈

| 组件 | 版本/选型 |
|------|----------|
| Java | 25 |
| 框架 | Spring Boot 3.5.14 |
| 构建 | Maven (wrapper `./mvnw`) |
| ORM | MyBatis 3.0.5 |
| 数据库 | PostgreSQL (环境变量 `DS_URL`, `DS_USERNAME`, `DS_PASSWORD`) |
| 工具 | Lombok, MapStruct 1.6.3, Spring Security Crypto (BCrypt) |
| 对象映射 | MapStruct (PO↔Domain 用 default 方法 + 表达式, DTO↔Domain/Command 自动生成) |

## 分层架构 (DDD 四层)

每个业务模块严格遵循依赖方向：**api → application → domain ← infrastructure**

```
cn.tx.aiagent.<module>.
├── api/              # Controller + Request/Response DTO
├── application/      # 应用服务 (编排)、接口定义
├── domain/           # 领域实体、值对象、领域服务、仓储接口
└── infrastructure/   # 领域接口实现、MyBatis Mapper、PO 对象
```

**关键原则：domain 层零框架注解** — 不允许出现 `@Service`, `@Component`, `@Repository`, MyBatis 注解等。

## 包命名与职责

- `cn.tx.aiagent.<module>.api` — `@RestController`, Request/Response DTO, `@Validated` 校验
- `cn.tx.aiagent.<module>.application` — 应用层服务 (`@Service`)，编排领域服务，管理事务边界
- `cn.tx.aiagent.<module>.domain` — 领域实体 (`@Getter`), record 值对象, sealed interface, 仓储接口, 领域服务(无框架注解)
- `cn.tx.aiagent.<module>.infrastructure` — `@Component` 仓储实现, `@Configuration` 暴露领域服务 Bean, MyBatis Mapper 接口, PO 持久化对象
- `cn.tx.aiagent.common.api` — 全局 `Result<T>`, `ErrorCode` 枚举, `GlobalExceptionHandler`
- `cn.tx.aiagent.common.exception` — `BusinessException`
- `cn.tx.aiagent.common.config` — 静态持有者类 (如 `MessageSourceHolder`)

## 命名约定

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| Controller | `*Controller` | `LoginController` |
| 应用服务 | `*ApplicationService` | `LoginApplicationService` |
| 领域服务 | `*DomainService` | `LoginDomainService` |
| 仓库接口 | `*Repository` (接口) | `UserRepository` |
| 仓库实现 | `*RepositoryImpl` (@Component) | `UserRepositoryImpl` |
| MyBatis Mapper | `*Mapper` (接口) | `UserMapper` |
| 持久化对象 | `*PO` (POJO) | `UserPO` |
| 配置类 | `*Config` | `CorsConfig`, `LoginDomainConfig` |
| 领域值对象 | Java `record` | `LoginCredential`, `LoginContext` |
| 领域实体 | 类 + `@Getter` | `User` |
| DTO | `*Request` / `*Response` | `LoginRequest`, `LoginResponse` |
| 数据库表 | `t_*` 前缀 | `t_user` |
| MyBatis XML | `src/main/resources/mapper/*Mapper.xml` | `UserMapper.xml` |

## 代码模式

### 事务

当涉及到多个表的操作时，必须加上`@Transactional(rollbackFor = Exception.class)`

### DI: 构造器注入 (final 字段)

```java
public class LoginDomainService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public LoginDomainService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }
}
```

### 领域服务通过 @Configuration 暴露为 Bean (不在领域类上加注解)

```java
@Configuration
public class LoginDomainConfig {
    @Bean
    public LoginDomainService loginDomainService(UserRepository userRepository, PasswordHasher passwordHasher) {
        return new LoginDomainService(userRepository, passwordHasher);
    }
}
```

### sealed interface + record 表示结果类型

```java
public sealed interface AuthenticationResult {
    record Authenticated(long userId, String username) implements AuthenticationResult {}
    record InvalidCredentials() implements AuthenticationResult {}
    record AccountDisabled() implements AuthenticationResult {}
}
```

### 领域实体使用静态工厂方法

```java
@Getter
public class User {
    private Long id;
    private String username;
    private UserStatus status;
    // ...

    private User() {}  // 私有构造

    public static User fromPersistence(Long id, String username, ...) {
        User user = new User();
        user.id = id;
        // ...
        return user;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}
```

### Controller 使用 switch 模式匹配处理 sealed interface

```java
return switch (result) {
    case AuthenticationResult.Authenticated auth ->
            Result.success(new LoginResponse(auth.userId(), auth.username()));
    case AuthenticationResult.AccountDisabled __ ->
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
    case AuthenticationResult.InvalidCredentials __ ->
            throw new BusinessException(ErrorCode.CREDENTIALS_INVALID);
};
```

### 对象映射: MapStruct

所有 Entity ↔ PO ↔ DTO 转换通过 MapStruct Mapper 接口完成，禁止手写 setter 映射。

**Mapper 分类:**

| Mapper | 位置 | 职责 |
|--------|------|------|
| `*PoMapper` | infrastructure | PO ↔ Domain，枚举→code 用表达式 |
| `*DtoMapper` | application | Domain ↔ Request/Response DTO |

**PO Mapper** — `toDomain` 使用 default 方法委托实体工厂，`toPO` 由 MapStruct 自动生成：

```java
@Mapper(componentModel = "spring")
public interface ProjectPoMapper {

    default Project toDomain(ProjectPO po) {
        if (po == null) return null;
        return Project.fromPersistence(
                po.getId(), po.getCode(), po.getName(), po.getType(),
                po.getLocation(), po.getClientName(), po.getSupervisorUnit(),
                po.getProjectManagerId(), po.getPlannedStartDate(),
                po.getPlannedEndDate(), po.getActualStartDate(),
                po.getActualEndDate(), po.getStatus(), po.getRemark(),
                po.getIsDeleted(), po.getCreatedBy(),
                po.getCreatedAt(), po.getUpdatedAt()
        );
    }

    @Mapping(target = "type", expression = "java(project.getType() != null ? project.getType().getCode() : null)")
    @Mapping(target = "status", expression = "java(project.getStatus() != null ? project.getStatus().getCode() : null)")
    ProjectPO toPO(Project project);
}
```

**DTO Mapper** — 全自动映射，需额外处理的字段用 `ignore` + 调用方补充：

```java
@Mapper(componentModel = "spring")
public interface ProjectDtoMapper {

    @Mapping(target = "type", expression = "java(project.getType() != null ? project.getType().getCode() : null)")
    @Mapping(target = "status", expression = "java(project.getStatus() != null ? project.getStatus().getCode() : null)")
    @Mapping(target = "projectManagerName", ignore = true)
    ProjectResponse toResponse(Project project);

    ProjectUpdateCommand toCommand(UpdateProjectRequest request);
}
```

**仓储实现注入 Mapper：**

```java
@Component
public class ProjectRepositoryImpl implements ProjectRepository {
    private final ProjectMapper projectMapper;
    private final ProjectPoMapper poMapper;

    public ProjectRepositoryImpl(ProjectMapper projectMapper, ProjectPoMapper poMapper) {
        this.projectMapper = projectMapper;
        this.poMapper = poMapper;
    }

    @Override
    public Optional<Project> findById(Long id) {
        ProjectPO po = projectMapper.selectById(id);
        if (po == null) return Optional.empty();
        return Optional.of(poMapper.toDomain(po));
    }

    @Override
    public void save(Project project) {
        projectMapper.insert(poMapper.toPO(project));
    }
}
```

## API 规范

### 统一响应格式

所有响应使用 `Result<T>` 包装：
```json
{ "code": 0, "msg": "操作成功", "data": { "userId": 1, "username": "admin" } }
```

- `Result.success()` / `Result.success(data)` — 成功响应
- `Result.error(errorCode, args...)` — 错误响应
- 即使错误也返回 HTTP 200，通过 `code` 字段区分
- `data` 字段始终序列化 (null 也输出)

### ErrorCode 范围

| 范围 | 分类 |
|------|------|
| 0 | 成功 |
| 1xxx | 参数/校验错误 |
| 2xxx | 认证/授权错误 |
| 5xxx | 系统/服务器错误 |
| 9xxx | 未知错误 |

### 异常处理

- `BusinessException(ErrorCode, Object... args)` — 业务异常，由 `GlobalExceptionHandler` 捕获
- DTO 使用 `jakarta.validation` (`@NotBlank`, `@Size`) 校验，校验失败由 `GlobalExceptionHandler` 统一处理

### i18n

- 消息文件: `src/main/resources/i18n/messages[_en_US].properties`
- 默认中文 `zh_CN`
- 非 Bean 代码通过 `MessageSourceHolder.getMessage(key, args)` 获取国际化消息

## 测试规范

### 领域服务测试 (纯单元测试，不加载 Spring 上下文)

```java
class LoginDomainServiceTest {
    private UserRepository userRepository;
    private PasswordHasher passwordHasher;
    private LoginDomainService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordHasher = mock(PasswordHasher.class);
        service = new LoginDomainService(userRepository, passwordHasher);
    }
    // @Test methods...
}
```

### Controller 测试 (Web MVC 切片测试)

```java
@WebMvcTest(LoginController.class)
class LoginControllerTest {
    @MockitoBean private LoginApplicationService loginApplicationService;
    // MockMvc + JSON path assertions...
}
```

### 验证策略

- 领域服务: 验证返回的 sealed interface 类型和字段值
- Controller: 验证 HTTP 状态码和 JSON 结构

## 构建命令

```bash
./mvnw compile          # 编译
./mvnw test             # 运行测试
./mvnw spring-boot:run  # 启动应用
```

## 数据库约定

### 建表规范

所有业务表必须遵循以下模板：

```sql
CREATE TABLE IF NOT EXISTS t_<name> (
    id          BIGSERIAL    PRIMARY KEY,
    -- 业务字段...
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 序列由 BIGSERIAL 自动创建并关联，无需手动管理
```

- **主键**：必须使用 `BIGSERIAL`（PostgreSQL 自增），禁止使用 `INTEGER` 或手动管理序列
- **时间戳**：所有表必须包含 `created_at` + `updated_at`，`updated_at` 通过触发器自动更新
- **状态字段**：使用 `SMALLINT` 类型，Java 侧通过枚举映射（`fromCode()` / `getCode()`）
- **软删除**：需要软删除的表加 `is_deleted SMALLINT NOT NULL DEFAULT 0`（0=正常, 1=已删除）
- **表名**：统一 `t_` 前缀 + 下划线分隔小写（如 `t_user_role`）
- **MyBatis 配置**：`map-underscore-to-camel-case: true`（自动下划线转驼峰）

### 序列维护规范

`schema.sql` 末尾必须包含序列校准语句，确保序列值与表中已有数据一致：

```sql
-- 每个 BIGSERIAL 主键表对应一行
SELECT setval('t_<name>_id_seq', COALESCE((SELECT MAX(id) FROM t_<name>), 1));
```

原因：如果表中已有手动插入的数据（如初始化脚本），序列不会自动感知，后续通过应用 INSERT 会因主键冲突失败。新增表时必须同步添加对应的 `setval` 语句。

## 审批流程规范

### 流程引擎

使用 Flowable BPMN 引擎（仅引入 `flowable-spring-boot-starter-process`），BPMN 定义文件位于 `src/main/resources/processes/`。

### 流程路由规则

多节点审批中，拒绝退回上一节点，全部通过才算通过：

```
部门经理审批 ──通过──▶ 财务审批 ──通过──▶ 总经理审批 ──通过──▶ 已通过
     │                    │                      │
    拒绝                  拒绝                    拒绝
     │                    │                      │
     ▼                    ▼                      ▼
   已驳回            部门经理审批            财务审批
                  (退回上一节点)          (退回上一节点)
```

- 首节点（部门经理）拒绝 → 流程终止，到达 `rejected` 终点
- 中间节点（财务）拒绝 → 退回首节点（部门经理），流程继续
- 末节点（总经理）拒绝 → 退回上一节点（财务），流程继续
- 全部节点通过 → 到达 `approved` 终点

### 任务模块与业务解耦

任务模块（`cn.tx.aiagent.task`）是通用审批引擎，不依赖具体业务。

**业务接入方式**：实现 `BusinessTaskCallback` 接口，注册为 Spring Bean 后由 `CallbackRegistry` 自动发现。

| 钩子方法 | 调用时机 | 用途 |
|---------|---------|------|
| `getType()` | 注册 | 返回关联的 `BusinessType` 枚举 |
| `getBusinessInfo(businessId)` | 启动流程 | 返回业务标题/副标题（存入流程变量） |
| `validateBeforeStart(businessId, variables)` | 启动流程前 | 校验业务状态，不合法则抛异常 |
| `onProcessStarted(businessId)` | 流程启动后 | 更新业务状态为"审批中" |
| `onTaskCompleted(businessId, result)` | 每个任务完成后 | 处理审批结果（通过/拒绝） |

**BusinessType 枚举**：每个值携带 `processDefinitionKey`，指向对应的 BPMN 流程定义。

**操作校验**：
- 查询任务列表：通过 `taskCandidateOrAssigned(userId)` 过滤，用户只能看到分配给自己或候选组的任务
- 完成任务：校验当前登录人是否为任务 `assignee`，不匹配则抛出 `TASK_NOT_OWNER`

**审批结果存储**：每个任务的 `approvalResult` 和 `comment` 同时存入任务局部变量（历史查询独立读取）和流程变量（网关条件表达式路由）。

**WorkflowInstance 状态更新**：完成任务后检查是否还有活跃任务，无活跃任务时（流程到达终点）才更新状态为完成或终止。
