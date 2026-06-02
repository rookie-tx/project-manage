## ADDED Requirements

### Requirement: 上班打卡
系统 SHALL 在 `POST /api/attendance/clock-in` 允许临时工上班打卡。

#### Scenario: 成功打卡
- **WHEN** 临时工今天未打卡，请求上班打卡
- **THEN** 系统插入记录（clock_in=当前时间, status=1）

#### Scenario: 重复打卡
- **WHEN** 临时工今天已打卡，再次请求上班打卡
- **THEN** 系统返回错误"今天已打卡"

### Requirement: 下班打卡
系统 SHALL 在 `POST /api/attendance/clock-out` 允许临时工下班打卡。

#### Scenario: 成功下班
- **WHEN** 临时工今天已上班且未下班，请求下班打卡
- **THEN** 系统更新 clock_out=当前时间, status=2

#### Scenario: 未上班先下班
- **WHEN** 临时工今天未打卡，请求下班打卡
- **THEN** 系统返回错误"今天未打卡"

### Requirement: 当月记录
系统 SHALL 在 `GET /api/attendance/my?yearMonth=2026-06` 返回当前临时工当月打卡记录。

### Requirement: 项目打卡记录
系统 SHALL 在 `GET /api/projects/{id}/attendance?yearMonth=2026-06` 返回该项目下所有临时工的打卡记录。

### Requirement: 临时工登录跳转
系统 SHALL 在临时工登录后跳转到打卡页面，而非 Dashboard。
