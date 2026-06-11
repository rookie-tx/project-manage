import client from './client'

export function createWaste(payload) {
  return client.post('/construction-wastes', payload)
}

export function deleteWaste(id) {
  return client.delete(`/construction-wastes/${id}`)
}
