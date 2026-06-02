const storageKeys = {
  conversations: 'partner-chat-conversations',
  refreshTick: 'partner-chat-refresh-tick',
}

const defaultAvatarPalette = ['#2563eb', '#0ea5e9', '#14b8a6', '#f97316', '#ec4899']

const safeParse = (raw, fallback) => {
  try {
    const parsed = JSON.parse(raw)
    return parsed ?? fallback
  } catch (error) {
    return fallback
  }
}

const getCurrentUserId = () => {
  const profileRaw = uni.getStorageSync('user-info')
  const profile = typeof profileRaw === 'string' ? safeParse(profileRaw, null) : profileRaw
  return profile?.userId || null
}

const avatarColorFromId = (seed) => defaultAvatarPalette[Math.abs(Number(seed || 0)) % defaultAvatarPalette.length]

const readConversationList = () => {
  const raw = uni.getStorageSync(storageKeys.conversations) || '[]'
  const list = safeParse(raw, [])
  return Array.isArray(list) ? list : []
}

const writeConversationList = (list) => {
  uni.setStorageSync(storageKeys.conversations, JSON.stringify(list))
  uni.setStorageSync(storageKeys.refreshTick, String(Date.now()))
}

const createSystemReply = (demand) => ({
  id: Date.now() + 1,
  senderId: demand.publisherId || demand.id,
  senderName: demand.nickname || '发布者',
  type: 'TEXT',
  content: `你好，看到你的${demand.typeLabel || '搭子'}需求了，我先来打个招呼，后面我们可以在这里继续沟通细节。`,
  createdAt: new Date(Date.now() + 1).toISOString(),
})

export const getPartnerChatRefreshTick = () => uni.getStorageSync(storageKeys.refreshTick) || ''

export const getPartnerConversations = (currentUserId) => {
  const list = readConversationList()
  return list
    .map((item) => {
      const unreadCount = currentUserId ? Number(item.unreadMap?.[currentUserId] || 0) : 0
      return {
        ...item,
        unreadCount,
      }
    })
    .sort((a, b) => new Date(b.updatedAt || 0).getTime() - new Date(a.updatedAt || 0).getTime())
}

export const ensurePartnerConversation = ({ demand, currentUser }) => {
  const currentUserId = currentUser?.userId || getCurrentUserId() || Date.now()
  const all = readConversationList()
  const existing = all.find((item) => item.demandId === demand.id && item.applicantId === currentUserId)
  if (existing) {
    return existing
  }

  const now = new Date().toISOString()
  const applicantName = currentUser?.nickname || '我'
  const conversation = {
    id: `partner-${demand.id}-${currentUserId}`,
    demandId: demand.id,
    demandTitle: `${demand.typeLabel || '搭子'} · ${demand.location || '待定地点'}`,
    demandSummary: demand.description || '',
    demandType: demand.type,
    demandLocation: demand.location,
    publisherId: demand.publisherId || demand.id,
    publisherName: demand.nickname || '发布者',
    publisherAvatarColor: avatarColorFromId(demand.publisherId || demand.id),
    applicantId: currentUserId,
    applicantName,
    applicantAvatarColor: avatarColorFromId(currentUserId),
    status: 'PENDING',
    applicantStatus: 'PENDING',
    latestMessage: '已发起搭子申请',
    latestMessageType: 'TEXT',
    updatedAt: now,
    unreadMap: {
      [currentUserId]: 0,
      [demand.publisherId || demand.id]: 1,
    },
    messages: [
      {
        id: Date.now(),
        senderId: currentUserId,
        senderName: applicantName,
        type: 'TEXT',
        content: `你好，我想申请你的${demand.typeLabel || '搭子'}需求，方便继续聊一下活动安排吗？`,
        createdAt: now,
      },
      createSystemReply(demand),
    ],
  }

  conversation.latestMessage = conversation.messages[conversation.messages.length - 1].content
  conversation.latestMessageType = conversation.messages[conversation.messages.length - 1].type
  conversation.updatedAt = conversation.messages[conversation.messages.length - 1].createdAt

  writeConversationList([conversation, ...all])
  return conversation
}

export const getPartnerConversationDetail = (conversationId) => {
  const all = readConversationList()
  return all.find((item) => item.id === conversationId) || null
}

export const markPartnerConversationRead = (conversationId, currentUserId) => {
  if (!currentUserId) {
    return
  }
  const next = readConversationList().map((item) => {
    if (item.id !== conversationId) {
      return item
    }
    return {
      ...item,
      unreadMap: {
        ...(item.unreadMap || {}),
        [currentUserId]: 0,
      },
    }
  })
  writeConversationList(next)
}

export const appendPartnerMessage = ({ conversationId, currentUser, type, content }) => {
  const currentUserId = currentUser?.userId || getCurrentUserId()
  const next = readConversationList().map((item) => {
    if (item.id !== conversationId) {
      return item
    }
    const targetUserId = item.publisherId === currentUserId ? item.applicantId : item.publisherId
    const message = {
      id: Date.now(),
      senderId: currentUserId,
      senderName: currentUser?.nickname || '我',
      type,
      content,
      createdAt: new Date().toISOString(),
    }
    return {
      ...item,
      latestMessage: type === 'IMAGE' ? '[图片消息]' : content,
      latestMessageType: type,
      updatedAt: message.createdAt,
      unreadMap: {
        ...(item.unreadMap || {}),
        [currentUserId]: 0,
        [targetUserId]: Number(item.unreadMap?.[targetUserId] || 0) + 1,
      },
      messages: [...(item.messages || []), message],
    }
  })
  writeConversationList(next)
}

export const deletePartnerConversation = (conversationId) => {
  writeConversationList(readConversationList().filter((item) => item.id !== conversationId))
}

export const updatePartnerConversationStatus = ({ conversationId, status, operatorName }) => {
  const next = readConversationList().map((item) => {
    if (item.id !== conversationId) {
      return item
    }
    const statusTextMap = {
      PENDING: '申请待回复',
      ACCEPTED: '申请已同意，已匹配成功',
      REJECTED: '申请已拒绝',
    }
    const systemMessage = {
      id: Date.now(),
      senderId: 0,
      senderName: '系统通知',
      type: 'TEXT',
      content: `${operatorName || '发布者'}${statusTextMap[status] || '更新了申请状态'}`,
      createdAt: new Date().toISOString(),
    }
    return {
      ...item,
      status,
      applicantStatus: status,
      latestMessage: systemMessage.content,
      latestMessageType: 'TEXT',
      updatedAt: systemMessage.createdAt,
      unreadMap: {
        ...(item.unreadMap || {}),
        [item.publisherId]: Number(item.unreadMap?.[item.publisherId] || 0) + 1,
        [item.applicantId]: Number(item.unreadMap?.[item.applicantId] || 0) + 1,
      },
      messages: [...(item.messages || []), systemMessage],
    }
  })
  writeConversationList(next)
}

export const findPartnerConversationByDemandAndUser = (demandId, currentUserId) => {
  const list = readConversationList()
  return (
    list.find(
      (item) =>
        String(item.demandId) === String(demandId) &&
        (Number(item.applicantId) === Number(currentUserId) || Number(item.publisherId) === Number(currentUserId))
    ) || null
  )
}
