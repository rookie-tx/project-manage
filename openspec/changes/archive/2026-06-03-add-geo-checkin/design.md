## Context

当前系统已有基础的 attendance 模块（`cn.tx.aiagent.attendance`），支持上班打卡和下班的简单记录。但该模块存在以下不足：

1. **无地理位置校验**：用户可在任意地点打卡，无法确保工人确实在工地
2. **缺少 ApplicationService 层**：Controller 直接调用 Repository，不符合项目 DDD 四层架构规范
3. **无打卡区域概念**：没有地方配置和存储打卡的地理围栏
4. **小程序尚未接入**：Dashboard 的考勤入口仍为"即将上线"占位

本次设计基于上述现状，新增地理围栏打卡能力，同时修复架构缺陷。

## Goals / Non-Goals

**Goals:**
- 小程序端提供完整的打卡交互（上班/下班打卡 + 本月记录查看）
- 后端基于 Haversine 公式校验用户 GPS 坐标是否在项目打卡区域内
- 每次打卡记录用户的 GPS 坐标、精度及风险等级
- Web 管理端支持项目打卡区域的增删改查
- 模拟定位检测：联合 accuracy、verticalAccuracy、speed、时间戳等指标评估风险
- 补全 attendance 模块的 ApplicationService 层，符合 DDD 规范

**Non-Goals:**
- 不包含拍照打卡功能
- 不包含多边形围栏（仅圆形）
- 不包含 WiFi/蓝牙信标辅助定位
- 不改变现有 TempWorker 模型与打卡的关联关系
- 不包含打卡异常的人工申诉/审批流程（后续迭代）

## Decisions

### 决策 1：地理围栏模型 — 圆心 + 半径

**选择**：`t_attendance_area` 存储中心点经纬度（`center_lat`, `center_lng`）和半径（`radius`，单位米）。

**备选方案**：PostGIS 多边形围栏 — 更精确但引入 PostGIS 扩展依赖，增加运维复杂度。对于工地场景，圆形围栏足够覆盖实际需求。

**Haversine 距离计算在后端执行**（而非前端），原因是：
- 前端坐标可被篡改，后端以其存储的围栏数据为权威
- 避免向前端暴露围栏中心点和半径数据

### 决策 2：打卡区域 — 一个项目可配置多个

**选择**：`project_id` 不设唯一约束，一个项目可以有多个打卡区域（如正门、侧门各一个）。用户只需在任意一个区域内即可打卡成功。

**SQL 验证逻辑**：查询项目下所有未删除的打卡区域，计算用户坐标到每个区域中心的 Haversine 距离，只要有一个距离 ≤ 半径即通过。

### 决策 3：距离计算在后端 Java 代码中完成

**选择**：在 `common/util/LocationUtils.java` 中实现 Haversine 公式，而非 SQL 查询中使用 PostGIS 函数。

**理由**：
- 无需引入 PostGIS 或额外 PostgreSQL 扩展
- Java 计算足够快（项目打卡区域通常不超过 5 个）
- 可复用 — 模拟定位检测等其他场景也需用到

### 决策 4：模拟定位检测策略

**选择**：前端小程序 + 后端联合检测，后端最终评定风险等级。

| 检测项 | 阈值 | 风险贡献 |
|--------|------|----------|
| `accuracy > 50` | 精度超过 50 米 | +1（可疑） |
| `verticalAccuracy < 0` | 垂直精度为负（模拟器特征） | +2（高风险） |
| `speed > 0` | 打卡时有移动速度 | +1（可疑） |
| 时间戳偏差 > 5min | 手机时间与服务器时间差 | +1（可疑） |
| `altitude === 0` | 海拔恰好为 0（伪造特征） | +1（可疑） |

风险等级：0 = 正常, 1-2 = 可疑, 3+ = 高风险。高风险打卡仍然记录但不阻止，由管理员审查。

### 决策 5：后端模块拆分

**选择**：新建 `attendancearea` 独立模块管理打卡区域配置，与现有 `attendance` 模块解耦。

```
attendance/              attendancearea/
├── api/                 ├── api/
│   └── AttendanceController  │   └── AttendanceAreaController
├── application/         ├── application/
│   └── AttendanceAppService   │   └── AttendanceAreaAppService
├── domain/              ├── domain/
│   ├── Attendance       │   ├── AttendanceArea
│   ├── AttendanceStatus │   ├── AttendanceAreaRepository
│   └── AttendanceRepository   └── AttendanceAreaDomainService
└── infrastructure/      └── infrastructure/
    (已有)                    ├── AttendanceAreaMapper
                             ├── AttendanceAreaPO
                             ├── AttendanceAreaPoMapper
                             └── AttendanceAreaRepositoryImpl
```

`AttendanceApplicationService` 在打卡流程中注入 `AttendanceAreaRepository` 查询打卡区域，实现跨模块编排。

### 决策 6：小程序端页面结构

**选择**：新建 `pages/checkin/checkin` 独立页面，不在现有 Dashboard 中内嵌。

小程序路由：`login` → `dashboard` → `checkin`

Dashboard 中的"考勤打卡"入口从占位改为 `wx.navigateTo({url: '/pages/checkin/checkin'})`。

## Risks / Trade-offs

- **[精度依赖]** GPS 在室内或高楼密集区精度下降，可能导致合法打卡被拒绝 → 适当放宽半径配置建议（≥ 100m），管理员可根据实际场地调整
- **[Haversine 误差]** Haversine 公式假设正球体，实际地球为椭球体，误差约 0.3% → 对于工地打卡场景（数百米量级）完全可以忽略
- **[模拟定位绕过]** 高级模拟定位工具可能通过所有检测 → 当前检测覆盖常见模拟手段，后续可迭代增加更多指标
- **[GCJ-02 坐标系]** 微信返回 GCJ-02 坐标，数据库存储也应是 GCJ-02 → 管理员在后台设置圆心时需使用 GCJ-02 坐标系（可集成地图选点辅助）
- **[重构风险]** attendance 模块新增 Service 层会改变现有调用链 → 保留 Controller 的 API 路径不变，仅内部实现重构
