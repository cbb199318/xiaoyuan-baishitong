<template>
  <view :class="[embedded ? 'embedded-page' : 'page-shell']">
    <view class="search-panel">
      <view class="search-box">
        <input
          v-model.trim="keyword"
          class="search-input"
          confirm-type="search"
          placeholder="搜索服务类型、取件地址、派送地址、需求备注"
          @confirm="handleSearch"
          @input="handleKeywordInput"
        />
        <text v-if="keyword" class="search-clear" @click="clearKeyword">清空</text>
        <view class="search-button" @click="handleSearch">搜索</view>
      </view>
    </view>

    <view class="square-body">
      <view v-if="orders.length" class="order-list">
        <view
          v-for="item in orders"
          :key="item.id"
          class="order-card"
          @click="openDetail(item.id)"
        >
          <view class="card-top">
            <text :class="['service-tag', `service-${(item.serviceType || '').toLowerCase()}`]">
              {{ serviceTypeMap[item.serviceType] || item.serviceType || '跑腿' }}
            </text>
            <view class="price-box">
              <text class="price-symbol">￥</text>
              <text class="price-value">{{ formatAmount(item.totalFee) }}</text>
            </view>
          </view>

          <view class="address-panel">
            <view class="address-row">
              <text class="address-label pickup">取</text>
              <text class="address-text">{{ trimText(item.pickupAddress) }}</text>
            </view>
            <view class="address-row">
              <text class="address-label delivery">送</text>
              <text class="address-text">{{ trimText(item.deliveryAddress) }}</text>
            </view>
          </view>

          <text v-if="item.remark || item.detailContent" class="remark-line">
            {{ trimText(item.remark || item.detailContent, 36) }}
          </text>

          <view class="meta-row">
            <view class="tag-row">
              <text v-if="item.urgentFlag" class="meta-tag urgent">加急</text>
              <text v-if="item.fragileFlag" class="meta-tag fragile">易碎</text>
            </view>
            <text class="publisher-text">发布人：{{ maskName(item.publisher?.nickname) }}</text>
          </view>

          <view class="card-bottom">
            <view class="deadline-box">
              <text class="deadline-label">剩余接单时间</text>
              <text :class="['deadline-value', isExpired(item) ? 'expired' : '']">
                {{ formatRemaining(item.acceptDeadline) }}
              </text>
            </view>
            <view
              :class="['accept-btn', canAcceptOrder(item) ? 'active' : 'disabled']"
              @click.stop="acceptOrder(item)"
            >
              {{ canAcceptOrder(item) ? '立即接单' : '不可接单' }}
            </view>
          </view>
        </view>
      </view>

      <view v-else-if="!loading" class="empty-state">
        <view class="empty-illustration"></view>
        <text class="empty-title">暂无跑腿订单，稍后再来查看</text>
        <text class="empty-desc">可以下拉刷新，或试试更换搜索关键词。</text>
      </view>

      <view v-if="loading && !orders.length" class="loading-state">订单加载中...</view>
      <view v-else-if="orders.length" class="list-footer">
        <text v-if="loadingMore" class="footer-text">正在加载更多订单...</text>
        <text v-else-if="finished" class="footer-text">没有更多公开订单了</text>
        <text v-else class="footer-text load-more-action" @click="handleLoadMore">点击加载更多历史订单</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onBeforeUnmount, ref } from 'vue'
import { onReachBottom, onShow } from '@dcloudio/uni-app'
import request from '../../utils/request'

defineProps({
  embedded: {
    type: Boolean,
    default: false,
  },
})

const serviceTypeMap = {
  PICKUP: '取件',
  DELIVERY: '送件',
  PURCHASE: '代购',
  PRINT: '代打印',
}

const keyword = ref('')
const orders = ref([])
const current = ref(1)
const size = 10
const total = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const finished = ref(false)
const timer = ref(null)
const now = ref(Date.now())

const startTicker = () => {
  if (timer.value) {
    return
  }
  timer.value = setInterval(() => {
    now.value = Date.now()
  }, 1000)
}

const stopTicker = () => {
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
  }
}

const parseTime = (value) => {
  if (!value) {
    return 0
  }
  const safeValue = value.includes('T') ? value : value.replace(' ', 'T')
  return new Date(safeValue).getTime()
}

const isExpired = (item) => parseTime(item.acceptDeadline) <= now.value

const isOrderActive = (item) =>
  item.status === 'PUBLISHED' && item.publicVisible && !isExpired(item)

const canAcceptOrder = (item) => isOrderActive(item) && !!item.canAccept

const formatRemaining = (value) => {
  const remain = parseTime(value) - now.value
  if (remain <= 0) {
    return '已失效'
  }
  const totalSeconds = Math.floor(remain / 1000)
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}

const trimText = (value, max = 18) => {
  if (!value) {
    return '-'
  }
  return value.length > max ? `${value.slice(0, max)}...` : value
}

const maskName = (value) => {
  if (!value) {
    return '匿名用户'
  }
  if (value.length === 1) {
    return `${value}**`
  }
  return `${value[0]}${'*'.repeat(Math.min(2, value.length - 1))}`
}

const formatAmount = (value) => {
  if (value === null || value === undefined || value === '') {
    return '0.00'
  }
  const amount = Number(value)
  return Number.isNaN(amount) ? String(value) : amount.toFixed(2)
}

const syncListState = (records, reset) => {
  if (reset) {
    orders.value = records
  } else {
    orders.value = [...orders.value, ...records]
  }
  finished.value = orders.value.length >= total.value || records.length < size
}

const loadOrders = async ({ reset = false, silent = false } = {}) => {
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
    const data = await request.get('/errand/orders/square', {
      params: {
        keyword: keyword.value,
        current: current.value,
        size,
      },
    })
    total.value = Number(data.total || 0)
    const records = Array.isArray(data.records) ? data.records : []
    syncListState(records, reset)
    if (!finished.value) {
      current.value += 1
    }
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const handleSearch = () => {
  loadOrders({ reset: true })
}

const handleKeywordInput = () => {
  if (!keyword.value) {
    loadOrders({ reset: true, silent: true })
  }
}

const clearKeyword = () => {
  keyword.value = ''
  loadOrders({ reset: true })
}

const handlePullRefresh = async () => {
  await loadOrders({ reset: true, silent: true })
  uni.stopPullDownRefresh()
}

const handleLoadMore = () => {
  loadOrders()
}

const acceptOrder = async (item) => {
  if (!canAcceptOrder(item)) {
    uni.showToast({ title: '当前订单不可接单', icon: 'none' })
    return
  }
  await request.post(`/errand/orders/${item.id}/accept`)
  uni.showToast({ title: '接单成功', icon: 'success' })
  await loadOrders({ reset: true, silent: true })
}

const openDetail = (id) => {
  uni.navigateTo({ url: `/pages/errand/detail?id=${id}` })
}

const handleShellRefresh = () => {
  handlePullRefresh()
}

onShow(() => {
  startTicker()
  loadOrders({ reset: true })
})

onReachBottom(() => {
  handleLoadMore()
})

onBeforeUnmount(() => {
  stopTicker()
  uni.$off('errand-square-refresh', handleShellRefresh)
})

uni.$off('errand-square-refresh', handleShellRefresh)
uni.$on('errand-square-refresh', handleShellRefresh)
</script>

<style scoped lang="scss">
.embedded-page {
  min-height: 100%;
  padding: 0 24rpx 24rpx;
  box-sizing: border-box;
}

.search-panel {
  position: sticky;
  top: 0;
  z-index: 5;
  padding: 20rpx 0 18rpx;
  background: #f4f7fb;
}

.search-box {
  display: flex;
  align-items: center;
  min-height: 88rpx;
  padding: 0 14rpx 0 24rpx;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 28rpx rgba(15, 23, 42, 0.06);
}

.search-input {
  flex: 1;
  font-size: 26rpx;
  color: #0f172a;
}

.search-clear {
  margin-left: 12rpx;
  color: #64748b;
  font-size: 24rpx;
}

.search-button {
  margin-left: 14rpx;
  min-width: 120rpx;
  height: 64rpx;
  line-height: 64rpx;
  padding: 0 24rpx;
  border-radius: 16rpx;
  text-align: center;
  font-size: 24rpx;
  font-weight: 600;
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
}

.square-body {
  min-height: 400rpx;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  padding-bottom: 28rpx;
}

.order-card {
  padding: 24rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.06);
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.service-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 116rpx;
  height: 52rpx;
  padding: 0 20rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 700;
}

.service-pickup {
  color: #2563eb;
  background: rgba(37, 99, 235, 0.12);
}

.service-delivery {
  color: #059669;
  background: rgba(5, 150, 105, 0.12);
}

.service-purchase {
  color: #d97706;
  background: rgba(217, 119, 6, 0.14);
}

.service-print {
  color: #7c3aed;
  background: rgba(124, 58, 237, 0.14);
}

.price-box {
  display: flex;
  align-items: flex-end;
  color: #dc2626;
}

.price-symbol {
  font-size: 26rpx;
  font-weight: 700;
  margin-right: 4rpx;
}

.price-value {
  font-size: 40rpx;
  font-weight: 800;
  line-height: 1;
}

.address-panel {
  margin-top: 20rpx;
  padding: 18rpx 20rpx;
  border-radius: 20rpx;
  background: #f8fafc;
}

.address-row {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
}

.address-row + .address-row {
  margin-top: 12rpx;
}

.address-label {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40rpx;
  height: 40rpx;
  border-radius: 12rpx;
  color: #ffffff;
  font-size: 22rpx;
  font-weight: 700;
  flex-shrink: 0;
}

.pickup {
  background: #2d7ff9;
}

.delivery {
  background: #0f766e;
}

.address-text {
  flex: 1;
  color: #334155;
  font-size: 26rpx;
  line-height: 1.6;
}

.remark-line {
  display: block;
  margin-top: 16rpx;
  color: #64748b;
  font-size: 24rpx;
  line-height: 1.7;
}

.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-top: 18rpx;
}

.tag-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
  flex-wrap: wrap;
}

.meta-tag {
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 600;
}

.urgent {
  color: #ea580c;
  background: rgba(234, 88, 12, 0.12);
}

.fragile {
  color: #0284c7;
  background: rgba(2, 132, 199, 0.12);
}

.publisher-text {
  color: #475569;
  font-size: 24rpx;
  text-align: right;
}

.card-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  margin-top: 22rpx;
  padding-top: 20rpx;
  border-top: 1px solid #edf2f7;
}

.deadline-box {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.deadline-label {
  color: #64748b;
  font-size: 22rpx;
}

.deadline-value {
  color: #0f766e;
  font-size: 28rpx;
  font-weight: 700;
}

.deadline-value.expired {
  color: #94a3b8;
}

.accept-btn {
  min-width: 180rpx;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 18rpx;
  text-align: center;
  font-size: 26rpx;
  font-weight: 700;
}

.accept-btn.active {
  background: #2d7ff9;
  color: #ffffff;
}

.accept-btn.disabled {
  background: #e2e8f0;
  color: #94a3b8;
}

.loading-state,
.list-footer {
  padding: 28rpx 0 40rpx;
  text-align: center;
}

.footer-text,
.loading-state {
  color: #94a3b8;
  font-size: 24rpx;
}

.load-more-action {
  color: #2d7ff9;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 720rpx;
  padding: 40rpx 24rpx 80rpx;
  text-align: center;
}

.empty-illustration {
  width: 220rpx;
  height: 220rpx;
  border-radius: 50%;
  background:
    radial-gradient(circle at 50% 38%, rgba(45, 127, 249, 0.2), transparent 52%),
    linear-gradient(180deg, #dbeafe 0%, #eff6ff 100%);
  position: relative;
  margin-bottom: 28rpx;
}

.empty-illustration::before,
.empty-illustration::after {
  content: '';
  position: absolute;
  background: #93c5fd;
  border-radius: 999rpx;
}

.empty-illustration::before {
  width: 96rpx;
  height: 16rpx;
  left: 62rpx;
  top: 84rpx;
}

.empty-illustration::after {
  width: 72rpx;
  height: 16rpx;
  left: 74rpx;
  top: 116rpx;
}

.empty-title {
  color: #1e293b;
  font-size: 28rpx;
  font-weight: 700;
}

.empty-desc {
  margin-top: 12rpx;
  color: #94a3b8;
  font-size: 24rpx;
  line-height: 1.7;
}
</style>
