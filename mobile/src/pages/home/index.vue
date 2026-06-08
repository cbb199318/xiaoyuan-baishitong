<template>
  <view class="page-shell">
    <view class="section-title">系统公告</view>
    <view class="card notice-card">
      <text class="notice-text">{{ announcement }}</text>
    </view>

    <view class="section-title">功能入口</view>
    <view class="grid">
      <view v-for="item in modules" :key="item.title" class="card grid-item" @click="openPlaceholder(item)">
        <text class="grid-title">{{ item.title }}</text>
        <text class="grid-desc">{{ item.desc }}</text>
      </view>
    </view>

    <view class="section-title">快捷入口</view>
    <view class="card quick-list">
      <view class="quick-item" @click="go('/pages/verification/index')">实名认证</view>
      <view class="quick-item" @click="go('/pages/report/create')">提交举报</view>
      <view class="quick-item" @click="go('/pages/report/list')">举报记录</view>
      <view class="quick-item" @click="go('/pages/profile/edit')">编辑资料</view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const announcement = computed(
  () => store.systemAnnouncement || '欢迎来到校园百事通，祝你使用顺利。'
)

const modules = [
  { title: '校园跑腿', desc: '发单、接单、履约流转' },
  { title: '找搭子', desc: '需求发布、申请匹配、聊天' },
  { title: '美妆选购', desc: '商品展示、种草、举报' },
  { title: '勤工助学', desc: '岗位发布、报名、审核' },
]

onShow(() => {
  store.syncAnnouncement()
})

const openPlaceholder = (item) => {
  if (item.title === '校园跑腿') {
    uni.navigateTo({ url: '/pages/errand/index?tab=square' })
    return
  }
  if (item.title === '找搭子') {
    uni.navigateTo({ url: '/pages/partner/index' })
    return
  }
  if (item.title === '勤工助学') {
    uni.navigateTo({ url: '/pages/jobs/index?tab=home' })
    return
  }
  if (item.title === '美妆选购') {
    uni.navigateTo({ url: '/pages/beauty/index' })
    return
  }
  uni.navigateTo({ url: `/pages/placeholder/index?title=${item.title}` })
}

const go = (url) => {
  uni.navigateTo({ url })
}
</script>

<style scoped lang="scss">
.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 22rpx;
  margin-bottom: 28rpx;
}

.notice-card {
  padding: 26rpx 24rpx;
  margin-bottom: 28rpx;
}

.notice-text {
  color: #475569;
  line-height: 1.7;
}

.grid-item {
  padding: 26rpx 22rpx;
  min-height: 170rpx;
}

.grid-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  margin-bottom: 12rpx;
}

.grid-desc {
  color: #64748b;
  font-size: 24rpx;
}

.quick-list {
  overflow: hidden;
}

.quick-item {
  padding: 28rpx;
  border-bottom: 2rpx solid #eef2f7;
}

.quick-item:last-child {
  border-bottom: 0;
}
</style>
