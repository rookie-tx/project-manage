## Context

当前系统无供应商管理模块，采购记录中的供应商信息以 `supplier_name`/`supplier_phone` 纯文本存储。微信小程序拥有完整的 WeChat 登录 → 账号绑定流程，但缺少供应商注册入口。本次设计遵循现有 DDD 四层架构和 TempWorker 的 1:1 用户扩展模式，建立供应商管理基础设施。

**约束**：Java 25 + Spring Boot 3.5.14 + MyBatis 3.0.5 + PostgreSQL；后端分层: api → application → domain ← infrastructure；domain 层零框架注解；MapStruct 进行对象映射；统一 `Result<T>` 响应格式。

## Goals / Non-Goals

**Goals:**
- 建立 `t_supplier` 表，与 `t_user` 一对一关联
- 小程序供应商注册 API：微信授权后填写企业信息，自动创建 User + Supplier + Wechat 绑定，返回 JWT
- Web 管理端供应商 CRUD API（需认证+权限）
- Web 端供应商列表页和详情/编辑页
- 小程序供应商注册页和个人资料页
- 预置 `supplier` 角色及 6 条权限

**Non-Goals:**
- 不修改 `t_purchase_order` 表（供应商字段保持纯文本）
- 不做供应商接单/需求匹配功能
- 不关联项目（平台级实体）
- 不做审批流程（注册直接通过）
- 管理端不开放注册入口（仅小程序注册）
- 不做供应商资质审核/认证功能

## Decisions

### 1. 数据模型：User + Supplier 1:1 扩展

选择 TempWorker 模式（`t_temp_worker.user_id UNIQUE REFERENCES t_user`），而非独立实体或 User 表加字段。理由：

- 供应商首先是一个系统用户，享有登录能力、角色、权限
- 1:1 关系简单清晰，后续可扩展为 1:N 但本次不需要
- 与 TempWorker 模式一致，降低团队认知成本
- 业务字段与企业信息相关，不应污染 User 表

**替代方案**：直接在 User 表加 `company_name` 等字段 → 拒绝，User 表会随业务扩展膨胀，且供应商信息与普通用户信息语义不同。

### 2. 注册流程：一步完成

小程序供应商注册 API 为公开接口（无需预认证），一次请求完成三件事：

```
POST /api/supplier/register { code, companyName, licenseNo, ... }
  → code2Session 获取 openid
  → 创建 User（username=supplier_{openid截断}, 随机密码, supplier 角色）
  → 创建 WechatUserBinding（user_id + openid）
  → 创建 Supplier（user_id + 企业信息）
  → 签发 JWT → 返回 { token, user, supplier }
```

**替代方案**：先注册账号再补充企业资料 → 拒绝，用户已明确"注册即绑定，一步完成"。

### 3. 管理 API 路径与权限

| 接口 | 方法 | 路径 | 权限 |
|------|------|------|------|
| 供应商注册 | POST | `/api/supplier/register` | 公开 |
| 供应商列表 | GET | `/api/suppliers` | `supplier:read` |
| 供应商详情 | GET | `/api/suppliers/{id}` | `supplier:read` |
| 创建供应商 | POST | `/api/suppliers` | `supplier:create` |
| 编辑供应商 | PUT | `/api/suppliers/{id}` | `supplier:update` |
| 删除供应商 | DELETE | `/api/suppliers/{id}` | `supplier:delete` |
| 我的供应商资料 | GET | `/api/supplier/my-profile` | `supplier:profile` |
| 更新我的资料 | PUT | `/api/supplier/my-profile` | `supplier:profile` |

管理接口 RESTful 风格，注册接口独立路径以区分公开/认证语义。

### 4. 模块结构

遵循现有 DDD 四层 + MapStruct 模式：

```
cn.tx.aiagent.supplier
├── api/
│   ├── SupplierController.java          # 管理端 CRUD
│   ├── SupplierRegisterController.java  # 公开注册接口
│   ├── CreateSupplierRequest.java
│   ├── UpdateSupplierRequest.java
│   ├── SupplierRegisterRequest.java
│   ├── SupplierRegisterResponse.java
│   └── SupplierResponse.java
├── application/
│   ├── SupplierApplicationService.java
│   └── SupplierDtoMapper.java
├── domain/
│   ├── Supplier.java
│   ├── SupplierStatus.java
│   ├── SupplierRepository.java
│   └── SupplierDomainService.java
└── infrastructure/
    ├── SupplierPO.java
    ├── SupplierMapper.java (MyBatis interface)
    ├── SupplierPoMapper.java (MapStruct PO↔Domain)
    ├── SupplierRepositoryImpl.java
    └── SupplierDomainConfig.java
```

### 5. 数据库表设计

```sql
CREATE TABLE IF NOT EXISTS t_supplier (
    id                  BIGSERIAL    PRIMARY KEY,
    user_id             BIGINT       NOT NULL UNIQUE REFERENCES t_user(id),
    company_name        VARCHAR(200) NOT NULL,
    license_no          VARCHAR(100),
    contact_person      VARCHAR(50)  NOT NULL,
    contact_phone       VARCHAR(30)  NOT NULL,
    address             VARCHAR(300),
    status              SMALLINT     NOT NULL DEFAULT 1,
    remark              VARCHAR(500),
    is_deleted          SMALLINT     NOT NULL DEFAULT 0,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

- `user_id` UNIQUE 保证 1:1
- `status`: 1=正常, 0=禁用
- `is_deleted`: 软删除标记
- `license_no`: 统一社会信用代码，可选但用于企业标识
- 注册时 username 生成规则：`supplier_` + openid 后 12 位，保证唯一性

### 6. 角色与权限

新增 `supplier` 角色（`is_protected=0`，允许删除）：

| 权限码 | 名称 | 授予角色 |
|--------|------|---------|
| `supplier:read` | 查看供应商 | admin |
| `supplier:create` | 创建供应商 | admin |
| `supplier:update` | 编辑供应商 | admin |
| `supplier:delete` | 删除供应商 | admin |
| `supplier:register` | 供应商注册 | supplier（公开接口无需校验） |
| `supplier:profile` | 查看编辑自己的资料 | supplier |

## Risks / Trade-offs

- **username 冲突**：`supplier_` + openid 后12位可能冲突 → 注册时检查 username 唯一性，冲突时追加随机数
- **重复注册**：同一 openid 重复调用注册接口 → 检查 openid 是否已绑定 `t_user_wechat`，已绑定则拒绝并提示
- **公开接口滥用**：注册接口无认证，可能被恶意调用 → 需通过微信 code 换取 openid，天然绑定微信用户身份，配合频率限制
- **后续需求扩展**：1:1 模型后续若需支持一个用户多个企业 → 届时将 `user_id UNIQUE` 约束改为普通索引，本次保持简洁
