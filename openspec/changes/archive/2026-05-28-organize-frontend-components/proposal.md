## Why

当前 `src/components/` 目录所有组件平铺存放，审批、项目、采购、通用组件混在一起。随着组件增多，文件定位和维护成本上升，需要按功能域分类整理。

## What Changes

- `src/components/` 下新建 `common/`、`project/`、`purchase/`、`approval/` 四个子目录
- 将现有 7 个组件按功能移入对应子目录，**BREAKING**：所有 import 路径需同步更新
- `src/stores/` 保持平铺不变（5 个文件已按领域命名）
- `src/views/` 保持现有按功能分目录不变

## Capabilities

### New Capabilities

<!-- 纯目录重组，无新增能力 -->

### Modified Capabilities

<!-- 无 spec 级需求变更 -->

## Impact

- 受影响文件：7 个组件移动 + 所有引用这些组件的 .vue/.js 文件（约 10 处 import 路径更新）
- 无 API 变更，无依赖变更
