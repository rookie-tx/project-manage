## ADDED Requirements

### Requirement: 按类型查询字典项

系统 SHALL 在 `GET /api/dicts/{type}` 端点返回指定字典类型的所有启用项，按 `sort` 升序排列。

#### Scenario: 成功查询单位字典

- **WHEN** 请求 `GET /api/dicts/UNIT`
- **THEN** 系统返回 `code: 0`，`data` 为字典项数组，每项包含 `code`（String）、`label`（String）、`sort`（Integer），按 `sort` 升序排列

#### Scenario: 过滤已禁用项

- **WHEN** 请求 `GET /api/dicts/UNIT?enabled=true`
- **THEN** 系统仅返回 `enabled=1` 的字典项，`enabled=0` 的项不出现在结果中

#### Scenario: 字典类型不存在

- **WHEN** 请求 `GET /api/dicts/UNKNOWN_TYPE`，该类型在数据库中无记录
- **THEN** 系统返回 `code: 0`，`data` 为空数组 `[]`

#### Scenario: 返回所有项（含禁用）

- **WHEN** 请求 `GET /api/dicts/UNIT?enabled=false`
- **THEN** 系统返回所有项（含 `enabled=0` 的），按 `sort` 升序

### Requirement: 字典项响应格式

字典项 SHALL 包含以下字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| code | String | 字典项编码，如 "TON" |
| label | String | 显示名，如 "吨" |
| sort | Integer | 排序权重 |

#### Scenario: 响应结构

- **WHEN** 查询字典项成功
- **THEN** 每条记录包含 `code`、`label`、`sort` 三个字段，不暴露内部 `id`、`type`、`enabled` 字段
