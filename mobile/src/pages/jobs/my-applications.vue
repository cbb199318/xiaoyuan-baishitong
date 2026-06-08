<template>
  <view class="page-shell jobs-application-page">
    <view class="filter-row">
      <view
        v-for="item in tabs"
        :key="item.value"
        :class="['filter-chip', activeTab === item.value ? 'active' : '']"
        @click="activeTab = item.value"
      >
        {{ item.label }}
      </view>
    </view>

    <view v-if="filteredList.length" class="card-list">
      <view v-for="item in filteredList" :key="item.id" class="card application-card">
        <view class="card-head">
          <text class="card-title">{{ item.title }}</text>
          <text :class="['status-tag', statusMeta(item).tone]">{{ statusMeta(item).label }}</text>
        </view>
        <text class="card-meta">发布方：{{ item.publisherName || '岗位发布者' }}</text>
        <text class="card-meta">地点：{{ item.location || '-' }}</text>
        <text class="card-meta">时间：{{ item.timeText || '-' }}</text>
        <text class="card-meta">酬劳：{{ item.salaryText || '-' }}</text>
        <view class="action-row">
          <view class="mini-btn ghost" @click="openDetail(item.jobId)">查看详情</view>
          <view class="mini-btn" @click="openChat(item)">私聊对方</view>
        </view>
      </view>
    </view>

    <view v-else class="card empty-card">
      <text class="empty-title">暂无报名记录</text>
      <text class="empty-desc">你报名过的岗位会按状态展示在这里，支持查看详情和私聊对方。</text>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onBackPress, onLoad, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { handleJobsMineBackPress } from '../../utils/jobsNavigation'
import { listMyJobApplications } from '../../utils/jobsApi'

const store = useUserStore()
const list = ref([])
const activeTab = ref('PENDING')
let pageOptions = {}

const tabs = [
  { label: '待审核', value: 'PENDING' },
  { label: '已录用', value: 'ADMITTED' },
  { label: '未录用', value: 'REJECTED' },
  { label: '已过期', value: 'EXPIRED' },
]

const normalizeStatus = (item) => {
  if (item.status === 'ADMITTED') {
    return 'ADMITTED'
  }
  if (item.status === 'REJECTED') {
    return 'REJECTED'
  }
  if (item.status === 'EXPIRED') {
    return 'EXPIRED'
  }
  return 'PENDING'
}

const statusMeta = (item) => {
  const status = normalizeStatus(item)
  if (status === 'ADMITTED') {
    return { label: '已录用', tone: 'green' }
  }
  if (status === 'REJECTED') {
    return { label: '未录用', tone: 'red' }
  }
  if (status === 'EXPIRED') {
    return { label: '已过期', tone: 'slate' }
  }
  return { label: '待审核', tone: 'amber' }
}

const filteredList = computed(() => list.value.filter((item) => normalizeStatus(item) === activeTab.value))

const loadList = async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  list.value = await listMyJobApplications()
}

const openDetail = (jobId) => {
  uni.navigateTo({ url: `/pages/jobs/detail?id=${jobId}` })
}

const openChat = (item) => {
  if (!item.conversationId) {
    uni.showToast({ title: '当前还没有可用会话', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: `/pages/jobs/chat?id=${item.conversationId}&title=${encodeURIComponent(item.publisherName || '兼职沟通')}&jobId=${item.jobId || ''}&phone=${item.publisherPhone || ''}`,
  })
}

onLoad((options) => {
  pageOptions = options || {}
})

onShow(loadList)

onBackPress(() => handleJobsMineBackPress(pageOptions))
</script>

<style scoped lang="scss">
.jobs-application-page {
  padding-top: 20rpx;
}

.filter-row {
  display: flex;
  gap: 14rpx;
  overflow-x: auto;
  margin-bottom: 20rpx;
}

.filter-chip {
  flex-shrink: 0;
  min-height: 68rpx;
  padding: 0 24rpx;
  border-radius: 999rpx;
  background: #e2e8f0;
  color: #475569;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
}

.filter-chip.active {
  background: #1677ff;
  color: #ffffff;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.application-card {
  padding: 24rpx;
}

.card-head,
.action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
  flex-wrap: wrap;
}

.card-title,
.empty-title {
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
}

.card-meta,
.empty-desc {
  display: block;
  margin-top: 10rpx;
  color: #64748b;
  font-size: 23rpx;
  line-height: 1.7;
}

.action-row {
  justify-content: flex-start;
  margin-top: 18rpx;
}

.mini-btn,
.status-tag {
  border-radius: 18rpx;
  font-size: 22rpx;
}

.mini-btn {
  min-height: 68rpx;
  padding: 0 18rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #1677ff;
  color: #ffffff;
}

.mini-btn.ghost {
  background: #e2e8f0;
  color: #334155;
}

.status-tag {
  padding: 8rpx 14rpx;
  font-weight: 700;
}

.status-tag.green {
  background: #dcfce7;
  color: #15803d;
}

.status-tag.red {
  background: #fee2e2;
  color: #dc2626;
}

.status-tag.amber {
  background: #fef3c7;
  color: #d97706;
}

.status-tag.slate {
  background: #e2e8f0;
  color: #475569;
}

.empty-card {
  padding: 44rpx 28rpx;
  text-align: center;
}
</style>
