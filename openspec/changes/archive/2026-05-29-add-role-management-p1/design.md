## Context

P0 已建立 RBAC 数据模型（`t_role`、`t_permission`、`t_user_role`、`t_role_permission`）和用户管理模块。预置 4 个角色和 15 条权限，`LoginContext` 已携带 `permissions` 列表。但管理员无法通过界面管理角色（新增/修改/删除）和调整权限分配，后端 Controller 方法也没有权限校验保护。

P0 中前端菜单通过 `auth.hasPermission()` 控制显隐，但 API 端点仍然裸奔——知道 URL 即可调用。

## Goals / Non-Goals

**Goals:**
- 角色 CRUD 完整 API（创建、列表、编辑、软删除），遵循现有 DDD 四层架构
- 权限列表查询 API，角色-权限关联的查询和更新 API
- `admin` 角色受 `is_protected=1` 保护，不可删除
- `@RequirePermission("xxx")` 注解，Spring AOP 切面拦截 Controller 方法
- 前端角色管理页面（列表 + 创建/编辑对话框 + 权限复选框）
- 种子数据新增 4 条角色管理权限（`role:create`、`role:update`、`role:delete`、`role:permission:assign`）

**Non-Goals:**
- 项目/采购模块接入 `@RequirePermission`（P2）
- 权限缓存（后续性能优化）
- 角色嵌套/继承

## Decisions

### 决策 1: @RequirePermission 注解设计

**选择**: 简单单值 `@RequirePermission("user:delete")`，不支持 AND/OR 组合

**理由**:
- 当前需求足够——每个方法只需一种权限
- 需要多个权限时，方法内手动调用 `LoginContextHolder.getPermissions().containsAll(...)` 
- 注解简单，切面实现轻量

**实现位置**: `cn.tx.aiagent.common.security`

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    String value();
}
```

**切面**: 使用 Spring AOP `@Around`，注入 `LoginContextHolder`，从 ThreadLocal 读取当前用户的 permissions，`contains()` 检查。不满足时抛出 `BusinessException(ACCESS_DENIED)`。

### 决策 2: 角色保护机制

**选择**: 数据库 `t_role` 表添加 `is_protected SMALLINT DEFAULT 0`，仅 `admin` 角色设为 1；删除接口检查此标记

**理由**:
- 在数据库层面声明式标记，与现有 `is_deleted` 模式一致
- 扩展简单——需要保护更多角色时只需设置标记
- 保护逻辑在 `RoleDomainService.deleteRole()` 中检查，避免绕过

### 决策 3: 权限存储方式

**选择**: 保持 P0 的扁平权限列表（`t_permission` 表），前端权限选择使用分组复选框

**理由**:
- 当前权限数量有限（~20 条），无需树形结构
- P2 扩展时如有需要可引入 `parent_id` 构建树
- 前端按 `资源:` 前缀分组显示即可（"用户管理"→user:*,"项目管理"→project:*）

### 决策 4: 角色管理页面架构

**选择**: 独立菜单项 + 单页面（列表带内联创建/编辑弹窗），不使用多路由

**理由**:
- P0 用户管理用了多路由（列表/创建/编辑三个页面），角色管理更简单，弹窗更高效
- 创建和编辑都使用同一弹窗组件，通过 `mode` prop 区分
- 权限勾选使用 `a-checkbox-group` 分组展示

### 决策 5: Role Domain Model

**选择**: 复用现有 `Role` 领域实体，扩展 `isProtected` 字段；新增 `RoleDomainService`

**理由**:
- P0 已有 Role 实体和 RoleRepository，直接扩展而非新建
- `RoleDomainService` 封装创建/更新/删除的业务规则（名称唯一性、保护检查）

## Risks / Trade-offs

- **[风险] AOP 切面拦截范围过大** → 仅对 `@RestController` 生效（切点限制在 `within(@org.springframework.web.bind.annotation.RestController *)`）
- **[风险] 角色被误删后种子数据不可恢复** → P2 考虑增加角色恢复功能
- **[取舍] 权限选择器用复选框而非树** → 后续权限数量增长时改为树形
