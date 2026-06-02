<template>
  <view class="page-shell">
    <view class="card profile-card">
      <view class="avatar">{{ (profile?.nickname || '用').slice(0, 1) }}</view>
      <view class="info">
        <text class="name">{{ profile?.nickname || '未登录用户' }}</text>
        <text class="phone">{{ profile?.phone || '-' }}</text>
        <text class="status">实名状态：{{ profile?.realNameStatus || 'UNSUBMITTED' }}</text>
        <text class="status">未读消息：{{ store.unread }}</text>
      </view>
    </view>

    <view class="card action-list">
      <view class="action-item" @click="go('/pages/profile/edit')">编辑个人资料</view>
      <view class="action-item" @click="go('/pages/verification/index')">实名认证</view>
      <view class="action-item" @click="go('/pages/report/list')">举报记录</view>
      <view class="action-item" @click="logout">退出登录</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import request from '../../utils/request'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const profile = ref(null)

const loadProfile = async () => {
  profile.value = await request.get('/user/profile')
  await store.fetchUnread()
}

onShow(loadProfile)

const go = (url) => {
  uni.navigateTo({ url })
}

const logout = async () => {
  await request.post('/auth/logout')
  store.clearAuth()
  uni.reLaunch({ url: '/pages/login/index' })
}
</script>

<style scoped lang="scss">
.profile-card {
  padding: 28rpx;
  display: flex;
  align-items: center;
  margin-bottom: 26rpx;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb, #38bdf8);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  font-weight: 700;
  margin-right: 24rpx;
}

.info text {
  display: block;
}

.name {
  font-size: 34rpx;
  font-weight: 700;
  margin-bottom: 10rpx;
}

.phone,
.status {
  color: #64748b;
}

.action-list {
  overflow: hidden;
}

.action-item {
  padding: 28rpx;
  border-bottom: 2rpx solid #eef2f7;
}

.action-item:last-child {
  border-bottom: 0;
}
</style>
