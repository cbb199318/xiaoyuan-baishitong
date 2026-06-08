<template>
  <view :class="[embedded ? 'embedded-page' : 'page-shell']">
    <view class="hero-card">
      <view class="hero-head">
        <view class="avatar-wrap">
          <image v-if="profile?.avatarUrl" class="avatar-image" :src="profile.avatarUrl" mode="aspectFill"></image>
          <view v-else class="avatar-fallback">{{ avatarText }}</view>
        </view>
        <view class="hero-main">
          <view class="name-row">
            <text class="hero-name">{{ profile?.nickname || '勤工同学' }}</text>
            <text :class="['identity-tag', isBusiness ? 'business' : 'student']">
              {{ isBusiness ? '认证商家' : '普通学生' }}
            </text>
          </view>
          <text class="hero-meta">实名认证：{{ verifyStatusText }}</text>
          <text class="hero-meta">手机号：{{ profile?.phone || profile?.contactPhone || '-' }}</text>
        </view>
      </view>

      <view class="hero-stats">
        <view class="stat-item">
          <text class="stat-value">{{ publishCount }}</text>
          <text class="stat-label">我的发布</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ applicationCount }}</text>
          <text class="stat-label">我的报名</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ favoriteCount }}</text>
          <text class="stat-label">我的收藏</text>
        </view>
      </view>
    </view>

    <view class="grid-card">
      <view class="grid-list">
        <view
          v-for="item in gridItems"
          :key="item.key"
          class="grid-item"
          @click="openGrid(item)"
        >
          <view :class="['grid-icon', item.tone]">{{ item.short }}</view>
          <text class="grid-title">{{ item.title }}</text>
        </view>
      </view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">快捷操作</text>
      </view>
      <view class="quick-list">
        <view class="quick-item" @click="goPublish">去发布新需求</view>
        <view class="quick-item" @click="go('/pages/profile/edit?from=jobs-mine')">编辑通用资料</view>
        <view class="quick-item" @click="go('/pages/report/list?from=jobs-mine')">查看举报记录</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import request from '../../utils/request'
import { useUserStore } from '../../stores/user'
import { getJobsReviewProgress, listFavoriteJobs, listMyJobApplications, listMyJobs } from '../../utils/jobsApi'

const props = defineProps({
  embedded: {
    type: Boolean,
    default: false,
  },
})

const store = useUserStore()
const verification = ref(null)
const publishCount = ref(0)
const applicationCount = ref(0)
const favoriteCount = ref(0)

const profile = computed(() => store.profile || null)
const isBusiness = computed(() =>
  String(profile.value?.publishRole || profile.value?.roleType || '').toUpperCase() === 'BUSINESS'
)
const avatarText = computed(() => (profile.value?.nickname || '勤').slice(0, 1))
const verifyStatusText = computed(() => {
  const status = String(verification.value?.status || profile.value?.realNameStatus || 'UNSUBMITTED').toUpperCase()
  if (status === 'APPROVED') {
    return '已认证'
  }
  if (status === 'PENDING') {
    return '审核中'
  }
  if (status === 'REJECTED') {
    return '已驳回'
  }
  return '未认证'
})

const gridItems = computed(() => [
  { key: 'posts', title: '我的发布', short: '发', tone: 'blue' },
  { key: 'applications', title: '我的报名', short: '报', tone: 'green' },
  { key: 'favorites', title: '我的收藏', short: '藏', tone: 'amber' },
  { key: 'resume', title: '我的简历', short: '简', tone: 'violet' },
  { key: 'review', title: '审核进度', short: '审', tone: 'cyan' },
  { key: 'messages', title: '消息通知', short: '消', tone: 'rose' },
  { key: 'help', title: '帮助中心', short: '助', tone: 'slate' },
  { key: 'settings', title: '系统设置', short: '设', tone: 'indigo' },
])

const openGrid = (item) => {
  switch (item.key) {
    case 'posts':
      go('/pages/jobs/my-posts?from=jobs-mine')
      return
    case 'applications':
      go('/pages/jobs/my-applications?from=jobs-mine')
      return
    case 'favorites':
      go('/pages/jobs/favorites?from=jobs-mine')
      return
    case 'resume':
      go('/pages/jobs/resume?from=jobs-mine')
      return
    case 'review':
      if (!isBusiness.value) {
        uni.showToast({ title: '仅认证商家可查看审核进度', icon: 'none' })
        return
      }
      go('/pages/jobs/review?from=jobs-mine')
      return
    case 'messages':
      if (props.embedded) {
        uni.$emit('jobs-switch-tab', 'messages')
      } else {
        uni.redirectTo({ url: '/pages/jobs/index?tab=messages' })
      }
      return
    case 'help':
      go('/pages/jobs/help?from=jobs-mine')
      return
    case 'settings':
      go('/pages/profile/edit?from=jobs-mine')
      return
    default:
      break
  }
}

const go = (url) => {
  uni.navigateTo({ url })
}

const goPublish = () => {
  if (props.embedded) {
    uni.$emit('jobs-switch-tab', 'publish')
    return
  }
  uni.redirectTo({ url: '/pages/jobs/index?tab=publish' })
}

const loadPage = async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  try {
    verification.value = await request.get('/verification/current')
  } catch (error) {
    verification.value = null
  }
  const [myJobs, myApplications, favorites] = await Promise.all([
    listMyJobs(),
    listMyJobApplications(),
    listFavoriteJobs(),
  ])
  publishCount.value = (myJobs || []).length
  applicationCount.value = (myApplications || []).length
  favoriteCount.value = (favorites || []).length
  if (isBusiness.value) {
    await getJobsReviewProgress()
  }
}

onShow(loadPage)
</script>

<style scoped lang="scss">
.embedded-page {
  min-height: 100%;
  padding: 20rpx 24rpx 24rpx;
  box-sizing: border-box;
}

.hero-card,
.grid-card,
.section-card {
  margin-bottom: 20rpx;
  padding: 24rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.06);
}

.hero-head {
  display: flex;
  gap: 18rpx;
  align-items: center;
}

.avatar-wrap {
  flex-shrink: 0;
}

.avatar-image,
.avatar-fallback {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
}

.avatar-fallback {
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  font-weight: 800;
}

.hero-main {
  flex: 1;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
}

.hero-name,
.section-title,
.grid-title,
.stat-value {
  color: #0f172a;
}

.hero-name {
  font-size: 30rpx;
  font-weight: 800;
}

.identity-tag {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 21rpx;
  font-weight: 700;
}

.identity-tag.student {
  background: #dbeafe;
  color: #1d4ed8;
}

.identity-tag.business {
  background: #dcfce7;
  color: #15803d;
}

.hero-meta,
.stat-label {
  display: block;
  margin-top: 10rpx;
  font-size: 23rpx;
  color: #64748b;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14rpx;
  margin-top: 22rpx;
}

.stat-item {
  padding: 18rpx 12rpx;
  border-radius: 22rpx;
  background: #f8fafc;
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
}

.grid-list {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18rpx 12rpx;
}

.grid-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.grid-icon {
  width: 84rpx;
  height: 84rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 800;
}

.grid-icon.blue {
  background: #dbeafe;
  color: #1d4ed8;
}

.grid-icon.green {
  background: #dcfce7;
  color: #15803d;
}

.grid-icon.amber {
  background: #fef3c7;
  color: #d97706;
}

.grid-icon.violet {
  background: #ede9fe;
  color: #6d28d9;
}

.grid-icon.cyan {
  background: #cffafe;
  color: #0f766e;
}

.grid-icon.rose {
  background: #ffe4e6;
  color: #be123c;
}

.grid-icon.slate {
  background: #e2e8f0;
  color: #334155;
}

.grid-icon.indigo {
  background: #e0e7ff;
  color: #4338ca;
}

.grid-title,
.section-title {
  font-size: 26rpx;
  font-weight: 700;
}

.quick-list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  margin-top: 14rpx;
}

.quick-item {
  padding: 22rpx 20rpx;
  border-radius: 22rpx;
  background: #f8fafc;
  color: #334155;
  font-size: 24rpx;
}
</style>
