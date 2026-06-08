import {
  applyPartnerDemand,
  getPartnerConversationDetail as fetchPartnerConversationDetail,
  listPartnerConversations,
  markPartnerConversationRead as postPartnerConversationRead,
  sendPartnerConversationMessage,
  updatePartnerConversationStatus as postPartnerConversationStatus,
} from './partnerApi'

const storageKeys = {
  refreshTick: 'partner-chat-refresh-tick',
}

const touchRefreshTick = () => {
  const tick = String(Date.now())
  uni.setStorageSync(storageKeys.refreshTick, tick)
  return tick
}

const readCurrentProfile = () => {
  const raw = uni.getStorageSync('user-info')
  if (!raw) {
    return null
  }
  if (typeof raw === 'object') {
    return raw
  }
  try {
    return JSON.parse(raw)
  } catch (error) {
    return null
  }
}

export const getPartnerChatRefreshTick = () => uni.getStorageSync(storageKeys.refreshTick) || ''

export const getPartnerConversations = async () => {
  const list = await listPartnerConversations()
  touchRefreshTick()
  return Array.isArray(list) ? list : []
}

export const ensurePartnerConversation = async ({ demand }) => {
  if (!demand?.id) {
    throw new Error('partner demand id required')
  }
  const conversation = await applyPartnerDemand(demand.id)
  touchRefreshTick()
  return conversation
}

export const getPartnerConversationDetail = async (conversationId) => {
  if (!conversationId) {
    return null
  }
  const detail = await fetchPartnerConversationDetail(conversationId)
  touchRefreshTick()
  return detail || null
}

export const markPartnerConversationRead = async (conversationId, currentUserId) => {
  if (!conversationId || !currentUserId) {
    return null
  }
  const detail = await postPartnerConversationRead(conversationId)
  touchRefreshTick()
  return detail || null
}

export const appendPartnerMessage = async ({ conversationId, currentUser, type, content }) => {
  if (!conversationId || !content) {
    return null
  }
  const payload = {
    type: String(type || 'TEXT').toUpperCase(),
    content,
  }
  const detail = await sendPartnerConversationMessage(conversationId, payload)
  touchRefreshTick()
  return detail || null
}

export const deletePartnerConversation = async () => {
  uni.showToast({ title: '暂不支持删除会话', icon: 'none' })
  return false
}

export const updatePartnerConversationStatus = async ({ conversationId, status }) => {
  if (!conversationId || !status) {
    return null
  }
  const detail = await postPartnerConversationStatus(conversationId, { status })
  touchRefreshTick()
  return detail || null
}

export const findPartnerConversationByDemandAndUser = async (demandId, currentUserId) => {
  if (!demandId || !currentUserId) {
    return null
  }
  const list = await getPartnerConversations()
  return (
    list.find(
      (item) =>
        String(item.demandId) === String(demandId) &&
        (Number(item.applicantId) === Number(currentUserId) || Number(item.publisherId) === Number(currentUserId))
    ) || null
  )
}
