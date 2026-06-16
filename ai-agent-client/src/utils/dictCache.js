import client from '@/api/client'

const cache = {}

/**
 * 获取指定类型的字典项列表（带缓存）
 * @param {string} type 字典类型，如 'UNIT'
 * @returns {Promise<Array<{code: string, label: string, sort: number}>>}
 */
export async function getDict(type) {
  if (cache[type]) return cache[type]
  const { data } = await client.get(`/dicts/${type}`, { params: { enabled: true } })
  cache[type] = data
  return cache[type]
}

/**
 * 根据 code 获取中文标签
 * @param {string} type 字典类型
 * @param {string} code 编码
 * @returns {Promise<string>}
 */
export async function getDictLabel(type, code) {
  if (code == null) return ''
  const dict = await getDict(type)
  return dict.find(d => d.code === code)?.label || code
}

/**
 * 获取下拉选项格式（value/label）
 * @param {string} type 字典类型
 * @returns {Promise<Array<{value: string, label: string}>>}
 */
export async function getDictOptions(type) {
  const dict = await getDict(type)
  return dict.map(d => ({ value: d.code, label: d.label }))
}
