## ADDED Requirements

### Requirement: 小程序打卡页面

小程序 SHALL 提供独立的打卡页面（`pages/checkin/checkin`），展示今日打卡状态、上班/下班按钮和本月打卡记录。

#### Scenario: 页面展示今日未打卡状态

- **WHEN** 用户当天尚未打卡且进入打卡页面
- **THEN** 页面显示"上班打卡"按钮为可用状态，"下班打卡"按钮为禁用状态，显示"今日尚未打卡"

#### Scenario: 页面展示今日已上班状态

- **WHEN** 用户当天已上班打卡但未下班打卡
- **THEN** 页面显示"上班打卡"按钮为禁用并标注"已打卡"，"下班打卡"按钮为可用状态，显示上班时间

#### Scenario: 页面展示今日已完成状态

- **WHEN** 用户当天已完成上下班打卡
- **THEN** 两个按钮均禁用，分别标注"已打卡"和"已下班"，显示上下班时间

#### Scenario: 未登录用户进入打卡页

- **WHEN** 未登录用户直接访问打卡页面
- **THEN** 系统自动跳转到登录页

### Requirement: 上班打卡

系统 SHALL 允许用户在获取 GPS 位置后提交上班打卡请求。

#### Scenario: 在打卡区域内上班打卡成功

- **WHEN** 用户点击"上班打卡"，小程序成功获取 GPS 坐标（accuracy ≤ 50），且用户位置在项目某个打卡区域内
- **THEN** 后端记录打卡（含经纬度、精度），返回打卡成功响应，页面更新为已上班状态

#### Scenario: 超出打卡区域上班打卡失败

- **WHEN** 用户点击"上班打卡"，用户位置与所有打卡区域的距离均超过配置半径
- **THEN** 后端返回错误"不在打卡范围内"，提示用户距离最近打卡点还有 X 米

#### Scenario: 已上班重复打卡

- **WHEN** 用户当天已上班打卡成功，再次点击"上班打卡"
- **THEN** 后端返回错误"今日已打卡上班"，前端不做重复请求

#### Scenario: GPS 获取失败

- **WHEN** 用户点击"上班打卡"，但 `wx.getLocation` 调用失败（如用户拒绝授权）
- **THEN** 页面提示"获取位置失败，请检查定位权限"，不发起打卡请求

### Requirement: 下班打卡

系统 SHALL 允许已上班打卡的用户提交下班打卡请求。

#### Scenario: 在打卡区域内下班打卡成功

- **WHEN** 用户点击"下班打卡"，GPS 坐标在项目打卡区域内
- **THEN** 后端更新打卡记录（含下班时间和坐标），返回成功，页面更新为已完成状态

#### Scenario: 未上班直接下班

- **WHEN** 用户当天未上班打卡，直接点击"下班打卡"
- **THEN** 后端返回错误"尚未打卡上班"，前端提示用户

#### Scenario: 已下班重复打卡

- **WHEN** 用户当天已完成下班打卡，再次点击"下班打卡"
- **THEN** 后端返回错误"今日已下班打卡"

### Requirement: 打卡位置记录

系统 SHALL 在每次打卡时记录用户的 GPS 坐标和定位精度。

#### Scenario: 上班打卡记录坐标

- **WHEN** 用户上班打卡成功
- **THEN** `t_attendance` 记录的 `clock_in_lat`、`clock_in_lng` 字段存储用户 GPS 坐标

#### Scenario: 下班打卡记录坐标

- **WHEN** 用户下班打卡成功
- **THEN** `t_attendance` 记录的 `clock_out_lat`、`clock_out_lng` 字段存储用户 GPS 坐标

### Requirement: 模拟定位检测

系统 SHALL 在打卡时进行模拟定位风险检测，并记录风险等级。

#### Scenario: 正常定位检测通过

- **WHEN** 用户打卡时 GPS accuracy ≤ 50、verticalAccuracy ≥ 0、speed = 0、时间戳与服务器偏差 ≤ 5 分钟
- **THEN** `location_risk` 设为 0（正常），打卡正常完成

#### Scenario: 精度异常标记可疑

- **WHEN** 用户打卡时 GPS accuracy > 50
- **THEN** `location_risk` 至少为 1（可疑），打卡仍然完成但记录风险标记

#### Scenario: 垂直精度异常标记高风险

- **WHEN** 用户打卡时 verticalAccuracy < 0（模拟定位特征）
- **THEN** `location_risk` 至少为 2（高风险），打卡仍然完成但需管理员审查

#### Scenario: 综合多项异常标记高风险

- **WHEN** 用户打卡时同时出现 accuracy > 50、speed > 0、时间戳偏差 > 5 分钟
- **THEN** `location_risk` 为 3（高风险），打卡仍然完成但需管理员审查

### Requirement: 本月打卡记录查询

小程序 SHALL 提供查看本月打卡记录的功能。

#### Scenario: 查询本月记录

- **WHEN** 用户进入打卡页面
- **THEN** 页面底部展示当前月份的所有打卡记录，包含日期、上班时间、下班时间、状态

#### Scenario: 无记录月份

- **WHEN** 用户当月没有任何打卡记录
- **THEN** 页面显示"本月暂无打卡记录"空状态
