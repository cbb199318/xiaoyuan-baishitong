<template>
  <view class="page-shell">
    <view class="section-title">{{ pageTitle }}</view>
    <view v-if="moduleLabel" class="module-banner">当前仅查看：{{ moduleLabel }}</view>
    <view class="status-tabs">
      <view
        v-for="item in tabs"
        :key="item.value"
        :class="['status-tab', activeStatus === item.value ? 'active' : '']"
        @click="activeStatus = item.value"
      >
        {{ item.label }}
      </view>
    </view>
    <view v-if="filteredList.length" class="card report-list">
      <view v-for="item in filteredList" :key="item.id" class="report-item">
        <view class="report-head">
          <text class="report-type">{{ item.reportType }}</text>
          <text class="report-status">{{ formatStatus(item.status) }}</text>
        </view>
        <text class="report-module">{{ formatModule(item.module) }} / {{ item.targetType || '内容' }}</text>
        <text class="report-desc">{{ item.description }}</text>
        <text v-if="item.contactPhone" class="report-meta">联系方式：{{ item.contactPhone }}</text>
        <text v-if="item.attachments?.length" class="report-meta">举证附件：{{ item.attachments.length }} 张</text>
        <text v-if="item.handleRemark" class="report-meta">处理备注：{{ item.handleRemark }}</text>
        <text class="report-meta">提交时间：{{ formatTime(item.createdAt) }}</text>
        <text v-if="item.handledAt" class="report-meta">处理时间：{{ formatTime(item.handledAt) }}</text>
      </view>
    </view>
    <view v-else class="card empty">{{ moduleLabel ? `当前还没有${moduleLabel}举报记录。` : '还没有举报记录。' }}</view>
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { onBackPress, onLoad, onShow } from '@dcloudio/uni-app'
import request from '../../utils/request'
import { useUserStore } from '../../stores/user'
import { handleJobsMineBackPress } from '../../utils/jobsNavigation'

const store = useUserStore()
const list = ref([])
const activeStatus = ref('ALL')
const currentModule = ref('')
const pageTitle = ref('举报记录')
let pageOptions = {}
const tabs = [
  { label: '全部', value: 'ALL' },
  { label: '待处理', value: 'PENDING' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '已处理', value: 'RESOLVED' },
  { label: '已驳回', value: 'REJECTED' },
]

const moduleLabelMap = {
  beauty: '美妆',
  errand: '跑腿',
  partner: '搭子',
  jobs: '勤工助学',
}

const moduleLabel = computed(() => moduleLabelMap[currentModule.value] || '')

const filteredList = computed(() =>
  list.value
    .filter((item) => !currentModule.value || item.module === currentModule.value)
    .filter((item) => activeStatus.value === 'ALL' ? true : item.status === activeStatus.value)
)

const loadList = async () => {
  list.value = await request.get('/reports')
}

onLoad((options) => {
  pageOptions = options || {}
  currentModule.value = options?.module || ''
  pageTitle.value = options?.title || '举报记录'
})

onShow(loadList)

onBackPress(() => handleJobsMineBackPress(pageOptions))

const formatModule = (value) => moduleLabelMap[value] || value || '通用'

const formatStatus = (value) => {
  const map = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    RESOLVED: '已处理',
    REJECTED: '已驳回',
  }
  return map[String(value || '').toUpperCase()] || value || '待处理'
}

const formatTime = (value) => {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ')
}

watch(
  () => store.reportStatusTick,
  () => {
    loadList()
  }
)
</script>

<style scoped lang="scss">
.module-banner {
  margin-bottom: 18rpx;
  padding: 18rpx 22rpx;
  border-radius: 22rpx;
  background: #eef4ff;
  color: #2d5baf;
  font-size: 23rpx;
}

.status-tabs {
  display: flex;
  gap: 12rpx;
  overflow-x: auto;
  margin-bottom: 20rpx;
}

.status-tab {
  padding: 12rpx 22rpx;
  border-radius: 999rpx;
  background: #e2e8f0;
  color: #475569;
  white-space: nowrap;
  font-size: 24rpx;
}

.status-tab.active {
  background: #1677ff;
  color: #fff;
}

.report-list {
  overflow: hidden;
}

.report-item {
  padding: 26rpx 24rpx;
  border-bottom: 2rpx solid #eef2f7;
}

.report-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.report-item:last-child {
  border-bottom: 0;
}

.report-type {
  display: block;
  font-weight: 700;
  margin-bottom: 8rpx;
}

.report-status,
.report-module {
  display: block;
  color: #2d7ff9;
  font-size: 22rpx;
}

.report-module {
  margin-top: 8rpx;
}

.report-desc,
.report-meta,
.empty {
  display: block;
  color: #64748b;
}

.report-meta {
  margin-top: 10rpx;
}

.empty {
  padding: 30rpx;
}
</style>
