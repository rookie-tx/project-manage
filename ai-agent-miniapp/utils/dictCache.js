const client = require('../api/client')

const cache = {}

/**
 * 获取指定类型的字典项列表（带缓存）
 * @param {string} type 字典类型，如 'UNIT'
 * @returns {Promise<Array<{code: string, label: string, sort: number}>>}
 */
function getDict(type) {
  if (cache[type]) return Promise.resolve(cache[type])
  return client.get('/dicts/' + type, { enabled: true }).then(data => {
    cache[type] = data
    return data
  })
}

/**
 * 根据 code 获取中文标签
 * @param {string} type 字典类型
 * @param {string} code 编码
 * @returns {Promise<string>}
 */
function getDictLabel(type, code) {
  if (code == null) return Promise.resolve('')
  return getDict(type).then(dict => {
    const item = dict.find(d => d.code === code)
    return item ? item.label : code
  })
}

/**
 * 获取下拉选项格式（code/label）
 * @param {string} type 字典类型
 * @returns {Promise<Array<{code: string, label: string}>>}
 */
function getDictOptions(type) {
  return getDict(type).then(dict => {
    return dict.map(d => ({ code: d.code, label: d.label }))
  })
}

module.exports = { getDict, getDictLabel, getDictOptions }
