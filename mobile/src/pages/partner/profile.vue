<template>
  <view class="partner-profile-page">
    <view class="partner-navbar">
      <view class="partner-nav-side partner-nav-left" hover-class="partner-nav-hover" @click="goBack">‹</view>
      <view class="partner-nav-center">
        <text class="partner-nav-title">对方主页</text>
      </view>
      <view class="partner-nav-side partner-nav-right" hover-class="partner-nav-hover" @click="openReport">举报</view>
    </view>

    <scroll-view scroll-y class="partner-profile-scroll">
      <view class="partner-profile-content">
        <view v-if="userProfile" class="card partner-profile-card">
          <view class="profile-head">
            <view class="profile-avatar">{{ (userProfile.nickname || '搭').slice(0, 1) }}</view>
            <view class="profile-main">
              <text class="profile-name">{{ userProfile.nickname }}</text>
              <text class="profile-role">校园搭子用户</text>
              <text class="profile-meta">手机号：{{ maskedPhone }}</text>
            </view>
          </view>

          <view v-if="userProfile.tags?.length" class="profile-tag-group">
            <text v-for="tag in userProfile.tags" :key="tag" class="profile-tag">{{ tag }}</text>
          </view>

          <view class="profile-info-card">
            <view class="info-item">
              <text class="info-label">发布需求</text>
              <text class="info-value">{{ userProfile.demandCount }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">展示中</text>
              <text class="info-value">{{ userProfile.activeDemandCount }}</text>
            </view>
          </view>

          <view class="profile-bio-card">
            <text class="section-title">个人简介</text>
            <text class="profile-bio">{{ userProfile.bio || '这个同学暂时还没有留下更多自我介绍。' }}</text>
          </view>

          <view class="profile-action-row">
            <view class="profile-action ghost" @click="viewDemands">查看Ta的需求</view>
            <view class="profile-action" @click="openReport">发起举报</view>
          </view>
        </view>

        <view v-else class="card empty-card">
          <text class="empty-title">未找到该用户资料</text>
          <text class="empty-desc">该用户可能暂时不可查看，稍后再试。</text>
        </view>
      </view>
    </scroll-view>

    <PartnerReportModal
      v-model:visible="showReportModal"
      module="partner"
      target-type="user"
      :target-id="targetUserId"
    />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import PartnerReportModal from '../../components/PartnerReportModal.vue'
import { getPartnerUserProfile } from '../../utils/partnerApi'

const targetUserId = ref('')
const userProfile = ref(null)
const showReportModal = ref(false)

const maskedPhone = computed(() => {
  const phone = String(userProfile.value?.phone || '')
  if (phone.length < 7) {
    return phone || '-'
  }
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`
})

const loadProfile = async () => {
  if (!targetUserId.value) {
    userProfile.value = null
    return
  }
  try {
    userProfile.value = await getPartnerUserProfile(targetUserId.value)
  } catch (error) {
    userProfile.value = null
  }
}

const openReport = () => {
  showReportModal.value = true
}

const viewDemands = () => {
  uni.navigateTo({ url: `/pages/partner/index?publisherId=${targetUserId.value}` })
}

const goBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.redirectTo({ url: '/pages/partner/index' })
    },
  })
}

onLoad((options) => {
  targetUserId.value = options?.userId || ''
})

onShow(async () => {
  await loadProfile()
})
</script>

<style lang="scss">
.partner-profile-page {
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

.partner-nav-right {
  font-size: 24rpx;
  color: #ef4444;
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

.partner-nav-hover {
  background: #f1f5f9;
}

.partner-profile-scroll {
  height: calc(100vh - 112rpx - env(safe-area-inset-top));
}

.partner-profile-content {
  padding: 24rpx;
  box-sizing: border-box;
}

.partner-profile-card,
.empty-card {
  padding: 26rpx;
}

.profile-head {
  display: flex;
  gap: 18rpx;
}

.profile-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  font-weight: 800;
  flex-shrink: 0;
}

.profile-main text,
.info-item text {
  display: block;
}

.profile-name {
  font-size: 32rpx;
  font-weight: 800;
  color: #0f172a;
}

.profile-role,
.profile-meta,
.profile-bio,
.empty-desc {
  margin-top: 8rpx;
  font-size: 23rpx;
  line-height: 1.7;
  color: #64748b;
}

.profile-tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 22rpx;
}

.profile-tag {
  padding: 12rpx 18rpx;
  border-radius: 999rpx;
  background: #dbeafe;
  color: #1d4ed8;
  font-size: 22rpx;
  font-weight: 600;
}

.profile-info-card,
.profile-bio-card {
  margin-top: 22rpx;
  padding: 22rpx;
  border-radius: 24rpx;
  background: #f8fafc;
}

.profile-info-card {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14rpx;
}

.info-item {
  padding: 12rpx 8rpx;
}

.info-label {
  font-size: 22rpx;
  color: #64748b;
}

.info-value {
  margin-top: 8rpx;
  font-size: 32rpx;
  font-weight: 800;
  color: #0f172a;
}

.section-title,
.empty-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
}

.profile-action-row {
  display: flex;
  gap: 14rpx;
  margin-top: 24rpx;
}

.profile-action {
  flex: 1;
  min-height: 84rpx;
  border-radius: 22rpx;
  background: linear-gradient(135deg, #ef4444, #fb7185);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 700;
}

.profile-action.ghost {
  background: #e2e8f0;
  color: #334155;
}
</style>
