## ADDED Requirements

### Requirement: 文件上传

系统 SHALL 提供通用文件上传接口，接受 multipart/form-data 格式的文件，存储至本地磁盘，返回文件 ID 供业务模块关联使用。

#### Scenario: 成功上传图片文件

- **WHEN** 用户通过 `POST /api/files/upload` 上传一个 JPEG 图片，文件大小不超过配置上限
- **THEN** 系统将文件保存到 `{uploadDir}/{business_type}/{yyyy-MM}/{uuid}.jpg`，在 `t_upload_file` 表中插入一条记录，返回 `{ "id": <fileId>, "originalName": "现场照片.jpg", "fileSize": 204800, "contentType": "image/jpeg" }`

#### Scenario: 上传时指定业务类型

- **WHEN** 用户上传文件时附带 `businessType=construction_waste` 参数
- **THEN** 系统在 `t_upload_file` 记录中设置 `business_type = 'construction_waste'`，`business_id` 初始为 NULL（待业务记录创建后回填）

#### Scenario: 文件大小超限

- **WHEN** 用户上传的文件超过配置的最大上传大小（默认 10MB）
- **THEN** 系统返回错误码 `FILE_TOO_LARGE`，不存储文件

#### Scenario: 文件类型不允许

- **WHEN** 用户上传非图片类型的文件（如可执行文件）
- **THEN** 系统返回错误码 `FILE_TYPE_NOT_ALLOWED`，不存储文件

### Requirement: 文件下载/预览

系统 SHALL 提供文件下载接口，根据文件 ID 返回文件二进制流及正确的 Content-Type，支持浏览器直接预览图片。

#### Scenario: 成功预览图片

- **WHEN** 用户通过 `GET /api/files/{id}` 请求一个存在的图片文件
- **THEN** 系统以 `Content-Type: image/jpeg` 返回文件二进制流，浏览器可直接渲染

#### Scenario: 文件不存在

- **WHEN** 用户请求一个不存在的文件 ID
- **THEN** 系统返回错误码 `FILE_NOT_FOUND`

### Requirement: 文件删除

系统 SHALL 提供文件删除接口，同时删除磁盘上的物理文件和数据库记录。

#### Scenario: 成功删除文件

- **WHEN** 用户通过 `DELETE /api/files/{id}` 删除一个存在的文件
- **THEN** 系统删除磁盘上的物理文件，删除 `t_upload_file` 中对应记录，返回成功

#### Scenario: 删除不存在的文件

- **WHEN** 用户请求删除一个不存在的文件 ID
- **THEN** 系统返回错误码 `FILE_NOT_FOUND`
