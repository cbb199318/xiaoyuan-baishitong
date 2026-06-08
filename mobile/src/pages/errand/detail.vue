<template>
  <view class="detail-page">
    <view class="errand-navbar">
      <view class="nav-side nav-left" hover-class="nav-side-hover" @click="goBack">‹</view>
      <view class="nav-center">
        <text class="errand-title">订单详情</text>
      </view>
      <view class="nav-side nav-placeholder"></view>
    </view>

    <view class="page-shell detail-shell">
    <view v-if="detail" class="card detail-card">
      <view class="detail-head">
        <text class="detail-title">{{ serviceTypeMap[detail.serviceType] || detail.serviceType }}</text>
        <text class="detail-reward">￥{{ detail.totalFee }}</text>
      </view>
      <text class="detail-line">订单号：{{ detail.orderNo }}</text>
      <text class="detail-line">状态：{{ detail.status }}</text>
      <text class="detail-line">取件地址：{{ detail.pickupAddress }}</text>
      <text class="detail-line">送达地址：{{ detail.deliveryAddress }}</text>
      <text class="detail-line">预约时间：{{ detail.pickupTimeText || '-' }}</text>
      <text class="detail-line">需求明细：{{ detail.detailContent }}</text>
      <text class="detail-line">补充备注：{{ detail.remark || '-' }}</text>
      <text class="detail-line">发布人：{{ detail.publisher?.nickname }} / {{ detail.publisher?.phone }}</text>
      <text class="detail-line">接单人：{{ detail.runner?.nickname || '-' }} / {{ detail.runner?.phone || '-' }}</text>
      <view v-if="detail.attachments?.length" class="attachment-list">
        <image v-for="item in detail.attachments" :key="item.fileId" class="attachment-image" :src="item.url" mode="aspectFill"></image>
      </view>
      <view v-if="detail.status === 'COMPLETED'" class="evaluation-summary-card">
        <text class="evaluation-summary-title">评价摘要</text>

        <view v-if="myEvaluation" class="evaluation-item">
          <text class="evaluation-role">我的评价</text>
          <text class="evaluation-score">★ {{ myEvaluation.rating }}</text>
          <text v-if="myEvaluation.content" class="evaluation-content">{{ myEvaluation.content }}</text>
          <view v-if="myEvaluation.tags?.length" class="evaluation-tags">
            <text v-for="tag in myEvaluation.tags" :key="tag" class="evaluation-tag">{{ tag }}</text>
          </view>
        </view>
        <view v-else class="evaluation-empty-line">你还没有评价对方，完成评价后会在这里显示。</view>

        <view v-if="receivedEvaluation" class="evaluation-item">
          <text class="evaluation-role">对方给我的评价</text>
          <text class="evaluation-score">★ {{ receivedEvaluation.rating }}</text>
          <text v-if="receivedEvaluation.content" class="evaluation-content">{{ receivedEvaluation.content }}</text>
          <view v-if="receivedEvaluation.tags?.length" class="evaluation-tags">
            <text v-for="tag in receivedEvaluation.tags" :key="tag" class="evaluation-tag">{{ tag }}</text>
          </view>
        </view>
        <view v-else class="evaluation-empty-line">对方暂未提交评价。</view>
      </view>
      <view class="action-grid">
        <view v-if="detail.canAccept" class="primary-btn action-btn" @click="acceptOrder">接单</view>
        <view v-if="detail.canCancel" class="ghost-btn action-btn" @click="cancelOrder">取消订单</view>
        <view v-if="detail.canStart" class="primary-btn action-btn" @click="updateStatus('start')">开始履约</view>
        <view v-if="detail.canDeliver" class="primary-btn action-btn" @click="updateStatus('deliver')">标记处理中</view>
        <view v-if="detail.canComplete" class="primary-btn action-btn" @click="updateStatus('complete')">确认完成</view>
        <view class="ghost-btn action-btn" @click="contactPhone">电话联系</view>
        <view class="ghost-btn action-btn" @click="openChat">订单聊天</view>
        <view v-if="detail.status === 'COMPLETED'" class="ghost-btn action-btn" @click="openEvaluate">
          {{ hasEvaluation ? '查看评价' : '评价对方' }}
        </view>
        <view v-if="detail.canReport" class="ghost-btn action-btn" @click="openReport">发起举报</view>
      </view>
    </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import request from '../../utils/request'
import { useUserStore } from '../../stores/user'
import { buildErrandCounterparty, listErrandEvaluationsByOrder } from '../../utils/errandEvaluation'

const store = useUserStore()
const id = ref('')
const detail = ref(null)
const evaluationList = ref([])
const serviceTypeMap = {
  PICKUP: '取件',
  DELIVERY: '送件',
  PURCHASE: '代购',
  PRINT: '代打印',
}

const hasEvaluation = computed(() => {
  if (!detail.value?.id || !store.profile?.userId || detail.value?.status !== 'COMPLETED') {
    return false
  }
  return evaluationList.value.some((item) => String(item.fromUserId) === String(store.profile.userId))
})

const myEvaluation = computed(() =>
  evaluationList.value.find((item) => String(item.fromUserId) === String(store.profile?.userId)) || null)

const receivedEvaluation = computed(() =>
  evaluationList.value.find((item) => String(item.toUserId) === String(store.profile?.userId)) || null)

const saveBrowseHistory = (item) => {
  if (!item?.id) {
    return
  }
  const storageKey = `errand-browse-history-${store.profile?.userId || 'guest'}`
  const raw = uni.getStorageSync(storageKey) || '[]'
  let history = []
  try {
    history = JSON.parse(raw)
  } catch (error) {
    history = []
  }
  const nextItem = {
    id: item.id,
    orderNo: item.orderNo,
    serviceType: item.serviceType,
    pickupAddress: item.pickupAddress,
    deliveryAddress: item.deliveryAddress,
    pickupTimeText: item.pickupTimeText,
    detailContent: item.detailContent,
    remark: item.remark,
    baseFee: item.baseFee,
    totalFee: item.totalFee,
    urgentFlag: !!item.urgentFlag,
    fragileFlag: !!item.fragileFlag,
    status: item.status,
    createdAt: item.createdAt,
    viewedAt: new Date().toISOString(),
  }
  history = [nextItem, ...history.filter((record) => record.id !== item.id)].slice(0, 50)
  uni.setStorageSync(storageKey, JSON.stringify(history))
}

const loadDetail = async () => {
  if (!id.value) {
    return
  }
  detail.value = await request.get(`/errand/orders/${id.value}`)
  evaluationList.value = listErrandEvaluationsByOrder(id.value)
  saveBrowseHistory(detail.value)
}

const acceptOrder = async () => {
  detail.value = await request.post(`/errand/orders/${id.value}/accept`)
  uni.showToast({ title: '接单成功', icon: 'success' })
}

const cancelOrder = async () => {
  detail.value = await request.post(`/errand/orders/${id.value}/cancel`)
  uni.showToast({ title: '订单已取消', icon: 'success' })
}

const updateStatus = async (action) => {
  detail.value = await request.post(`/errand/orders/${id.value}/${action}`)
  uni.showToast({ title: '状态已更新', icon: 'success' })
}

const openChat = async () => {
  const data = await request.get(`/errand/orders/${id.value}/conversation`)
  const counterpartPhone =
    detail.value.publisher?.userId === store.profile?.userId
      ? detail.value.runner?.phone
      : detail.value.publisher?.phone
  uni.navigateTo({
    url: `/pages/errand/chat?id=${data.conversationId}&title=${encodeURIComponent(detail.value.orderNo)}&orderId=${id.value}&phone=${counterpartPhone || ''}`,
  })
}

const openReport = () => {
  uni.navigateTo({
    url: `/pages/report/create?module=errand&targetType=order&targetId=${id.value}`,
  })
}

const openEvaluate = () => {
  if (!detail.value || detail.value.status !== 'COMPLETED') {
    return
  }
  const counterpartInfo = buildErrandCounterparty(detail.value, store.profile?.userId)
  if (!counterpartInfo.counterparty?.userId) {
    uni.showToast({ title: '当前暂无可评价对象', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: `/pages/errand/evaluate?orderId=${id.value}`,
  })
}

const callPhone = (phone) => {
  if (!phone) {
    uni.showToast({ title: '暂无可拨打号码', icon: 'none' })
    return
  }
  uni.makePhoneCall({ phoneNumber: phone })
}

const contactPhone = () => {
  if (!detail.value) {
    return
  }
  const phone =
    detail.value.publisher?.userId === store.profile?.userId
      ? detail.value.runner?.phone
      : detail.value.publisher?.phone
  callPhone(phone)
}

const goBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.redirectTo({ url: '/pages/errand/index?tab=square' })
    },
  })
}

onLoad((options) => {
  id.value = options?.id || ''
})

onShow(loadDetail)
</script>

<style scoped lang="scss">
.detail-page {
  min-height: 100vh;
  background: #f4f7fb;
}

.detail-shell {
  padding-top: 24rpx;
}

.errand-navbar {
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

.nav-side {
  min-height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1f2937;
  font-size: 28rpx;
  border-radius: 18rpx;
}

.nav-left {
  font-size: 42rpx;
}

.nav-right {
  font-size: 24rpx;
  color: #2d7ff9;
}

.nav-placeholder {
  pointer-events: none;
}

.nav-center {
  text-align: center;
  min-width: 0;
}

.errand-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav-side-hover {
  background: #f1f5f9;
}

.detail-card {
  padding: 30rpx 26rpx;
}

.detail-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.detail-title {
  font-size: 34rpx;
  font-weight: 800;
}

.detail-reward {
  color: #dc2626;
  font-size: 36rpx;
  font-weight: 800;
}

.detail-line {
  display: block;
  color: #475569;
  line-height: 1.8;
  margin-bottom: 8rpx;
}

.attachment-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14rpx;
  margin: 18rpx 0;
}

.attachment-image {
  width: 100%;
  height: 220rpx;
  border-radius: 18rpx;
}

.evaluation-summary-card {
  margin-top: 18rpx;
  padding: 22rpx;
  border-radius: 22rpx;
  background: #f8fafc;
}

.evaluation-summary-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}

.evaluation-item {
  margin-top: 18rpx;
}

.evaluation-role {
  display: block;
  font-size: 24rpx;
  font-weight: 700;
  color: #334155;
}

.evaluation-score {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  font-weight: 800;
  color: #f59e0b;
}

.evaluation-content,
.evaluation-empty-line {
  display: block;
  margin-top: 10rpx;
  font-size: 23rpx;
  line-height: 1.7;
  color: #64748b;
}

.evaluation-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 12rpx;
}

.evaluation-tag {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: #eef4ff;
  color: #2d7ff9;
  font-size: 22rpx;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14rpx;
  margin-top: 20rpx;
}

.action-btn {
  height: 78rpx;
  line-height: 78rpx;
}
</style>
