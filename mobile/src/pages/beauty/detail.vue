<template>
  <view class="beauty-detail-page">
    <view class="beauty-detail-navbar">
      <view class="beauty-detail-side beauty-detail-left" hover-class="beauty-detail-hover" @click="goBack">‹</view>
      <view class="beauty-detail-center">
        <text class="beauty-detail-title">{{ pageTitle }}</text>
      </view>
      <view class="beauty-detail-placeholder"></view>
    </view>

    <scroll-view scroll-y class="beauty-detail-scroll">
      <view v-if="topic" class="beauty-detail-content">
        <view class="beauty-topic-hero">
          <text class="beauty-topic-hero-title">{{ topic.title }}</text>
          <text class="beauty-topic-hero-scene">{{ topic.scene }}</text>
          <text class="beauty-topic-hero-desc">{{ topic.desc }}</text>
          <view class="beauty-topic-report-btn" @click="openTopicReport">举报该专题</view>
        </view>

        <view class="beauty-topic-section">
          <text class="beauty-section-title">适配平价好物</text>
          <view class="beauty-topic-related">
            <view v-for="item in topicGoods" :key="item.id" class="beauty-related-card" @click="openGood(item.id)">
              <image class="beauty-related-image" :src="item.imageUrl || item.image" mode="aspectFill"></image>
              <view class="beauty-related-main">
                <text class="beauty-related-title">{{ item.title }}</text>
                <text class="beauty-related-price">￥{{ item.price }}</text>
                <text class="beauty-related-summary">{{ item.summary }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view v-else-if="detail" class="beauty-detail-content">
        <swiper class="beauty-gallery" circular indicator-dots autoplay interval="3600" duration="500">
          <swiper-item v-for="(image, index) in galleryList" :key="`${detail.id}-${index}`">
            <image
              class="beauty-gallery-image"
              :src="getGalleryImage(image, index)"
              mode="aspectFill"
              @error="markGalleryFallback(index)"
            ></image>
          </swiper-item>
        </swiper>

        <view class="beauty-good-panel">
          <text class="beauty-good-price">￥{{ detail.price }}</text>
          <text class="beauty-good-title">{{ detail.title }}</text>
          <text class="beauty-good-summary">{{ detail.summary }}</text>

          <view class="beauty-good-tags">
            <text v-for="tag in detail.skinTags" :key="tag" class="beauty-good-tag">{{ tag }}</text>
          </view>

          <view class="beauty-meta-list">
            <view class="beauty-meta-item">
              <text class="beauty-meta-label">产品简介</text>
              <text class="beauty-meta-value">{{ detail.summary }}</text>
            </view>
            <view class="beauty-meta-item">
              <text class="beauty-meta-label">适用场景说明</text>
              <text class="beauty-meta-value">{{ detail.sceneText }}</text>
            </view>
          </view>
        </view>

        <view class="beauty-review-card">
          <view class="beauty-review-head">
            <text class="beauty-section-title">用户真实评价</text>
            <text class="beauty-review-count">{{ detail.reviewList?.length || 0 }} 条</text>
          </view>
          <view v-if="detail.reviewList?.length" class="beauty-review-list">
            <view v-for="item in detail.reviewList" :key="item.id" class="beauty-review-item">
              <text class="beauty-review-user">{{ item.user }}</text>
              <text class="beauty-review-content">{{ item.content }}</text>
            </view>
          </view>
        </view>

        <view class="beauty-block">
          <text class="beauty-section-title">种草测评</text>
          <text class="beauty-block-text">{{ detail.evaluation }}</text>
        </view>

        <view class="beauty-block">
          <text class="beauty-section-title">宿舍使用体验</text>
          <text class="beauty-block-text">{{ detail.dormExperience }}</text>
        </view>

        <view class="beauty-block">
          <text class="beauty-section-title">新手避坑指南</text>
          <text class="beauty-block-text">{{ detail.avoidanceGuide }}</text>
        </view>

        <view class="beauty-block">
          <text class="beauty-section-title">所属分类</text>
          <text class="beauty-block-text">{{ beautyCategoryMap[detail.category] || detail.category }}</text>
        </view>
      </view>

      <view v-else class="beauty-empty-card">
        <text class="beauty-empty-title">未找到对应内容</text>
        <text class="beauty-empty-desc">可能是当前内容已下架，返回列表页看看其他平价好物。</text>
      </view>
    </scroll-view>

    <view v-if="detail" class="beauty-fixed-bar">
      <view :class="['beauty-fixed-fav', favoriteActive ? 'active' : '']" @click="toggleFavorite">
        {{ favoriteActive ? '已收藏' : '收藏' }}
      </view>
      <view class="beauty-fixed-report" @click="openReport">举报</view>
      <view class="beauty-fixed-main" @click="goBackToList">返回选购列表</view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  beautyCategoryMap,
  getBeautyTopicById,
  getBeautyTopicGoods,
} from '../../utils/beautyMock'
import { favoriteBeautyGood, getBeautyGoodDetail, unfavoriteBeautyGood } from '../../utils/beautyApi'

const detail = ref(null)
const topic = ref(null)
const favoriteActive = ref(false)
const galleryFallbackMap = ref({})

const topicGoods = computed(() => {
  if (!topic.value?.id) {
    return []
  }
  return getBeautyTopicGoods(topic.value.id)
})

const pageTitle = computed(() => (topic.value ? '妆容专题' : '好物详情'))
const galleryList = computed(() => {
  if (!detail.value) {
    return []
  }
  const list =
    Array.isArray(detail.value.gallery) && detail.value.gallery.length
      ? detail.value.gallery
      : [detail.value.imageUrl || detail.value.image]
  return list.filter(Boolean)
})

const beautyGalleryFallback =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 720 460">
      <defs>
        <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#ffe6f2"/>
          <stop offset="100%" stop-color="#ffd7ea"/>
        </linearGradient>
      </defs>
      <rect width="720" height="460" rx="32" fill="url(#bg)"/>
      <circle cx="582" cy="126" r="72" fill="rgba(255,255,255,0.46)"/>
      <circle cx="136" cy="334" r="90" fill="rgba(255,255,255,0.3)"/>
      <text x="360" y="202" text-anchor="middle" font-size="34" font-weight="700" fill="#be185d">校园百事通</text>
      <text x="360" y="256" text-anchor="middle" font-size="28" fill="#db2777">平价美妆好物展示图</text>
    </svg>
  `)

const syncFavoriteState = () => {
  if (!detail.value?.id) {
    favoriteActive.value = false
    return
  }
  favoriteActive.value = !!detail.value.favorite
}

const getGalleryImage = (image, index) => {
  if (!image || galleryFallbackMap.value[index]) {
    return beautyGalleryFallback
  }
  return image
}

const markGalleryFallback = (index) => {
  galleryFallbackMap.value = {
    ...galleryFallbackMap.value,
    [index]: true,
  }
}

const goBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.redirectTo({ url: '/pages/beauty/index' })
    },
  })
}

const goBackToList = () => {
  uni.redirectTo({ url: '/pages/beauty/index' })
}

const openGood = (id) => {
  uni.navigateTo({ url: `/pages/beauty/detail?id=${id}` })
}

const openReport = () => {
  if (!detail.value?.id) {
    return
  }
  uni.navigateTo({
    url: `/pages/report/create?module=beauty&targetType=${detail.value.reportTargetType || 'good'}&targetId=${detail.value.id}`,
  })
}

const openTopicReport = () => {
  if (!topic.value?.id) {
    return
  }
  uni.navigateTo({
    url: `/pages/report/create?module=beauty&targetType=topic&title=${topic.value.title}`,
  })
}

const toggleFavorite = () => {
  if (!detail.value?.id) {
    return
  }
  const nextAction = favoriteActive.value ? 'unfavorite' : 'favorite'
  const run = nextAction === 'unfavorite' ? unfavoriteBeautyGood(detail.value.id) : favoriteBeautyGood(detail.value.id)
  run
    .then((data) => {
      detail.value = data
      syncFavoriteState()
      uni.showToast({
        title: nextAction === 'favorite' ? '已加入收藏' : '已取消收藏',
        icon: 'none',
      })
    })
    .catch(() => {})
}

onLoad((options) => {
  galleryFallbackMap.value = {}
  if (options?.topicId) {
    topic.value = getBeautyTopicById(options.topicId)
    return
  }
  getBeautyGoodDetail(options?.id || '')
    .then((data) => {
      detail.value = data
      syncFavoriteState()
    })
    .catch(() => {
      detail.value = null
    })
})
</script>

<style scoped lang="scss">
.beauty-detail-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top right, rgba(244, 114, 182, 0.18), transparent 22%),
    linear-gradient(180deg, #fff7fb 0%, #f7f8fc 24%, #f4f7fb 100%);
  padding-bottom: calc(148rpx + env(safe-area-inset-bottom));
}

.beauty-detail-navbar {
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

.beauty-detail-side {
  min-height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18rpx;
  color: #1f2937;
}

.beauty-detail-left {
  font-size: 42rpx;
}

.beauty-detail-hover {
  background: #fff0f7;
}

.beauty-detail-center {
  text-align: center;
  min-width: 0;
}

.beauty-detail-title {
  display: block;
  font-size: 32rpx;
  font-weight: 800;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.beauty-detail-placeholder {
  min-height: 72rpx;
}

.beauty-detail-scroll {
  height: calc(100vh - 112rpx - env(safe-area-inset-top));
}

.beauty-detail-content,
.beauty-empty-card {
  padding: 24rpx;
  box-sizing: border-box;
}

.beauty-topic-hero,
.beauty-topic-section,
.beauty-good-panel,
.beauty-review-card,
.beauty-block,
.beauty-empty-card {
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18rpx 44rpx rgba(236, 72, 153, 0.08);
}

.beauty-topic-hero {
  padding: 30rpx 26rpx;
}

.beauty-topic-report-btn {
  margin-top: 22rpx;
  min-height: 68rpx;
  padding: 0 24rpx;
  border-radius: 20rpx;
  background: #fff1f2;
  color: #e11d48;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 23rpx;
  font-weight: 700;
}

.beauty-topic-hero-title,
.beauty-good-title,
.beauty-section-title,
.beauty-related-title,
.beauty-related-price,
.beauty-good-price,
.beauty-empty-title,
.beauty-good-summary,
.beauty-meta-label,
.beauty-meta-value,
.beauty-review-user,
.beauty-review-content,
.beauty-review-count {
  display: block;
}

.beauty-topic-hero-title {
  font-size: 34rpx;
  font-weight: 800;
  color: #111827;
}

.beauty-topic-hero-scene {
  display: inline-flex;
  margin-top: 12rpx;
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #ffe4ee;
  color: #be185d;
  font-size: 21rpx;
}

.beauty-topic-hero-desc,
.beauty-block-text,
.beauty-related-summary,
.beauty-empty-desc {
  display: block;
  margin-top: 14rpx;
  font-size: 24rpx;
  line-height: 1.75;
  color: #64748b;
}

.beauty-topic-section,
.beauty-review-card,
.beauty-block {
  margin-top: 18rpx;
  padding: 24rpx 22rpx;
}

.beauty-section-title {
  font-size: 28rpx;
  font-weight: 800;
  color: #111827;
}

.beauty-topic-related {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 18rpx;
}

.beauty-related-card {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding: 14rpx;
  border-radius: 22rpx;
  background: #fff7fb;
}

.beauty-related-image {
  width: 110rpx;
  height: 110rpx;
  border-radius: 20rpx;
  flex-shrink: 0;
}

.beauty-related-main {
  flex: 1;
  min-width: 0;
}

.beauty-related-title {
  font-size: 25rpx;
  font-weight: 800;
  color: #111827;
  line-height: 1.5;
}

.beauty-related-price {
  margin-top: 8rpx;
  font-size: 24rpx;
  font-weight: 800;
  color: #e11d48;
}

.beauty-gallery {
  height: 460rpx;
  border-radius: 30rpx;
  overflow: hidden;
  box-shadow: 0 18rpx 44rpx rgba(236, 72, 153, 0.08);
}

.beauty-gallery-image {
  width: 100%;
  height: 100%;
  background: #fde6f1;
}

.beauty-good-panel {
  margin-top: 18rpx;
  padding: 26rpx 24rpx 24rpx;
}

.beauty-good-price {
  font-size: 32rpx;
  font-weight: 800;
  color: #e11d48;
}

.beauty-good-title {
  margin-top: 12rpx;
  font-size: 34rpx;
  font-weight: 800;
  color: #111827;
  line-height: 1.45;
}

.beauty-good-summary {
  margin-top: 14rpx;
  font-size: 24rpx;
  line-height: 1.75;
  color: #64748b;
}

.beauty-good-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 16rpx;
}

.beauty-good-tag {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #fff1f6;
  color: #be185d;
  font-size: 20rpx;
}

.beauty-meta-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 18rpx;
}

.beauty-meta-item {
  padding: 18rpx;
  border-radius: 20rpx;
  background: #fff5f9;
}

.beauty-meta-label {
  font-size: 22rpx;
  font-weight: 700;
  color: #be185d;
}

.beauty-meta-value {
  margin-top: 10rpx;
  font-size: 24rpx;
  line-height: 1.75;
  color: #475569;
}

.beauty-review-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.beauty-review-count {
  flex-shrink: 0;
  font-size: 22rpx;
  color: #94a3b8;
}

.beauty-review-list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  margin-top: 18rpx;
}

.beauty-review-item {
  padding: 18rpx;
  border-radius: 20rpx;
  background: #fff6fa;
}

.beauty-review-user {
  font-size: 23rpx;
  font-weight: 700;
  color: #111827;
}

.beauty-review-content {
  margin-top: 10rpx;
  font-size: 23rpx;
  line-height: 1.75;
  color: #64748b;
}

.beauty-empty-card {
  margin: 24rpx;
  padding: 42rpx 28rpx;
  text-align: center;
}

.beauty-empty-title {
  font-size: 30rpx;
  font-weight: 800;
  color: #111827;
}

.beauty-fixed-bar {
  position: fixed;
  left: 24rpx;
  right: 24rpx;
  bottom: calc(24rpx + env(safe-area-inset-bottom));
  display: grid;
  grid-template-columns: 148rpx 148rpx 1fr;
  gap: 12rpx;
  padding: 14rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18rpx 40rpx rgba(15, 23, 42, 0.14);
}

.beauty-fixed-fav,
.beauty-fixed-report,
.beauty-fixed-main {
  min-height: 82rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 23rpx;
  font-weight: 700;
}

.beauty-fixed-fav {
  background: #fff4f8;
  color: #db2777;
}

.beauty-fixed-fav.active {
  background: #fce7f3;
  color: #be185d;
}

.beauty-fixed-report {
  background: #fff1f2;
  color: #e11d48;
}

.beauty-fixed-main {
  background: linear-gradient(135deg, #ec4899, #fb7185);
  color: #ffffff;
}
</style>
