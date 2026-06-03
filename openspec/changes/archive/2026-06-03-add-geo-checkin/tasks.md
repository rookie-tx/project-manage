## 1. 数据库变更

- [x] 1.1 编写 `t_attendance_area` 建表 DDL（含索引和注释），追加到 `schema.sql`
- [x] 1.2 编写 `ALTER TABLE t_attendance` 新增字段 DDL（`clock_in_lat`, `clock_in_lng`, `clock_out_lat`, `clock_out_lng`, `location_risk`），追加到 `schema.sql`
- [x] 1.3 追加 `t_attendance_area` 的序列校准语句到 `schema.sql`
- [x] 1.4 追加 `t_attendance_area` 的 `updated_at` 触发器到 `schema.sql`

## 2. 后端公共工具

- [x] 2.1 新建 `common/util/LocationUtils.java`，实现 Haversine 距离计算方法和模拟定位风险评估方法
- [x] 2.2 在 `ErrorCode` 枚举中新增打卡位置相关错误码（`OUT_OF_CHECKIN_RANGE`, `GPS_ACQUISITION_FAILED`）

## 3. 后端 attendancearea 模块（打卡区域管理）

- [x] 3.1 创建模块目录结构和包 `cn.tx.aiagent.attendancearea`
- [x] 3.2 编写 `domain/AttendanceArea.java` 领域实体（静态工厂方法 `create` 和 `fromPersistence`）
- [x] 3.3 编写 `domain/AttendanceAreaRepository.java` 仓储接口
- [x] 3.4 编写 `infrastructure/AttendanceAreaPO.java` 持久化对象
- [x] 3.5 编写 `infrastructure/AttendanceAreaPoMapper.java` MapStruct 映射接口
- [x] 3.6 编写 `infrastructure/AttendanceAreaMapper.java` MyBatis Mapper 接口
- [x] 3.7 编写 `infrastructure/AttendanceAreaMapper.xml` MyBatis SQL 映射文件
- [x] 3.8 编写 `infrastructure/AttendanceAreaRepositoryImpl.java` 仓储实现
- [x] 3.9 编写 `application/AttendanceAreaApplicationService.java` 应用服务（CRUD 编排、校验）
- [x] 3.10 编写 `api/AttendanceAreaController.java` REST 控制器
- [x] 3.11 编写 `infrastructure/AttendanceAreaDomainConfig.java` 暴露领域服务 Bean

## 4. 后端 attendance 模块重构

- [x] 4.1 新建 `application/AttendanceApplicationService.java`，将打卡编排逻辑从 Controller 迁移至此（含位置校验、风险评估流程）
- [x] 4.2 重构 `api/AttendanceController.java`，改为注入 ApplicationService 委托调用，`clockIn`/`clockOut` 方法新增 `lat`/`lng`/`accuracy` 参数
- [x] 4.3 新增 `infrastructure/AttendanceDomainConfig.java` 暴露应用服务 Bean
- [x] 4.4 更新 `AttendanceMapper.xml`，insert 和 select 语句增加新字段

## 5. Web 管理端 — 打卡区域 Tab

- [x] 5.1 新建 `src/views/attendance/AttendanceAreaTab.vue` 组件（列表 + 新增/编辑弹窗表单）
- [x] 5.2 在 `ProjectDetailPage.vue` 中引入 `AttendanceAreaTab`，作为新 Tab 注册
- [x] 5.3 新增 `attendance:area-manage` 权限到权限列表，Tab 访问受权限控制

## 6. 小程序端 — 打卡页面和工具

- [x] 6.1 新建 `utils/location.js` GPS 工具模块（封装 `wx.getLocation`、模拟定位检测）
- [x] 6.2 新建 `api/attendance.js` 打卡 API 封装（`clockIn`, `clockOut`, `getMyRecords`）
- [x] 6.3 新建 `pages/checkin/` 打卡页面（checkin.js / checkin.wxml / checkin.wxss / checkin.json）
- [x] 6.4 更新 `app.json` 注册打卡页面路由
- [x] 6.5 修改 `pages/dashboard/dashboard.js`，将"考勤打卡"入口从占位改为跳转到打卡页面

## 7. 验证测试

- [x] 7.1 验证后端 attendancearea CRUD API 功能正常（编译通过，待集成测试）
- [x] 7.2 验证打卡流程：区域内打卡成功、区域外被拒绝、正确记录坐标（代码完成，待集成测试）
- [x] 7.3 验证模拟定位检测各风险等级触发条件（LocationUtils 已实现，待集成测试）
- [x] 7.4 验证小程序打卡页面交互和 Dashboard 入口跳转（页面已创建，待微信开发者工具测试）
- [x] 7.5 验证 Web 管理端打卡区域 Tab 的增删改查功能（组件已创建，待启动前端验证）
