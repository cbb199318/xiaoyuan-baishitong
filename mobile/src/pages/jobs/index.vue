<template>
  <view class="jobs-shell">
    <view v-if="activeTab !== 'publish'" class="jobs-navbar">
      <view class="jobs-nav-side jobs-nav-left" hover-class="jobs-nav-hover" @click="goBack">‹</view>
      <view class="jobs-nav-center">
        <text class="jobs-title">{{ currentTab.title }}</text>
      </view>
      <view class="jobs-nav-placeholder"></view>
    </view>

    <view class="jobs-content">
      <JobsHome v-if="activeTab === 'home'" embedded />
      <JobsPublish v-else-if="activeTab === 'publish'" embedded />
      <JobsMessages v-else-if="activeTab === 'messages'" embedded />
      <JobsMine v-else embedded />
    </view>

    <view class="jobs-tabbar">
      <view
        v-for="item in tabs"
        :key="item.key"
        :class="['jobs-tab-item', activeTab === item.key ? 'active' : '']"
        @click="switchInnerTab(item.key)"
      >
        <text class="jobs-tab-label">{{ item.label }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { onLoad, onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import JobsHome from './home.vue'
import JobsPublish from './publish.vue'
import JobsMessages from './messages.vue'
import JobsMine from './mine.vue'

const tabs = [
  { key: 'home', label: '岗位首页', title: '校园就近勤工助学' },
  { key: 'publish', label: '发布需求', title: '发布需求' },
  { key: 'messages', label: '消息中心', title: '兼职消息中心' },
  { key: 'mine', label: '个人中心', title: '勤工助学个人中心' },
]

const activeTab = ref('home')
const currentTab = computed(() => tabs.find((item) => item.key === activeTab.value) || tabs[0])
const validTabKeys = new Set(tabs.map((item) => item.key))

const setActiveTab = (tab) => {
  if (typeof tab === 'string' && validTabKeys.has(tab)) {
    activeTab.value = tab
  }
}

const updateHash = (tab) => {
  if (typeof window === 'undefined' || !validTabKeys.has(tab)) {
    return
  }
  const nextHash = `#/pages/jobs/index?tab=${tab}`
  if (window.location.hash !== nextHash) {
    window.history.replaceState(null, '', nextHash)
  }
}

const resolveTabFromRoute = (options) => {
  if (typeof options?.tab === 'string' && validTabKeys.has(options.tab)) {
    return options.tab
  }
  if (typeof window !== 'undefined') {
    const hash = window.location.hash || ''
    const query = hash.includes('?') ? hash.split('?')[1] : ''
    const tab = new URLSearchParams(query).get('tab')
    if (tab && validTabKeys.has(tab)) {
      return tab
    }
  }
  const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : []
  const current = pages[pages.length - 1]
  const pageTab = current?.options?.tab
  if (typeof pageTab === 'string' && validTabKeys.has(pageTab)) {
    return pageTab
  }
  return ''
}

const syncActiveTab = (options) => {
  const nextTab = resolveTabFromRoute(options)
  if (nextTab) {
    setActiveTab(nextTab)
  }
}

const handleSwitchTab = (tab) => {
  if (typeof tab === 'string' && validTabKeys.has(tab)) {
    setActiveTab(tab)
    updateHash(tab)
  }
}

const switchInnerTab = (tab) => {
  setActiveTab(tab)
  updateHash(tab)
}

const goBack = () => {
  uni.switchTab({ url: '/pages/home/index' })
}

onLoad((options) => {
  syncActiveTab(options)
})

onShow(() => {
  syncActiveTab()
})

onMounted(() => {
  if (typeof window === 'undefined') {
    return
  }
  window.addEventListener('hashchange', syncActiveTab)
})

onPullDownRefresh(() => {
  if (activeTab.value !== 'home') {
    uni.stopPullDownRefresh()
    return
  }
  uni.$emit('jobs-home-refresh')
})

uni.$off('jobs-switch-tab', handleSwitchTab)
uni.$on('jobs-switch-tab', handleSwitchTab)

onBeforeUnmount(() => {
  uni.$off('jobs-switch-tab', handleSwitchTab)
  if (typeof window !== 'undefined') {
    window.removeEventListener('hashchange', syncActiveTab)
  }
})
</script>

<style scoped lang="scss">
.jobs-shell {
  min-height: 100vh;
  background: #f4f7fb;
  padding-bottom: calc(168rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.jobs-navbar {
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

.jobs-nav-placeholder {
  min-height: 72rpx;
}

.jobs-nav-side {
  min-height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18rpx;
  color: #1f2937;
}

.jobs-nav-left {
  font-size: 42rpx;
}

.jobs-nav-hover {
  background: #eef3fb;
}

.jobs-nav-center {
  text-align: center;
  min-width: 0;
}

.jobs-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.jobs-content {
  min-height: calc(100vh - 180rpx);
}

.jobs-tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  padding: 10rpx 0 calc(10rpx + env(safe-area-inset-bottom));
  background: #ffffff;
  border-top: 1px solid #d7dbe2;
}

.jobs-tab-item {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 88rpx;
  color: #5f6b7a;
}

.jobs-tab-item.active {
  color: #2d7ff9;
}

.jobs-tab-label {
  font-size: 24rpx;
  font-weight: 500;
}
</style>
