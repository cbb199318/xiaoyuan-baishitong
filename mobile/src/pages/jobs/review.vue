<template>
  <view class="page-shell jobs-review-page">
    <view v-if="isBusiness" class="card review-card">
      <text class="section-title">商家审核进度</text>
      <view class="review-block">
        <text class="block-title">资质审核</text>
        <text class="block-status">{{ qualificationText }}</text>
        <text class="block-meta">最近提交时间：{{ reviewProgress?.lastSubmittedAt || '暂未提交' }}</text>
        <text v-if="reviewProgress?.qualificationRejectReason" class="block-reject">
          驳回原因：{{ reviewProgress.qualificationRejectReason }}
        </text>
        <view v-if="reviewProgress?.qualificationStatus === 'REJECTED' && rejectedJobId" class="review-action-row">
          <view class="review-action-btn" @click="editRejectedJob">去修改重提</view>
        </view>
      </view>

      <view class="review-block">
        <text class="block-title">岗位审核</text>
        <view v-if="businessPosts.length" class="review-list">
          <view v-for="item in businessPosts" :key="item.id" class="review-item">
            <view class="review-item-head">
              <text class="review-item-title">{{ item.title }}</text>
              <text :class="['review-item-status', statusTone(item.status)]">{{ statusLabel(item.status, item.publicVisible) }}</text>
            </view>
            <text class="review-item-meta">{{ item.location }} · {{ item.timeText }}</text>
          </view>
        </view>
        <text v-else class="block-meta">你还没有提交过商家岗位。</text>
      </view>
    </view>

    <view v-else class="card empty-card">
      <text class="empty-title">当前不是商家身份</text>
      <text class="empty-desc">切换为认证商家身份后，这里会展示资质审核和岗位审核进度。</text>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onBackPress, onLoad, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { handleJobsMineBackPress } from '../../utils/jobsNavigation'
import { getJobsReviewProgress, listMyJobs } from '../../utils/jobsApi'

const store = useUserStore()
const reviewProgress = ref(null)
const businessPosts = ref([])
let pageOptions = {}

const isBusiness = computed(() =>
  String(store.profile?.publishRole || store.profile?.roleType || '').toUpperCase() === 'BUSINESS'
)

const qualificationText = computed(() => {
  const status = String(reviewProgress.value?.qualificationStatus || 'UNSUBMITTED').toUpperCase()
  if (status === 'PENDING_REVIEW') {
    return '审核中'
  }
  if (status === 'APPROVED') {
    return '已通过'
  }
  if (status === 'REJECTED') {
    return '已驳回'
  }
  return '未提交'
})

const rejectedJobId = computed(() => reviewProgress.value?.latestJobId || '')

const statusLabel = (status, publicVisible) => {
  if (status === 'PENDING_REVIEW') {
    return '审核中'
  }
  if (status === 'REJECTED') {
    return '已驳回'
  }
  if (publicVisible && (status === 'APPROVED' || status === 'ACTIVE')) {
    return '招聘中'
  }
  return '已通过'
}

const statusTone = (status) => {
  if (status === 'PENDING_REVIEW') {
    return 'amber'
  }
  if (status === 'REJECTED') {
    return 'red'
  }
  return 'green'
}

const loadPage = async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  const [progress, myPosts] = await Promise.all([getJobsReviewProgress(), listMyJobs()])
  reviewProgress.value = progress
  businessPosts.value = (myPosts || []).filter((item) => item.roleType === 'BUSINESS')
}

const editRejectedJob = () => {
  if (!rejectedJobId.value) {
    uni.showToast({ title: '暂无可修改岗位', icon: 'none' })
    return
  }
  uni.navigateTo({ url: `/pages/jobs/publish?editId=${rejectedJobId.value}` })
}

onLoad((options) => {
  pageOptions = options || {}
})

onShow(loadPage)

onBackPress(() => handleJobsMineBackPress(pageOptions))
</script>

<style scoped lang="scss">
.jobs-review-page {
  padding-top: 20rpx;
}

.review-card,
.empty-card {
  padding: 28rpx 24rpx;
}

.section-title,
.block-title,
.empty-title {
  display: block;
  color: #0f172a;
}

.section-title {
  font-size: 30rpx;
  font-weight: 800;
}

.review-block {
  margin-top: 18rpx;
  padding: 22rpx;
  border-radius: 22rpx;
  background: #f8fafc;
}

.block-title {
  font-size: 26rpx;
  font-weight: 700;
}

.block-status,
.block-meta,
.block-reject,
.review-item-meta,
.empty-desc {
  display: block;
  margin-top: 10rpx;
  font-size: 23rpx;
  line-height: 1.7;
  color: #64748b;
}

.block-status {
  color: #1677ff;
  font-weight: 700;
}

.block-reject {
  color: #dc2626;
}

.review-action-row {
  display: flex;
  margin-top: 16rpx;
}

.review-action-btn {
  min-height: 68rpx;
  padding: 0 22rpx;
  border-radius: 18rpx;
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 700;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  margin-top: 16rpx;
}

.review-item {
  padding: 18rpx;
  border-radius: 18rpx;
  background: #ffffff;
}

.review-item-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.review-item-title {
  font-size: 25rpx;
  font-weight: 700;
  color: #0f172a;
}

.review-item-status {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  font-size: 21rpx;
  font-weight: 700;
}

.review-item-status.green {
  background: #dcfce7;
  color: #15803d;
}

.review-item-status.amber {
  background: #fef3c7;
  color: #d97706;
}

.review-item-status.red {
  background: #fee2e2;
  color: #dc2626;
}

.empty-card {
  text-align: center;
}
</style>
