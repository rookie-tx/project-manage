## 1. 数据库

- [x] 1.1 `t_user` 表新增 `must_change_password SMALLINT DEFAULT 0` 字段
- [x] 1.2 创建 `t_temp_worker` 表（id, user_id FK UNIQUE, project_id FK, worker_type, entry_date, exit_date, status, is_deleted）
- [x] 1.3 种子数据新增 `temp_worker` 角色 + 关联 `user:read` 权限
- [x] 1.4 序列修正

## 2. User 模块扩展

- [x] 2.1 `User.java` 新增 `mustChangePassword` 字段 + `UserPO` / `UserManageMapper.xml` 新增对应字段
- [x] 2.2 `UserRepository` 新增 `updateMustChangePassword` 方法
- [x] 2.3 `LoginResponse.java` 新增 `mustChangePassword` 字段
- [x] 2.4 `PUT /api/auth/password` 改密成功后清除 `must_change_password`

## 3. TempWorker 领域层

- [x] 3.1 创建 `WorkerStatus` 枚举 + `TempWorker` 领域实体
- [x] 3.2 创建 `TempWorkerRepository` 接口（findByProjectId, save, update, findById, softDelete）

## 4. TempWorker 基础设施层

- [x] 4.1 创建 `TempWorkerPO` + `TempWorkerMapper` + XML + `TempWorkerPoMapper`
- [x] 4.2 创建 `TempWorkerRepositoryImpl`
- [x] 4.3 创建 `TempWorkerDomainService`（create 联动创建用户+赋角色，leave 禁用用户）

## 5. TempWorker API 层

- [x] 5.1 创建 `CreateTempWorkerRequest` / `UpdateTempWorkerRequest` DTO
- [x] 5.2 创建 `TempWorkerController`（GET /api/projects/{id}/temp-workers, POST /api/temp-workers, PUT/DELETE /api/temp-workers/{id}）

## 6. 前端

- [x] 6.1 创建 `useTempWorkerStore` + `TempWorkerTab.vue`（表格+创建/编辑弹窗）
- [x] 6.2 `ProjectDetailPage.vue` 新增"临时工"Tab
- [x] 6.3 `App.vue` 检测 `mustChangePassword` 强制跳转修改密码

## 7. 验证

- [x] 7.1 编译 + 测试
