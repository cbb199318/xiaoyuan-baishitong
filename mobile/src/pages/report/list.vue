<template>
  <view class="page-shell">
    <view class="section-title">举报记录</view>
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
        <text class="report-type">{{ item.reportType }}</text>
        <text class="report-desc">{{ item.description }}</text>
        <text class="report-meta">状态：{{ item.status }}</text>
        <text v-if="item.handleRemark" class="report-meta">处理备注：{{ item.handleRemark }}</text>
      </view>
    </view>
    <view v-else class="card empty">还没有举报记录。</view>
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import request from '../../utils/request'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const list = ref([])
const activeStatus = ref('ALL')
const tabs = [
  { label: '全部', value: 'ALL' },
  { label: '待处理', value: 'PENDING' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '已处理', value: 'RESOLVED' },
  { label: '已驳回', value: 'REJECTED' },
]

const filteredList = computed(() =>
  activeStatus.value === 'ALL' ? list.value : list.value.filter((item) => item.status === activeStatus.value)
)

const loadList = async () => {
  list.value = await request.get('/reports')
}

onShow(loadList)

watch(
  () => store.reportStatusTick,
  () => {
    loadList()
  }
)
</script>

<style scoped lang="scss">
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

.report-item:last-child {
  border-bottom: 0;
}

.report-type {
  display: block;
  font-weight: 700;
  margin-bottom: 8rpx;
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
