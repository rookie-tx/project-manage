## MODIFIED Requirements

### Requirement: Dashboard 首页

小程序 SHALL 提供 Dashboard 首页，展示当前用户信息和功能入口。

#### Scenario: 展示用户信息

- **WHEN** 已登录用户进入 Dashboard
- **THEN** 页面显示用户名和当前角色信息

#### Scenario: 基础布局

- **WHEN** 用户查看 Dashboard 页面
- **THEN** 展示顶部导航栏（标题"智能管理平台"）和功能入口网格（考勤打卡、项目管理等）

#### Scenario: 考勤打卡入口可跳转

- **WHEN** 用户点击"考勤打卡"功能入口
- **THEN** 跳转到打卡页面 `/pages/checkin/checkin`
