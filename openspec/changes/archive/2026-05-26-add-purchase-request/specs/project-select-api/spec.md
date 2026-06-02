## ADDED Requirements

### Requirement: 查询项目下拉列表
系统 SHALL 在 `GET /api/projects/select` 端点返回所有未删除项目的精简列表。

#### Scenario: 成功获取
- **WHEN** 前端请求 `GET /api/projects/select`
- **THEN** 系统返回 HTTP 200，响应体为 `{ "code": 0, "msg": "操作成功", "data": [{ "id": 1, "code": "PRJ-2026-001", "name": "某某项目" }] }`

#### Scenario: 无项目
- **WHEN** 所有项目已被软删除
- **THEN** 系统返回 HTTP 200，data 为空数组 `[]`

#### Scenario: 排除已删除项目
- **WHEN** 存在 isDeleted=1 的项目
- **THEN** 响应不包含已删除项目

#### Scenario: 未认证访问
- **WHEN** 未登录用户请求
- **THEN** 系统返回 HTTP 401
