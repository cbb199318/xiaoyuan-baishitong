<template>
  <view class="beauty-page">
    <view class="beauty-navbar">
      <view class="beauty-nav-side beauty-nav-left" hover-class="beauty-nav-hover" @click="goBack">‹</view>
      <view class="beauty-nav-center">
        <text class="beauty-nav-title">平价美妆好物选购</text>
      </view>
      <view class="beauty-nav-placeholder"></view>
    </view>

    <scroll-view scroll-y class="beauty-scroll">
      <view class="beauty-search-wrap">
        <view class="beauty-search-box">
          <input
            v-model.trim="keyword"
            class="beauty-search-input"
            placeholder="搜索美妆产品、妆容教程、用户种草内容"
            confirm-type="search"
            @input="handleInput"
            @confirm="handleSearch"
          />
          <view class="beauty-search-btn" @click="handleSearch">搜索</view>
        </view>
      </view>

      <scroll-view scroll-x class="beauty-category-scroll" show-scrollbar="false">
        <view class="beauty-category-row">
          <view
            v-for="item in beautyCategoryOptions"
            :key="item.value"
            :class="['beauty-category-chip', activeCategory === item.value ? 'active' : '']"
            @click="activeCategory = item.value"
          >
            {{ item.label }}
          </view>
        </view>
      </scroll-view>

      <scroll-view scroll-x class="beauty-filter-scroll" show-scrollbar="false">
        <view class="beauty-filter-row">
          <view
            v-for="item in priceRangeOptions"
            :key="item.value || 'all-price'"
            :class="['beauty-filter-chip', activePriceRange === item.value ? 'active' : '']"
            @click="selectPriceRange(item.value)"
          >
            {{ item.label }}
          </view>
        </view>
      </scroll-view>

      <scroll-view scroll-x class="beauty-filter-scroll filter-second" show-scrollbar="false">
        <view class="beauty-filter-row">
          <view
            v-for="item in skinTagOptions"
            :key="item.value || 'all-skin'"
            :class="['beauty-filter-chip', activeSkinTag === item.value ? 'active' : '']"
            @click="selectSkinTag(item.value)"
          >
            {{ item.label }}
          </view>
        </view>
      </scroll-view>

      <swiper class="beauty-banner" circular autoplay interval="3800" duration="500">
        <swiper-item v-for="item in beautyBannerList" :key="item.id">
          <view class="beauty-banner-card" :style="{ backgroundImage: `url(${item.image})` }">
            <view class="beauty-banner-mask">
              <text class="beauty-banner-title">{{ item.title }}</text>
              <text class="beauty-banner-desc">{{ item.subtitle }}</text>
            </view>
          </view>
        </swiper-item>
      </swiper>

      <view class="beauty-section-head">
        <view>
          <text class="beauty-section-title">平价种草清单</text>
          <text class="beauty-section-sub">双列展示学生高频回购的美妆与配套好物</text>
        </view>
      </view>

      <view v-if="goodsList.length" class="beauty-waterfall">
        <view v-for="item in goodsList" :key="item.id" class="beauty-card" @click="openDetail(item.id)">
          <image class="beauty-card-image" :src="item.imageUrl || item.image" mode="aspectFill"></image>
          <view class="beauty-card-body">
            <text class="beauty-card-title">{{ item.title }}</text>
            <text class="beauty-card-price">￥{{ item.price }}</text>
            <view class="beauty-tag-row">
              <text v-for="tag in item.skinTags" :key="tag" class="beauty-tag">{{ tag }}</text>
            </view>
            <text class="beauty-card-summary">{{ item.summary }}</text>
            <view class="beauty-card-foot">
              <view class="beauty-detail-btn" @click.stop="openDetail(item.id)">查看详情</view>
              <view class="beauty-report-btn" @click.stop="openReport(item)">举报</view>
            </view>
          </view>
        </view>
      </view>
      <view v-else-if="!loading" class="beauty-empty-card">
        <text class="beauty-empty-title">暂无匹配好物</text>
        <text class="beauty-empty-desc">换个关键词看看，或者自己发布一条平价好物种草。</text>
      </view>
      <view v-if="loading && !goodsList.length" class="beauty-list-status">
        <text class="beauty-list-status-text">好物内容加载中...</text>
      </view>
      <view v-else-if="goodsList.length" class="beauty-list-footer">
        <text v-if="loadingMore" class="beauty-list-status-text">正在加载更多好物...</text>
        <text v-else-if="finished" class="beauty-list-status-text">没有更多平价好物了</text>
        <text v-else class="beauty-list-status-text action" @click="handleLoadMore">点击加载更多</text>
      </view>

      <view class="beauty-section-head topic-head">
        <view>
          <text class="beauty-section-title">妆容专题</text>
          <text class="beauty-section-sub">结合学生高频场景，直接关联适配好物清单</text>
        </view>
      </view>

      <view v-if="filteredTopicList.length" class="beauty-topic-list">
        <view v-for="topic in filteredTopicList" :key="topic.id" class="beauty-topic-card">
          <view class="beauty-topic-header">
            <view>
              <text class="beauty-topic-title">{{ topic.title }}</text>
              <text class="beauty-topic-scene">{{ topic.scene }}</text>
            </view>
            <view class="beauty-topic-actions">
              <view class="beauty-topic-report" @click.stop="openTopicReport(topic)">举报</view>
              <view class="beauty-topic-link" @click.stop="openTopic(topic.id)">查看专题</view>
            </view>
          </view>
          <text class="beauty-topic-desc">{{ topic.desc }}</text>
          <view class="beauty-topic-goods">
            <view
              v-for="item in getTopicGoods(topic.id)"
              :key="`${topic.id}-${item.id}`"
              class="beauty-topic-good"
              @click="openDetail(item.id)"
            >
              <image class="beauty-topic-image" :src="item.imageUrl || item.image" mode="aspectFill"></image>
              <view class="beauty-topic-good-main">
                <text class="beauty-topic-good-title">{{ item.title }}</text>
                <text class="beauty-topic-good-price">￥{{ item.price }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      <view v-else class="beauty-topic-empty">
        <text class="beauty-empty-title">暂无匹配专题</text>
        <text class="beauty-empty-desc">可以试试搜索早八、通勤、约会或新手等关键词。</text>
      </view>
    </scroll-view>

    <BeautyTabBar current="home" @change="switchTab" />
  </view>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import BeautyTabBar from '../../components/BeautyTabBar.vue'
import {
  beautyBannerList,
  beautyCategoryOptions,
  beautyTopicList,
  getBeautyTopicGoods,
} from '../../utils/beautyMock'
import { listBeautyGoods } from '../../utils/beautyApi'

const keyword = ref('')
const activeCategory = ref('ALL')
const activePriceRange = ref('')
const activeSkinTag = ref('')
const goodsList = ref([])
const current = ref(1)
const size = 10
const total = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const finished = ref(false)

const priceRangeOptions = [
  { label: '全部价位', value: '' },
  { label: '50 元以下', value: 'UNDER_50' },
  { label: '50-100 元', value: 'BETWEEN_50_100' },
  { label: '100 元以上', value: 'OVER_100' },
]

const skinTagOptions = [
  { label: '全部肤质', value: '' },
  { label: '混油皮', value: '混油皮' },
  { label: '黄皮友好', value: '黄皮友好' },
  { label: '敏感肌', value: '敏感肌' },
  { label: '新手适用', value: '新手适用' },
  { label: '宿舍友好', value: '宿舍友好' },
]

const filteredTopicList = computed(() => {
  const search = keyword.value.trim().toLowerCase()
  if (!search) {
    return beautyTopicList
  }
  return beautyTopicList.filter((item) =>
    [item.title, item.scene, item.desc].join(' ').toLowerCase().includes(search)
  )
})

const syncListState = (records, reset) => {
  if (reset) {
    goodsList.value = records
  } else {
    goodsList.value = [...goodsList.value, ...records]
  }
  finished.value = goodsList.value.length >= total.value || records.length < size
}

const loadGoods = async ({ reset = false, silent = false } = {}) => {
  if (loading.value || loadingMore.value) {
    return
  }
  if (reset) {
    current.value = 1
    finished.value = false
    if (!silent) {
      loading.value = true
    }
  } else {
    if (finished.value) {
      return
    }
    loadingMore.value = true
  }
  try {
    const data = await listBeautyGoods({
      keyword: keyword.value,
      category: activeCategory.value === 'ALL' ? '' : activeCategory.value,
      priceRange: activePriceRange.value,
      skinTag: activeSkinTag.value,
      current: current.value,
      size,
    })
    total.value = Number(data?.total || 0)
    const records = Array.isArray(data?.records) ? data.records : []
    syncListState(records, reset)
    if (!finished.value) {
      current.value += 1
    }
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const handleSearch = async () => {
  await loadGoods({ reset: true })
  uni.showToast({
    title: total.value ? `找到 ${total.value} 条好物` : '暂无匹配内容',
    icon: 'none',
  })
}

const handleInput = () => {
  if (!keyword.value) {
    loadGoods({ reset: true, silent: true })
  }
}

const getTopicGoods = (topicId) => getBeautyTopicGoods(topicId).slice(0, 3)

watch([activeCategory, activePriceRange, activeSkinTag], () => {
  loadGoods({ reset: true })
})

onShow(() => {
  loadGoods({ reset: true, silent: goodsList.value.length > 0 })
})

onReachBottom(() => {
  handleLoadMore()
})

onPullDownRefresh(() => {
  handlePullRefresh()
})

const selectPriceRange = (value) => {
  activePriceRange.value = value
}

const selectSkinTag = (value) => {
  activeSkinTag.value = value
}

const handlePullRefresh = async () => {
  await loadGoods({ reset: true, silent: true })
  uni.stopPullDownRefresh()
}

const handleLoadMore = () => {
  loadGoods()
}

const goBack = () => {
  uni.switchTab({ url: '/pages/home/index' })
}

const switchTab = (tab) => {
  if (tab === 'publish') {
    openPublish()
    return
  }
  if (tab === 'mine') {
    openMine()
  }
}

const openPublish = () => {
  uni.navigateTo({ url: '/pages/beauty/publish' })
}

const openMine = () => {
  uni.navigateTo({ url: '/pages/beauty/mine' })
}

const openDetail = (id) => {
  uni.navigateTo({ url: `/pages/beauty/detail?id=${id}` })
}

const openTopic = (topicId) => {
  uni.navigateTo({ url: `/pages/beauty/detail?topicId=${topicId}` })
}

const openTopicReport = (topic) => {
  uni.navigateTo({
    url: `/pages/report/create?module=beauty&targetType=topic&title=${topic.title}`,
  })
}

const openReport = (item) => {
  uni.navigateTo({
    url: `/pages/report/create?module=beauty&targetType=${item.reportTargetType || 'good'}&targetId=${item.id}`,
  })
}

const handleBeautyRefresh = () => {
  loadGoods({ reset: true, silent: true })
}

uni.$off('beauty-refresh', handleBeautyRefresh)
uni.$on('beauty-refresh', handleBeautyRefresh)

onBeforeUnmount(() => {
  uni.$off('beauty-refresh', handleBeautyRefresh)
})
</script>

<style scoped lang="scss">
.beauty-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top right, rgba(244, 114, 182, 0.18), transparent 22%),
    linear-gradient(180deg, #fff7fb 0%, #f7f8fc 24%, #f4f7fb 100%);
  padding-bottom: calc(184rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.beauty-navbar {
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

.beauty-scroll {
  height: calc(100vh - 112rpx - env(safe-area-inset-top) - 168rpx - env(safe-area-inset-bottom));
}

.beauty-search-wrap {
  padding: 24rpx 24rpx 14rpx;
}

.beauty-search-box {
  display: grid;
  grid-template-columns: 1fr 112rpx;
  gap: 14rpx;
  min-height: 88rpx;
  padding: 14rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 16rpx 40rpx rgba(236, 72, 153, 0.08);
}

.beauty-search-input {
  height: 78rpx;
  padding: 0 22rpx;
  border-radius: 20rpx;
  background: #fff4f8;
}

.beauty-search-btn {
  height: 78rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #ec4899, #fb7185);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 700;
}

.beauty-category-scroll {
  white-space: nowrap;
  padding: 0 24rpx 16rpx;
  box-sizing: border-box;
}

.beauty-filter-scroll {
  white-space: nowrap;
  padding: 0 24rpx 12rpx;
  box-sizing: border-box;
}

.beauty-filter-scroll.filter-second {
  padding-bottom: 22rpx;
}

.beauty-category-row {
  display: inline-flex;
  gap: 14rpx;
}

.beauty-filter-row {
  display: inline-flex;
  gap: 12rpx;
}

.beauty-category-chip {
  padding: 14rpx 24rpx;
  border-radius: 999rpx;
  background: #ffe8f3;
  color: #9d174d;
  font-size: 23rpx;
}

.beauty-category-chip.active {
  background: linear-gradient(135deg, #ec4899, #fb7185);
  color: #ffffff;
  box-shadow: 0 12rpx 24rpx rgba(236, 72, 153, 0.2);
}

.beauty-filter-chip {
  padding: 12rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.92);
  color: #6b7280;
  font-size: 22rpx;
  border: 1px solid rgba(244, 114, 182, 0.08);
}

.beauty-filter-chip.active {
  background: linear-gradient(135deg, #fb7185, #ec4899);
  color: #ffffff;
  border-color: transparent;
  box-shadow: 0 12rpx 24rpx rgba(236, 72, 153, 0.16);
}

.beauty-banner {
  height: 300rpx;
  margin: 0 24rpx 28rpx;
  border-radius: 28rpx;
  overflow: hidden;
}

.beauty-banner-card {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
}

.beauty-banner-mask {
  width: 100%;
  height: 100%;
  padding: 32rpx 30rpx;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.06), rgba(15, 23, 42, 0.64));
}

.beauty-banner-title,
.beauty-banner-desc,
.beauty-section-title,
.beauty-card-title,
.beauty-card-price,
.beauty-topic-title,
.beauty-topic-good-title,
.beauty-topic-good-price {
  display: block;
}

.beauty-banner-title {
  font-size: 34rpx;
  font-weight: 800;
  color: #ffffff;
}

.beauty-banner-desc {
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.86);
}

.beauty-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
  margin-bottom: 18rpx;
}

.topic-head {
  margin-top: 10rpx;
}

.beauty-section-title {
  font-size: 32rpx;
  font-weight: 800;
  color: #111827;
}

.beauty-section-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #64748b;
}

.beauty-waterfall {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
  padding: 0 24rpx;
}

.beauty-list-status,
.beauty-list-footer {
  padding: 22rpx 24rpx 8rpx;
  text-align: center;
}

.beauty-list-status-text {
  display: block;
  font-size: 23rpx;
  color: #94a3b8;
}

.beauty-list-status-text.action {
  color: #ec4899;
  font-weight: 700;
}

.beauty-card {
  overflow: hidden;
  border-radius: 26rpx;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18rpx 44rpx rgba(236, 72, 153, 0.08);
}

.beauty-card-image {
  width: 100%;
  height: 240rpx;
  background: #fbe7f1;
}

.beauty-card-body {
  padding: 20rpx 18rpx 18rpx;
}

.beauty-card-title {
  font-size: 26rpx;
  font-weight: 800;
  color: #111827;
  line-height: 1.45;
}

.beauty-card-price {
  margin-top: 10rpx;
  font-size: 28rpx;
  font-weight: 800;
  color: #e11d48;
}

.beauty-tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 12rpx;
}

.beauty-tag {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #fff1f6;
  color: #be185d;
  font-size: 20rpx;
}

.beauty-card-summary {
  display: block;
  margin-top: 12rpx;
  font-size: 22rpx;
  line-height: 1.7;
  color: #64748b;
}

.beauty-card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
  margin-top: 18rpx;
}

.beauty-detail-btn,
.beauty-report-btn,
.beauty-topic-link {
  min-height: 62rpx;
  padding: 0 18rpx;
  border-radius: 18rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 21rpx;
  font-weight: 700;
}

.beauty-detail-btn {
  background: linear-gradient(135deg, #ec4899, #fb7185);
  color: #ffffff;
}

.beauty-report-btn {
  background: #fff1f2;
  color: #e11d48;
}

.beauty-topic-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  padding: 0 24rpx 36rpx;
}

.beauty-topic-card {
  padding: 24rpx 22rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 16rpx 38rpx rgba(236, 72, 153, 0.08);
}

.beauty-topic-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
}

.beauty-topic-actions {
  display: flex;
  align-items: center;
  gap: 10rpx;
  flex-shrink: 0;
}

.beauty-topic-title {
  font-size: 28rpx;
  font-weight: 800;
  color: #111827;
}

.beauty-topic-scene {
  display: inline-flex;
  margin-top: 10rpx;
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #ffe4ee;
  color: #be185d;
  font-size: 20rpx;
}

.beauty-topic-link {
  flex-shrink: 0;
  background: #fff0f7;
  color: #db2777;
}

.beauty-topic-report {
  min-height: 62rpx;
  padding: 0 18rpx;
  border-radius: 18rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 21rpx;
  font-weight: 700;
  background: #fff1f2;
  color: #e11d48;
}

.beauty-topic-desc {
  display: block;
  margin-top: 14rpx;
  font-size: 23rpx;
  line-height: 1.75;
  color: #64748b;
}

.beauty-topic-goods {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  margin-top: 18rpx;
}

.beauty-topic-empty {
  padding: 0 24rpx 36rpx;
}

.beauty-topic-good {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding: 14rpx;
  border-radius: 22rpx;
  background: #fff7fb;
}

.beauty-topic-image {
  width: 96rpx;
  height: 96rpx;
  border-radius: 20rpx;
  flex-shrink: 0;
}

.beauty-topic-good-main {
  flex: 1;
  min-width: 0;
}

.beauty-topic-good-title {
  font-size: 24rpx;
  font-weight: 700;
  color: #111827;
  line-height: 1.5;
}

.beauty-topic-good-price {
  margin-top: 8rpx;
  font-size: 24rpx;
  font-weight: 800;
  color: #e11d48;
}

.beauty-empty-card {
  margin: 0 24rpx 30rpx;
  padding: 44rpx 30rpx;
  border-radius: 30rpx;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 18rpx 44rpx rgba(236, 72, 153, 0.08);
  text-align: center;
}

.beauty-empty-title,
.beauty-empty-desc {
  display: block;
}

.beauty-empty-title {
  font-size: 30rpx;
  font-weight: 800;
  color: #111827;
}

.beauty-empty-desc {
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: #64748b;
}
</style>
