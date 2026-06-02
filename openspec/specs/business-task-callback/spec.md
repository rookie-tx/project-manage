# business-task-callback Specification

## Purpose
TBD - created by archiving change extract-generic-task-module. Update Purpose after archive.
## Requirements
### Requirement: BusinessTaskCallback 接口定义
系统 SHALL 在 task 模块 domain 层定义 `BusinessTaskCallback` 接口，业务模块通过实现此接口注册到 task 模块。

#### Scenario: 接口方法签名
- **WHEN** 定义 BusinessTaskCallback 接口
- **THEN** 接口包含三个方法：`getType()` 返回 BusinessType、`getBusinessInfo(Long businessId)` 返回 BusinessInfo 记录、`onTaskCompleted(Long businessId, TaskResult result)` 无返回值

### Requirement: BusinessInfo 值对象
系统 SHALL 定义 `BusinessInfo` 为 Java record，包含 `title` 和 `subtitle` 两个字段。

#### Scenario: 提供展示信息
- **WHEN** task 模块在 startProcess 时调用 callback.getBusinessInfo(businessId)
- **THEN** 业务模块返回该业务对象的标题（如采购申请名称）和副标题（如所属项目名称），供任务列表展示使用

### Requirement: TaskResult 值对象
系统 SHALL 定义 `TaskResult` 为 sealed interface，包含 `Approved` 和 `Rejected` 两种实现。

#### Scenario: 审批通过结果
- **WHEN** 审批人通过任务
- **THEN** task 模块构造 `TaskResult.Approved` 传递给 callback.onTaskCompleted()

#### Scenario: 审批驳回结果
- **WHEN** 审批人驳回任务
- **THEN** task 模块构造 `TaskResult.Rejected` 传递给 callback.onTaskCompleted()

### Requirement: CallbackRegistry 自动发现
系统 SHALL 通过 `CallbackRegistry` 自动收集所有 `BusinessTaskCallback` 的 Spring Bean 实现。

#### Scenario: 注册回调实现
- **WHEN** 业务模块在自己的 infrastructure 包中实现 BusinessTaskCallback 并标注 @Component
- **THEN** CallbackRegistry 通过构造器注入 `List<BusinessTaskCallback>` 自动收集所有实现，维护 BusinessType → BusinessTaskCallback 映射

#### Scenario: 查找回调
- **WHEN** task 模块需要通过 businessType 查找对应的回调实现
- **THEN** CallbackRegistry 根据 BusinessType 返回对应的 BusinessTaskCallback 实例

#### Scenario: 未注册的业务类型
- **WHEN** 查找的 businessType 没有对应的回调实现
- **THEN** 返回 Optional.empty()，调用方抛出 BusinessException

### Requirement: 业务模块实现 Callback
每个需要审批能力的业务模块 SHALL 在自己的 infrastructure 包中实现 `BusinessTaskCallback` 接口。

#### Scenario: 采购申请回调实现
- **WHEN** 采购申请模块实现 BusinessTaskCallback
- **THEN** getType() 返回 PURCHASE_REQUEST，getBusinessInfo() 查询采购申请名称和所属项目名称，onTaskCompleted() 更新采购申请状态（通过→已通过，驳回→已驳回）

