## ADDED Requirements

### Requirement: 废渣运出记录列表

小程序 SHALL 提供废渣运出记录列表页面，支持按项目、废渣种类、日期范围筛选，支持上拉加载更多和下拉刷新。

#### Scenario: 首次加载列表

- **WHEN** 已登录用户从小程序 Dashboard 点击"废渣管理"入口进入列表页
- **THEN** 页面调用 `GET /api/construction-wastes?offset=0&limit=20`，以卡片形式展示废渣记录列表，每条记录显示项目名称、废渣种类、数量单位、运出日期、司机姓名和车牌号

#### Scenario: 按项目筛选

- **WHEN** 用户在列表页的项目 picker 中选择某个项目
- **THEN** 页面重新请求 `GET /api/construction-wastes?projectId=<id>&offset=0&limit=20`，仅展示该项目的废渣记录

#### Scenario: 按废渣种类筛选

- **WHEN** 用户在种类输入框中输入关键词（如"混凝土"）并确认搜索
- **THEN** 页面重新请求 `GET /api/construction-wastes?wasteType=混凝土&offset=0&limit=20`，仅展示种类包含该关键词的记录

#### Scenario: 按日期范围筛选

- **WHEN** 用户选择开始日期和结束日期
- **THEN** 页面重新请求列表 API，携带 `startDate` 和 `endDate` 参数，仅展示运出日期在范围内的记录

#### Scenario: 上拉加载更多

- **WHEN** 用户滚动到列表底部且还有更多数据未加载
- **THEN** 页面请求下一页数据（offset 递增 20），将新记录追加到列表末尾，同时显示加载中提示

#### Scenario: 下拉刷新

- **WHEN** 用户在列表顶部下拉触发刷新
- **THEN** 页面清空已有列表，重新从 `offset=0` 请求第一页数据，加载完成后停止刷新动画

#### Scenario: 无数据

- **WHEN** 列表请求返回空数组
- **THEN** 页面显示"暂无废渣运出记录"的空状态提示

#### Scenario: 点击记录跳转详情

- **WHEN** 用户点击某条废渣记录
- **THEN** 页面跳转到 `/pages/waste/detail?id=<记录ID>`

### Requirement: 新建废渣运出记录

小程序 SHALL 提供废渣运出记录创建页面，允许有 `waste:create` 权限的用户通过填写表单、拍照或选择相册照片上传后提交。

#### Scenario: 有权限用户进入创建页

- **WHEN** 拥有 `waste:create` 权限的用户点击列表页"新建"按钮
- **THEN** 页面跳转到 `/pages/waste/create`，展示空白表单

#### Scenario: 无权限用户隐藏入口

- **WHEN** 登录用户没有 `waste:create` 权限
- **THEN** 列表页不显示"新建"按钮

#### Scenario: 选择项目

- **WHEN** 用户在创建页点击"所属项目"选择器
- **THEN** 弹出项目名称列表（数据来自 `GET /api/projects/select`），用户选择后显示所选项目名称

#### Scenario: 拍照上传照片

- **WHEN** 用户点击照片添加区域并选择"拍照"
- **THEN** 调用 `wx.chooseImage({ sourceType: ['camera'] })` 打开相机，拍照完成后获取临时文件路径，调用 `wx.uploadFile` 上传至 `POST /api/files/upload?businessType=construction_waste`，上传成功后获取 `fileId` 并在页面显示缩略图

#### Scenario: 从相册选择照片

- **WHEN** 用户点击照片添加区域并选择"从相册选择"
- **THEN** 调用 `wx.chooseImage({ sourceType: ['album'] })` 打开相册，选择后获取临时文件路径，调用 `wx.uploadFile` 上传，成功后获取 `fileId` 并在页面显示缩略图

#### Scenario: 并发上传多张照片

- **WHEN** 用户同时选择多张照片（不超过 9 张）
- **THEN** 所有照片并发上传，每张上传完成后在页面显示缩略图和上传进度

#### Scenario: 照片上传失败

- **WHEN** 某张照片上传失败（网络错误或后端返回错误）
- **THEN** 页面显示错误提示"第 N 张照片上传失败，请重试"，已上传成功的文件 ID 不清除

#### Scenario: 移除已选照片（上传前）

- **WHEN** 用户点击某张已选但尚未上传的照片的删除图标
- **THEN** 该照片从待上传列表中移除

#### Scenario: 成功提交表单

- **WHEN** 用户填写完所有必填字段（项目、废渣种类、数量、单位、运出日期、至少一张已上传照片）并点击"提交记录"
- **THEN** 调用 `POST /api/construction-wastes` 携带表单数据和已上传的 `fileIds` 数组，成功后显示"创建成功"Toast，返回列表页

#### Scenario: 必填字段校验

- **WHEN** 用户提交表单但缺少必填字段（项目、废渣种类、数量、单位、运出日期任一项未填，或未上传照片）
- **THEN** 页面显示对应的校验错误提示（如"请选择项目""至少需要上传一张现场照片"），不发起提交请求

#### Scenario: 网络错误处理

- **WHEN** 提交表单时网络请求失败
- **THEN** 页面显示"网络连接失败，请检查网络"的 Toast 提示，保持在创建页不跳转，保留已填写的数据

### Requirement: 查看废渣运出记录详情

小程序 SHALL 提供废渣运出记录详情页面，展示记录的完整信息和关联照片。

#### Scenario: 成功加载详情

- **WHEN** 用户从列表页进入详情页，页面 ID 参数有效
- **THEN** 页面调用 `GET /api/construction-wastes/{id}`，展示项目名称、废渣种类、数量、单位、车牌号、司机姓名、处置去向、运出日期、备注、操作人和创建时间

#### Scenario: 记录不存在

- **WHEN** 详情页请求的记录 ID 不存在或已被软删除
- **THEN** 页面显示"记录不存在"提示，提供返回按钮

#### Scenario: 展示关联照片

- **WHEN** 详情页加载成功且记录关联了照片
- **THEN** 页面底部以网格形式展示所有照片的缩略图

#### Scenario: 照片预览

- **WHEN** 用户点击某张照片缩略图
- **THEN** 调用 `wx.previewImage` 以全屏模式预览该照片，支持左右滑动浏览所有关联照片

### Requirement: 删除废渣运出记录

小程序 SHALL 允许有 `waste:delete` 权限的用户在详情页删除废渣运出记录。

#### Scenario: 有权限用户删除记录

- **WHEN** 拥有 `waste:delete` 权限的用户在详情页点击"删除记录"按钮并确认删除
- **THEN** 系统弹出确认弹窗"确定要删除该运出记录吗？关联的照片将一并删除。"，用户确认后调用 `DELETE /api/construction-wastes/{id}`，成功后显示"已删除"Toast，返回列表页

#### Scenario: 无权限用户隐藏删除按钮

- **WHEN** 登录用户没有 `waste:delete` 权限
- **THEN** 详情页不显示"删除记录"按钮

#### Scenario: 取消删除

- **WHEN** 用户在确认弹窗中点击"取消"
- **THEN** 弹窗关闭，不执行删除操作，停留在详情页

### Requirement: Dashboard 废渣管理入口激活

小程序 Dashboard SHALL 将废渣管理功能入口从"即将上线"占位状态激活为可跳转的有效入口。

#### Scenario: 点击废渣管理入口

- **WHEN** 已登录用户在 Dashboard 功能入口网格中点击"废渣管理"
- **THEN** 跳转到废渣列表页 `/pages/waste/list`

#### Scenario: 入口图标和文字

- **WHEN** 用户查看 Dashboard 功能入口网格
- **THEN** 废渣管理入口显示图标"📷"、名称"废渣管理"、提示文字"点击进入"
