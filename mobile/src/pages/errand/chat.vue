<template>
  <view class="chat-page">
    <view class="errand-navbar">
      <view class="nav-side nav-left" hover-class="nav-side-hover" @click="goBack">‹</view>
      <view class="nav-center">
        <text class="errand-title">订单聊天</text>
      </view>
      <view class="nav-side nav-placeholder"></view>
    </view>

    <view class="chat-shell">
      <view class="chat-head card">
        <view class="head-main">
          <text class="chat-counterparty">{{ title }}</text>
          <text class="chat-summary">{{ orderSummary || '一单一会话，聊天记录永久留存用于售后与举报取证。' }}</text>
        </view>
        <view class="chat-actions">
          <view class="ghost-btn tiny-btn" @click="callPhone">电话联系</view>
          <view class="ghost-btn tiny-btn" @click="openDetail">订单详情</view>
          <view class="ghost-btn tiny-btn" @click="openReport">发起举报</view>
        </view>
      </view>

      <view v-if="messages.length" class="message-list">
        <view
          v-for="item in messages"
          :key="item.id"
          :class="['message-row', item.senderId === currentUserId ? 'self' : 'other']"
        >
          <view class="card message-card">
            <image
              v-if="item.type === 'IMAGE'"
              class="image-message"
              :src="item.content"
              mode="widthFix"
              @click="previewImage(item.content)"
            ></image>
            <text v-else class="text-message">{{ item.content }}</text>
            <text class="message-time">{{ formatTime(item.createdAt) }}</text>
          </view>
        </view>
      </view>
      <view v-else class="card empty-card">
        <text class="empty-title">暂无聊天记录</text>
        <text class="empty-desc">接单成功、履约进度变更、对方留言等消息会在这里完整留存。</text>
      </view>
    </view>

    <view class="composer-wrap">
      <view class="composer card">
        <input v-model.trim="content" class="composer-input" placeholder="输入聊天内容" />
        <view class="composer-tools">
          <view class="tool-chip" @click="sendImage('errand_chat_image')">发送图片</view>
          <view class="tool-chip" @click="sendImage('errand_chat_voucher')">发送凭证</view>
          <view :class="['send-btn', !content ? 'disabled' : '']" @click="sendText">发送</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import request from '../../utils/request'
import { uploadImage, previewImage as previewLocalImage } from '../../utils/upload'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const conversationId = ref('')
const title = ref('订单聊天')
const orderId = ref('')
const phone = ref('')
const orderNo = ref('')
const orderSummary = ref('')
const content = ref('')
const messages = ref([])

const currentUserId = ref(null)

const loadMessages = async () => {
  if (!conversationId.value) {
    return
  }
  messages.value = await request.get(`/conversations/${conversationId.value}/messages`)
  await request.post(`/conversations/${conversationId.value}/read`)
  await store.fetchUnread()
}

const sendText = async () => {
  if (!content.value) {
    return
  }
  await request.post('/messages/send', {
    conversationId: Number(conversationId.value),
    type: 'TEXT',
    content: content.value,
  })
  content.value = ''
  await loadMessages()
}

const sendImage = async (bizType) => {
  const file = await uploadImage({ bizType })
  await request.post('/messages/send', {
    conversationId: Number(conversationId.value),
    type: 'IMAGE',
    content: file.url,
  })
  await loadMessages()
}

const previewImage = (url) => {
  previewLocalImage(url)
}

const callPhone = () => {
  if (!phone.value) {
    uni.showToast({ title: '暂无可拨打号码', icon: 'none' })
    return
  }
  uni.makePhoneCall({ phoneNumber: phone.value })
}

const openDetail = () => {
  if (!orderId.value) {
    return
  }
  uni.navigateTo({ url: `/pages/errand/detail?id=${orderId.value}` })
}

const openReport = () => {
  if (!orderId.value) {
    return
  }
  uni.navigateTo({
    url: `/pages/report/create?module=errand&targetType=order&targetId=${orderId.value}`,
  })
}

const formatTime = (value) => {
  return value ? value.replace('T', ' ') : ''
}

const goBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.redirectTo({ url: '/pages/errand/index?tab=messages' })
    },
  })
}

watch(() => store.messageEventTick, () => {
  loadMessages()
})

onLoad((options) => {
  conversationId.value = options?.id || ''
  title.value = decodeURIComponent(options?.title || '订单聊天')
  orderId.value = options?.orderId || ''
  phone.value = options?.phone || ''
  orderNo.value = decodeURIComponent(options?.orderNo || '')
  orderSummary.value = decodeURIComponent(options?.summary || '')
  currentUserId.value = store.profile?.userId || null
})

onShow(async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  currentUserId.value = store.profile?.userId || null
  await loadMessages()
})
</script>

<style lang="scss">
.chat-page {
  min-height: 100vh;
  background: #f4f7fb;
}

.errand-navbar {
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

.nav-side {
  min-height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1f2937;
  font-size: 28rpx;
  border-radius: 18rpx;
}

.nav-left {
  font-size: 42rpx;
}

.nav-placeholder {
  pointer-events: none;
}

.nav-center {
  text-align: center;
  min-width: 0;
}

.errand-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav-side-hover {
  background: #f1f5f9;
}

.chat-shell {
  padding: 24rpx 24rpx calc(200rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.chat-head {
  padding: 22rpx;
  margin-bottom: 18rpx;
}

.head-main {
  margin-bottom: 14rpx;
}

.chat-counterparty {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #0f172a;
}

.chat-summary {
  display: block;
  margin-top: 8rpx;
  color: #64748b;
  line-height: 1.7;
  font-size: 23rpx;
}

.chat-actions {
  display: flex;
  gap: 12rpx;
}

.tiny-btn {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.message-row {
  display: flex;
}

.message-row.self {
  justify-content: flex-end;
}

.message-row.other {
  justify-content: flex-start;
}

.message-card {
  max-width: 78%;
  padding: 22rpx;
  border-radius: 24rpx;
}

.message-row.self .message-card {
  background: linear-gradient(135deg, #2d7ff9, #3aa8ff);
}

.message-row.self .text-message,
.message-row.self .message-time {
  color: #ffffff;
}

.message-row.other .message-card {
  background: #ffffff;
}

.text-message {
  display: block;
  line-height: 1.7;
  color: #1f2937;
  word-break: break-all;
}

.message-time {
  display: block;
  margin-top: 12rpx;
  color: #94a3b8;
  font-size: 22rpx;
}

.image-message {
  width: 100%;
  border-radius: 18rpx;
  display: block;
}

.empty-card {
  padding: 34rpx;
  text-align: center;
}

.empty-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #0f172a;
}

.empty-desc {
  display: block;
  margin-top: 12rpx;
  color: #94a3b8;
  font-size: 24rpx;
  line-height: 1.8;
}

.composer-wrap {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 25;
  padding: 14rpx 24rpx calc(14rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.98);
  border-top: 1px solid #e2e8f0;
}

.composer {
  padding: 18rpx;
}

.composer-input {
  width: 100%;
  min-height: 82rpx;
  border-radius: 18rpx;
  background: #f8fafc;
  padding: 0 22rpx;
  box-sizing: border-box;
  font-size: 26rpx;
}

.composer-tools {
  display: flex;
  gap: 12rpx;
  margin-top: 14rpx;
}

.tool-chip {
  flex: 1;
  height: 70rpx;
  line-height: 70rpx;
  border-radius: 16rpx;
  text-align: center;
  font-size: 24rpx;
  color: #2d7ff9;
  background: #eef4ff;
}

.send-btn {
  flex: 1;
  height: 70rpx;
  line-height: 70rpx;
  border-radius: 16rpx;
  text-align: center;
  font-size: 24rpx;
  color: #ffffff;
  background: linear-gradient(135deg, #1677ff, #38bdf8);
}

.send-btn.disabled {
  background: #cbd5e1;
}
</style>
