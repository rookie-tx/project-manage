## Context

当前采购申请模块（`purchaserequest`）覆盖了申请→审批流程，审批通过后状态变为"已通过"即结束。仓库模块（`warehouse`）有 StockRecord 可关联 purchaseRequestId，但缺少中间的采购执行环节。文件上传模块（`file`）已有通用的 `t_upload_file` 表，支持 `business_type` + `business_id` 关联。

## Goals / Non-Goals

**Goals:**
- 新建 `purchasemanagement` 模块，DDD 四层架构
- 采购记录 CRUD + 状态流转（采购中→已采购→已到货）
- 文件上传关联（合同/发票），复用 `t_upload_file`
- Web SPA 3 页面 + Dashboard 导航菜单
- 小程序 3 页面 + Dashboard 入口

**Non-Goals:**
- 不修改采购申请状态
- 不包含仓库自动入库（后续迭代）
- 不支持一对多采购（一个申请多条采购记录）
- 不包含审批流程（采购记录不需要审批）

## Decisions

### 决策 1：一对一关系

**选择**：`purchase_request_id` 设 UNIQUE 约束，一个采购申请仅对应一条采购记录。

**理由**：用户明确选择一对一。多批次采购可通过上传多个合同文件到同一条记录实现。

### 决策 2：状态枚举

**选择**：`PURCHASING(0, "采购中")` → `PURCHASED(1, "已采购")` → `ARRIVED(2, "已到货")`

线性流转，不设"已取消"状态（可软删除）。

### 决策 3：文件上传复用

**选择**：表单提交时先上传文件获 fileIds，再提交业务数据时传入 fileIds 数组。后端创建记录后回填 `t_upload_file.business_id`。

与废渣管理（`construction_waste`）的文件上传流程完全一致。

### 决策 4：模块命名

**选择**：包名 `purchasemanagement`，表名 `t_purchase_order`，API 路径 `/api/purchase-orders`。

### 决策 5：小程序表单复用

**选择**：`pages/purchase-order/form` 通过 `id` 参数区分新建/编辑。与采购申请、项目管理的表单复用模式一致。

### 决策 6：权限控制

**选择**：`purchase:create` 控制新建，`purchase:update` 控制编辑，`purchase:delete` 控制删除。

## Risks / Trade-offs

- **[UNIQUE 约束]** 一个申请只能建一条采购记录 → 如需多批次，通过多文件上传解决
- **[状态流转]** 线性不可逆 → 如需回退，由管理员直接改数据库
- **[采购申请筛选]** 新建采购记录时需从已通过的申请中选择 → `GET /api/purchase-requests?status=2` 获取可采购的申请列表
