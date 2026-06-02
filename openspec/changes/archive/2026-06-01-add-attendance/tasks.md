## 1. 数据库

- [x] 1.1 创建 `t_attendance` 表 + 触发器 + 序列修正
- [x] 1.2 种子数据新增 `attendance:clock-in`、`attendance:clock-out` 权限 + temp_worker 关联

## 2. 领域层

- [x] 2.1 创建 `AttendanceStatus` 枚举 + `Attendance` 领域实体
- [x] 2.2 创建 `AttendanceRepository` 接口（findTodayByWorker, save, update, findByWorkerMonth, findByProjectMonth）

## 3. 基础设施层

- [x] 3.1 创建 `AttendancePO` + `AttendanceMapper` + XML + `AttendancePoMapper`
- [x] 3.2 创建 `AttendanceRepositoryImpl`

## 4. API 层

- [x] 4.1 创建 `AttendanceController`（POST clock-in, POST clock-out, GET my, GET projects/{id}/attendance）
- [x] 4.2 Controller 加 `@RequirePermission` 注解

## 5. 前端

- [x] 5.1 创建 `AttendancePage.vue`（打卡按钮 + 当月记录）
- [x] 5.2 路由添加 `/attendance`
- [x] 5.3 `App.vue` 临时工登录后跳转 `/attendance`
- [x] 5.4 `TempWorkerTab.vue` 新增打卡记录查看

## 6. 验证

- [x] 6.1 编译 + 测试
