<template>
  <view class="jobs-chat-page">
    <view class="jobs-navbar">
      <view class="jobs-nav-side jobs-nav-left" hover-class="jobs-nav-hover" @click="goBack">‹</view>
      <view class="jobs-nav-center">
        <text class="jobs-nav-title">{{ title || '兼职聊天' }}</text>
      </view>
      <view class="jobs-nav-placeholder"></view>
    </view>

    <view class="jobs-chat-shell">
      <view v-if="detail" class="jobs-chat-info">
        <view class="jobs-chat-info-main">
          <text class="jobs-chat-info-title">{{ detail.jobTitle || '兼职沟通' }}</text>
          <text class="jobs-chat-info-sub">{{ detail.counterpartyName || '对方用户' }}</text>
        </view>
        <view class="jobs-chat-info-actions">
          <view class="chat-mini-btn" @click="callPhone">电话联系</view>
          <view class="chat-mini-btn" @click="openDetail">岗位详情</view>
          <view class="chat-mini-btn danger" @click="openReport">举报反馈</view>
        </view>
      </view>

      <view v-if="messages.length" class="jobs-message-list">
        <view
          v-for="item in messages"
          :key="item.id"
          :class="['jobs-message-row', item.senderId === currentUserId ? 'self' : 'other']"
        >
          <view class="jobs-message-card">
            <image
              v-if="item.type === 'IMAGE'"
              class="jobs-image-message"
              :src="item.content"
              mode="widthFix"
              @click="previewLocalImage(item.content)"
            ></image>
            <text v-else class="jobs-text-message">{{ item.content }}</text>
            <text class="jobs-message-time">{{ formatTime(item.createdAt) }}</text>
          </view>
        </view>
      </view>

      <view v-else class="jobs-empty-card">
        <text class="jobs-empty-title">暂无聊天记录</text>
        <text class="jobs-empty-desc">可以先发一条消息，和对方确认兼职细节。</text>
      </view>
    </view>

    <view class="jobs-composer-wrap">
      <view class="jobs-composer">
        <input v-model.trim="content" class="jobs-composer-input" placeholder="输入聊天内容" />
        <view class="jobs-composer-tools">
          <view class="jobs-tool-chip" @click="sendImage">图片</view>
          <view :class="['jobs-send-btn', !content ? 'disabled' : '']" @click="sendText">发送</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import request from '../../utils/request'
import { getJobsConversationDetail } from '../../utils/jobsApi'
import { previewImage as previewLocalImage, uploadImage } from '../../utils/upload'

const store = useUserStore()
const conversationId = ref('')
const title = ref('兼职聊天')
const content = ref('')
const detail = ref(null)
const jobId = ref('')
const messages = ref([])

const readCurrentRouteOptions = () => {
  const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : []
  const current = pages[pages.length - 1]
  const pageOptions = current?.options || {}
  if (Object.keys(pageOptions).length) {
    return pageOptions
  }
  if (typeof window !== 'undefined') {
    const hash = window.location.hash || ''
    const query = hash.includes('?') ? hash.split('?')[1] : ''
    const params = new URLSearchParams(query)
    return {
      id: params.get('id') || '',
      title: params.get('title') || '',
      jobId: params.get('jobId') || '',
      phone: params.get('phone') || '',
    }
  }
  return {}
}

const applyRouteOptions = (options = {}) => {
  const routeOptions = Object.keys(options).length ? options : readCurrentRouteOptions()
  if (routeOptions.id) {
    conversationId.value = routeOptions.id
  }
  if (routeOptions.title) {
    title.value = decodeURIComponent(routeOptions.title)
  }
  if (routeOptions.jobId) {
    jobId.value = routeOptions.jobId
  }
}

const currentUserId = computed(() => store.profile?.userId || null)

const loadConversation = async () => {
  if (!conversationId.value || !currentUserId.value) {
    return
  }
  const [conversation, list] = await Promise.all([
    getJobsConversationDetail(conversationId.value),
    request.get(`/conversations/${conversationId.value}/messages`),
  ])
  detail.value = conversation
  messages.value = list || []
}

const sendText = async () => {
  if (!content.value || !conversationId.value) {
    return
  }
  await request.post('/messages/send', {
    conversationId: Number(conversationId.value),
    type: 'TEXT',
    content: content.value,
  })
  content.value = ''
  await loadConversation()
}

const sendImage = async () => {
  const file = await uploadImage({ bizType: 'jobs_chat_image' })
  await request.post('/messages/send', {
    conversationId: Number(conversationId.value),
    type: 'IMAGE',
    content: file.url,
  })
  await loadConversation()
}

const formatTime = (value) => (value ? value.replace('T', ' ').slice(0, 16) : '')

const callPhone = () => {
  const phone = detail.value?.counterpartyPhone || ''
  if (!phone) {
    uni.showToast({ title: '暂无可拨打号码', icon: 'none' })
    return
  }
  uni.makePhoneCall({ phoneNumber: phone })
}

const openDetail = () => {
  if (!jobId.value) {
    uni.showToast({ title: '暂无岗位详情', icon: 'none' })
    return
  }
  uni.navigateTo({ url: `/pages/jobs/detail?id=${jobId.value}` })
}

const openReport = () => {
  if (!jobId.value) {
    uni.showToast({ title: '暂无可举报目标', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: `/pages/report/create?module=jobs&targetType=job&targetId=${jobId.value}`,
  })
}

const goBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.redirectTo({ url: '/pages/jobs/index?tab=messages' })
    },
  })
}

onLoad((options) => {
  applyRouteOptions(options || {})
})

onShow(async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  applyRouteOptions()
  if (conversationId.value && currentUserId.value) {
    await request.post(`/conversations/${conversationId.value}/read`)
    await store.fetchUnread()
  }
  await loadConversation()
})
</script>

<style scoped lang="scss">
.jobs-chat-page {
  min-height: 100vh;
  background: #f4f7fb;
}

.jobs-navbar {
  position: sticky;
  top: 0;
  z-index: 30;
  display: grid;
  grid-template-columns: 88rpx 1fr 88rpx;
  align-items: center;
  min-height: 96rpx;
  padding: calc(16rpx + env(safe-area-inset-top)) 16rpx 12rpx;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
}

.jobs-nav-side {
  min-height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18rpx;
  color: #1f2937;
}

.jobs-nav-left {
  font-size: 42rpx;
}

.jobs-nav-center {
  text-align: center;
  min-width: 0;
}

.jobs-nav-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.jobs-nav-placeholder {
  min-height: 72rpx;
}

.jobs-nav-hover {
  background: #f1f5f9;
}

.jobs-chat-shell {
  padding: 24rpx 24rpx calc(190rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.jobs-chat-info {
  margin-bottom: 20rpx;
  padding: 22rpx 20rpx;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.06);
}

.jobs-chat-info-title,
.jobs-chat-info-sub {
  display: block;
}

.jobs-chat-info-title {
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
}

.jobs-chat-info-sub {
  margin-top: 10rpx;
  font-size: 23rpx;
  color: #64748b;
}

.jobs-chat-info-actions {
  display: flex;
  gap: 12rpx;
  margin-top: 18rpx;
  flex-wrap: wrap;
}

.chat-mini-btn {
  min-height: 64rpx;
  padding: 0 18rpx;
  border-radius: 18rpx;
  background: #eff6ff;
  color: #2563eb;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
}

.chat-mini-btn.danger {
  background: #fef2f2;
  color: #dc2626;
}

.jobs-message-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.jobs-message-row {
  display: flex;
}

.jobs-message-row.self {
  justify-content: flex-end;
}

.jobs-message-row.other {
  justify-content: flex-start;
}

.jobs-message-card,
.jobs-empty-card {
  max-width: 78%;
  padding: 22rpx;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.06);
}

.jobs-message-row.self .jobs-message-card {
  background: linear-gradient(135deg, #2d7ff9, #38bdf8);
}

.jobs-message-row.self .jobs-text-message,
.jobs-message-row.self .jobs-message-time {
  color: #ffffff;
}

.jobs-text-message,
.jobs-message-time,
.jobs-empty-desc {
  display: block;
  line-height: 1.7;
  color: #64748b;
}

.jobs-message-time {
  margin-top: 12rpx;
  font-size: 22rpx;
}

.jobs-empty-card {
  max-width: 100%;
}

.jobs-empty-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
}

.jobs-image-message {
  width: 280rpx;
  border-radius: 18rpx;
}

.jobs-composer-wrap {
  position: fixed;
  left: 24rpx;
  right: 24rpx;
  bottom: calc(24rpx + env(safe-area-inset-bottom));
}

.jobs-composer {
  padding: 18rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 16rpx 40rpx rgba(15, 23, 42, 0.12);
}

.jobs-composer-input {
  width: 100%;
  min-height: 84rpx;
  padding: 0 20rpx;
  border-radius: 20rpx;
  background: #f8fafc;
  box-sizing: border-box;
}

.jobs-composer-tools {
  display: flex;
  gap: 12rpx;
  margin-top: 14rpx;
}

.jobs-tool-chip,
.jobs-send-btn {
  flex: 1;
  min-height: 74rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 700;
}

.jobs-tool-chip {
  background: #eff6ff;
  color: #2563eb;
}

.jobs-send-btn {
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
}

.jobs-send-btn.disabled {
  background: #cbd5e1;
}
</style>
