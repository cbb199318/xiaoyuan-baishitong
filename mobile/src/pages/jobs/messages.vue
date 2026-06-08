<template>
  <view :class="[embedded ? 'embedded-page' : 'page-shell']">
    <view v-if="list.length" class="conversation-list">
      <view v-for="item in list" :key="item.id" class="conversation-card" @click="openChat(item)">
        <view class="conversation-avatar" :style="{ background: item.counterpartyAvatarColor }">
          {{ (item.counterpartyName || '兼').slice(0, 1) }}
        </view>
        <view class="conversation-main">
          <view class="conversation-head">
            <text class="conversation-name">{{ item.counterpartyName || '岗位沟通' }}</text>
            <text class="conversation-time">{{ formatTime(item.updatedAt) }}</text>
          </view>
          <text class="conversation-summary">{{ item.jobTitle || '岗位咨询会话' }}</text>
          <view class="conversation-foot">
            <text class="conversation-preview">{{ item.latestMessage || '点击进入会话开始沟通' }}</text>
            <text v-if="item.unreadCount" class="conversation-badge">{{ item.unreadCount }}</text>
          </view>
          <view class="conversation-actions">
            <view class="mini-action" @click.stop="clearUnread(item)">清空未读</view>
            <view class="mini-action danger" @click.stop="removeConversation(item)">删除会话</view>
          </view>
        </view>
      </view>
    </view>

    <view v-else class="empty-card">
      <text class="empty-title">暂无兼职沟通消息</text>
      <text class="empty-desc">报名、接单或岗位咨询后，会自动生成对应聊天会话并展示在这里。</text>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import request from '../../utils/request'
import { hideJobsConversation, listJobsConversations } from '../../utils/jobsApi'

defineProps({
  embedded: {
    type: Boolean,
    default: false,
  },
})

const store = useUserStore()
const list = ref([])

const buildList = async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  const conversations = await listJobsConversations()
  list.value = conversations || []
  await store.fetchUnread()
}

const formatTime = (value) => (value ? value.replace('T', ' ').slice(0, 16) : '')

const openChat = (item) => {
  uni.navigateTo({
    url: `/pages/jobs/chat?id=${item.id}&title=${encodeURIComponent(item.counterpartyName || '兼职沟通')}&jobId=${item.jobId || ''}&phone=${item.counterpartyPhone || ''}`,
  })
}

const clearUnread = async (item) => {
  await request.post(`/conversations/${item.id}/read`)
  await buildList()
  uni.showToast({ title: '未读已清空', icon: 'success' })
}

const removeConversation = (item) => {
  uni.showModal({
    title: '删除会话',
    content: '删除后会在当前账号的勤工消息列表中隐藏，但不会删除后台聊天记录，是否继续？',
    success: async (result) => {
      if (!result.confirm) {
        return
      }
      await hideJobsConversation(item.id)
      await buildList()
      uni.showToast({ title: '会话已删除', icon: 'success' })
    },
  })
}

watch(() => store.messageEventTick, buildList)

onShow(buildList)
</script>

<style scoped lang="scss">
.embedded-page {
  min-height: 100%;
  padding: 20rpx 24rpx 24rpx;
  box-sizing: border-box;
}

.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.conversation-card,
.empty-card {
  display: flex;
  gap: 18rpx;
  padding: 24rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.06);
}

.conversation-avatar {
  width: 84rpx;
  height: 84rpx;
  border-radius: 50%;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 800;
  flex-shrink: 0;
}

.conversation-main {
  flex: 1;
  min-width: 0;
}

.conversation-head,
.conversation-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.conversation-name,
.empty-title {
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
}

.conversation-time {
  flex-shrink: 0;
  font-size: 22rpx;
  color: #94a3b8;
}

.conversation-summary,
.conversation-preview,
.empty-desc {
  display: block;
  margin-top: 10rpx;
  font-size: 23rpx;
  line-height: 1.7;
  color: #64748b;
}

.conversation-preview {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 0;
}

.conversation-badge {
  min-width: 36rpx;
  height: 36rpx;
  padding: 0 10rpx;
  border-radius: 999rpx;
  background: #ef4444;
  color: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  font-weight: 700;
  flex-shrink: 0;
}

.conversation-actions {
  display: flex;
  gap: 12rpx;
  margin-top: 16rpx;
}

.mini-action {
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

.mini-action.danger {
  background: #fef2f2;
  color: #dc2626;
}

.empty-card {
  display: block;
}
</style>
