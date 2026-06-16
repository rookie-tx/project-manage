# 修复采购申请编辑时单位展示 code 而非 label

## Context

在 `add-generic-dict` 变更中，`PurchaseRequestResponse` 的 `unitLabel` 字段被移除，`materialUnit` 现在返回的是字典 code（如 `"TON"`）而非中文标签（如 `"吨"`）。Web 端和小程序端在编辑采购申请时，表单回填逻辑直接使用了 `materialUnit` 字段作为显示文本，导致编辑表单中单位展示的是 code。

## 根因

| 端 | 文件 | 行 | 问题代码 |
|----|------|-----|---------|
| Web | `PurchaseRequestForm.vue` | 61 | `materialUnitLabel: vals.materialUnit ?? ''` — 直接用 code 赋值 |
| 小程序 | `pages/purchase/form.js` | 104 | `materialUnitLabel: detail.materialUnit \|\| ''` — 直接用 code 赋值 |

两处都已具备可用的字典缓存（`unitLabelMap` / `store.unitLabelMap`），只需在回填时做一次 code→label 转换。

## 修改方案

### 1. Web 端 — `PurchaseRequestForm.vue`

**文件**: `ai-agent-client/src/components/purchase/PurchaseRequestForm.vue`

修改 watch 回调中的回填逻辑（约第 61 行），将：

```javascript
materialUnitLabel: vals.materialUnit ?? '',
```

改为使用 store 的 `getUnitLabel()` 从 unitLabelMap 缓存取 label：

```javascript
materialUnitLabel: store.getUnitLabel(vals.unitCode) || vals.materialUnit || '',
```

`vals.unitCode` 是 PurchaseRequest 自身的单位 code（创建时由物料带入，存储于 t_purchase_request.unit）。`store.getUnitLabel()` 是同步方法，从已预加载的 `unitLabelMap` reactive map 中查找。

### 2. 小程序端 — `pages/purchase/form.js`

**文件**: `ai-agent-miniapp/pages/purchase/form.js`

修改 `loadDetail()` 方法中的回填逻辑（约第 104 行），将：

```javascript
materialUnitLabel: detail.materialUnit || '',
```

改为使用 `unitLabelMap` 查找：

```javascript
materialUnitLabel: this.data.unitLabelMap[detail.unitCode] || detail.materialUnit || '',
```

`detail.unitCode` 是后端返回的采购申请单位 code。`unitLabelMap` 在 `loadUnitLabels()` 中已预加载。

## 验证

1. Web 端：编辑一个已有采购申请，检查"单位"字段是否显示中文标签（如"吨"）而非 code（如"TON"）
2. 小程序端：同上，编辑采购申请时检查单位显示
