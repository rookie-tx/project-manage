## Context

`src/components/` 当前 7 个组件平铺存放，按功能域可分为四组：

| 组件 | 所属功能域 | 被引用位置 |
|------|-----------|-----------|
| ApprovalDialog.vue | 审批 | TaskListPage.vue |
| ApprovalHistory.vue | 审批 | PurchaseRequestDetailPage.vue |
| SubmitApprovalDialog.vue | 审批 | PurchaseRequestListPage.vue, PurchaseRequestDetailPage.vue |
| ProjectForm.vue | 项目 | ProjectCreatePage.vue, ProjectEditPage.vue |
| PurchaseRequestForm.vue | 采购 | PurchaseRequestCreatePage.vue, PurchaseRequestEditPage.vue |
| ProjectSelect.vue | 通用 | PurchaseRequestForm.vue |
| UserSelect.vue | 通用 | ProjectForm.vue |

## Goals / Non-Goals

**Goals:**
- `components/` 下按功能域建立子目录 `common/`、`project/`、`purchase/`、`approval/`
- 将每个组件移入对应子目录
- 更新所有跨目录 import 路径

**Non-Goals:**
- 不改变 `stores/` 目录结构（已按领域命名，5 个文件平铺足够清晰）
- 不改变 `views/` 目录结构（已按功能分类）
- 不改变组件内部逻辑或 API

## Decisions

### 子目录命名

与 `views/` 和 `stores/` 保持一致的功能域命名：

| 子目录 | 包含组件 | 命名依据 |
|--------|---------|---------|
| `common/` | ProjectSelect, UserSelect | 被多个功能域引用，属于通用/可复用组件 |
| `project/` | ProjectForm | 仅被项目页面引用 |
| `purchase/` | PurchaseRequestForm | 仅被采购页面引用 |
| `approval/` | ApprovalDialog, ApprovalHistory, SubmitApprovalDialog | 仅被审批功能引用 |

### 为什么不用 `@/components/common/` 之外的更深嵌套

当前组件数量少，每个子目录仅 1-3 个文件，二级目录已足够。后续组件增多时可以在对应子目录内再细分。

## Risks / Trade-offs

- [跨目录 import 遗漏] → 全局搜索 `@/components/` 逐个确认，编译阶段即可发现
- [IDE 重构不支持 import 更新] → 手动逐个修改 import 路径
