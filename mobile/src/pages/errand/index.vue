<template>
  <view class="errand-shell">
    <view class="errand-navbar">
      <view class="nav-side nav-left" hover-class="nav-side-hover" @click="goBack">‹</view>
      <view class="nav-center">
        <text class="errand-title">{{ currentTab.title }}</text>
      </view>
      <view
        v-if="activeTab === 'square'"
        class="nav-side nav-right"
        hover-class="nav-side-hover"
        @click="openRules"
      >
        规则
      </view>
      <view v-else class="nav-side nav-placeholder"></view>
    </view>

    <view class="errand-content">
      <SquarePage v-if="activeTab === 'square'" embedded />
      <CreatePage v-else-if="activeTab === 'create'" embedded />
      <MessagesPage v-else-if="activeTab === 'messages'" embedded />
      <MinePage v-else embedded />
    </view>

    <view class="errand-tabbar">
      <view
        v-for="item in tabs"
        :key="item.key"
        :class="['tab-item', activeTab === item.key ? 'active' : '']"
        @click="activeTab = item.key"
      >
        <text class="tab-label">{{ item.label }}</text>
      </view>
    </view>

    <view v-if="showRules" class="rules-mask" @click="showRules = false">
      <view class="rules-panel" @click.stop>
        <view class="rules-header">
          <text class="rules-title">平台规则</text>
          <text class="rules-close" @click="showRules = false">关闭</text>
        </view>
        <scroll-view scroll-y class="rules-scroll">
          <view v-for="section in errandRuleSections" :key="section.title" class="rules-section">
            <text class="rules-section-title">{{ section.title }}</text>
            <text class="rules-section-body">{{ section.content }}</text>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import SquarePage from './square.vue'
import CreatePage from './create.vue'
import MessagesPage from './messages.vue'
import MinePage from './mine.vue'
import { errandRuleSections } from '../../utils/errandRules'

const tabs = [
  { key: 'square', label: '订单广场', title: '校园跑腿代取服务' },
  { key: 'create', label: '发布订单', title: '发布跑腿订单' },
  { key: 'messages', label: '消息中心', title: '跑腿消息中心' },
  { key: 'mine', label: '个人中心', title: '个人跑腿中心' },
]

const activeTab = ref('square')
const showRules = ref(false)

const currentTab = computed(() => tabs.find((item) => item.key === activeTab.value) || tabs[0])

onLoad((options) => {
  const tab = options?.tab
  if (typeof tab === 'string' && tabs.some((item) => item.key === tab)) {
    activeTab.value = tab
  }
})

onPullDownRefresh(() => {
  if (activeTab.value !== 'square') {
    uni.stopPullDownRefresh()
    return
  }
  uni.$emit('errand-square-refresh')
})

const goBack = () => {
  uni.switchTab({ url: '/pages/home/index' })
}

const openRules = () => {
  showRules.value = true
}
</script>

<style scoped lang="scss">
.errand-shell {
  min-height: 100vh;
  background: #f4f7fb;
  padding-bottom: calc(168rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
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

.nav-right {
  font-size: 24rpx;
  color: #2d7ff9;
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

.errand-content {
  min-height: calc(100vh - 180rpx);
}

.errand-tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  padding: 10rpx 0 calc(10rpx + env(safe-area-inset-bottom));
  background: #ffffff;
  border-top: 1px solid #d7dbe2;
  box-shadow: none;
}

.tab-item {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 88rpx;
  color: #5f6b7a;
  background: transparent;
}

.tab-item.active {
  color: #2d7ff9;
}

.tab-label {
  font-size: 24rpx;
  font-weight: 500;
}

.nav-side-hover {
  background: #f1f5f9;
}

.rules-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.38);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 50;
}

.rules-panel {
  width: 100%;
  max-height: 78vh;
  background: #ffffff;
  border-radius: 28rpx 28rpx 0 0;
  overflow: hidden;
}

.rules-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 28rpx;
  border-bottom: 1px solid #eef2f7;
}

.rules-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #111827;
}

.rules-close {
  font-size: 24rpx;
  color: #64748b;
}

.rules-scroll {
  max-height: calc(78vh - 88rpx);
  padding: 10rpx 0 24rpx;
}

.rules-section {
  padding: 18rpx 28rpx 0;
}

.rules-section-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #111827;
  margin-bottom: 10rpx;
}

.rules-section-body {
  display: block;
  color: #4b5563;
  line-height: 1.8;
  font-size: 24rpx;
}
</style>
