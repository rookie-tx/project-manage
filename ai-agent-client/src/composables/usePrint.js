/**
 * 通用打印服务 — 打开新窗口渲染 A4 打印模板并自动触发浏览器打印。
 *
 * 用法:
 *   import { usePrint } from '@/composables/usePrint'
 *   const { openPrint } = usePrint()
 *   openPrint({ title: '物料采购统计', columns: [...], data: [...] })
 */
export function usePrint() {
  /**
   * @param {Object} options
   * @param {string} options.title     - 打印标题
   * @param {Array}  options.columns   - 列定义 [{ title, key }]
   * @param {Array}  options.data      - 数据数组
   */
  function openPrint({ title, columns, data }) {
    const printWindow = window.open('', '_blank', 'width=800,height=600')
    if (!printWindow) {
      return
    }

    const safeTitle = title || '打印'
    const safeColumns = columns || []
    const safeData = data || []

    const tableHead = safeColumns
      .map(col => `<th>${col.title || col.key || ''}</th>`)
      .join('')

    const tableBody = safeData.length > 0
      ? safeData.map(row =>
          '<tr>' +
          safeColumns.map(col => {
            const val = row[col.key] != null ? row[col.key] : ''
            return `<td>${String(val)}</td>`
          }).join('') +
          '</tr>'
        ).join('')
      : `<tr><td colspan="${safeColumns.length}" style="text-align:center;color:#999">暂无数据</td></tr>`

    const styles = `
      <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: "Microsoft YaHei", "PingFang SC", sans-serif; padding: 24px; }
        .print-title { text-align: center; font-size: 18px; font-weight: bold; margin-bottom: 20px; }
        .print-table { width: 100%; border-collapse: collapse; font-size: 13px; }
        .print-table th, .print-table td { border: 1px solid #333; padding: 8px 10px; text-align: center; }
        .print-table thead { background: #f0f0f0; }
        @media print {
          @page { size: A4 portrait; margin: 15mm; }
          body { padding: 0; }
        }
      </style>
    `

    const html = `<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>${safeTitle}</title>${styles}</head>
<body>
  <h2 class="print-title">${safeTitle}</h2>
  <table class="print-table">
    <thead><tr>${tableHead}</tr></thead>
    <tbody>${tableBody}</tbody>
  </table>
  <script>
    // 渲染完成后自动触发打印
    setTimeout(function () { window.print(); }, 300);
    // 打印对话框关闭后自动关闭窗口
    window.onafterprint = function () { window.close(); };
  <\/script>
</body>
</html>`

    printWindow.document.write(html)
    printWindow.document.close()
    printWindow.focus()
  }

  return { openPrint }
}
