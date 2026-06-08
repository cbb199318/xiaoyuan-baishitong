<template>
  <view class="partner-report-page">
    <view class="partner-navbar">
      <view class="partner-nav-side partner-nav-left" hover-class="partner-nav-hover" @click="goBack">‹</view>
      <view class="partner-nav-center">
        <text class="partner-nav-title">搭子举报记录</text>
      </view>
      <view class="partner-nav-side partner-nav-placeholder"></view>
    </view>

    <view class="partner-report-content">
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

      <view v-if="filteredList.length" class="report-list">
        <view v-for="item in filteredList" :key="item.id" class="card report-card">
          <view class="report-head">
            <text class="report-type">{{ item.reportType }}</text>
            <text :class="['report-status', `status-${String(item.status).toLowerCase()}`]">{{ statusLabelMap[item.status] || item.status }}</text>
          </view>
          <text class="report-meta">举报对象：{{ item.targetType }} #{{ item.targetId || '-' }}</text>
          <text class="report-desc">{{ item.description }}</text>
          <text v-if="item.handleRemark" class="report-result">处理结果：{{ item.handleRemark }}</text>
          <view v-if="item.attachments?.length" class="report-attachment-row">
            <image
              v-for="attachment in item.attachments"
              :key="attachment.fileId"
              class="report-attachment"
              :src="attachment.url"
              mode="aspectFill"
              @click="previewAttachment(item.attachments, attachment.url)"
            ></image>
          </view>
        </view>
      </view>

      <view v-else class="card empty-card">
        <text class="empty-title">暂无搭子举报记录</text>
        <text class="empty-desc">你提交的搭子工单、处理进度和官方反馈会展示在这里。</text>
      </view>
    </view>
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
  { label: '待审核', value: 'PENDING' },
  { label: '审核中', value: 'PROCESSING' },
  { label: '已处理', value: 'RESOLVED' },
  { label: '已驳回', value: 'REJECTED' },
]

const statusLabelMap = {
  PENDING: '待审核',
  PROCESSING: '审核中',
  RESOLVED: '已处理',
  REJECTED: '已驳回',
}

const filteredList = computed(() => {
  const partnerOnly = list.value.filter((item) => item.module === 'partner')
  return activeStatus.value === 'ALL' ? partnerOnly : partnerOnly.filter((item) => item.status === activeStatus.value)
})

const loadList = async () => {
  list.value = await request.get('/reports')
}

const previewAttachment = (attachments, current) => {
  uni.previewImage({
    current,
    urls: attachments.map((item) => item.url),
  })
}

const goBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.redirectTo({ url: '/pages/partner/mine' })
    },
  })
}

onShow(loadList)

watch(
  () => store.reportStatusTick,
  () => {
    loadList()
  }
)
</script>

<style lang="scss">
.partner-report-page {
  min-height: 100vh;
  background: #f4f7fb;
}

.partner-navbar {
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

.partner-nav-side {
  min-height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1f2937;
  font-size: 28rpx;
  border-radius: 18rpx;
}

.partner-nav-left {
  font-size: 42rpx;
}

.partner-nav-center {
  text-align: center;
}

.partner-nav-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
}

.partner-nav-placeholder {
  pointer-events: none;
}

.partner-nav-hover {
  background: #f1f5f9;
}

.partner-report-content {
  padding: 24rpx;
  box-sizing: border-box;
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
  color: #ffffff;
}

.report-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.report-card,
.empty-card {
  padding: 24rpx;
}

.report-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
}

.report-type,
.empty-title {
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
}

.report-status {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 700;
}

.status-pending {
  background: #dbeafe;
  color: #1d4ed8;
}

.status-processing {
  background: #fef3c7;
  color: #d97706;
}

.status-resolved {
  background: #dcfce7;
  color: #15803d;
}

.status-rejected {
  background: #fee2e2;
  color: #dc2626;
}

.report-meta,
.report-desc,
.report-result,
.empty-desc {
  display: block;
  margin-top: 10rpx;
  font-size: 23rpx;
  line-height: 1.7;
  color: #64748b;
}

.report-attachment-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
}

.report-attachment {
  width: 168rpx;
  height: 168rpx;
  border-radius: 18rpx;
  background: #e2e8f0;
}
</style>
