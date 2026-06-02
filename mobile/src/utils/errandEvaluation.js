const STORAGE_KEY = 'errand-order-evaluations'

export const errandEvaluationTagOptions = [
  '沟通顺畅',
  '按时完成',
  '态度友好',
  '位置清晰',
  '效率很高',
  '需求明确',
  '值得再次合作',
]

export const readErrandEvaluations = () => {
  const raw = uni.getStorageSync(STORAGE_KEY) || '[]'
  try {
    return JSON.parse(raw)
  } catch (error) {
    return []
  }
}

export const writeErrandEvaluations = (list) => {
  uni.setStorageSync(STORAGE_KEY, JSON.stringify(list))
}

export const saveErrandEvaluation = (record) => {
  const current = readErrandEvaluations()
  const next = [
    record,
    ...current.filter((item) => !(String(item.orderId) === String(record.orderId) && String(item.fromUserId) === String(record.fromUserId))),
  ]
  writeErrandEvaluations(next)
  return next
}

export const findErrandEvaluation = ({ orderId, fromUserId }) =>
  readErrandEvaluations().find((item) => String(item.orderId) === String(orderId) && String(item.fromUserId) === String(fromUserId))

export const listErrandEvaluationsByOrder = (orderId) =>
  readErrandEvaluations().filter((item) => String(item.orderId) === String(orderId))

export const buildErrandCounterparty = (order, currentUserId) => {
  const isPublisher = order?.publisher?.userId === currentUserId
  return {
    isPublisher,
    counterparty: isPublisher ? order?.runner : order?.publisher,
    selfRole: isPublisher ? 'publisher' : 'runner',
    counterpartRole: isPublisher ? 'runner' : 'publisher',
  }
}
