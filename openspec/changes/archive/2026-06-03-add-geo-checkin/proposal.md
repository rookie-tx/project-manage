## Why

小程序目前缺少实际的打卡功能，Dashboard 页面的"考勤打卡"入口仅显示"即将上线"。后端虽有基础的 attendance 模块（上班/下班打卡），但完全缺失地理位置校验能力，无法防止用户在任何地点随意打卡。本次变更为小程序新增基于地理围栏的打卡模块，确保用户只有在指定的项目打卡区域内才能完成打卡。

## What Changes

- **小程序新增打卡页面**：提供上班打卡和下班打卡功能，展示今日打卡状态和本月打卡记录
- **地理位置校验**：打卡时通过微信 `wx.getLocation` 获取 GPS 坐标，后端基于 Haversine 公式计算用户与打卡区域的距离，超出范围拒绝打卡
- **打卡区域管理**：Web 管理端在项目详情页新增"打卡区域"Tab，支持为每个项目配置多个圆形打卡区域（圆心 + 半径）
- **模拟定位检测**：联合检测 GPS 精度、垂直精度、速度、时间戳等指标，对每次打卡标注风险等级（正常/可疑/高风险），管理员可在后台审查
- **打卡坐标记录**：`t_attendance` 表新增经纬度字段，每次打卡记录用户的实际 GPS 坐标
- **后端 attendance 模块重构**：补全缺失的 ApplicationService 层，遵循项目 DDD 四层架构规范

## Capabilities

### New Capabilities
- `geo-checkin`: 基于地理围栏的打卡功能，包括小程序打卡页面、位置校验、模拟定位检测、打卡记录查询
- `attendance-area-management`: Web 管理端的打卡区域配置管理（CRUD），作为项目详情页的 Tab

### Modified Capabilities
- `miniapp-shell`: Dashboard 首页的"考勤打卡"入口从占位状态改为跳转到实际打卡页面

## Impact

- **数据库**：新建 `t_attendance_area` 表，`t_attendance` 表新增 `clock_in_lat`, `clock_in_lng`, `clock_out_lat`, `clock_out_lng`, `location_risk` 列
- **后端模块**：重构 `attendance/` 模块（新增 ApplicationService 层、位置校验逻辑），新增 `attendancearea/` 模块，新增 `common/util/LocationUtils.java` 工具类
- **Web 管理端**：项目详情页 `ProjectDetailPage.vue` 新增 `AttendanceAreaTab.vue` Tab 组件，修改路由和菜单
- **小程序**：新增 `pages/checkin/` 页面、`api/attendance.js` API 封装、`utils/location.js` GPS 工具，修改 `app.json` 路由和 Dashboard 入口
- **权限**：新增 `attendance:area-manage` 权限用于打卡区域管理
