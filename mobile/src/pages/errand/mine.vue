<template>
  <view :class="embedded ? 'errand-mine-root errand-mine-root--embedded' : 'errand-mine-root'">
    <view class="errand-mine-card errand-mine-hero">
      <view class="errand-mine-hero-main">
        <view class="errand-mine-avatar">{{ avatarText }}</view>
        <view class="errand-mine-profile">
          <text class="errand-mine-name">{{ profile?.nickname || '未登录用户' }}</text>
          <text class="errand-mine-role">跑腿身份：全员通用用户</text>
          <text class="errand-mine-phone">手机号：{{ profile?.phone || '-' }}</text>
        </view>
      </view>

      <view class="errand-mine-shortcuts">
        <view class="errand-mine-shortcut" @click="openRuleCenter">
          <text class="errand-mine-shortcut-title">规则中心</text>
          <text class="errand-mine-shortcut-desc">查看平台规范</text>
        </view>
        <view class="errand-mine-shortcut" @click="openHelp">
          <text class="errand-mine-shortcut-title">帮助售后</text>
          <text class="errand-mine-shortcut-desc">反馈与咨询入口</text>
        </view>
      </view>
    </view>

    <view class="errand-mine-card">
      <view class="errand-mine-section-head">
        <text class="errand-mine-section-title">我的跑腿订单</text>
        <text class="errand-mine-section-desc">统一查看我发布和我承接的全状态订单</text>
      </view>

      <view class="errand-mine-tabs">
        <view
          :class="['errand-mine-tab', activeTab === 'published' ? 'is-active' : '']"
          @click="activeTab = 'published'"
        >
          我发布的订单
        </view>
        <view
          :class="['errand-mine-tab', activeTab === 'accepted' ? 'is-active' : '']"
          @click="activeTab = 'accepted'"
        >
          我承接的订单
        </view>
      </view>

      <view class="errand-mine-stats">
        <view class="errand-mine-stat">
          <text class="errand-mine-stat-label">订单总数</text>
          <text class="errand-mine-stat-value">{{ displayList.length }}</text>
        </view>
        <view class="errand-mine-stat">
          <text class="errand-mine-stat-label">已完成金额</text>
          <text class="errand-mine-stat-value">￥{{ activeTab === 'published' ? totalSpend.toFixed(2) : totalIncome.toFixed(2) }}</text>
        </view>
      </view>

      <view v-if="displayList.length" class="errand-mine-order-list">
        <view v-for="item in displayList" :key="item.id" class="errand-mine-order">
          <view class="errand-mine-order-top" @click="openDetail(item.id)">
            <view class="errand-mine-order-title-wrap">
              <text class="errand-mine-order-title">{{ serviceTypeMap[item.serviceType] || item.serviceType }}</text>
              <text :class="['errand-mine-status', `status-${normalizeStatus(item.status)}`]">
                {{ statusMap[item.status] || item.status }}
              </text>
            </view>
            <text class="errand-mine-order-fee">￥{{ formatAmount(item.totalFee) }}</text>
          </view>

          <view class="errand-mine-order-body" @click="openDetail(item.id)">
            <text class="errand-mine-order-line">订单号：{{ item.orderNo || '-' }}</text>
            <text class="errand-mine-order-line">取件地址：{{ item.pickupAddress || '-' }}</text>
            <text class="errand-mine-order-line">派送地址：{{ item.deliveryAddress || '-' }}</text>
            <text class="errand-mine-order-line">预约时间：{{ item.pickupTimeText || '-' }}</text>
          </view>

          <view class="errand-mine-action-row">
            <view class="errand-mine-action" @click="openDetail(item.id)">查看详情</view>
            <view class="errand-mine-action" @click="recreateOrder(item)">再次下单</view>
            <view class="errand-mine-action" @click="contactOrder(item)">联系对方</view>
            <view
              v-if="item.status === 'COMPLETED'"
              :class="['errand-mine-action', hasEvaluated(item) ? '' : 'errand-mine-action--primary']"
              @click="goEvaluate(item.id)"
            >
              {{ hasEvaluated(item) ? '已评详情' : '去评价' }}
            </view>
            <view class="errand-mine-action" @click="reportOrder(item)">发起举报</view>
            <view class="errand-mine-action errand-mine-action--danger" @click="afterSale(item)">售后申诉</view>
          </view>
        </view>
      </view>
      <view v-else class="errand-mine-empty">
        <text class="errand-mine-empty-title">当前还没有相关跑腿订单</text>
        <text class="errand-mine-empty-desc">先去广场接单，或者发布你的第一条跑腿订单。</text>
      </view>
    </view>

    <view class="errand-mine-card">
      <view class="errand-mine-section-head">
        <text class="errand-mine-section-title">历史浏览记录</text>
        <text class="errand-mine-section-desc">自动记录浏览过的订单，支持回看、收藏和批量清空</text>
      </view>

      <view v-if="browseHistory.length" class="errand-mine-history-list">
        <view v-for="item in browseHistory" :key="item.id" class="errand-mine-history">
          <view class="errand-mine-history-main" @click="openDetail(item.id)">
            <text class="errand-mine-history-title">{{ serviceTypeMap[item.serviceType] || item.serviceType }} · {{ item.orderNo || '跑腿订单' }}</text>
            <text class="errand-mine-history-line">{{ item.pickupAddress || '-' }} -> {{ item.deliveryAddress || '-' }}</text>
            <text class="errand-mine-history-line">浏览时间：{{ formatTime(item.viewedAt) }}</text>
          </view>
          <view class="errand-mine-history-actions">
            <view class="errand-mine-action" @click="toggleFavorite(item)">
              {{ isFavorite(item.id) ? '取消收藏' : '收藏订单' }}
            </view>
            <view class="errand-mine-action" @click="recreateOrder(item)">再次下单</view>
          </view>
        </view>

        <view class="errand-mine-history-footer">
          <view class="errand-mine-action errand-mine-action--danger" @click="clearBrowseHistory">批量清空浏览记录</view>
        </view>
      </view>
      <view v-else class="errand-mine-empty">
        <text class="errand-mine-empty-title">暂无浏览记录</text>
        <text class="errand-mine-empty-desc">从订单广场或订单详情浏览过的内容，会自动同步到这里。</text>
      </view>
    </view>

    <view class="errand-mine-card">
      <view class="errand-mine-section-head">
        <text class="errand-mine-section-title">辅助功能列表</text>
        <text class="errand-mine-section-desc">收藏、收支、评价、售后、规则和帮助入口统一收纳</text>
      </view>

      <view class="errand-mine-income-grid">
        <view class="errand-mine-income-box">
          <text class="errand-mine-income-label">总支出</text>
          <text class="errand-mine-income-value">￥{{ totalSpend.toFixed(2) }}</text>
        </view>
        <view class="errand-mine-income-box">
          <text class="errand-mine-income-label">总收入</text>
          <text class="errand-mine-income-value">￥{{ totalIncome.toFixed(2) }}</text>
        </view>
      </view>

      <view class="errand-mine-helper-list">
        <view class="errand-mine-helper" @click="openFavorites">
          <view class="errand-mine-helper-text">
            <text class="errand-mine-helper-title">我的收藏</text>
            <text class="errand-mine-helper-desc">收藏优质订单、常用模板，支持一键复用发单</text>
          </view>
          <text class="errand-mine-helper-arrow">›</text>
        </view>

        <view class="errand-mine-helper" @click="showIncomeDetail">
          <view class="errand-mine-helper-text">
            <text class="errand-mine-helper-title">收支明细</text>
            <text class="errand-mine-helper-desc">查看总收入、总支出及每笔订单费用概览</text>
          </view>
          <text class="errand-mine-helper-arrow">›</text>
        </view>

        <view class="errand-mine-helper" @click="showEvaluation">
          <view class="errand-mine-helper-text">
            <text class="errand-mine-helper-title">我的评价</text>
            <text class="errand-mine-helper-desc">查看双向评价记录、他人评价与历史回溯</text>
          </view>
          <text class="errand-mine-helper-arrow">›</text>
        </view>

        <view class="errand-mine-helper" @click="go('/pages/report/list')">
          <view class="errand-mine-helper-text">
            <text class="errand-mine-helper-title">售后与举报记录</text>
            <text class="errand-mine-helper-desc">统一查看全部工单、核查进度和处理结果</text>
          </view>
          <text class="errand-mine-helper-arrow">›</text>
        </view>

        <view class="errand-mine-helper" @click="openRuleCenter">
          <view class="errand-mine-helper-text">
            <text class="errand-mine-helper-title">平台规则中心</text>
            <text class="errand-mine-helper-desc">快捷查看计费、履约、售后与免责声明</text>
          </view>
          <text class="errand-mine-helper-arrow">›</text>
        </view>

        <view class="errand-mine-helper" @click="openHelp">
          <view class="errand-mine-helper-text">
            <text class="errand-mine-helper-title">帮助与售后</text>
            <text class="errand-mine-helper-desc">问题反馈、异常咨询、售后说明入口</text>
          </view>
          <text class="errand-mine-helper-arrow">›</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import request from '../../utils/request'
import { useUserStore } from '../../stores/user'
import { findErrandEvaluation } from '../../utils/errandEvaluation'

defineProps({
  embedded: {
    type: Boolean,
    default: false,
  },
})

const store = useUserStore()
const profile = ref(null)
const activeTab = ref('published')
const publishedList = ref([])
const acceptedList = ref([])
const browseHistory = ref([])
const favorites = ref([])

const serviceTypeMap = {
  PICKUP: '取件',
  DELIVERY: '送件',
  PURCHASE: '代购',
  PRINT: '代打印',
}

const statusMap = {
  PUBLISHED: '待接单',
  ACCEPTED: '已承接',
  IN_PROGRESS: '进行中',
  DELIVERING: '派送中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  DISPUTED: '举报处理中',
  EXPIRED: '已过期',
}

const displayList = computed(() => (activeTab.value === 'published' ? publishedList.value : acceptedList.value))
const avatarText = computed(() => (profile.value?.nickname || '用').slice(0, 1))

const totalSpend = computed(() =>
  publishedList.value
    .filter((item) => item.status === 'COMPLETED')
    .reduce((sum, item) => sum + Number(item.totalFee || 0), 0))

const totalIncome = computed(() =>
  acceptedList.value
    .filter((item) => item.status === 'COMPLETED')
    .reduce((sum, item) => sum + Number(item.totalFee || 0), 0))

const favoriteKey = computed(() => `errand-favorites-${profile.value?.userId || 'guest'}`)
const browseKey = computed(() => `errand-browse-history-${profile.value?.userId || 'guest'}`)

const readJsonList = (key) => {
  const raw = uni.getStorageSync(key) || '[]'
  try {
    return JSON.parse(raw)
  } catch (error) {
    return []
  }
}

const formatAmount = (value) => {
  const amount = Number(value || 0)
  return Number.isNaN(amount) ? '0.00' : amount.toFixed(2)
}

const normalizeStatus = (value) => String(value || '').toLowerCase()

const formatTime = (value) => (value ? value.replace('T', ' ') : '')

const isFavorite = (id) => favorites.value.some((item) => item.id === id)

const buildOrderDraft = (item) => ({
  serviceType: item.serviceType || 'PICKUP',
  pickupAddress: item.pickupAddress || '',
  deliveryAddress: item.deliveryAddress || '',
  pickupTimeText: item.pickupTimeText || '',
  detailContent: item.detailContent || '',
  remark: item.remark || '',
  baseFee: item.baseFee || item.totalFee || '',
  urgentFlag: !!item.urgentFlag,
  fragileFlag: !!item.fragileFlag,
})

const loadData = async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  profile.value = store.profile
  publishedList.value = await request.get('/errand/orders/my/published')
  acceptedList.value = await request.get('/errand/orders/my/accepted')
  browseHistory.value = readJsonList(browseKey.value)
  favorites.value = readJsonList(favoriteKey.value)
}

const openDetail = (id) => {
  uni.navigateTo({ url: `/pages/errand/detail?id=${id}` })
}

const hasEvaluated = (item) => {
  if (item?.status !== 'COMPLETED' || !profile.value?.userId) {
    return false
  }
  return !!findErrandEvaluation({
    orderId: item.id,
    fromUserId: profile.value.userId,
  })
}

const recreateOrder = (item) => {
  const draft = buildOrderDraft(item)
  const query = [
    `serviceType=${draft.serviceType}`,
    `pickupAddress=${encodeURIComponent(draft.pickupAddress)}`,
    `deliveryAddress=${encodeURIComponent(draft.deliveryAddress)}`,
    `pickupTimeText=${encodeURIComponent(draft.pickupTimeText)}`,
    `detailContent=${encodeURIComponent(draft.detailContent)}`,
    `remark=${encodeURIComponent(draft.remark)}`,
    `baseFee=${draft.baseFee}`,
    `urgentFlag=${draft.urgentFlag ? 1 : 0}`,
    `fragileFlag=${draft.fragileFlag ? 1 : 0}`,
  ].join('&')
  uni.redirectTo({ url: `/pages/errand/index?tab=create&${query}` })
}

const contactOrder = async (item) => {
  const data = await request.get(`/errand/orders/${item.id}/conversation`)
  const isPublisher = item.publisher?.userId === profile.value?.userId
  const counterparty = isPublisher ? item.runner : item.publisher
  uni.navigateTo({
    url: `/pages/errand/chat?id=${data.conversationId}&title=${encodeURIComponent(counterparty?.nickname || '订单聊天')}&orderId=${item.id}&phone=${counterparty?.phone || ''}&orderNo=${encodeURIComponent(item.orderNo || '')}&summary=${encodeURIComponent(`${item.pickupAddress || '-'} -> ${item.deliveryAddress || '-'}`)}`,
  })
}

const reportOrder = (item) => {
  uni.navigateTo({
    url: `/pages/report/create?module=errand&targetType=order&targetId=${item.id}`,
  })
}

const goEvaluate = (orderId) => {
  uni.navigateTo({ url: `/pages/errand/evaluation?focusOrderId=${orderId}` })
}

const afterSale = (item) => {
  uni.navigateTo({
    url: `/pages/report/create?module=errand&targetType=order&targetId=${item.id}`,
  })
}

const toggleFavorite = (item) => {
  const exists = isFavorite(item.id)
  if (exists) {
    favorites.value = favorites.value.filter((record) => record.id !== item.id)
    uni.showToast({ title: '已取消收藏', icon: 'none' })
  } else {
    favorites.value = [
      {
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
        savedAt: new Date().toISOString(),
      },
      ...favorites.value,
    ]
    uni.showToast({ title: '已加入收藏', icon: 'success' })
  }
  uni.setStorageSync(favoriteKey.value, JSON.stringify(favorites.value))
}

const clearBrowseHistory = () => {
  uni.showModal({
    title: '清空浏览记录',
    content: '确认清空所有历史浏览记录吗？',
    success: (result) => {
      if (!result.confirm) {
        return
      }
      browseHistory.value = []
      uni.setStorageSync(browseKey.value, JSON.stringify([]))
      uni.showToast({ title: '已清空', icon: 'success' })
    },
  })
}

const go = (url) => {
  uni.navigateTo({ url })
}

const openRuleCenter = () => {
  uni.navigateTo({ url: '/pages/errand/rules' })
}

const openHelp = () => {
  uni.navigateTo({ url: '/pages/errand/support' })
}

const openFavorites = () => {
  uni.navigateTo({ url: '/pages/errand/favorites' })
}

const showIncomeDetail = () => {
  uni.navigateTo({ url: '/pages/errand/income' })
}

const showEvaluation = () => {
  uni.navigateTo({ url: '/pages/errand/evaluation' })
}

onShow(loadData)
</script>
