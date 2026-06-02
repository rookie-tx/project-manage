## Context

当前系统通过 `integrate-flowable-approval` 变更引入了基于 Flowable 的审批工作流，但实现上 task 模块与 `purchaserequest` 业务紧耦合。系统未来会为 `project` 等其他业务模块添加审批能力，需要先将 task 模块抽象为通用审批引擎。

**当前耦合点：**

```
TaskApplicationService
  ├─ 依赖 PurchaseRequestRepository（驳回时更新状态、查询展示信息）
  ├─ TaskResponse 含 prName、purchaseRequestId 专属字段
  └─ toTaskResponse() 硬编码 PR 查询逻辑

PurchaseRequestApplicationService
  ├─ submit() 直接调 RuntimeService.startProcessInstanceByKey()
  ├─ getApprovalHistory() 直接调 HistoryService
  ├─ toResponse() 直接调 TaskService 查当前审批节点
  └─ PurchaseRequest 实体持有 processInstanceId
```

## Goals / Non-Goals

**Goals:**
- task 模块零业务依赖，仅依赖 `BusinessTaskCallback` 接口
- 业务模块通过 `businessType + businessId` 与 task 模块交互
- 新增业务类型只需：新增 `BusinessType` 枚举值 + 实现 `BusinessTaskCallback`
- domain 层引入 `WorkflowTask` 实体包装 Flowable 原生类型，保持零框架注解
- 提供通用审批历史查询端点

**Non-Goals:**
- 不改变 Flowable 引擎本身的行为
- 不修改 BPMN 流程定义（仍使用现有 `purchase-request-approval` 流程）
- 不改变前端审批对话框的审批逻辑（通过/驳回），仅适配字段
- 不引入事件总线或异步消息机制

## Decisions

### 1. BusinessType 枚举携带流程定义 Key

```
BusinessType 枚举
  ├─ PURCHASE_REQUEST("purchase-request-approval")
  ├─ 未来: PROJECT("project-approval")
  └─ 每个值自带 processDefinitionKey
```

**理由**：枚举值即文档，编译期检查，新增业务类型只需加一行。相比配置文件更直观，比数据库配置更轻量。

**替代方案**：配置文件（`application.yml` 中维护映射）——更灵活但割裂了类型定义，且对于编译期可确定的映射没有实际收益。

### 2. BusinessTaskCallback 接口 + Spring 自动发现

```java
public interface BusinessTaskCallback {
    BusinessType getType();
    BusinessInfo getBusinessInfo(Long businessId);
    void onTaskCompleted(Long businessId, TaskResult result);
}
```

`CallbackRegistry` 通过构造器注入 `List<BusinessTaskCallback>` 收集所有实现，内部维护 `Map<BusinessType, BusinessTaskCallback>`。

**理由**：标准 Spring 模式，无需额外框架。各业务模块在自己的 infrastructure 包内实现，修改隔离在模块内部。

### 3. 展示信息在 startProcess 时预填充到流程变量

`startProcess` 时调用 `callback.getBusinessInfo(id)` 得到 `{title, subtitle}`，写入流程变量 `businessTitle`、`businessSubtitle`。列表查询时从变量直接读取，无需回调查库。

**理由**：避免 N+1 查询——任务列表可能有 20 条记录，如果每条都要回调业务模块查库，性能很差。流程变量在 Flowable 内部已经是键值存储，读取成本低。

**风险**：业务标题变更后，流程变量中的旧值不会自动更新。→ 对于审批场景，提交后标题不应再变，这是合理约束。

### 4. WorkflowInstance 独立表

```sql
CREATE TABLE t_workflow_instance (
    id BIGSERIAL PRIMARY KEY,
    business_type VARCHAR(50) NOT NULL,
    business_id BIGINT NOT NULL,
    process_instance_id VARCHAR(64) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE UNIQUE INDEX idx_wf_business ON t_workflow_instance(business_type, business_id);
```

**理由**：
- 业务实体不再持有 `processInstanceId`，保持领域纯净
- 支持同一业务未来有多版本流程实例（去掉 unique 索引即可）
- task 模块完全拥有这张表，不侵入业务表结构

### 5. WorkflowTask 领域实体包装 Flowable Task

```java
@Getter
public class WorkflowTask {
    private final String taskId;
    private final String nodeName;
    private final String businessType;
    private final Long businessId;
    private final String businessTitle;
    private final String businessSubtitle;
    private final LocalDateTime createTime;
    // 私有构造 + 静态工厂 fromFlowableTask()
}
```

`WorkflowTaskRepository` 接口返回此实体（而非 `org.flowable.task.api.Task`），domain 层保持零框架注解。

### 6. 通用审批历史端点

`GET /api/tasks/history?businessType=PURCHASE_REQUEST&businessId=123`

内部流程：
1. 通过 `businessType + businessId` 查 `t_workflow_instance` 获取 `processInstanceId`
2. 查 Flowable `HistoricTaskInstance` 获取已完成任务列表
3. 返回通用 `HistoryResponse(taskId, nodeName, assignee, result, comment, time)`

**理由**：审批历史的查询逻辑对任何业务类型都一样——查历史任务表。没必要每个业务模块重复实现。

### 7. startProcess 签名

```java
public void startProcess(BusinessType businessType, Long businessId, Map<String, Object> variables)
```

业务模块负责构建流程变量（含审批人 ID），task 模块负责：
- 查 `BusinessType` 获取 `processDefinitionKey`
- 自动注入 `businessType`、`businessId`、`businessTitle`、`businessSubtitle` 到变量
- 启动流程实例
- 持久化 `WorkflowInstance`

**理由**：业务模块仍控制自己的流程变量（审批人等信息），task 模块负责通用的框架性工作。

## Risks / Trade-offs

- **[风险] `BusinessTaskCallback.onTaskCompleted` 中如果业务操作失败** → task 模块先完成 Flowable 任务，再回调。回调失败时 Flowable 任务已完成但业务状态未更新，产生不一致。缓解：回调中业务操作应尽量简单（更新状态），且由数据库事务保护。
- **[风险] 历史 BPMN 流程变量名变更** → 当前 BPMN 使用 `${purchaseRequestId}`，改为 `${businessId}`。需要同步更新 BPMN 文件和所有引用点。
- **[取舍] 流程定义 Key 硬编码在枚举** → 新增业务类型需要改代码、编译、部署。当前系统规模下可接受，未来如频繁新增可改为数据库配置。
- **[取舍] 同一业务只允许一个活跃流程实例** → 通过 unique 索引保证。审批场景合理：一个采购申请不应同时有两个审批流程在跑。

## Migration Plan

1. 创建 `t_workflow_instance` 表（DDL 自动执行或手动）
2. 新增 task 模块 domain/infrastructure 代码（纯新增，不影响现有功能）
3. 重构 `TaskApplicationService` 和 `TaskController`（适配新接口）
4. 实现 `PRTaskCallback`，重构 `PurchaseRequestApplicationService`
5. 迁移 `t_purchase_request.process_instance_id` 数据到 `t_workflow_instance`
6. 移除 `t_purchase_request.process_instance_id` 列
7. 更新 BPMN 流程变量名（`purchaseRequestId` → `businessId`）
8. 前端适配通用字段

建议在一个 PR 中完成全部变更，通过编译和测试保证一致性。

## Open Questions

- 当前 BPMN 文件 `purchase-request-approval.bpmn20.xml` 使用 `${purchaseRequestId}` 变量名，是否需要改为通用的 `${businessId}`，还是保持兼容？
