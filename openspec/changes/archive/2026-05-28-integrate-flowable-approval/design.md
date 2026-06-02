## Context

当前采购申请模块使用硬编码枚举 `PurchaseRequestStatus`（PENDING/CANCELLED）管理状态，`canTransitionTo()` 方法实现简单状态机。采购申请后续需要经过"提交→部门审批→财务审批"等环节后才能确认采购，需要接入工作流引擎。

约束：
- Java 25 + Spring Boot 3.5.14 + PostgreSQL
- DDD 四层架构，domain 层零框架注解
- Flowable 6.8+ 适配 Spring Boot 3.x
- BPMN 流程定义需版本可控（XML 文件纳入代码仓库）

## Goals / Non-Goals

**Goals:**
- 集成 Flowable 引擎，采购申请通过 BPMN 流程驱动状态流转
- 实现"提交审批→部门审批→财务审批"三级审批流程，支持审批通过/驳回
- 提供待办任务列表接口和审批操作接口
- 前端展示审批状态、待办任务、审批历史时间线

**Non-Goals:**
- 不实现动态审批人分配（审批人由流程变量在启动时指定）
- 不实现条件分支（第一版为线性审批链）
- 不实现会签（并行审批）
- 不恢复已驳回的流程（驳回后重新提交需新建采购申请）

## Decisions

### 1. Flowable 集成策略：Spring Boot Starter

选择 `flowable-spring-boot-starter`（版本 7.0.0），理由：
- Spring Boot 3.x 原生支持，自动配置 DataSource、ProcessEngine
- BPMN 2.0 XML 流程定义放在 `src/main/resources/processes/`，自动部署
- 无需额外数据库配置，复用项目已有的 PostgreSQL 连接

备选方案：手动构建 `ProcessEngineConfiguration`。否决原因：与 Spring Boot 自动配置重复，增加配置复杂度。

### 2. domain 层：processInstanceId 而非继承 Flowable 实体

`PurchaseRequest` 实体新增 `processInstanceId` 字段（String 类型），不继承 Flowable 的 Entity 体系：
- domain 层保持零框架注解
- `PurchaseRequest` 仍是纯 POJO，不依赖 Flowable API
- 通过 `processInstanceId` 关联 Flowable 的 `act_hi_procinst` 表

### 3. 状态枚举扩展

```java
PENDING(0, "待提交"),      // 新建后未提交审批
APPROVING(1, "审批中"),     // 已提交，流程运行中
APPROVED(2, "已通过"),      // 审批全部通过
REJECTED(3, "已驳回"),      // 审批被驳回
CANCELLED(4, "已取消");     // 手动取消（保留原有语义）
```

`canTransitionTo()` 方法废弃，状态流转由 Flowable 流程驱动。`CANCELLED` 保留，允许在提交审批前手动取消。

### 4. 审批流程设计（BPMN 2.0）

```
[开始] → [提交审批] → [部门经理审批] → [财务审批] → [总经理审批] → [结束(已通过)]
                  ↘            ↘             ↘
                   [结束(已驳回)]
```

每个审批节点：
- 使用 Flowable `UserTask`，审批人通过流程变量 `${deptManagerId}` 等指定
- 审批意见写入 `comment` 字段
- 驳回直接结束流程，流程变量 `outcome=REJECTED`

备选方案：审批人通过 Flowable Identity Service 动态查询。否决原因：当前系统无角色体系，流程变量方式更简单直接，后续可升级。

### 5. 提交审批接口设计

`POST /api/purchase-requests/{id}/submit`：启动 Flowable 流程实例，传入审批人变量，更新 status 为 APPROVING。

审批操作复用 Flowable Task API：
- `GET /api/tasks?assignee={userId}` — 查询待办列表
- `POST /api/tasks/{taskId}/complete` — 完成任务（通过/驳回），附带审批意见

### 6. 审批历史

通过 Flowable 的 `HistoryService` 查询 `act_hi_taskinst` 表获取已完成的审批节点，组装为时间线数据返回前端。

## Risks / Trade-offs

- **[R] Flowable 自动创建 30+ 张表，增加数据库复杂度** → 迁移方案：Flowable Starter 在首次启动时自动 DDL，不需要手动管理；日常运维无需关注这些表
- **[R] BPMN 流程变更不兼容** → 迁移方案：流程定义版本化，通过文件名后缀或 Flowable 的流程图版本机制管理
- **[R] 审批人硬编码在流程变量中** → 迁移方案：当前仅限 3 级固定审批，后续可通过 `TaskListener` 实现动态查询
- **[R] 驳回后无法恢复，需重新提交** → 迁移方案：如需此功能，后续可通过 Flowable 的"退回"或"跳转" API 实现

## Open Questions

- 审批人的确定方式：是否需要在启动流程时从前端传入审批人 ID？还是系统自动按项目配置查询？
- 是否需要审批超时提醒机制？
