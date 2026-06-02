## 1. 创建子目录

- [x] 1.1 创建 `src/components/common/`、`src/components/project/`、`src/components/purchase/`、`src/components/approval/` 四个子目录

## 2. 移动组件文件

- [x] 2.1 移动 `ProjectSelect.vue`、`UserSelect.vue` 到 `src/components/common/`
- [x] 2.2 移动 `ProjectForm.vue` 到 `src/components/project/`
- [x] 2.3 移动 `PurchaseRequestForm.vue` 到 `src/components/purchase/`
- [x] 2.4 移动 `ApprovalDialog.vue`、`ApprovalHistory.vue`、`SubmitApprovalDialog.vue` 到 `src/components/approval/`

## 3. 更新 import 路径

- [x] 3.1 更新 `TaskListPage.vue` — `@/components/ApprovalDialog.vue` → `@/components/approval/ApprovalDialog.vue`
- [x] 3.2 更新 `PurchaseRequestListPage.vue` — `@/components/SubmitApprovalDialog.vue` → `@/components/approval/SubmitApprovalDialog.vue`
- [x] 3.3 更新 `PurchaseRequestDetailPage.vue` — `ApprovalHistory` 和 `SubmitApprovalDialog` 的 import 路径
- [x] 3.4 更新 `PurchaseRequestForm.vue` — `@/components/ProjectSelect.vue` → `@/components/common/ProjectSelect.vue`
- [x] 3.5 更新 `ProjectForm.vue` — `@/components/UserSelect.vue` → `@/components/common/UserSelect.vue`
- [x] 3.6 更新 `ProjectCreatePage.vue` — `@/components/ProjectForm.vue` → `@/components/project/ProjectForm.vue`
- [x] 3.7 更新 `ProjectEditPage.vue` — `@/components/ProjectForm.vue` → `@/components/project/ProjectForm.vue`
- [x] 3.8 更新 `PurchaseRequestCreatePage.vue` — `@/components/PurchaseRequestForm.vue` → `@/components/purchase/PurchaseRequestForm.vue`
- [x] 3.9 更新 `PurchaseRequestEditPage.vue` — `@/components/PurchaseRequestForm.vue` → `@/components/purchase/PurchaseRequestForm.vue`

## 4. 验证

- [x] 4.1 运行前端编译确认无路径错误
