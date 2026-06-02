<template>
  <view class="page-shell errand-favorites-page">
    <view class="card favorites-summary">
      <text class="summary-title">我的收藏</text>
      <text class="summary-desc">统一收纳收藏的优质订单和常用发单参考，支持回看与一键复用。</text>
      <view class="summary-grid">
        <view class="summary-item">
          <text class="summary-label">收藏数量</text>
          <text class="summary-value">{{ favorites.length }}</text>
        </view>
        <view class="summary-item">
          <text class="summary-label">最近收藏</text>
          <text class="summary-value summary-value--small">{{ latestSavedAt }}</text>
        </view>
      </view>
      <view v-if="favorites.length" class="summary-actions">
        <view class="ghost-btn summary-btn" @click="clearFavorites">清空收藏</view>
      </view>
    </view>

    <view v-if="favorites.length" class="favorites-list">
      <view v-for="item in favorites" :key="`${item.id}-${item.savedAt}`" class="card favorite-card">
        <view class="favorite-head">
          <view>
            <text class="favorite-type">{{ serviceTypeMap[item.serviceType] || item.serviceType || '跑腿订单' }}</text>
            <text class="favorite-time">收藏时间：{{ formatTime(item.savedAt) }}</text>
          </view>
          <text class="favorite-fee">￥{{ formatAmount(item.totalFee || item.baseFee) }}</text>
        </view>

        <text class="favorite-line">订单号：{{ item.orderNo || '-' }}</text>
        <text class="favorite-line">取件地址：{{ item.pickupAddress || '-' }}</text>
        <text class="favorite-line">派送地址：{{ item.deliveryAddress || '-' }}</text>
        <text class="favorite-line">预约时间：{{ item.pickupTimeText || '-' }}</text>
        <text class="favorite-line">需求说明：{{ item.detailContent || item.remark || '暂无补充说明' }}</text>

        <view class="favorite-tags">
          <text v-if="item.urgentFlag" class="favorite-tag favorite-tag--urgent">加急</text>
          <text v-if="item.fragileFlag" class="favorite-tag favorite-tag--fragile">易碎</text>
        </view>

        <view class="favorite-actions">
          <view class="favorite-action" @click="viewOrder(item)">查看详情</view>
          <view class="favorite-action primary" @click="recreateOrder(item)">再次下单</view>
          <view class="favorite-action danger" @click="removeFavorite(item)">取消收藏</view>
        </view>
      </view>
    </view>

    <view v-else class="card favorites-empty">
      <text class="empty-title">暂无收藏内容</text>
      <text class="empty-desc">你在浏览记录里收藏过的订单会出现在这里，方便后续再次下单或回看。</text>
      <view class="primary-btn empty-btn" @click="goSquare">去订单广场看看</view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const favorites = ref([])

const serviceTypeMap = {
  PICKUP: '取件',
  DELIVERY: '送件',
  PURCHASE: '代购',
  PRINT: '代打印',
}

const favoriteKey = computed(() => `errand-favorites-${store.profile?.userId || 'guest'}`)

const readJsonList = (key) => {
  const raw = uni.getStorageSync(key) || '[]'
  try {
    return JSON.parse(raw)
  } catch (error) {
    return []
  }
}

const loadFavorites = async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  favorites.value = readJsonList(favoriteKey.value)
}

const formatTime = (value) => {
  if (!value) {
    return '-'
  }
  return value.replace('T', ' ')
}

const formatAmount = (value) => {
  const amount = Number(value || 0)
  return Number.isNaN(amount) ? '0.00' : amount.toFixed(2)
}

const latestSavedAt = computed(() => (favorites.value[0]?.savedAt ? formatTime(favorites.value[0].savedAt) : '暂无'))

const buildCreateUrl = (item) => {
  const query = [
    `serviceType=${item.serviceType || 'PICKUP'}`,
    `pickupAddress=${encodeURIComponent(item.pickupAddress || '')}`,
    `deliveryAddress=${encodeURIComponent(item.deliveryAddress || '')}`,
    `pickupTimeText=${encodeURIComponent(item.pickupTimeText || '')}`,
    `detailContent=${encodeURIComponent(item.detailContent || '')}`,
    `remark=${encodeURIComponent(item.remark || '')}`,
    `baseFee=${item.baseFee || item.totalFee || ''}`,
    `urgentFlag=${item.urgentFlag ? 1 : 0}`,
    `fragileFlag=${item.fragileFlag ? 1 : 0}`,
  ].join('&')
  return `/pages/errand/index?tab=create&${query}`
}

const recreateOrder = (item) => {
  uni.redirectTo({ url: buildCreateUrl(item) })
}

const viewOrder = (item) => {
  if (!item.id) {
    recreateOrder(item)
    return
  }
  uni.navigateTo({ url: `/pages/errand/detail?id=${item.id}` })
}

const persistFavorites = () => {
  uni.setStorageSync(favoriteKey.value, JSON.stringify(favorites.value))
}

const removeFavorite = (item) => {
  favorites.value = favorites.value.filter((record) => !(record.id === item.id && record.savedAt === item.savedAt))
  persistFavorites()
  uni.showToast({ title: '已取消收藏', icon: 'none' })
}

const clearFavorites = () => {
  uni.showModal({
    title: '清空收藏',
    content: '确认清空当前账号的全部收藏内容吗？',
    success: (result) => {
      if (!result.confirm) {
        return
      }
      favorites.value = []
      persistFavorites()
      uni.showToast({ title: '已清空', icon: 'success' })
    },
  })
}

const goSquare = () => {
  uni.redirectTo({ url: '/pages/errand/index?tab=square' })
}

onShow(loadFavorites)
</script>

<style scoped lang="scss">
.favorites-summary {
  padding: 28rpx 26rpx;
}

.summary-title {
  display: block;
  font-size: 36rpx;
  font-weight: 800;
  color: #0f172a;
}

.summary-desc {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: #64748b;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18rpx;
  margin-top: 24rpx;
}

.summary-item {
  padding: 22rpx;
  border-radius: 22rpx;
  background: #f8fafc;
}

.summary-label {
  display: block;
  font-size: 22rpx;
  color: #64748b;
}

.summary-value {
  display: block;
  margin-top: 10rpx;
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
}

.summary-value--small {
  font-size: 24rpx;
  line-height: 1.6;
}

.summary-actions {
  margin-top: 24rpx;
}

.summary-btn {
  height: 82rpx;
  line-height: 82rpx;
}

.favorites-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 22rpx;
}

.favorite-card {
  padding: 26rpx 24rpx;
}

.favorite-head {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
}

.favorite-type {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #0f172a;
}

.favorite-time,
.favorite-line {
  display: block;
  margin-top: 10rpx;
  font-size: 23rpx;
  line-height: 1.7;
  color: #64748b;
}

.favorite-fee {
  color: #dc2626;
  font-size: 34rpx;
  font-weight: 800;
  flex-shrink: 0;
}

.favorite-tags {
  display: flex;
  gap: 10rpx;
  margin-top: 16rpx;
}

.favorite-tag {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
}

.favorite-tag--urgent {
  background: rgba(239, 68, 68, 0.12);
  color: #ef4444;
}

.favorite-tag--fragile {
  background: rgba(245, 158, 11, 0.12);
  color: #d97706;
}

.favorite-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 20rpx;
}

.favorite-action {
  padding: 12rpx 20rpx;
  border-radius: 999rpx;
  background: #eef4ff;
  color: #2d7ff9;
  font-size: 22rpx;
}

.favorite-action.primary {
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
}

.favorite-action.danger {
  background: #fef2f2;
  color: #ef4444;
}

.favorites-empty {
  margin-top: 22rpx;
  padding: 56rpx 30rpx;
  text-align: center;
}

.empty-title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
}

.empty-desc {
  display: block;
  margin-top: 14rpx;
  font-size: 24rpx;
  line-height: 1.8;
  color: #64748b;
}

.empty-btn {
  margin-top: 28rpx;
}
</style>
