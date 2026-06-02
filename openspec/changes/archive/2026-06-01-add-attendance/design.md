## Context

P0 已实现临时工模块（t_temp_worker 关联 t_user），临时工可登录但功能有限（仅 user:read 权限）。P1 新增打卡功能。

## Goals / Non-Goals

**Goals:**
- 上班打卡 / 下班打卡（任何时间）
- 临时工查看自己当月打卡记录
- 管理员按项目查看打卡记录
- 临时工登录后跳转打卡页

**Non-Goals:**
- 打卡时间校验（如必须 8:00 前）
- 考勤统计/异常处理
- GPS 定位

## Decisions

### 决策 1: 数据模型

```
t_attendance
├── worker_id FK → t_temp_worker
├── project_id FK → t_project (冗余，方便按项目查询)
├── clock_in TIMESTAMP
├── clock_out TIMESTAMP (nullable)
├── status SMALLINT (1=已上班, 2=已下班)
```

同一临时工同一天只能有一条记录。上班打卡 INSERT，下班打卡 UPDATE。

### 决策 2: 打卡逻辑

```java
// 上班: 查今天是否已打卡，未打卡才可打
if (attendanceRepo.findTodayByWorker(workerId).isPresent())
    throw BusinessException("今天已打卡");
attendanceRepo.save(new Attendance(workerId, projectId, clockIn=NOW(), status=CLOCKED_IN));

// 下班: 查今天打卡记录，未下班才可打
Attendance today = attendanceRepo.findTodayByWorker(workerId)
    .orElseThrow(() -> BusinessException("今天未打卡"));
if (today.getStatus() == CLOCKED_OUT)
    throw BusinessException("今天已下班");
attendanceRepo.update(today.withClockOut(NOW()));
```

### 决策 3: 权限

| 权限 | 说明 |
|------|------|
| attendance:clock-in | 上班打卡 |
| attendance:clock-out | 下班打卡 |

temp_worker 角色自动获得这两条权限。

### 决策 4: 前端

- 临时工登录后 `App.vue` 检测到 `temp_worker` 角色 → 跳转 `/attendance`
- 打卡页显示大按钮 + 当月记录表格
- 项目详情临时工 Tab 新增子 tab 查看打卡记录
