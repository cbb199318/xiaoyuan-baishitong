<template>
  <view class="page-shell errand-evaluation-page">
    <view class="card evaluation-summary">
      <text class="summary-title">我的评价</text>
      <text class="summary-desc">已完成订单会自动进入待评价队列，提交后可在这里查看我发出的评价和我收到的评价。</text>
      <view class="summary-grid">
        <view class="summary-item">
          <text class="summary-label">待评价</text>
          <text class="summary-value">{{ pendingList.length }}</text>
        </view>
        <view class="summary-item">
          <text class="summary-label">我发出的评价</text>
          <text class="summary-value">{{ sentList.length }}</text>
        </view>
        <view class="summary-item">
          <text class="summary-label">我收到的评价</text>
          <text class="summary-value">{{ receivedList.length }}</text>
        </view>
        <view class="summary-item">
          <text class="summary-label">平均评分</text>
          <text class="summary-value">{{ averageScore }}</text>
        </view>
      </view>
    </view>

    <view class="card evaluation-tabs">
      <view class="tab-row">
        <view :class="['tab-item', activeTab === 'pending' ? 'active' : '']" @click="activeTab = 'pending'">待评价</view>
        <view :class="['tab-item', activeTab === 'sent' ? 'active' : '']" @click="activeTab = 'sent'">我发出的</view>
        <view :class="['tab-item', activeTab === 'received' ? 'active' : '']" @click="activeTab = 'received'">我收到的</view>
      </view>

      <view v-if="activeTab !== 'pending'" class="filter-row">
        <view
          v-for="item in starFilters"
          :key="item.value"
          :class="['filter-chip', starFilter === item.value ? 'active' : '']"
          @click="starFilter = item.value"
        >
          {{ item.label }}
        </view>
      </view>

      <view v-if="activeTab === 'pending' && pendingList.length" class="record-list">
        <view v-for="item in pendingList" :key="item.id" class="record-card">
          <view class="record-head">
            <view>
              <text class="record-title">{{ serviceTypeMap[item.serviceType] || item.serviceType }} · 待评价</text>
              <text class="record-meta">订单号：{{ item.orderNo || '-' }}</text>
            </view>
            <text class="record-score wait">待处理</text>
          </view>
          <text class="record-line">对方用户：{{ item.counterpartyName }}</text>
          <text class="record-line">{{ item.pickupAddress || '-' }} -> {{ item.deliveryAddress || '-' }}</text>
          <text class="record-line">完成时间：{{ formatTime(item.completedAt || item.updatedAt || item.createdAt) }}</text>
          <view class="record-actions">
            <view class="record-action" @click="openDetail(item.id)">查看订单</view>
            <view class="record-action primary" @click="openEvaluate(item.id)">去评价</view>
          </view>
        </view>
      </view>

      <view v-else-if="activeTab === 'sent' && filteredSentList.length" class="record-list">
        <view v-for="item in filteredSentList" :key="`${item.orderId}-${item.fromUserId}`" class="record-card">
          <view class="record-head">
            <view>
              <text class="record-title">{{ serviceTypeMap[item.serviceType] || item.serviceType }} · 我发出的评价</text>
              <text class="record-meta">给 {{ item.toUserName || '对方用户' }}</text>
            </view>
            <text class="record-score">★ {{ item.rating }}</text>
          </view>
          <text class="record-line">订单号：{{ item.orderNo || '-' }}</text>
          <text class="record-line">{{ item.pickupAddress || '-' }} -> {{ item.deliveryAddress || '-' }}</text>
          <text v-if="item.content" class="record-line">评价内容：{{ item.content }}</text>
          <view v-if="item.tags?.length" class="tag-row">
            <text v-for="tag in item.tags" :key="tag" class="record-tag">{{ tag }}</text>
          </view>
          <text class="record-line">评价时间：{{ formatTime(item.createdAt) }}</text>
          <view class="record-actions">
            <view class="record-action" @click="openDetail(item.orderId)">查看订单</view>
            <view class="record-action primary" @click="openEvaluate(item.orderId)">修改评价</view>
          </view>
        </view>
      </view>

      <view v-else-if="activeTab === 'received' && filteredReceivedList.length" class="record-list">
        <view v-for="item in filteredReceivedList" :key="`${item.orderId}-${item.fromUserId}`" class="record-card">
          <view class="record-head">
            <view>
              <text class="record-title">{{ serviceTypeMap[item.serviceType] || item.serviceType }} · 我收到的评价</text>
              <text class="record-meta">来自 {{ item.fromUserName || '对方用户' }}</text>
            </view>
            <text class="record-score">★ {{ item.rating }}</text>
          </view>
          <text class="record-line">订单号：{{ item.orderNo || '-' }}</text>
          <text v-if="item.content" class="record-line">评价内容：{{ item.content }}</text>
          <view v-if="item.tags?.length" class="tag-row">
            <text v-for="tag in item.tags" :key="tag" class="record-tag">{{ tag }}</text>
          </view>
          <text class="record-line">评价时间：{{ formatTime(item.createdAt) }}</text>
          <view class="record-actions">
            <view class="record-action" @click="openDetail(item.orderId)">查看订单</view>
          </view>
        </view>
      </view>

      <view v-else class="empty-panel">
        <text class="empty-title">{{ emptyTitle }}</text>
        <text class="empty-desc">{{ emptyDesc }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import request from '../../utils/request'
import { useUserStore } from '../../stores/user'
import {
  buildErrandCounterparty,
  findErrandEvaluation,
  readErrandEvaluations,
} from '../../utils/errandEvaluation'

const store = useUserStore()
const activeTab = ref('pending')
const starFilter = ref('all')
const publishedList = ref([])
const acceptedList = ref([])
const evaluations = ref([])

const starFilters = [
  { label: '全部', value: 'all' },
  { label: '5星', value: '5' },
  { label: '4星', value: '4' },
  { label: '3星及以下', value: 'lte3' },
]

const serviceTypeMap = {
  PICKUP: '取件',
  DELIVERY: '送件',
  PURCHASE: '代购',
  PRINT: '代打印',
}

const normalizeList = (list) => (Array.isArray(list) ? list : [])

const loadData = async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  const [published, accepted] = await Promise.all([
    request.get('/errand/orders/my/published'),
    request.get('/errand/orders/my/accepted'),
  ])
  publishedList.value = normalizeList(published)
  acceptedList.value = normalizeList(accepted)
  evaluations.value = readErrandEvaluations()
}

const completedOrders = computed(() =>
  [...publishedList.value, ...acceptedList.value]
    .filter((item) => item.status === 'COMPLETED')
    .reduce((list, item) => {
      if (list.some((record) => String(record.id) === String(item.id))) {
        return list
      }
      const counterpartInfo = buildErrandCounterparty(item, store.profile?.userId)
      list.push({
        ...item,
        counterpartyId: counterpartInfo.counterparty?.userId,
        counterpartyName: counterpartInfo.counterparty?.nickname || '对方用户',
      })
      return list
    }, []))

const pendingList = computed(() =>
  completedOrders.value.filter((item) =>
    item.counterpartyId && !findErrandEvaluation({ orderId: item.id, fromUserId: store.profile?.userId })))

const sentList = computed(() =>
  evaluations.value
    .filter((item) => String(item.fromUserId) === String(store.profile?.userId))
    .sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime()))

const receivedList = computed(() =>
  evaluations.value
    .filter((item) => String(item.toUserId) === String(store.profile?.userId))
    .sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime()))

const matchStarFilter = (item) => {
  if (starFilter.value === 'all') {
    return true
  }
  if (starFilter.value === 'lte3') {
    return Number(item.rating || 0) <= 3
  }
  return String(item.rating || '') === starFilter.value
}

const filteredSentList = computed(() => sentList.value.filter(matchStarFilter))
const filteredReceivedList = computed(() => receivedList.value.filter(matchStarFilter))

const averageScore = computed(() => {
  if (!receivedList.value.length) {
    return '--'
  }
  const total = receivedList.value.reduce((sum, item) => sum + Number(item.rating || 0), 0)
  return (total / receivedList.value.length).toFixed(1)
})

const emptyTitle = computed(() => {
  if (activeTab.value === 'pending') {
    return '暂无待评价订单'
  }
  if (activeTab.value === 'sent') {
    return starFilter.value === 'all' ? '暂未发出评价' : '当前筛选下暂无评价'
  }
  return starFilter.value === 'all' ? '暂未收到评价' : '当前筛选下暂无评价'
})

const emptyDesc = computed(() => {
  if (activeTab.value === 'pending') {
    return '已完成订单在你还未评价对方前，会自动出现在这里。'
  }
  if (activeTab.value === 'sent') {
    return '你提交过的跑腿评价会沉淀到这里，支持按星级快速筛选和回看。'
  }
  return '当对方账号也完成评价后，这里会展示你收到的评价记录，并支持按星级筛选。'
})

const formatTime = (value) => {
  if (!value) {
    return '-'
  }
  return value.replace('T', ' ')
}

const openDetail = (id) => {
  uni.navigateTo({ url: `/pages/errand/detail?id=${id}` })
}

const openEvaluate = (orderId) => {
  uni.navigateTo({ url: `/pages/errand/evaluate?orderId=${orderId}` })
}

onLoad((options) => {
  if (options?.focusOrderId) {
    activeTab.value = 'pending'
  }
})

onShow(loadData)
</script>

<style scoped lang="scss">
.evaluation-summary,
.evaluation-tabs {
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
  padding: 24rpx 22rpx;
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

.evaluation-tabs {
  margin-top: 22rpx;
}

.tab-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.tab-item {
  min-height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20rpx;
  background: #e2e8f0;
  color: #475569;
  font-size: 24rpx;
  font-weight: 600;
}

.tab-item.active {
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 18rpx;
}

.filter-chip {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: #eef4ff;
  color: #2d7ff9;
  font-size: 22rpx;
}

.filter-chip.active {
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 22rpx;
}

.record-card {
  padding: 24rpx;
  border-radius: 24rpx;
  background: #f8fafc;
}

.record-head {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
}

.record-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}

.record-meta,
.record-line {
  display: block;
  margin-top: 10rpx;
  font-size: 23rpx;
  line-height: 1.7;
  color: #64748b;
}

.record-score {
  flex-shrink: 0;
  font-size: 28rpx;
  font-weight: 800;
  color: #f59e0b;
}

.record-score.wait {
  color: #2d7ff9;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 14rpx;
}

.record-tag {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: #eef4ff;
  color: #2d7ff9;
  font-size: 22rpx;
}

.record-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 18rpx;
}

.record-action {
  padding: 12rpx 20rpx;
  border-radius: 999rpx;
  background: #e2e8f0;
  color: #475569;
  font-size: 22rpx;
}

.record-action.primary {
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
}

.empty-panel {
  margin-top: 22rpx;
  padding: 54rpx 30rpx;
  border-radius: 24rpx;
  background: #f8fafc;
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
