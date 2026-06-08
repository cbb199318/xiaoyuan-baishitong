<template>
  <view class="partner-page">
    <view class="partner-navbar">
      <view class="partner-nav-side partner-nav-left" hover-class="partner-nav-hover" @click="goBack">‹</view>
      <view class="partner-nav-center">
        <text class="partner-nav-title">校园搭子</text>
      </view>
      <view class="partner-nav-side partner-nav-right" hover-class="partner-nav-hover" @click="toggleMore">
        ≡
      </view>
    </view>

    <view v-if="showMore" class="partner-more-mask" @click="showMore = false">
      <view class="partner-more-panel" @click.stop>
        <view class="partner-more-item" @click="openMore('/pages/partner/messages')">消息</view>
        <view class="partner-more-item" @click="openMore('/pages/partner/mine')">个人中心</view>
      </view>
    </view>

    <view class="partner-search-wrap">
      <view class="partner-search-tip">
        <text class="partner-search-tip-title">今日搭子匹配</text>
        <text class="partner-search-tip-desc">支持类型、关键词和地点模糊搜索，快速找到合拍同学。</text>
      </view>
      <view class="partner-search-box">
        <input
          v-model.trim="keyword"
          class="partner-search-input"
          placeholder="搜索搭子类型、关键词、活动地点"
          confirm-type="search"
          @confirm="refreshList"
        />
        <view class="partner-search-btn" @click="refreshList">搜索</view>
      </view>
    </view>

    <scroll-view scroll-x class="partner-category-scroll" show-scrollbar="false">
      <view class="partner-category-row">
        <view
          v-for="item in partnerCategoryOptions"
          :key="item.value"
          :class="['partner-category-chip', activeCategory === item.value ? 'active' : '']"
          @click="selectCategory(item.value)"
        >
          {{ item.label }}
        </view>
      </view>
    </scroll-view>

    <view class="partner-list-wrap">
      <view class="partner-list-header">
        <view>
          <text class="partner-list-title">搭子需求广场</text>
          <text class="partner-list-subtitle">
            {{ activeCategory === 'ALL' ? '全部需求' : partnerCategoryMap[activeCategory] }} · {{ displayList.length }} 条结果
          </text>
        </view>
      </view>

      <view v-if="displayList.length" class="partner-list">
        <view v-for="item in displayList" :key="item.id" class="partner-card" @click="openDetail(item.id)">
          <view class="partner-card-top">
            <view class="partner-type-wrap">
              <view class="partner-card-line">
                <text :class="['partner-type-tag', `type-${String(item.type).toLowerCase()}`]">
                  {{ partnerCategoryMap[item.type] || item.type }}
                </text>
                <text class="partner-score">{{ item.matchScore }}% 匹配</text>
              </view>
              <text class="partner-meta">需求时间：{{ item.timeText }}</text>
              <text class="partner-meta">活动地点：{{ item.location }}</text>
            </view>
            <view class="partner-report" @click.stop="reportItem(item)">举报</view>
          </view>

          <view class="partner-tag-group">
            <text v-for="tag in item.needTags" :key="tag" class="partner-tag partner-tag--need">{{ tag }}</text>
            <text v-for="tag in item.userTags" :key="tag" class="partner-tag partner-tag--user">{{ tag }}</text>
          </view>

          <text class="partner-desc">{{ item.description }}</text>

          <view class="partner-card-bottom">
            <view class="partner-user-info">
              <text class="partner-user-line">剩余名额：{{ item.remainingSlots }}</text>
              <text class="partner-user-line clickable" @click.stop="openPublisherProfile(item)">发布昵称：{{ item.nickname }}</text>
            </view>

            <view
              :class="['partner-apply-btn', isOwnDemand(item) || isApplied(item.id) ? 'disabled' : '']"
              @click.stop="applyPartner(item)"
            >
              {{ isOwnDemand(item) ? '我发布的' : (isApplied(item.id) ? '已申请待回复' : '申请搭子') }}
            </view>
          </view>
        </view>
      </view>

      <view v-else class="partner-empty">
        <view class="partner-empty-graphic">搭</view>
        <text class="partner-empty-title">暂无搭子需求</text>
        <text class="partner-empty-desc">换个关键词或分类试试，也可以先发布你自己的搭子需求。</text>
      </view>
    </view>

    <view class="partner-floating-bar">
      <view class="partner-floating-btn" @click="go('/pages/partner/publish')">发布搭子需求</view>
    </view>

    <PartnerReportModal
      v-model:visible="showReportModal"
      module="partner"
      target-type="demand"
      :target-id="activeReportTargetId"
    />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onHide, onLoad, onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import PartnerReportModal from '../../components/PartnerReportModal.vue'
import {
  partnerCategoryMap,
  partnerCategoryOptions,
} from '../../utils/partnerMock'
import { listPartnerDemands } from '../../utils/partnerApi'
import { ensurePartnerConversation, getPartnerConversations } from '../../utils/partnerChat'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const keyword = ref('')
const activeCategory = ref('ALL')
const appliedIds = ref([])
const showMore = ref(false)
const partnerList = ref([])
const loading = ref(false)
const showReportModal = ref(false)
const activeReportTargetId = ref('')
const filterPublisherId = ref('')

const normalizedKeyword = computed(() => keyword.value.trim().toLowerCase())

const filteredList = computed(() => {
  const key = normalizedKeyword.value
  return partnerList.value
    .filter((item) => activeCategory.value === 'ALL' || item.type === activeCategory.value)
    .filter((item) => !filterPublisherId.value || Number(item.publisherId || 0) === Number(filterPublisherId.value))
    .filter((item) => {
      if (!key) {
        return true
      }
      const text = [
        partnerCategoryMap[item.type] || '',
        item.location || '',
        item.description || '',
        item.nickname || '',
        ...(item.needTags || []),
        ...(item.userTags || []),
      ]
        .join(' ')
        .toLowerCase()
      return text.includes(key)
    })
})

const normalizeDemandItem = (item) => ({
  ...item,
  matchScore: Number(item?.matchScore || 100),
})

const reloadPartnerContent = async (showToast = false) => {
  loading.value = true
  try {
    const [list, conversations] = await Promise.all([
      listPartnerDemands({
        keyword: keyword.value,
        type: activeCategory.value === 'ALL' ? '' : activeCategory.value,
        publisherId: filterPublisherId.value || undefined,
      }),
      store.profile ? getPartnerConversations() : Promise.resolve([]),
    ])
    partnerList.value = Array.isArray(list) ? list.map(normalizeDemandItem) : []
    appliedIds.value = Array.isArray(conversations)
      ? conversations
          .filter((item) => Number(item.applicantId) === Number(store.profile?.userId || 0))
          .map((item) => item.demandId)
      : []
  } finally {
    loading.value = false
  }
  if (showToast) {
    uni.showToast({ title: '列表已刷新', icon: 'none' })
  }
}

const displayList = computed(() =>
  [...filteredList.value].sort((a, b) => {
    const timeDiff = new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    if (timeDiff !== 0) {
      return timeDiff
    }
    return Number(b.matchScore || 0) - Number(a.matchScore || 0)
  }))

const isApplied = (id) => appliedIds.value.includes(id)
const isOwnDemand = (item) => Number(item.publisherId || 0) === Number(store.profile?.userId || 0)

const applyPartner = async (item) => {
  if (isOwnDemand(item)) {
    uni.showToast({ title: '自己发布的需求无需申请', icon: 'none' })
    return
  }
  if (isApplied(item.id)) {
    uni.navigateTo({ url: '/pages/partner/messages' })
    return
  }
  try {
    const conversation = await ensurePartnerConversation({ demand: item, currentUser: store.profile })
    appliedIds.value = [...new Set([...appliedIds.value, item.id])]
    uni.showToast({ title: '已提交搭子申请', icon: 'success' })
    setTimeout(() => {
      uni.navigateTo({ url: `/pages/partner/chat?id=${conversation.id}` })
    }, 220)
  } catch (error) {
  }
}

const reportItem = (item) => {
  activeReportTargetId.value = item.id
  showReportModal.value = true
}

const selectCategory = (value) => {
  activeCategory.value = value
  reloadPartnerContent()
}

const toggleMore = () => {
  showMore.value = !showMore.value
}

const refreshList = () => {
  reloadPartnerContent(true)
}

const goBack = () => {
  uni.switchTab({ url: '/pages/home/index' })
}

const openMore = (url) => {
  showMore.value = false
  uni.navigateTo({ url })
}

const go = (url) => {
  showMore.value = false
  uni.navigateTo({ url })
}

const openDetail = (id) => {
  uni.navigateTo({ url: `/pages/partner/detail?id=${id}` })
}

const openPublisherProfile = (item) => {
  if (!item?.publisherId) {
    return
  }
  uni.navigateTo({ url: `/pages/partner/profile?userId=${item.publisherId}` })
}

const handlePublished = () => {
  reloadPartnerContent()
  uni.showToast({ title: '搭子需求已更新', icon: 'none' })
}

onShow(async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  uni.$on('partner-published', handlePublished)
  await reloadPartnerContent()
})

onHide(() => {
  uni.$off('partner-published', handlePublished)
  showReportModal.value = false
  activeReportTargetId.value = ''
})

onPullDownRefresh(() => {
  reloadPartnerContent().finally(() => {
    uni.stopPullDownRefresh()
  })
})

onLoad((options) => {
  filterPublisherId.value = options?.publisherId || ''
})
</script>

<style scoped lang="scss">
.partner-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8fbff 0%, #f4f7fb 32%, #f4f7fb 100%);
  padding-bottom: calc(176rpx + env(safe-area-inset-bottom));
}

.partner-navbar {
  position: sticky;
  top: 0;
  z-index: 20;
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
  font-size: 34rpx;
  color: #2d7ff9;
}

.partner-nav-center {
  text-align: center;
  min-width: 0;
}

.partner-nav-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.partner-nav-hover {
  background: #f1f5f9;
}

.partner-more-mask {
  position: fixed;
  inset: 0;
  z-index: 40;
}

.partner-more-panel {
  position: absolute;
  top: calc(96rpx + env(safe-area-inset-top));
  right: 20rpx;
  width: 220rpx;
  padding: 12rpx;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 18rpx 40rpx rgba(15, 23, 42, 0.14);
  border: 1px solid #e2e8f0;
}

.partner-more-item {
  min-height: 76rpx;
  padding: 0 20rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  color: #1e293b;
  font-size: 24rpx;
  font-weight: 600;
}

.partner-more-item + .partner-more-item {
  margin-top: 8rpx;
}

.partner-search-wrap {
  padding: 22rpx 24rpx 10rpx;
}

.partner-search-tip {
  padding: 18rpx 24rpx 22rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #0f172a, #1d4ed8);
  color: #ffffff;
  margin-bottom: 18rpx;
  box-shadow: 0 16rpx 40rpx rgba(29, 78, 216, 0.18);
}

.partner-search-tip-title {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
}

.partner-search-tip-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 23rpx;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.82);
}

.partner-search-box {
  display: grid;
  grid-template-columns: 1fr 112rpx;
  gap: 14rpx;
  padding: 14rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.06);
}

.partner-search-input {
  height: 78rpx;
  border-radius: 20rpx;
  background: #f8fafc;
  padding: 0 24rpx;
}

.partner-search-btn {
  height: 78rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 700;
}

.partner-category-scroll {
  white-space: nowrap;
  padding: 8rpx 0 6rpx;
}

.partner-category-row {
  display: inline-flex;
  gap: 14rpx;
  padding: 0 24rpx;
}

.partner-category-chip {
  padding: 14rpx 22rpx;
  border-radius: 999rpx;
  background: #e7eef8;
  color: #475569;
  font-size: 23rpx;
  font-weight: 600;
}

.partner-category-chip.active {
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
}

.partner-list-wrap {
  padding: 12rpx 24rpx 0;
}

.partner-list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18rpx;
}

.partner-list-title {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
}

.partner-list-subtitle {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #64748b;
}

.partner-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.partner-card {
  padding: 24rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.06);
}

.partner-card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18rpx;
}

.partner-type-wrap {
  flex: 1;
  min-width: 0;
}

.partner-card-line {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
  margin-bottom: 10rpx;
}

.partner-type-tag {
  display: inline-block;
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 700;
  color: #ffffff;
}

.partner-type-tag.type-study {
  background: linear-gradient(135deg, #2563eb, #38bdf8);
}

.partner-type-tag.type-meal {
  background: linear-gradient(135deg, #f97316, #fb7185);
}

.partner-type-tag.type-sport {
  background: linear-gradient(135deg, #10b981, #22c55e);
}

.partner-type-tag.type-travel {
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
}

.partner-type-tag.type-explore {
  background: linear-gradient(135deg, #ec4899, #f43f5e);
}

.partner-meta {
  display: block;
  margin-top: 6rpx;
  font-size: 23rpx;
  line-height: 1.7;
  color: #64748b;
}

.partner-score {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: #eefbf3;
  color: #16a34a;
  font-size: 21rpx;
  font-weight: 700;
}

.partner-report {
  flex-shrink: 0;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: #fef2f2;
  color: #ef4444;
  font-size: 22rpx;
}

.partner-tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 16rpx;
}

.partner-tag {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 21rpx;
}

.partner-tag--need {
  background: #e0f2fe;
  color: #0284c7;
}

.partner-tag--user {
  background: #ede9fe;
  color: #7c3aed;
}

.partner-desc {
  display: block;
  margin-top: 18rpx;
  font-size: 25rpx;
  line-height: 1.8;
  color: #334155;
}

.partner-card-bottom {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18rpx;
  margin-top: 22rpx;
}

.partner-user-info {
  flex: 1;
  min-width: 0;
}

.partner-user-line {
  display: block;
  margin-top: 6rpx;
  font-size: 23rpx;
  color: #64748b;
}

.partner-user-line.clickable {
  color: #1677ff;
}

.partner-apply-btn {
  flex-shrink: 0;
  min-width: 180rpx;
  height: 82rpx;
  padding: 0 22rpx;
  border-radius: 22rpx;
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 700;
  text-align: center;
}

.partner-apply-btn.disabled {
  background: #cbd5e1;
  color: #ffffff;
}

.partner-empty {
  margin-top: 48rpx;
  padding: 56rpx 30rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.06);
  text-align: center;
}

.partner-empty-graphic {
  width: 120rpx;
  height: 120rpx;
  margin: 0 auto 18rpx;
  border-radius: 36rpx;
  background: linear-gradient(135deg, #dbeafe, #eff6ff);
  color: #1677ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  font-weight: 800;
}

.partner-empty-title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
}

.partner-empty-desc {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.8;
  color: #64748b;
}

.partner-floating-bar {
  position: fixed;
  left: 24rpx;
  right: 24rpx;
  bottom: calc(26rpx + env(safe-area-inset-bottom));
  display: flex;
  justify-content: center;
  pointer-events: none;
}

.partner-floating-btn {
  min-width: 320rpx;
  height: 92rpx;
  padding: 0 38rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #0f172a, #2563eb);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  font-weight: 800;
  box-shadow: 0 18rpx 40rpx rgba(37, 99, 235, 0.24);
  pointer-events: auto;
}
</style>
