<template>
  <view class="page-shell">
    <view class="section-title">消息会话</view>
    <view v-if="list.length" class="card list">
      <view v-for="item in list" :key="item.id" class="msg-item" @click="openDetail(item)">
        <view class="msg-head">
          <text class="msg-title">{{ item.title || '未命名会话' }}</text>
          <text class="msg-badge" v-if="item.unreadCount">{{ item.unreadCount }}</text>
        </view>
        <text class="msg-content">{{ item.lastMessage?.content || '暂无消息' }}</text>
        <text class="msg-time">{{ formatTime(item.lastMessage?.createdAt || item.updatedAt) }}</text>
      </view>
    </view>
    <view v-else class="card empty">暂无聊天记录，后台系统消息会显示在这里。</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import request from '../../utils/request'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const list = ref([])

const loadList = async () => {
  list.value = await request.get('/conversations')
  await store.fetchUnread()
}

onShow(loadList)

const openDetail = (item) => {
  uni.navigateTo({
    url: `/pages/messages/detail?id=${item.id}&title=${encodeURIComponent(item.title || '会话详情')}&currentUserId=${store.profile?.userId || ''}`,
  })
}

const formatTime = (value) => {
  return value ? value.replace('T', ' ') : ''
}
</script>

<style scoped lang="scss">
.list {
  overflow: hidden;
}

.msg-item {
  padding: 26rpx 24rpx;
  border-bottom: 2rpx solid #eef2f7;
}

.msg-item:last-child {
  border-bottom: 0;
}

.msg-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.msg-title {
  font-weight: 700;
}

.msg-badge {
  min-width: 38rpx;
  padding: 4rpx 10rpx;
  border-radius: 999rpx;
  background: #ef4444;
  color: #fff;
  font-size: 22rpx;
  text-align: center;
}

.msg-content,
.msg-time,
.empty {
  color: #64748b;
}

.msg-time {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
}

.empty {
  padding: 30rpx;
}
</style>
