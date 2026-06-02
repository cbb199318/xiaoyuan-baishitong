<template>
  <view class="page-shell">
    <view class="section-title">{{ title }}</view>
    <view v-if="messages.length" class="message-list">
      <view v-for="item in messages" :key="item.id" :class="['message-card', item.senderId === currentUserId ? 'self' : 'other']">
        <image v-if="item.type === 'IMAGE'" class="image-message" :src="item.content" mode="widthFix"></image>
        <text v-else class="text-message">{{ item.content }}</text>
        <text class="message-time">{{ formatTime(item.createdAt) }}</text>
      </view>
    </view>
    <view v-else class="card empty">当前会话还没有消息。</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import request from '../../utils/request'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const conversationId = ref('')
const title = ref('会话详情')
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

onLoad((options) => {
  conversationId.value = options?.id || ''
  title.value = options?.title || '会话详情'
  currentUserId.value = Number(options?.currentUserId || 0) || null
})

onShow(loadMessages)

const formatTime = (value) => {
  return value ? value.replace('T', ' ') : ''
}
</script>

<style scoped lang="scss">
.message-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.message-card {
  max-width: 78%;
  padding: 22rpx 24rpx;
  border-radius: 22rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 22rpx rgba(15, 23, 42, 0.06);
}

.message-card.self {
  align-self: flex-end;
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #fff;
}

.message-card.other {
  align-self: flex-start;
}

.text-message {
  display: block;
  line-height: 1.6;
}

.message-time {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  opacity: 0.72;
}

.image-message {
  width: 100%;
  border-radius: 16rpx;
}

.empty {
  padding: 30rpx;
  color: #64748b;
}
</style>
