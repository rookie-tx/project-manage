const client = require('../../api/client')
const auth = require('../../utils/auth')

Page({
  data: {
    projectId: null,
    projectName: '',
    items: [],
    loading: true,
    canvasReady: false
  },

  onLoad(options) {
    if (!auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    const id = options.id
    this.setData({ projectId: id })
    this.loadProjectName(id)
    this.loadStats(id)
  },

  async loadProjectName(id) {
    try {
      const data = await client.get('/projects/' + id)
      this.setData({ projectName: data.name || '' })
    } catch { /* name fallback to id */ }
  },

  async loadStats(id) {
    this.setData({ loading: true })
    try {
      const items = await client.get('/projects/' + id + '/purchase-statistics')
      this.setData({ items: items || [] })
    } catch {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  /** Canvas 打印 — 绘制统计表格并生成图片 */
  handlePrint() {
    const that = this
    if (!that.data.items.length) {
      wx.showToast({ title: '暂无数据可打印', icon: 'none' })
      return
    }

    wx.showLoading({ title: '生成打印图片...' })

    // 计算 Canvas 尺寸
    const colWidths = [180, 140, 100, 140, 160]
    const totalWidth = colWidths.reduce((a, b) => a + b, 0) // 720
    const rowHeight = 52
    const headerHeight = 56
    const titleHeight = 64
    const canvasHeight = titleHeight + headerHeight + (that.data.items.length + 1) * rowHeight + 20

    that.setData({ canvasWidth: totalWidth, canvasHeight }, () => {
      const query = wx.createSelectorQuery()
      query.select('#statsCanvas')
        .fields({ node: true, size: true })
        .exec((res) => {
          if (!res || !res[0]) {
            wx.hideLoading()
            wx.showToast({ title: 'Canvas 初始化失败', icon: 'none' })
            return
          }
          const canvas = res[0].node
          const ctx = canvas.getContext('2d')
          const dpr = wx.getSystemInfoSync().pixelRatio
          canvas.width = totalWidth * dpr
          canvas.height = canvasHeight * dpr
          ctx.scale(dpr, dpr)

          const items = that.data.items
          const columns = ['物料名称', '规格', '单位', '总采购数量', '总采购金额']

          // 背景
          ctx.fillStyle = '#ffffff'
          ctx.fillRect(0, 0, totalWidth, canvasHeight)

          // 标题
          ctx.fillStyle = '#1f2937'
          ctx.font = 'bold 18px sans-serif'
          ctx.textAlign = 'center'
          ctx.fillText(that.data.projectName + ' — 物料采购统计', totalWidth / 2, 36)

          // 表头
          let x = 0
          const headerY = titleHeight
          ctx.fillStyle = '#f3f4f6'
          ctx.fillRect(0, headerY, totalWidth, headerHeight)
          ctx.fillStyle = '#1f2937'
          ctx.font = 'bold 13px sans-serif'
          ctx.textAlign = 'center'
          columns.forEach((col, i) => {
            const cx = x + colWidths[i] / 2
            ctx.fillText(col, cx, headerY + 34)
            x += colWidths[i]
          })

          // 数据行
          items.forEach((item, rowIdx) => {
            const y = headerY + headerHeight + rowIdx * rowHeight
            if (rowIdx % 2 === 1) {
              ctx.fillStyle = '#fafafa'
              ctx.fillRect(0, y, totalWidth, rowHeight)
            }

            // 列分隔线
            ctx.strokeStyle = '#e5e7eb'
            ctx.lineWidth = 0.5
            ctx.beginPath()
            ctx.moveTo(0, y + rowHeight)
            ctx.lineTo(totalWidth, y + rowHeight)
            ctx.stroke()

            ctx.fillStyle = '#374151'
            ctx.font = '12px sans-serif'
            ctx.textAlign = 'center'

            const vals = [
              item.materialName || '-',
              item.materialSpec || '-',
              item.materialUnit || '-',
              item.totalQuantity != null ? String(item.totalQuantity) : '-',
              item.totalAmount != null ? '¥' + Number(item.totalAmount).toLocaleString() : '-'
            ]

            let vx = 0
            vals.forEach((val, ci) => {
              // 截断过长文字
              const text = String(val).length > 12 ? String(val).substring(0, 11) + '…' : String(val)
              ctx.fillText(text, vx + colWidths[ci] / 2, y + 32)
              vx += colWidths[ci]
            })
          })

          // 导出图片
          wx.canvasToTempFilePath({
            canvas,
            success(res) {
              wx.hideLoading()
              wx.previewImage({
                urls: [res.tempFilePath],
                current: res.tempFilePath
              })
            },
            fail() {
              wx.hideLoading()
              wx.showToast({ title: '生成打印图片失败', icon: 'none' })
            }
          })
        })
    })
  },

  /** 格式化金额 */
  formatAmount(v) {
    return v != null ? '¥' + Number(v).toLocaleString() : '-'
  }
})
