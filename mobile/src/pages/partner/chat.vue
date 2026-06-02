<template>
  <view class="partner-chat-page">
    <view class="partner-navbar">
      <view class="partner-nav-side partner-nav-left" hover-class="partner-nav-hover" @click="goBack">‹</view>
      <view class="partner-nav-center">
        <text class="partner-nav-title">{{ detail?.counterpartyName || '搭子聊天' }}</text>
      </view>
      <view class="partner-nav-side partner-nav-right" hover-class="partner-nav-hover" @click="openReport">
        举报
      </view>
    </view>

    <view class="partner-chat-shell">
      <view class="partner-chat-head card">
        <text class="partner-chat-demand">{{ detail?.demandTitle || '搭子需求沟通' }}</text>
        <text class="partner-chat-summary">{{ detail?.demandSummary || '申请成功后可以在这里继续沟通活动细节。' }}</text>
        <view v-if="counterpartyTags.length" class="partner-chat-tags">
          <text v-for="tag in counterpartyTags" :key="tag" class="partner-chat-tag">{{ tag }}</text>
        </view>
        <view class="partner-chat-actions">
          <view class="partner-chat-link" @click="openCounterpartyProfile">对方主页</view>
          <view class="partner-chat-link" @click="openDemandDetail">需求详情</view>
        </view>
      </view>

      <view v-if="messages.length" class="partner-message-list">
        <view
          v-for="item in messages"
          :key="item.id"
          :class="['partner-message-row', item.senderId === currentUserId ? 'self' : 'other']"
        >
          <view class="card partner-message-card">
            <image
              v-if="item.type === 'IMAGE'"
              class="partner-image-message"
              :src="item.content"
              mode="widthFix"
              @click="previewImage(item.content)"
            ></image>
            <text v-else class="partner-text-message">{{ item.content }}</text>
            <text class="partner-message-time">{{ formatTime(item.createdAt) }}</text>
          </view>
        </view>
      </view>

      <view v-else class="card partner-empty-card">
        <text class="partner-empty-title">暂无聊天记录</text>
        <text class="partner-empty-desc">发送第一条消息，和对方继续确认搭子细节吧。</text>
      </view>
    </view>

    <view class="partner-composer-wrap">
      <view class="partner-composer card">
        <input v-model.trim="content" class="partner-composer-input" placeholder="输入聊天内容" />
        <view class="partner-composer-tools">
          <view class="partner-tool-chip" @click="sendImage">图片</view>
          <view :class="['partner-send-btn', !content ? 'disabled' : '']" @click="sendText">发送</view>
        </view>
      </view>
    </view>

    <PartnerReportModal
      v-model:visible="showReportModal"
      module="partner"
      target-type="conversation"
      :target-id="detail?.id || conversationId"
    />
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import PartnerReportModal from '../../components/PartnerReportModal.vue'
import {
  appendPartnerMessage,
  getPartnerChatRefreshTick,
  getPartnerConversationDetail,
  markPartnerConversationRead,
} from '../../utils/partnerChat'
import { getPartnerProfileTags } from '../../utils/partnerMock'
import { previewImage as previewLocalImage, uploadImage } from '../../utils/upload'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const conversationId = ref('')
const content = ref('')
const refreshTick = ref('')
const detail = ref(null)
const showReportModal = ref(false)

const currentUserId = computed(() => store.profile?.userId || null)
const messages = computed(() => detail.value?.messages || [])
const counterpartyTags = computed(() => {
  if (!detail.value || !currentUserId.value) {
    return []
  }
  const targetId = detail.value.publisherId === currentUserId.value ? detail.value.applicantId : detail.value.publisherId
  return getPartnerProfileTags(targetId).slice(0, 4)
})

const syncConversation = () => {
  if (!conversationId.value) {
    return
  }
  refreshTick.value = getPartnerChatRefreshTick()
  detail.value = getPartnerConversationDetail(conversationId.value)
  if (detail.value && currentUserId.value) {
    detail.value.counterpartyName = detail.value.publisherId === currentUserId.value
      ? detail.value.applicantName
      : detail.value.publisherName
  }
}

const sendText = () => {
  if (!content.value || !conversationId.value) {
    return
  }
  appendPartnerMessage({
    conversationId: conversationId.value,
    currentUser: store.profile,
    type: 'TEXT',
    content: content.value,
  })
  content.value = ''
  syncConversation()
}

const sendImage = async () => {
  const file = await uploadImage({ bizType: 'partner_chat_image' })
  appendPartnerMessage({
    conversationId: conversationId.value,
    currentUser: store.profile,
    type: 'IMAGE',
    content: file.url,
  })
  syncConversation()
}

const previewImage = (url) => {
  previewLocalImage(url)
}

const openReport = () => {
  showReportModal.value = true
}

const openDemandDetail = () => {
  if (!detail.value?.demandId) {
    return
  }
  uni.navigateTo({ url: `/pages/partner/detail?id=${detail.value.demandId}` })
}

const openCounterpartyProfile = () => {
  if (!detail.value || !currentUserId.value) {
    return
  }
  const targetId = detail.value.publisherId === currentUserId.value ? detail.value.applicantId : detail.value.publisherId
  if (!targetId) {
    return
  }
  uni.navigateTo({ url: `/pages/partner/profile?userId=${targetId}` })
}

const goBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.navigateTo({ url: '/pages/partner/messages' })
    },
  })
}

const formatTime = (value) => (value ? value.replace('T', ' ').slice(0, 16) : '')

watch(() => store.messageEventTick, syncConversation)

onLoad((options) => {
  conversationId.value = options?.id || ''
})

onShow(async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  syncConversation()
  if (conversationId.value && currentUserId.value) {
    markPartnerConversationRead(conversationId.value, currentUserId.value)
    syncConversation()
  }
})
</script>

<style lang="scss">
.partner-chat-page {
  min-height: 100vh;
  background: #f4f7fb;
}

.partner-navbar {
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

.partner-nav-side {
  min-height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1f2937;
  font-size: 28rpx;
  border-radius: 18rpx;
}

.partner-nav-left {
  font-size: 42rpx;
}

.partner-nav-right {
  font-size: 24rpx;
  color: #ef4444;
}

.partner-nav-center {
  text-align: center;
  min-width: 0;
}

.partner-nav-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.partner-nav-hover {
  background: #f1f5f9;
}

.partner-chat-shell {
  padding: 24rpx 24rpx calc(196rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.partner-chat-head {
  padding: 22rpx;
  margin-bottom: 18rpx;
}

.partner-chat-demand {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}

.partner-chat-summary {
  display: block;
  margin-top: 8rpx;
  font-size: 23rpx;
  line-height: 1.7;
  color: #64748b;
}

.partner-chat-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 12rpx;
}

.partner-chat-tag {
  padding: 6rpx 12rpx;
  border-radius: 999rpx;
  background: #eef2ff;
  color: #4f46e5;
  font-size: 20rpx;
}

.partner-chat-actions {
  display: flex;
  margin-top: 14rpx;
}

.partner-chat-link {
  min-height: 68rpx;
  padding: 0 18rpx;
  border-radius: 18rpx;
  background: #eff6ff;
  color: #2563eb;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
}

.partner-message-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.partner-message-row {
  display: flex;
}

.partner-message-row.self {
  justify-content: flex-end;
}

.partner-message-row.other {
  justify-content: flex-start;
}

.partner-message-card {
  max-width: 78%;
  padding: 22rpx;
  border-radius: 24rpx;
}

.partner-message-row.self .partner-message-card {
  background: linear-gradient(135deg, #2d7ff9, #3aa8ff);
}

.partner-message-row.self .partner-text-message,
.partner-message-row.self .partner-message-time {
  color: #ffffff;
}

.partner-message-row.other .partner-message-card {
  background: #ffffff;
}

.partner-text-message {
  display: block;
  line-height: 1.7;
  color: #1f2937;
  word-break: break-all;
}

.partner-message-time {
  display: block;
  margin-top: 12rpx;
  color: #94a3b8;
  font-size: 22rpx;
}

.partner-image-message {
  width: 100%;
  border-radius: 18rpx;
  display: block;
}

.partner-empty-card {
  padding: 34rpx;
  text-align: center;
}

.partner-empty-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #0f172a;
}

.partner-empty-desc {
  display: block;
  margin-top: 10rpx;
  color: #64748b;
  font-size: 23rpx;
  line-height: 1.7;
}

.partner-composer-wrap {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  background: rgba(244, 247, 251, 0.95);
  backdrop-filter: blur(14rpx);
}

.partner-composer {
  padding: 18rpx;
}

.partner-composer-input {
  width: 100%;
  min-height: 84rpx;
  padding: 0 22rpx;
  border-radius: 20rpx;
  background: #f8fafc;
  box-sizing: border-box;
}

.partner-composer-tools {
  display: flex;
  gap: 12rpx;
  margin-top: 14rpx;
}

.partner-tool-chip,
.partner-send-btn {
  flex: 1;
  min-height: 76rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 700;
}

.partner-tool-chip {
  background: #eef2ff;
  color: #3151c9;
}

.partner-send-btn {
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
}

.partner-send-btn.disabled {
  background: #cbd5e1;
}
</style>
