<template>
  <view class="page-shell errand-income-page">
    <view class="card income-overview">
      <text class="overview-title">收支明细</text>
      <text class="overview-desc">按已完成订单统计跑腿支出与收入，帮助你快速回看个人履约记录。</text>
      <view class="overview-grid">
        <view class="overview-item">
          <text class="overview-label">总支出</text>
          <text class="overview-value expense">￥{{ totalSpend.toFixed(2) }}</text>
        </view>
        <view class="overview-item">
          <text class="overview-label">总收入</text>
          <text class="overview-value income">￥{{ totalIncome.toFixed(2) }}</text>
        </view>
        <view class="overview-item">
          <text class="overview-label">已完成发单</text>
          <text class="overview-value">{{ completedPublished.length }}</text>
        </view>
        <view class="overview-item">
          <text class="overview-label">已完成接单</text>
          <text class="overview-value">{{ completedAccepted.length }}</text>
        </view>
      </view>
    </view>

    <view v-if="records.length" class="income-list">
      <view v-for="item in records" :key="`${item.type}-${item.id}`" class="card income-card" @click="openDetail(item.id)">
        <view class="income-head">
          <view>
            <text class="income-type">{{ item.type === 'expense' ? '支出订单' : '收入订单' }}</text>
            <text class="income-time">{{ formatTime(item.time) }}</text>
          </view>
          <text :class="['income-amount', item.type]">{{ item.type === 'expense' ? '-' : '+' }}￥{{ formatAmount(item.totalFee) }}</text>
        </view>
        <text class="income-line">订单号：{{ item.orderNo || '-' }}</text>
        <text class="income-line">服务类型：{{ serviceTypeMap[item.serviceType] || item.serviceType }}</text>
        <text class="income-line">{{ item.pickupAddress || '-' }} -> {{ item.deliveryAddress || '-' }}</text>
      </view>
    </view>

    <view v-else class="card income-empty">
      <text class="empty-title">暂无收支记录</text>
      <text class="empty-desc">当前还没有已完成的跑腿订单，完成履约后会自动汇总到这里。</text>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import request from '../../utils/request'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const publishedList = ref([])
const acceptedList = ref([])

const serviceTypeMap = {
  PICKUP: '取件',
  DELIVERY: '送件',
  PURCHASE: '代购',
  PRINT: '代打印',
}

const loadData = async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  publishedList.value = await request.get('/errand/orders/my/published')
  acceptedList.value = await request.get('/errand/orders/my/accepted')
}

const completedPublished = computed(() => publishedList.value.filter((item) => item.status === 'COMPLETED'))
const completedAccepted = computed(() => acceptedList.value.filter((item) => item.status === 'COMPLETED'))

const totalSpend = computed(() => completedPublished.value.reduce((sum, item) => sum + Number(item.totalFee || 0), 0))
const totalIncome = computed(() => completedAccepted.value.reduce((sum, item) => sum + Number(item.totalFee || 0), 0))

const records = computed(() =>
  [
    ...completedPublished.value.map((item) => ({ ...item, type: 'expense', time: item.completedAt || item.updatedAt || item.createdAt })),
    ...completedAccepted.value.map((item) => ({ ...item, type: 'income', time: item.completedAt || item.updatedAt || item.createdAt })),
  ].sort((a, b) => new Date(b.time || 0).getTime() - new Date(a.time || 0).getTime()))

const formatAmount = (value) => {
  const amount = Number(value || 0)
  return Number.isNaN(amount) ? '0.00' : amount.toFixed(2)
}

const formatTime = (value) => {
  if (!value) {
    return '-'
  }
  return value.replace('T', ' ')
}

const openDetail = (id) => {
  uni.navigateTo({ url: `/pages/errand/detail?id=${id}` })
}

onShow(loadData)
</script>

<style scoped lang="scss">
.income-overview {
  padding: 28rpx 26rpx;
}

.overview-title {
  display: block;
  font-size: 36rpx;
  font-weight: 800;
  color: #0f172a;
}

.overview-desc {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: #64748b;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18rpx;
  margin-top: 24rpx;
}

.overview-item {
  padding: 24rpx 22rpx;
  border-radius: 22rpx;
  background: #f8fafc;
}

.overview-label {
  display: block;
  font-size: 22rpx;
  color: #64748b;
}

.overview-value {
  display: block;
  margin-top: 10rpx;
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
}

.overview-value.expense {
  color: #dc2626;
}

.overview-value.income {
  color: #059669;
}

.income-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 22rpx;
}

.income-card {
  padding: 26rpx 24rpx;
}

.income-head {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
}

.income-type {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #0f172a;
}

.income-time,
.income-line {
  display: block;
  margin-top: 10rpx;
  font-size: 23rpx;
  line-height: 1.7;
  color: #64748b;
}

.income-amount {
  flex-shrink: 0;
  font-size: 34rpx;
  font-weight: 800;
}

.income-amount.expense {
  color: #dc2626;
}

.income-amount.income {
  color: #059669;
}

.income-empty {
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
</style>
