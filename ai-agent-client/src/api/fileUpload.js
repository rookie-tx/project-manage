import axios from 'axios'

const uploadClient = axios.create({
  baseURL: 'http://localhost:8088/api',
  withCredentials: true,
})

/**
 * 上传单个文件
 * @param {File} file - 文件对象
 * @param {string} [businessType] - 业务类型（可选）
 * @returns {Promise<{id: number, originalName: string, storedPath: string, fileSize: number, contentType: string}>}
 */
export async function uploadFile(file, businessType) {
  const formData = new FormData()
  formData.append('file', file)
  if (businessType) {
    formData.append('businessType', businessType)
  }

  const response = await uploadClient.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })

  const { code, msg, data } = response.data
  if (code === 0) {
    return data
  }
  throw new Error(msg)
}

/**
 * 批量上传文件（逐张上传）
 * @param {File[]} files - 文件数组
 * @param {string} [businessType] - 业务类型
 * @returns {Promise<number[]>} 返回上传成功后的 fileId 列表
 */
export async function uploadFiles(files, businessType) {
  const results = []
  for (const file of files) {
    const result = await uploadFile(file, businessType)
    results.push(result.id)
  }
  return results
}

/**
 * 获取文件预览 URL
 * @param {number} fileId
 * @returns {string}
 */
export function getFileUrl(fileId) {
  return `http://localhost:8088/api/files/${fileId}`
}
