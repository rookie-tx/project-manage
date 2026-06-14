# project-manage 智能管理平台 — 整体项目规范

## 项目概述

智能管理平台是一个前后端分离的 Web 应用系统。

| 模块     | 目录                  |
|--------|---------------------|
| 前端 SPA | `ai-agent-client/`  |
| 后端服务   | `ai-agent-server/`  |
| 小程序    | `ai-agent-miniapp/` |

## 语言要求

- **所有对话、文档、注释、commit 消息必须使用中文**
- 代码标识符（变量名、函数名、类名、文件名）使用英文 camelCase/PascalCase/kebab-case

---

## 前后端通信协议

### 统一响应格式

所有后端 API 响应均使用 `Result<T>` 包装：

```json
{ "code": 0, "msg": "操作成功", "data": { ... } }
```

- `code === 0` → 成功，前端自动解包 `data` 字段
- `code !== 0` → 业务错误，前端 reject `Error(msg)`
- 即使出错也返回 HTTP 200，通过 `code` 字段区分
- `data` 字段始终序列化（null 也输出）

## OpenSpec 工作流

- 流程：Propose → Design → Spec → Tasks → Implement → Archive
- Spec 使用 Gherkin 风格场景 `openspec/specs/<capability>/spec.md`
- 已归档变更放在 `openspec/changes/archive/YYYY-MM-DD-change-name/`

## 数据库

使用`mysql`，sql模板也使用mysql
