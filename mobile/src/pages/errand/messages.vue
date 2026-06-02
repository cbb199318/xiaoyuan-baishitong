<template>
  <view :class="[embedded ? 'embedded-page' : 'page-shell']">
    <view class="page-intro">
      <text class="intro-title">跑腿消息中心</text>
      <text class="intro-desc">自动聚合所有已产生交易的订单会话，一单一会话，最新消息优先展示。</text>
    </view>

    <view v-if="list.length" class="conversation-list">
      <view
        v-for="item in list"
        :key="item.id"
        :class="['conversation-card', item.pinned ? 'pinned' : '']"
        @click="openChat(item)"
        @longpress="removeConversation(item)"
      >
        <view class="conversation-head">
          <view class="head-main">
            <text class="counterparty-name">{{ item.counterpartyName }}</text>
            <text v-if="item.pinned" class="pin-tag">置顶</text>
          </view>
          <view class="head-side">
            <text class="message-time">{{ formatTime(item.lastMessageTime) }}</text>
            <text v-if="item.unreadCount" class="msg-badge">{{ item.unreadCount }}</text>
          </view>
        </view>

        <text class="order-summary">{{ item.orderSummary }}</text>
        <text class="message-preview">{{ item.messagePreview }}</text>

        <view class="conversation-actions">
          <view class="mini-action" @click.stop="togglePin(item)">
            {{ item.pinned ? '取消置顶' : '会话置顶' }}
          </view>
          <view class="mini-action" @click.stop="clearUnread(item)">清空未读</view>
          <view class="mini-action danger" @click.stop="removeConversation(item)">删除会话</view>
        </view>
      </view>
    </view>

    <view v-else class="empty-card">
      <text class="empty-title">暂无聊天记录</text>
      <text class="empty-desc">跑腿订单一旦接单成功，就会自动生成对应会话并显示在这里。</text>
    </view>
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import request from '../../utils/request'
import { useUserStore } from '../../stores/user'

defineProps({
  embedded: {
    type: Boolean,
    default: false,
  },
})

const store = useUserStore()
const list = ref([])

const pinKey = computed(() => `errand-message-pins-${store.profile?.userId || 'guest'}`)
const hiddenKey = computed(() => `errand-message-hidden-${store.profile?.userId || 'guest'}`)

const readIdSet = (key) => {
  const raw = uni.getStorageSync(key) || '[]'
  try {
    return new Set(JSON.parse(raw))
  } catch (error) {
    return new Set()
  }
}

const writeIdSet = (key, ids) => {
  uni.setStorageSync(key, JSON.stringify(Array.from(ids)))
}

const buildConversationList = async () => {
  const [published, accepted, conversations] = await Promise.all([
    request.get('/errand/orders/my/published'),
    request.get('/errand/orders/my/accepted'),
    request.get('/conversations'),
  ])

  const hiddenIds = readIdSet(hiddenKey.value)
  const pinnedIds = readIdSet(pinKey.value)
  const conversationMap = new Map((conversations || []).map((item) => [item.id, item]))
  const mergedOrders = [...(published || []), ...(accepted || [])]
  const seenConversationIds = new Set()

  list.value = mergedOrders
    .filter((item) => item.conversationId && !hiddenIds.has(item.conversationId))
    .filter((item) => {
      if (seenConversationIds.has(item.conversationId)) {
        return false
      }
      seenConversationIds.add(item.conversationId)
      return true
    })
    .map((item) => {
      const conversation = conversationMap.get(item.conversationId) || {}
      const isPublisher = item.publisher?.userId === store.profile?.userId
      const counterparty = isPublisher ? item.runner : item.publisher
      const latestMessage = conversation.lastMessage
      return {
        id: item.conversationId,
        orderId: item.id,
        orderNo: item.orderNo,
        phone: counterparty?.phone || '',
        counterpartyName: counterparty?.nickname || (isPublisher ? '待接单用户' : '发单人'),
        orderSummary: `${item.pickupAddress} -> ${item.deliveryAddress}`,
        messagePreview: latestMessage?.type === 'IMAGE'
          ? '[图片消息]'
          : (latestMessage?.content || '暂无消息，点击进入会话开始沟通'),
        lastMessageTime: latestMessage?.createdAt || conversation.updatedAt || item.updatedAt || item.createdAt,
        unreadCount: conversation.unreadCount || 0,
        pinned: pinnedIds.has(item.conversationId),
      }
    })
    .sort((a, b) => {
      if (a.pinned !== b.pinned) {
        return a.pinned ? -1 : 1
      }
      return new Date(b.lastMessageTime || 0).getTime() - new Date(a.lastMessageTime || 0).getTime()
    })
}

const togglePin = (item) => {
  const pins = readIdSet(pinKey.value)
  if (pins.has(item.id)) {
    pins.delete(item.id)
    uni.showToast({ title: '已取消置顶', icon: 'none' })
  } else {
    pins.add(item.id)
    uni.showToast({ title: '会话已置顶', icon: 'success' })
  }
  writeIdSet(pinKey.value, pins)
  buildConversationList()
}

const clearUnread = async (item) => {
  await request.post(`/conversations/${item.id}/read`)
  await store.fetchUnread()
  await buildConversationList()
  uni.showToast({ title: '未读已清空', icon: 'success' })
}

const removeConversation = (item) => {
  uni.showModal({
    title: '删除会话',
    content: '删除后仅在当前账号的跑腿消息列表中隐藏，不会删除后台聊天记录，是否继续？',
    success: (result) => {
      if (!result.confirm) {
        return
      }
      const hiddenIds = readIdSet(hiddenKey.value)
      hiddenIds.add(item.id)
      writeIdSet(hiddenKey.value, hiddenIds)
      buildConversationList()
      uni.showToast({ title: '会话已删除', icon: 'success' })
    },
  })
}

const openChat = (item) => {
  uni.navigateTo({
    url: `/pages/errand/chat?id=${item.id}&title=${encodeURIComponent(item.counterpartyName)}&orderId=${item.orderId}&phone=${item.phone || ''}&orderNo=${encodeURIComponent(item.orderNo || '')}&summary=${encodeURIComponent(item.orderSummary || '')}`,
  })
}

const formatTime = (value) => {
  if (!value) {
    return ''
  }
  return value.replace('T', ' ')
}

watch(() => store.messageEventTick, () => {
  buildConversationList()
})

onShow(() => {
  buildConversationList()
})
</script>

<style lang="scss">
.embedded-page {
  padding: 24rpx 24rpx 0;
  box-sizing: border-box;
}

.page-intro {
  padding: 8rpx 6rpx 22rpx;
}

.intro-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #0f172a;
}

.intro-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 23rpx;
  line-height: 1.7;
  color: #64748b;
}

.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.conversation-card {
  padding: 24rpx;
  border-radius: 26rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.06);
}

.conversation-card.pinned {
  border: 2rpx solid rgba(45, 127, 249, 0.2);
  background: linear-gradient(180deg, rgba(239, 246, 255, 0.7), #ffffff 36%);
}

.conversation-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18rpx;
}

.head-main {
  display: flex;
  align-items: center;
  gap: 10rpx;
  min-width: 0;
}

.counterparty-name {
  font-size: 30rpx;
  font-weight: 700;
  color: #0f172a;
}

.pin-tag {
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
  background: rgba(45, 127, 249, 0.12);
  color: #2d7ff9;
  font-size: 20rpx;
}

.head-side {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-shrink: 0;
}

.message-time {
  color: #94a3b8;
  font-size: 22rpx;
}

.msg-badge {
  min-width: 40rpx;
  padding: 4rpx 10rpx;
  border-radius: 999rpx;
  background: #ef4444;
  color: #fff;
  font-size: 22rpx;
  text-align: center;
}

.order-summary {
  display: block;
  margin-top: 12rpx;
  color: #475569;
  font-size: 24rpx;
  line-height: 1.7;
}

.message-preview {
  display: block;
  margin-top: 10rpx;
  color: #64748b;
  font-size: 24rpx;
  line-height: 1.7;
}

.conversation-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 18rpx;
}

.mini-action {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: #eef4ff;
  color: #2d7ff9;
  font-size: 22rpx;
}

.mini-action.danger {
  background: #fef2f2;
  color: #ef4444;
}

.empty-card {
  padding: 40rpx 32rpx;
  border-radius: 26rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.06);
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
  line-height: 1.8;
  font-size: 24rpx;
}
</style>
