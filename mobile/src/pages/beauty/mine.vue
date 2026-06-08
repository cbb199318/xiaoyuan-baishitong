<template>
  <view class="beauty-mine-page">
    <view class="beauty-mine-navbar">
      <view class="beauty-nav-side beauty-nav-left" hover-class="beauty-nav-hover" @click="goBack">‹</view>
      <view class="beauty-nav-center">
        <text class="beauty-nav-title">美妆个人中心</text>
      </view>
      <view class="beauty-nav-placeholder"></view>
    </view>

    <scroll-view scroll-y class="beauty-mine-scroll">
      <view class="beauty-profile-card">
        <view class="beauty-profile-avatar">{{ displayInitial }}</view>
        <view class="beauty-profile-main">
          <text class="beauty-profile-name">{{ profile?.nickname || '校园用户' }}</text>
          <text class="beauty-profile-phone">{{ profile?.phone || '未补充手机号' }}</text>
          <text class="beauty-profile-desc">轻量化管理我的发布、我的收藏和常用设置</text>
        </view>
      </view>

      <view class="beauty-section-card">
        <text class="beauty-section-title">个人资料</text>
        <view class="beauty-action-list">
          <view class="beauty-action-item" @click="goProfileEdit">
            <text class="beauty-action-label">编辑个人资料</text>
            <text class="beauty-action-arrow">›</text>
          </view>
        </view>
      </view>

      <view class="beauty-section-card">
        <view class="beauty-section-head">
          <view class="beauty-content-tabs">
            <view
              :class="['beauty-content-tab', activeContentTab === 'posts' ? 'active' : '']"
              @click="activeContentTab = 'posts'"
            >
              我的发布
            </view>
            <view
              :class="['beauty-content-tab', activeContentTab === 'favorites' ? 'active' : '']"
              @click="activeContentTab = 'favorites'"
            >
              我的收藏
            </view>
          </view>
          <view class="beauty-section-actions">
            <view
              v-if="activeContentTab === 'posts' && myPosts.length > 5"
              class="beauty-section-link ghost"
              @click="openMyPosts"
            >
              查看更多
            </view>
            <view
              v-if="activeContentTab === 'favorites' && favoriteGoods.length > 5"
              class="beauty-section-link ghost"
              @click="openFavorites"
            >
              查看更多
            </view>
            <view v-if="activeContentTab === 'posts'" class="beauty-section-link" @click="openPublish">继续发布</view>
          </view>
        </view>

        <view v-if="activeContentTab === 'posts' && visibleMyPosts.length" class="beauty-card-list">
          <view v-for="item in visibleMyPosts" :key="item.id" class="beauty-good-card" @click="openDetail(item.id)">
            <image class="beauty-good-image" :src="item.imageUrl || item.image" mode="aspectFill"></image>
            <view class="beauty-good-main">
              <view class="beauty-good-head">
                <text class="beauty-good-title">{{ item.title }}</text>
                <text :class="['beauty-status-chip', `status-${String(item.status || '').toLowerCase()}`]">
                  {{ formatStatus(item.status) }}
                </text>
              </view>
              <text class="beauty-good-summary">{{ item.summary }}</text>
              <text class="beauty-good-status-note">{{ formatStatusNote(item) }}</text>
              <view class="beauty-good-meta">
                <text class="beauty-good-price">￥{{ item.price }}</text>
                <text class="beauty-good-time">{{ formatTime(item.createdAt) }}</text>
              </view>
            </view>
          </view>
        </view>

        <view v-else-if="activeContentTab === 'posts'" class="beauty-empty-block">
          <text class="beauty-empty-title">还没有发布过好物</text>
          <text class="beauty-empty-desc">上传商品实拍图和购买凭证后，就能把平价好物展示给其他同学。</text>
        </view>

        <view v-else-if="visibleFavoriteGoods.length" class="beauty-card-list">
          <view
            v-for="item in visibleFavoriteGoods"
            :key="item.id"
            class="beauty-good-card"
            @click="openDetail(item.id)"
          >
            <image class="beauty-good-image" :src="item.imageUrl || item.image" mode="aspectFill"></image>
            <view class="beauty-good-main">
              <text class="beauty-good-title">{{ item.title }}</text>
              <text class="beauty-good-summary">{{ item.summary }}</text>
              <view class="beauty-good-meta">
                <text class="beauty-good-price">￥{{ item.price }}</text>
                <text class="beauty-good-tag">{{ item.skinTags?.[0] || '平价好物' }}</text>
              </view>
            </view>
          </view>
        </view>
        <view v-else class="beauty-empty-block">
          <text class="beauty-empty-title">暂时没有收藏内容</text>
          <text class="beauty-empty-desc">在好物详情页点击收藏后，这里会同步展示你的平价种草清单。</text>
        </view>
      </view>

      <view class="beauty-section-card">
        <text class="beauty-section-title">售后与举报</text>
        <view class="beauty-action-list">
          <view class="beauty-action-item" @click="openBeautyReports">
            <text class="beauty-action-label">举报记录</text>
            <text class="beauty-action-arrow">›</text>
          </view>
        </view>
      </view>

      <view class="beauty-section-card">
        <text class="beauty-section-title">系统设置</text>
        <view class="beauty-action-list">
          <view class="beauty-action-item" @click="openPlaceholder('系统设置')">
            <text class="beauty-action-label">系统设置</text>
            <text class="beauty-action-arrow">›</text>
          </view>
          <view class="beauty-action-item" @click="openPlaceholder('帮助说明')">
            <text class="beauty-action-label">帮助说明</text>
            <text class="beauty-action-arrow">›</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <BeautyTabBar current="mine" @change="switchTab" />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import BeautyTabBar from '../../components/BeautyTabBar.vue'
import { useUserStore } from '../../stores/user'
import { listMyBeautyFavorites, listMyBeautyGoods } from '../../utils/beautyApi'

const store = useUserStore()
const myPosts = ref([])
const favoriteGoods = ref([])
const activeContentTab = ref('posts')

const profile = computed(() => store.profile || {})
const displayInitial = computed(() => (profile.value?.nickname || '美').slice(0, 1))
const visibleMyPosts = computed(() => myPosts.value.slice(0, 5))
const visibleFavoriteGoods = computed(() => favoriteGoods.value.slice(0, 5))

const loadData = async () => {
  myPosts.value = await listMyBeautyGoods()
  favoriteGoods.value = await listMyBeautyFavorites()
}

onShow(() => {
  loadData()
})

const goBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.redirectTo({ url: '/pages/beauty/index' })
    },
  })
}

const openHome = () => {
  uni.redirectTo({ url: '/pages/beauty/index' })
}

const switchTab = (tab) => {
  if (tab === 'home') {
    openHome()
    return
  }
  if (tab === 'publish') {
    openPublish()
  }
}

const goProfileEdit = () => {
  uni.navigateTo({ url: '/pages/profile/edit' })
}

const openPublish = () => {
  uni.navigateTo({ url: '/pages/beauty/publish' })
}

const openMyPosts = () => {
  uni.navigateTo({ url: '/pages/beauty/my-posts' })
}

const openFavorites = () => {
  uni.navigateTo({ url: '/pages/beauty/favorites' })
}

const openDetail = (id) => {
  uni.navigateTo({ url: `/pages/beauty/detail?id=${id}` })
}

const openBeautyReports = () => {
  uni.navigateTo({ url: '/pages/report/list?module=beauty&title=美妆举报记录' })
}

const openPlaceholder = (title) => {
  uni.navigateTo({ url: `/pages/placeholder/index?title=${title}` })
}

const formatStatus = (value) => {
  const map = {
    PENDING: '审核中',
    APPROVED: '已上架',
    REJECTED: '已驳回',
    OFFLINE: '已下架',
  }
  return map[String(value || '').toUpperCase()] || '待处理'
}

const formatTime = (value) => {
  if (!value) {
    return '刚刚发布'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return '刚刚发布'
  }
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${month}-${day}`
}

const formatStatusNote = (item) => {
  const status = String(item?.status || '').toUpperCase()
  if (status === 'PENDING') {
    return '审核中：当前仅自己可见，后台通过后会进入首页公开展示。'
  }
  if (status === 'REJECTED') {
    return `驳回原因：${item?.rejectReason || '内容暂不符合平台展示规范，可修改后重新提交。'}`
  }
  if (status === 'OFFLINE') {
    return '已下架：当前不会在首页公开展示。'
  }
  return '已上架：当前已在美妆首页公开展示。'
}
</script>

<style scoped lang="scss">
.beauty-mine-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top right, rgba(244, 114, 182, 0.18), transparent 22%),
    linear-gradient(180deg, #fff7fb 0%, #f7f8fc 24%, #f4f7fb 100%);
  padding-bottom: calc(184rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.beauty-mine-navbar {
  position: sticky;
  top: 0;
  z-index: 30;
  display: grid;
  grid-template-columns: 88rpx 1fr 88rpx;
  align-items: center;
  min-height: 96rpx;
  padding: calc(16rpx + env(safe-area-inset-top)) 16rpx 12rpx;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(18rpx);
  border-bottom: 1px solid rgba(244, 114, 182, 0.08);
}

.beauty-nav-side {
  min-height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18rpx;
  color: #1f2937;
}

.beauty-nav-left {
  font-size: 42rpx;
}

.beauty-nav-hover {
  background: #fff0f7;
}

.beauty-nav-center {
  min-width: 0;
  text-align: center;
}

.beauty-nav-title {
  display: block;
  font-size: 32rpx;
  font-weight: 800;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.beauty-nav-placeholder {
  min-height: 72rpx;
}

.beauty-mine-scroll {
  height: calc(100vh - 112rpx - env(safe-area-inset-top) - 168rpx - env(safe-area-inset-bottom));
}

.beauty-profile-card,
.beauty-section-card {
  margin: 24rpx;
  border-radius: 30rpx;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18rpx 44rpx rgba(236, 72, 153, 0.08);
}

.beauty-profile-card {
  padding: 28rpx 24rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.beauty-profile-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ec4899, #fb7185);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  font-weight: 800;
  flex-shrink: 0;
}

.beauty-profile-main,
.beauty-profile-name,
.beauty-profile-phone,
.beauty-profile-desc,
.beauty-section-title,
.beauty-good-title,
.beauty-good-summary,
.beauty-good-price,
.beauty-good-time,
.beauty-good-tag,
.beauty-empty-title,
.beauty-empty-desc {
  display: block;
}

.beauty-profile-name {
  font-size: 32rpx;
  font-weight: 800;
  color: #111827;
}

.beauty-profile-phone {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #64748b;
}

.beauty-profile-desc {
  margin-top: 10rpx;
  font-size: 23rpx;
  line-height: 1.6;
  color: #94a3b8;
}

.beauty-section-card {
  padding: 24rpx 22rpx;
}

.beauty-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.beauty-section-actions {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.beauty-content-tabs {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
}

.beauty-content-tab {
  min-height: 64rpx;
  padding: 0 22rpx;
  border-radius: 999rpx;
  background: #fff7fb;
  color: #94a3b8;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 700;
  border: 1px solid #fbcfe8;
}

.beauty-content-tab.active {
  background: linear-gradient(135deg, #ec4899, #fb7185);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 12rpx 24rpx rgba(236, 72, 153, 0.14);
}

.beauty-section-title {
  font-size: 28rpx;
  font-weight: 800;
  color: #111827;
}

.beauty-section-link {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: #fff1f6;
  color: #be185d;
  font-size: 22rpx;
  font-weight: 700;
}

.beauty-section-link.ghost {
  background: #fff7fb;
  color: #db2777;
  border: 1px solid #fbcfe8;
}

.beauty-card-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 18rpx;
}

.beauty-good-card {
  display: flex;
  gap: 14rpx;
  padding: 14rpx;
  border-radius: 24rpx;
  background: #fff7fb;
}

.beauty-good-image {
  width: 126rpx;
  height: 126rpx;
  border-radius: 20rpx;
  flex-shrink: 0;
  background: #fde6f1;
}

.beauty-good-main {
  flex: 1;
  min-width: 0;
}

.beauty-good-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12rpx;
}

.beauty-good-title {
  font-size: 25rpx;
  font-weight: 800;
  color: #111827;
  line-height: 1.45;
}

.beauty-status-chip {
  flex-shrink: 0;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  font-weight: 700;
}

.beauty-status-chip.status-pending {
  background: #fff7ed;
  color: #c2410c;
}

.beauty-status-chip.status-approved {
  background: #ecfdf3;
  color: #047857;
}

.beauty-status-chip.status-rejected {
  background: #fff1f2;
  color: #e11d48;
}

.beauty-status-chip.status-offline {
  background: #f1f5f9;
  color: #475569;
}

.beauty-good-summary {
  margin-top: 8rpx;
  font-size: 23rpx;
  line-height: 1.65;
  color: #64748b;
}

.beauty-good-status-note {
  display: block;
  margin-top: 8rpx;
  font-size: 21rpx;
  line-height: 1.6;
  color: #9a3412;
}

.beauty-good-meta {
  margin-top: 12rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.beauty-good-price {
  font-size: 24rpx;
  font-weight: 800;
  color: #e11d48;
}

.beauty-good-time,
.beauty-good-tag {
  font-size: 22rpx;
  color: #94a3b8;
}

.beauty-empty-block {
  margin-top: 18rpx;
  padding: 34rpx 20rpx;
  border-radius: 24rpx;
  background: #fff7fb;
  text-align: center;
}

.beauty-empty-title {
  font-size: 26rpx;
  font-weight: 800;
  color: #111827;
}

.beauty-empty-desc {
  margin-top: 10rpx;
  font-size: 23rpx;
  line-height: 1.7;
  color: #64748b;
}

.beauty-action-list {
  margin-top: 14rpx;
}

.beauty-action-item {
  min-height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  border-bottom: 1px solid #f1dce7;
}

.beauty-action-item:last-child {
  border-bottom: 0;
}

.beauty-action-label {
  color: #111827;
  font-size: 25rpx;
  font-weight: 600;
}

.beauty-action-arrow {
  color: #94a3b8;
  font-size: 30rpx;
}
</style>
