import { defineStore } from 'pinia'
import request from '../utils/request'
import { closeSocket, connectSocket } from '../utils/socket'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: uni.getStorageSync('token') || '',
    profile: null,
    unread: 0,
    systemAnnouncement: '',
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
      this.profile = profile
      uni.setStorageSync('user-info', profile || '')
    },
    clearAuth() {
      this.token = ''
      this.profile = null
      this.unread = 0
      this.systemAnnouncement = ''
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
      this.systemAnnouncement = systemConversation?.lastMessage?.content || '欢迎来到校园百事通，一期通用功能正在持续完善中。'
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
        if (message?.content) {
          this.systemAnnouncement = message.content
        }
        this.messageEventTick += 1
      }
      if (payload.event === 'report.status.changed') {
        this.reportStatusTick += 1
      }
    },
  },
})
