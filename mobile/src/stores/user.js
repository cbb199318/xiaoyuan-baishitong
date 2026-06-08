import { defineStore } from 'pinia'
import request from '../utils/request'
import { closeSocket, connectSocket } from '../utils/socket'

const DEFAULT_ANNOUNCEMENT = '欢迎来到校园百事通，祝你使用顺利。'
const ANNOUNCEMENT_PREFIXES = ['【系统公告】', '【公告】', '系统公告：', '公告：']
const NON_ANNOUNCEMENT_PREFIXES = [
  '举报工单状态已更新',
  '实名认证结果已更新',
  '账号状态已更新',
  '举报权限已更新',
  '跑腿订单已进入争议处理',
  '订单已被承接',
]

const normalizeAnnouncementContent = (content) => {
  const raw = String(content || '').trim()
  if (!raw) {
    return ''
  }
  const prefixed = ANNOUNCEMENT_PREFIXES.find((prefix) => raw.startsWith(prefix))
  if (prefixed) {
    return raw.slice(prefixed.length).trim() || raw
  }
  return raw
}

const isAnnouncementMessage = (content) => {
  const raw = String(content || '').trim()
  if (!raw) {
    return false
  }
  if (ANNOUNCEMENT_PREFIXES.some((prefix) => raw.startsWith(prefix))) {
    return true
  }
  if (NON_ANNOUNCEMENT_PREFIXES.some((prefix) => raw.startsWith(prefix))) {
    return false
  }
  return false
}

const readStoredProfile = () => {
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

export const useUserStore = defineStore('user', {
  state: () => ({
    token: uni.getStorageSync('token') || '',
    profile: readStoredProfile(),
    unread: 0,
    systemAnnouncement: '',
    systemConversationId: null,
    reportStatusTick: 0,
    messageEventTick: 0,
  }),
  actions: {
    setToken(token) {
      this.token = token
      uni.setStorageSync('token', token)
      connectSocket(token)
    },
    setProfile(profile) {
      if (!profile) {
        this.profile = null
        uni.setStorageSync('user-info', '')
        return
      }
      const currentPublishRole =
        this.profile?.publishRole ||
        this.profile?.roleType ||
        readStoredProfile()?.publishRole ||
        'STUDENT'
      const normalizedRole = String(profile.publishRole || profile.roleType || currentPublishRole).toUpperCase()
      const nextProfile = {
        ...this.profile,
        ...profile,
        publishRole: normalizedRole === 'BUSINESS' || normalizedRole === 'MERCHANT' || normalizedRole === 'ADMIN' ? 'BUSINESS' : 'STUDENT',
      }
      this.profile = nextProfile
      uni.setStorageSync('user-info', nextProfile)
    },
    setPublishRole(role) {
      const normalizedRole = String(role || '').toUpperCase() === 'BUSINESS' ? 'BUSINESS' : 'STUDENT'
      this.setProfile({
        ...(this.profile || {}),
        publishRole: normalizedRole,
      })
    },
    clearAuth() {
      this.token = ''
      this.profile = null
      this.unread = 0
      this.systemAnnouncement = ''
      this.systemConversationId = null
      this.messageEventTick = 0
      uni.removeStorageSync('token')
      uni.removeStorageSync('user-info')
      closeSocket()
    },
    async fetchProfile() {
      const profile = await request.get('/auth/me')
      this.setProfile(profile)
      return this.profile
    },
    async fetchUnread() {
      const data = await request.get('/messages/unread')
      this.unread = data.totalUnread || 0
      return this.unread
    },
    async syncAnnouncement() {
      const conversations = await request.get('/conversations')
      const systemConversation = (conversations || []).find((item) => item.type === 'SYSTEM')
      this.systemConversationId = systemConversation?.id || null
      if (!this.systemConversationId) {
        this.systemAnnouncement = DEFAULT_ANNOUNCEMENT
        return this.systemAnnouncement
      }
      const messages = await request.get(`/conversations/${this.systemConversationId}/messages`)
      const announcementMessage = [...(messages || [])].reverse().find((item) => isAnnouncementMessage(item?.content))
      this.systemAnnouncement = normalizeAnnouncementContent(announcementMessage?.content) || DEFAULT_ANNOUNCEMENT
      return this.systemAnnouncement
    },
    applySocketEvent(payload) {
      if (!payload?.event) {
        return
      }
      if (payload.event === 'conversation.unread') {
        this.unread = payload.data?.totalUnread || 0
        this.messageEventTick += 1
      }
      if (payload.event === 'message.new') {
        const message = payload.data?.message
        if (
          payload.data?.conversationId === this.systemConversationId &&
          isAnnouncementMessage(message?.content)
        ) {
          this.systemAnnouncement = normalizeAnnouncementContent(message.content) || DEFAULT_ANNOUNCEMENT
        }
        this.messageEventTick += 1
      }
      if (payload.event === 'report.status.changed') {
        this.reportStatusTick += 1
      }
    },
  },
})
