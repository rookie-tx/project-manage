## Context

当前系统有 project、purchaserequest、task、user、warehouse 五个业务模块。临时工需要登录系统进行后续打卡操作，因此不能仅作为数据记录，需要关联 `t_user` 账户。

## Goals / Non-Goals

**Goals:**
- 临时工 CRUD，创建时联动创建 `t_user` 账户（username=手机号，初始密码）
- `t_user` 新增 `must_change_password` 标记，临时工首次登录强制改密
- 按项目筛选临时工列表
- 新角色 `temp_worker` + 最小权限
- 前端项目详情页嵌入"临时工"Tab

**Non-Goals:**
- 打卡功能（P1）
- 工种字典
- 临时工考勤/工资

## Decisions

### 决策 1: 数据模型

```
t_user (扩展)                    t_temp_worker (新建)
┌──────────────────────┐        ┌──────────────────────────┐
│ id                   │◄───────│ user_id FK (UNIQUE)       │
│ username (=手机号)    │        │ project_id FK → t_project │
│ password_hash         │        │ worker_type VARCHAR(50)   │
│ must_change_password  │ ← NEW  │ entry_date DATE           │
│   SMALLINT DEFAULT 0  │        │ exit_date DATE (nullable)  │
│ email                 │        │ status SMALLINT           │
│ phone (=手机号)        │        │   1=在职, 0=离职           │
│ status                │        │ is_deleted SMALLINT       │
│ is_deleted            │        │ created_at / updated_at   │
└──────────────────────┘        └──────────────────────────┘
```

**创建流程**:
```
POST /api/temp-workers { projectId, phone, workerType, entryDate }
  → @Transactional
    1. UserDomainService.createUser(phone, "123456", null, phone)
    2. userRepo.updateMustChangePassword(userId, 1)
    3. userRoleRepo.assignRole(userId, temp_worker_role_id)
    4. tempWorkerRepo.save(TempWorker.create(userId, projectId, workerType, entryDate))
```

**离职流程**: `PUT` 设置 `exitDate` → 自动 `status=0` + 禁用 `t_user`

### 决策 2: 首次登录强制改密

**机制**: `GET /api/auth/me` 返回 `mustChangePassword: true/false`。前端 `App.vue` 检测到 `mustChangePassword` 且当前路径不是 `/settings` 时，弹窗或跳转到设置页改密。改密成功后清除标记。

### 决策 3: 权限

- 新角色 `temp_worker`（临时工）
- 权限：仅 `user:read`（查看自身），后续 P1 添加 `attendance:clock-in`
- admin 创建临时工时自动分配 `temp_worker` 角色

### 决策 4: 模块关系

**选择**: `tempworker` 模块依赖 `user` 模块的 `UserDomainService` 和 `UserRepository`

**理由**: 创建/离职时需操作 `t_user`，复用已有服务避免代码重复。DDD 中允许模块间通过领域接口通信。
