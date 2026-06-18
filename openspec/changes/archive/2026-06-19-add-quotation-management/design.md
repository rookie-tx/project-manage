## Context

当前系统已有采购申请 → 审批 → 采购单的完整链路。采购单（PurchaseOrder）在审批通过时自动创建，状态为 PURCHASING（采购中），但供应商的引入是完全被动的——管理员手动填写供应商名称和电话。供应商表（t_supplier）独立存在，未与采购流程打通。

本次设计需要在采购单和供应商之间引入报价单（Quotation），让供应商主动参与报价，管理员从报价中择优选用。

### 约束
- 必须遵循项目现有的 DDD 四层架构（api → application → domain → infrastructure）
- 实体使用私有构造器 + 静态工厂方法模式
- 数据库表使用 `t_` 前缀
- 代码注释和文档使用中文

## Goals / Non-Goals

**Goals:**
- 供应商能查看待采购物料（PURCHASING 状态的采购单）并提交报价
- 供应商只能看到自己的报价记录
- 管理员在 Web 端待采购详情页能看到该采购单收到的全部报价
- 管理员可选用某条报价，系统自动将报价中的供应商信息填入采购单并流转状态

**Non-Goals:**
- 不修改采购申请审批流程
- 不修改采购单状态机（PURCHASING → PURCHASED → ARRIVED 流转不变）
- 不引入报价审批流程（报价无需审批，管理员直接选用）
- 不增加新的角色或复杂权限码

## Decisions

### 1. 报价单关联到 PurchaseOrder 而非 PurchaseRequest

**选择**: Quotation.purchaseOrderId → t_purchase_order

**理由**:
- PurchaseOrder 已经是采购事务的聚合根，代表"这次采购行为"
- 审批通过后自动创建的 PurchaseOrder（PURCHASING）天然就是"待报价"的标的
- 选用报价后需要更新的是 PurchaseOrder 的供应商信息，关联更自然
- 不需要改动 PurchaseRequest 的任何逻辑

**备选方案**: 关联到 PurchaseRequest —— 但采购申请只是"需求"，采购单才是"执行"，报价面向执行环节，不应绕过采购单。

### 2. 同一供应商对同一采购单只能有一条有效报价

**选择**: 数据库唯一索引 `UNIQUE(purchase_order_id, supplier_id)`，重复提交时更新已有报价。

**理由**:
- 避免供应商刷报价造成混乱
- 管理员查看报价时每个供应商只有一条记录，便于比较
- 供应商修改报价是合理的业务需求（价格调整、交货日期变更）

### 3. 报价状态模型简化

**选择**: 两条状态 —— PENDING(0, 待审核)、SELECTED(1, 已选用)

**理由**:
- 无需 REJECTED 状态 —— 当某条报价被选用后，其他报价自然失效，不需要显式标记
- 如果未来需要拒绝报价并通知供应商，可以再扩展
- 保持模型简单

### 4. 选用报价的原子操作

**选择**: 选用报价时，在一个事务中完成：
1. 校验报价状态为 PENDING
2. 更新报价状态为 SELECTED
3. 将报价中的 supplierId 对应的供应商信息（companyName, contactPhone）写入 PurchaseOrder 的 supplierName, supplierPhone
4. 将报价中的 unitPrice, totalAmount 写入 PurchaseOrder
5. 将 PurchaseOrder 状态流转为 PURCHASED
6. 记录 purchaserId 为当前操作用户

**理由**: 选用即完成采购，减少操作步骤。管理员不需要先查看报价、再单独填写采购单。

### 5. 供应商端 API 复用现有 `/api/supplier/` 路径前缀

**选择**: 供应商端报价接口挂在 `/api/supplier/` 下（如 `/api/supplier/pending-orders`、`/api/supplier/quotations`），管理端接口挂在 `/api/purchase-orders/{id}/quotations` 下。

**理由**:
- 与现有 SupplierController 的路径约定一致（供应商自身操作走 `/api/supplier/`）
- 采购单维度的报价查看走 `/api/purchase-orders/`，语义清晰
- 权限隔离自然：供应商接口由 supplier 角色访问，管理端接口由管理员角色访问

### 6. 报价数据不包含附件

**选择**: 第一版报价仅包含文字字段（单价、总金额、交货日期、备注），不包含附件上传。

**理由**: 保持简单，减少文件管理复杂度。采购单本身已支持附件上传（合同、发票等），报价阶段不需要附件。

### 7. 小程序页面结构

**选择**: 新增独立页面而非在现有采购单页面中混合报价逻辑。

**页面设计**:
- `pages/quotation/pending` — 待报价物料列表
- `pages/quotation/submit` — 提交/修改报价表单
- `pages/quotation/my-list` — 我的报价列表

**理由**: 供应商关注的是"报价"而非"采购单管理"，独立页面让供应商视角更聚焦。混合在采购单页面会让权限和 UI 逻辑变复杂。

## Risks / Trade-offs

- **[风险] 选用报价后 PurchaseOrder 的 supplierName 是文本快照，不会随供应商表更新而同步** → **缓解**: 这是设计意图，历史记录的供应商名称应是报价时的快照，不应随供应商资料变更而改变。
- **[风险] 供应商被禁用后仍可能有历史报价** → **缓解**: 报价列表展示时 JOIN 供应商表获取当前状态，如供应商已禁用则在 UI 上标记。不影响历史数据的完整性。
- **[权衡] 没有报价截止时间** → 第一版不引入截止时间，管理员可在任何时候选用报价。如果后续需要，可在 PurchaseOrder 增加 deadline 字段。

## Open Questions

- 无
