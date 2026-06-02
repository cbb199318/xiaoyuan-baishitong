<template>
  <view class="partner-message-page">
    <view class="partner-navbar">
      <view class="partner-nav-side partner-nav-left" hover-class="partner-nav-hover" @click="goBack">‹</view>
      <view class="partner-nav-center">
        <text class="partner-nav-title">搭子消息</text>
      </view>
      <view class="partner-nav-side partner-nav-right" hover-class="partner-nav-hover" @click="openReportEntry">
        举报
      </view>
    </view>

    <view class="partner-message-content">
      <view v-if="list.length" class="partner-message-list">
        <view
          v-for="item in list"
          :key="item.id"
          class="partner-message-card"
          @click="openChat(item)"
          @longpress="removeConversation(item)"
        >
          <view class="partner-message-avatar" :style="{ background: item.counterpartyAvatarColor }">
            {{ (item.counterpartyName || '搭').slice(0, 1) }}
          </view>
          <view class="partner-message-main">
            <view class="partner-message-head">
              <text class="partner-message-name">{{ item.counterpartyName }}</text>
              <text class="partner-message-time">{{ formatTime(item.updatedAt) }}</text>
            </view>
            <text class="partner-message-summary">{{ item.demandTitle }}</text>
            <view v-if="item.counterpartyTags?.length" class="counterparty-tags">
              <text v-for="tag in item.counterpartyTags" :key="tag" class="counterparty-tag">{{ tag }}</text>
            </view>
            <view class="partner-message-foot">
              <text class="partner-message-preview">{{ item.latestMessage }}</text>
              <text v-if="item.unreadCount" class="partner-message-badge">{{ item.unreadCount }}</text>
            </view>
          </view>
        </view>
      </view>

      <view v-else class="partner-empty-card">
        <text class="partner-empty-title">暂无聊天记录</text>
        <text class="partner-empty-desc">申请搭子成功后，双方会自动生成独立聊天会话并显示在这里。</text>
      </view>
    </view>

    <PartnerReportModal
      v-model:visible="showReportModal"
      module="partner"
      target-type="conversation"
      :target-id="activeReportTargetId"
    />
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import PartnerReportModal from '../../components/PartnerReportModal.vue'
import { deletePartnerConversation, getPartnerChatRefreshTick, getPartnerConversations } from '../../utils/partnerChat'
import { getPartnerProfileTags } from '../../utils/partnerMock'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const list = ref([])
const refreshTick = ref('')
const showReportModal = ref(false)
const activeReportTargetId = ref('')

const buildList = () => {
  if (!store.profile) {
    return
  }
  refreshTick.value = getPartnerChatRefreshTick()
  const currentUserId = store.profile.userId
  list.value = getPartnerConversations(currentUserId).map((item) => {
    const isPublisher = item.publisherId === currentUserId
    return {
      ...item,
      counterpartyName: isPublisher ? item.applicantName : item.publisherName,
      counterpartyAvatarColor: isPublisher ? item.applicantAvatarColor : item.publisherAvatarColor,
      counterpartyTags: getPartnerProfileTags(isPublisher ? item.applicantId : item.publisherId).slice(0, 3),
    }
  })
}

const openChat = (item) => {
  uni.navigateTo({ url: `/pages/partner/chat?id=${item.id}` })
}

const removeConversation = (item) => {
  uni.showModal({
    title: '删除会话',
    content: '删除后只会清除当前本地会话记录，是否继续？',
    success: (result) => {
      if (!result.confirm) {
        return
      }
      deletePartnerConversation(item.id)
      buildList()
      uni.showToast({ title: '会话已删除', icon: 'success' })
    },
  })
}

const openReportEntry = () => {
  activeReportTargetId.value = ''
  showReportModal.value = true
}

const goBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.navigateTo({ url: '/pages/partner/index' })
    },
  })
}

const formatTime = (value) => (value ? value.replace('T', ' ').slice(0, 16) : '')

watch(() => store.messageEventTick, buildList)

onShow(async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  buildList()
})
</script>

<style lang="scss">
.partner-message-page {
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
}

.partner-nav-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
}

.partner-nav-hover {
  background: #f1f5f9;
}

.partner-message-content {
  padding: 24rpx;
  box-sizing: border-box;
}

.partner-message-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.partner-message-card {
  display: flex;
  gap: 18rpx;
  padding: 24rpx;
  border-radius: 26rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.06);
}

.partner-message-avatar {
  width: 84rpx;
  height: 84rpx;
  border-radius: 50%;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 700;
  flex-shrink: 0;
}

.partner-message-main {
  flex: 1;
  min-width: 0;
}

.partner-message-head,
.partner-message-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.partner-message-name {
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}

.partner-message-time {
  flex-shrink: 0;
  font-size: 22rpx;
  color: #94a3b8;
}

.partner-message-summary {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #64748b;
}

.counterparty-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 10rpx;
}

.counterparty-tag {
  padding: 6rpx 12rpx;
  border-radius: 999rpx;
  background: #eef2ff;
  color: #4f46e5;
  font-size: 20rpx;
}

.partner-message-preview {
  margin-top: 12rpx;
  flex: 1;
  min-width: 0;
  font-size: 24rpx;
  color: #334155;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.partner-message-badge {
  min-width: 38rpx;
  padding: 4rpx 10rpx;
  border-radius: 999rpx;
  background: #ef4444;
  color: #ffffff;
  font-size: 22rpx;
  text-align: center;
  flex-shrink: 0;
}

.partner-empty-card {
  padding: 48rpx 30rpx;
  border-radius: 26rpx;
  background: #ffffff;
  text-align: center;
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.06);
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
  line-height: 1.7;
  font-size: 23rpx;
  color: #64748b;
}
</style>
