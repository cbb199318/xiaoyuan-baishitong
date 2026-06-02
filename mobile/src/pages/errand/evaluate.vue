<template>
  <view class="page-shell errand-evaluate-page">
    <view v-if="order" class="card evaluate-card">
      <text class="evaluate-title">评价对方</text>
      <text class="evaluate-desc">完成后会记录在“我的评价”中，方便后续查看双向评价历史。</text>

      <view class="order-summary">
        <text class="summary-line">订单号：{{ order.orderNo || '-' }}</text>
        <text class="summary-line">服务类型：{{ serviceTypeMap[order.serviceType] || order.serviceType }}</text>
        <text class="summary-line">取送路线：{{ order.pickupAddress || '-' }} -> {{ order.deliveryAddress || '-' }}</text>
        <text class="summary-line">评价对象：{{ counterparty?.nickname || '对方用户' }} / {{ counterparty?.phone || '-' }}</text>
      </view>

      <view class="field-block">
        <text class="field-title">星级评分</text>
        <view class="star-row">
          <view
            v-for="item in 5"
            :key="item"
            :class="['star-item', form.rating >= item ? 'active' : '']"
            @click="form.rating = item"
          >
            ★
          </view>
        </view>
        <text class="field-tip">{{ ratingTip }}</text>
      </view>

      <view class="field-block">
        <text class="field-title">评价标签</text>
        <view class="tag-grid">
          <view
            v-for="item in errandEvaluationTagOptions"
            :key="item"
            :class="['tag-item', form.tags.includes(item) ? 'active' : '']"
            @click="toggleTag(item)"
          >
            {{ item }}
          </view>
        </view>
      </view>

      <view class="field-block">
        <text class="field-title">文字评价</text>
        <textarea
          v-model.trim="form.content"
          class="field-textarea"
          placeholder="可补充沟通体验、履约情况、响应速度等内容"
        ></textarea>
      </view>

      <view class="action-row">
        <view class="ghost-btn action-btn" @click="openDetail">查看订单</view>
        <view :class="['primary-btn action-btn', !canSubmit || submitting ? 'disabled' : '']" @click="submit">
          {{ submitting ? '提交中...' : '提交评价' }}
        </view>
      </view>
    </view>

    <view v-else class="card empty-card">
      <text class="empty-title">未找到可评价订单</text>
      <text class="empty-desc">请从已完成订单进入评价流程，或返回个人中心查看待评价记录。</text>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import request from '../../utils/request'
import { useUserStore } from '../../stores/user'
import {
  buildErrandCounterparty,
  errandEvaluationTagOptions,
  findErrandEvaluation,
  saveErrandEvaluation,
} from '../../utils/errandEvaluation'

const store = useUserStore()
const orderId = ref('')
const order = ref(null)
const counterparty = ref(null)
const submitting = ref(false)

const serviceTypeMap = {
  PICKUP: '取件',
  DELIVERY: '送件',
  PURCHASE: '代购',
  PRINT: '代打印',
}

const form = reactive({
  rating: 5,
  tags: [],
  content: '',
})

const ratingTipMap = {
  1: '体验较差，建议补充问题点方便后续回看',
  2: '仍有待改进，可以写明具体情况',
  3: '整体一般，可记录客观体验',
  4: '整体不错，合作过程较顺畅',
  5: '体验很好，值得再次合作',
}

const ratingTip = computed(() => ratingTipMap[form.rating] || '请选择评分')
const canSubmit = computed(() => !!order.value && !!counterparty.value && form.rating > 0)

const normalizeList = (list) => (Array.isArray(list) ? list : [])

const loadOrder = async () => {
  if (!orderId.value) {
    order.value = null
    return
  }
  if (!store.profile) {
    await store.fetchProfile()
  }
  const [published, accepted] = await Promise.all([
    request.get('/errand/orders/my/published'),
    request.get('/errand/orders/my/accepted'),
  ])
  const merged = [...normalizeList(published), ...normalizeList(accepted)]
  order.value = merged.find((item) => String(item.id) === String(orderId.value) && item.status === 'COMPLETED') || null
  if (!order.value) {
    counterparty.value = null
    return
  }
  const counterpartInfo = buildErrandCounterparty(order.value, store.profile?.userId)
  counterparty.value = counterpartInfo.counterparty || null
  const existing = findErrandEvaluation({
    orderId: order.value.id,
    fromUserId: store.profile?.userId,
  })
  if (existing) {
    form.rating = Number(existing.rating || 5)
    form.tags = Array.isArray(existing.tags) ? [...existing.tags] : []
    form.content = existing.content || ''
  } else {
    form.rating = 5
    form.tags = []
    form.content = ''
  }
}

const toggleTag = (tag) => {
  if (form.tags.includes(tag)) {
    form.tags = form.tags.filter((item) => item !== tag)
    return
  }
  if (form.tags.length >= 3) {
    uni.showToast({ title: '最多选择 3 个标签', icon: 'none' })
    return
  }
  form.tags = [...form.tags, tag]
}

const openDetail = () => {
  if (!order.value?.id) {
    return
  }
  uni.navigateTo({ url: `/pages/errand/detail?id=${order.value.id}` })
}

const submit = async () => {
  if (!canSubmit.value || submitting.value) {
    return
  }
  submitting.value = true
  try {
    saveErrandEvaluation({
      orderId: order.value.id,
      orderNo: order.value.orderNo,
      serviceType: order.value.serviceType,
      pickupAddress: order.value.pickupAddress,
      deliveryAddress: order.value.deliveryAddress,
      fromUserId: store.profile?.userId,
      fromUserName: store.profile?.nickname,
      toUserId: counterparty.value?.userId,
      toUserName: counterparty.value?.nickname,
      rating: form.rating,
      tags: form.tags,
      content: form.content,
      createdAt: new Date().toISOString(),
    })
    uni.showToast({ title: '评价已保存', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack({
        fail: () => {
          uni.navigateTo({ url: '/pages/errand/evaluation' })
        },
      })
    }, 300)
  } finally {
    submitting.value = false
  }
}

onLoad((options) => {
  orderId.value = options?.orderId || ''
})

onShow(loadOrder)
</script>

<style scoped lang="scss">
.evaluate-card {
  padding: 28rpx 26rpx;
}

.evaluate-title {
  display: block;
  font-size: 36rpx;
  font-weight: 800;
  color: #0f172a;
}

.evaluate-desc,
.summary-line,
.field-tip,
.empty-desc {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: #64748b;
}

.order-summary {
  margin-top: 24rpx;
  padding: 22rpx;
  border-radius: 22rpx;
  background: #f8fafc;
}

.field-block {
  margin-top: 26rpx;
}

.field-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}

.star-row {
  display: flex;
  gap: 12rpx;
  margin-top: 18rpx;
}

.star-item {
  width: 82rpx;
  height: 82rpx;
  border-radius: 20rpx;
  background: #e2e8f0;
  color: #94a3b8;
  font-size: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.star-item.active {
  background: linear-gradient(135deg, #f59e0b, #f97316);
  color: #ffffff;
}

.tag-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 18rpx;
}

.tag-item {
  padding: 12rpx 20rpx;
  border-radius: 999rpx;
  background: #eef4ff;
  color: #2d7ff9;
  font-size: 22rpx;
}

.tag-item.active {
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
}

.field-textarea {
  width: 100%;
  min-height: 220rpx;
  margin-top: 18rpx;
  padding: 24rpx;
  border-radius: 24rpx;
  background: #f8fafc;
  box-sizing: border-box;
}

.action-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18rpx;
  margin-top: 32rpx;
}

.action-btn {
  height: 88rpx;
  line-height: 88rpx;
}

.disabled {
  opacity: 0.55;
}

.empty-card {
  padding: 56rpx 30rpx;
  text-align: center;
}

.empty-title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
}
</style>
