<template>
  <view class="partner-detail-page">
    <view class="partner-navbar">
      <view class="partner-nav-side partner-nav-left" hover-class="partner-nav-hover" @click="goBack">‹</view>
      <view class="partner-nav-center">
        <text class="partner-nav-title">搭子详情</text>
      </view>
      <view class="partner-nav-side partner-nav-right" hover-class="partner-nav-hover" @click="openReport">
        举报
      </view>
    </view>

    <scroll-view scroll-y class="partner-detail-scroll">
      <view class="partner-detail-content">
        <view v-if="detail" class="card detail-card">
          <view class="detail-head">
            <view>
              <text :class="['detail-type-tag', `type-${String(detail.type).toLowerCase()}`]">
                {{ categoryMap[detail.type] || detail.type }}
              </text>
              <text class="detail-title">{{ detail.location }}</text>
            </view>
            <text class="detail-status">{{ detail.status === 'OFFLINE' ? '已下架' : '可申请' }}</text>
          </view>

          <text class="detail-time">需求时间：{{ detail.timeText }}</text>
          <text class="detail-meta">剩余名额：{{ detail.remainingSlots }} / {{ detail.totalSlots }}</text>
          <text class="detail-desc">{{ detail.description }}</text>

          <view v-if="detail.needTags?.length" class="detail-tag-group">
            <text v-for="tag in detail.needTags" :key="tag" class="detail-tag detail-tag--need">{{ tag }}</text>
          </view>
          <view v-if="publisherTags.length" class="detail-tag-group">
            <text v-for="tag in publisherTags" :key="tag" class="detail-tag detail-tag--user">{{ tag }}</text>
          </view>

          <view class="publisher-card">
            <view class="publisher-avatar">{{ (detail.nickname || '搭').slice(0, 1) }}</view>
            <view class="publisher-main">
              <text class="publisher-name">{{ detail.nickname }}</text>
              <text class="publisher-label">发布者资料标签</text>
            </view>
            <view class="publisher-link" @click="openPublisherProfile">主页</view>
          </view>

          <view class="detail-action-row">
            <view v-if="canChat" class="detail-btn detail-btn--ghost" @click="openChat">进入聊天</view>
            <view
              v-if="!isOwnDemand"
              :class="['detail-btn', !canApply ? 'disabled' : '']"
              @click="applyDemand"
            >
              {{ applyButtonText }}
            </view>
            <view v-else class="detail-btn detail-btn--ghost" @click="goMine">去个人中心管理</view>
          </view>
        </view>

        <view v-else class="card empty-card">
          <text class="empty-title">未找到该搭子需求</text>
          <text class="empty-desc">这条需求可能已被下架或暂时不可查看。</text>
        </view>
      </view>
    </scroll-view>

    <PartnerReportModal
      v-model:visible="showReportModal"
      module="partner"
      target-type="demand"
      :target-id="detail?.id || demandId"
    />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import PartnerReportModal from '../../components/PartnerReportModal.vue'
import { useUserStore } from '../../stores/user'
import { ensurePartnerConversation, findPartnerConversationByDemandAndUser } from '../../utils/partnerChat'
import { getPartnerProfileTags, partnerCategoryMap } from '../../utils/partnerMock'
import { getPartnerDemandDetail } from '../../utils/partnerApi'

const store = useUserStore()
const demandId = ref('')
const detail = ref(null)
const existingConversation = ref(null)
const showReportModal = ref(false)

const categoryMap = partnerCategoryMap

const currentUserId = computed(() => Number(store.profile?.userId || 0))
const isOwnDemand = computed(() => Number(detail.value?.publisherId || 0) === currentUserId.value)
const canChat = computed(() => Boolean(existingConversation.value?.id))
const canApply = computed(() => Boolean(detail.value) && !isOwnDemand.value && detail.value.status !== 'OFFLINE' && !existingConversation.value)
const applyButtonText = computed(() => {
  if (!detail.value) {
    return '申请搭子'
  }
  if (detail.value.status === 'OFFLINE') {
    return '需求已下架'
  }
  if (existingConversation.value) {
    return '已申请待回复'
  }
  return '申请搭子'
})
const publisherTags = computed(() => getPartnerProfileTags(detail.value?.publisherId).slice(0, 4))

const loadConversation = async () => {
  if (!demandId.value || !currentUserId.value) {
    existingConversation.value = null
    return
  }
  try {
    existingConversation.value = await findPartnerConversationByDemandAndUser(demandId.value, currentUserId.value)
  } catch (error) {
    existingConversation.value = null
  }
}

const loadDetail = async () => {
  if (!demandId.value) {
    detail.value = null
    return
  }
  try {
    detail.value = await getPartnerDemandDetail(demandId.value)
  } catch (error) {
    detail.value = null
  }
}

const applyDemand = async () => {
  if (!detail.value) {
    return
  }
  if (isOwnDemand.value) {
    uni.showToast({ title: '自己发布的需求无需申请', icon: 'none' })
    return
  }
  if (!canApply.value) {
    if (existingConversation.value) {
      openChat()
      return
    }
    uni.showToast({ title: '当前不可申请', icon: 'none' })
    return
  }
  try {
    const conversation = await ensurePartnerConversation({ demand: detail.value, currentUser: store.profile })
    existingConversation.value = conversation
    uni.showToast({ title: '已提交搭子申请', icon: 'success' })
    setTimeout(() => {
      uni.navigateTo({ url: `/pages/partner/chat?id=${conversation.id}` })
    }, 220)
  } catch (error) {
  }
}

const openChat = () => {
  if (!existingConversation.value) {
    uni.showToast({ title: '当前还没有会话', icon: 'none' })
    return
  }
  uni.navigateTo({ url: `/pages/partner/chat?id=${existingConversation.value.id}` })
}

const openReport = () => {
  showReportModal.value = true
}

const goMine = () => {
  uni.navigateTo({ url: '/pages/partner/mine' })
}

const openPublisherProfile = () => {
  if (!detail.value?.publisherId) {
    return
  }
  uni.navigateTo({ url: `/pages/partner/profile?userId=${detail.value.publisherId}` })
}

const goBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.redirectTo({ url: '/pages/partner/index' })
    },
  })
}

onLoad((options) => {
  demandId.value = options?.id || ''
})

onShow(async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  await loadDetail()
  await loadConversation()
})
</script>

<style lang="scss">
.partner-detail-page {
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

.partner-detail-scroll {
  height: calc(100vh - 112rpx - env(safe-area-inset-top));
}

.partner-detail-content {
  padding: 24rpx;
  box-sizing: border-box;
}

.detail-card,
.empty-card {
  padding: 24rpx;
}

.detail-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
}

.detail-type-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 700;
  color: #ffffff;
}

.detail-type-tag.type-study {
  background: linear-gradient(135deg, #2563eb, #38bdf8);
}

.detail-type-tag.type-meal {
  background: linear-gradient(135deg, #f97316, #fb7185);
}

.detail-type-tag.type-sport {
  background: linear-gradient(135deg, #10b981, #22c55e);
}

.detail-type-tag.type-travel {
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
}

.detail-type-tag.type-explore {
  background: linear-gradient(135deg, #ec4899, #f43f5e);
}

.detail-title {
  display: block;
  margin-top: 14rpx;
  font-size: 32rpx;
  font-weight: 700;
  color: #0f172a;
}

.detail-status,
.detail-time,
.detail-meta,
.detail-desc,
.publisher-label,
.empty-desc {
  display: block;
  font-size: 23rpx;
  line-height: 1.7;
  color: #64748b;
}

.detail-time,
.detail-meta,
.detail-desc {
  margin-top: 14rpx;
}

.detail-tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 16rpx;
}

.detail-tag {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  font-size: 21rpx;
}

.detail-tag--need {
  background: #e0f2fe;
  color: #0284c7;
}

.detail-tag--user {
  background: #ede9fe;
  color: #7c3aed;
}

.publisher-card {
  display: flex;
  align-items: center;
  gap: 14rpx;
  margin-top: 22rpx;
  padding: 20rpx;
  border-radius: 22rpx;
  background: #f8fafc;
}

.publisher-main {
  flex: 1;
  min-width: 0;
}

.publisher-avatar {
  width: 76rpx;
  height: 76rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb, #38bdf8);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 700;
  flex-shrink: 0;
}

.publisher-name,
.empty-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}

.publisher-link {
  flex-shrink: 0;
  min-height: 64rpx;
  padding: 0 18rpx;
  border-radius: 18rpx;
  background: #dbeafe;
  color: #1d4ed8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 700;
}

.detail-action-row {
  display: flex;
  gap: 14rpx;
  margin-top: 22rpx;
}

.detail-btn {
  flex: 1;
  min-height: 84rpx;
  border-radius: 22rpx;
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 700;
}

.detail-btn--ghost {
  background: #e2e8f0;
  color: #334155;
}

.detail-btn.disabled {
  background: #cbd5e1;
}

.empty-card {
  text-align: center;
}
</style>
