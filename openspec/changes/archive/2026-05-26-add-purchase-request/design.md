
## Context

采购申请是采购业务链的起点，由项目部发起。后端已建立 DDD 四层架构规范，前端已建立 Vue 3 Composition API + Ant Design Vue 4 规范，项目管理模块（`project-management-ui`）已完成。本次在现有架构基础上新增采购申请模块，字段参照 `pdd/采购系统.md` 中"采购申请（项目部）"的定义。

## Goals / Non-Goals

**Goals:**
- 项目部可创建采购申请（关联已有项目、填写物料信息）
- 列表页按项目筛选 + 分页浏览
- 支持编辑和软删除
- 状态支持 待采购→已取消（后续接入工作流扩展）
- 项目字段通过下拉选择器从已有项目中选择
- 单位字段为预定义下拉
- 数量支持小数

**Non-Goals:**
- 采购部实际采购操作（后续模块）
- 仓库入库确认（后续模块）
- 审批工作流（后续接入）
- 成本/财务核算（后续模块）
- 批量操作

## Decisions

### 1. 项目下拉选择：新增 `GET /api/projects/select` 接口

**选择：** 在项目管理模块新增专用轻量接口 `GET /api/projects/select`，只返回 id + code + name。

**理由：** 下拉选择器只需 id 用于关联、code+name 用于展示，不需要完整的 ProjectResponse。复用现有的 ProjectRepository 和 ProjectMapper，在 Controller 中直接调用。

**备选方案：** 复用已有的 `GET /api/projects?size=9999` 接口。拒绝原因——该接口返回完整字段且分页，下拉场景不需要。

### 2. ProjectSelect 组件：独立复用组件

**选择：** `ProjectSelect.vue` 封装 `<a-select>`，onMounted 调 `/api/projects/select` 获取全部项目列表。

```vue
<ProjectSelect v-model:value="formState.projectId" />
```

**理由：** 与 `UserSelect.vue` 模式一致，后续其他模块（仓库、成本）也可复用。

### 3. 单位枚举：前端 + 后端各自维护映射

**选择：** 后端 `Unit` 枚举存 code（SMALLINT），前端 store 存映射表。

```java
public enum Unit {
    PIECE(0, "个"), UNIT(1, "台"), SET(2, "套"),
    METER(3, "米"), TON(4, "吨"), CUBIC_METER(5, "立方米"),
    ROLL(6, "卷"), BUCKET(7, "桶"), BOX(8, "箱"), OTHER(9, "其他");
}
```

**理由：** 与项目管理中 ProjectType/ProjectStatus 的模式完全一致。

### 4. 数量类型：BigDecimal

**选择：** 后端使用 `BigDecimal`，前端使用 `number` 类型输入框。

PO 层使用 `Double`（MyBatis 直接映射），PoMapper 中做 `BigDecimal ↔ Double` 转换。

### 5. 状态流转：简单两步

**选择：** 当前仅支持 待采购(0) ↔ 已取消(1)。后续工作流引擎接入后扩展。

```
  待采购(0) ──▶ 已取消(1)
       │
       ▼ (后续工作流)
  采购中(2) → 已采购(3) → 已入库(4)
```

### 6. 编辑页状态流转 UI

**选择：** 编辑页如果当前状态是"待采购"，下拉选项只提供"已取消"。如果当前状态是"已取消"，不提供状态修改。

### 7. 列表筛选：仅按项目

**选择：** 列表页只有一个筛选条件——项目下拉选择。不做状态筛选、不做日期范围筛选。

**理由：** 用户明确只要求项目筛选，保持简单。后续按需添加。

### 8. 路由结构

沿用已建立的嵌套路由模式，DashboardLayout 作为父布局：

```
/procurement                          采购申请列表页
/procurement/new                      新建采购申请
/procurement/:id                      采购申请详情
/procurement/:id/edit                 编辑采购申请
```

URL 路径使用 `/procurement` 而非 `/purchase-requests`，保持中文业务语境下的简洁性。

## Risks / Trade-offs

- **项目下拉选择器加载全部项目**：`GET /api/projects/select` 返回全部未删除项目。当前项目量级小，无性能问题。项目数量增长后可改为搜索式下拉（按需加载）。
- **状态流转当前仅两步**：工作流引擎接入后需重构状态流转逻辑。但当前设计（`PurchaseRequestStatus` 枚举 + `transitionTo` 方法）已预留扩展空间，重构成本可控。
