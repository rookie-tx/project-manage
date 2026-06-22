## ADDED Requirements

### Requirement: Web 端通用打印

系统 SHALL 提供可复用的打印服务（usePrint 组合式函数），支持以新窗口方式渲染打印内容并自动触发浏览器打印。

#### Scenario: 调用打印

- **WHEN** 开发者调用 `usePrint().openPrint({ title, columns, data })`
- **THEN** 系统打开新浏览器窗口
- **AND** 新窗口渲染 A4 尺寸的打印模板（含 table 数据和 `@media print` CSS）
- **AND** 内容渲染完成后自动调用 `window.print()`
- **AND** 打印对话框关闭后新窗口自动关闭

#### Scenario: 打印空数据

- **WHEN** 开发者传入空的 data 数组
- **THEN** 打印模板展示"暂无数据"提示

### Requirement: 小程序端打印

系统 SHALL 在小程序端提供打印能力，通过 Canvas 绘制表格并生成图片供用户保存。

#### Scenario: 生成打印图片

- **WHEN** 用户在统计页面点击「打印」按钮
- **THEN** 系统使用 Canvas 绘制统计表格
- **AND** 通过 `canvasToTempFilePath` 生成 PNG 图片
- **AND** 打开图片预览
- **AND** 用户可长按保存或分享图片进行打印

#### Scenario: Canvas 绘制失败

- **WHEN** Canvas 绘制过程中发生错误
- **THEN** 系统展示错误提示「生成打印图片失败」
