<template>
  <view class="page-shell beauty-posts-page">
    <view v-if="list.length" class="beauty-card-list">
      <view v-for="item in list" :key="item.id" class="beauty-good-card" @click="openDetail(item.id)">
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

    <view v-else class="card beauty-empty-block">
      <text class="beauty-empty-title">还没有发布过好物</text>
      <text class="beauty-empty-desc">上传商品实拍图和购买凭证后，就能把平价好物展示给其他同学。</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { listMyBeautyGoods } from '../../utils/beautyApi'

const list = ref([])

const loadList = async () => {
  list.value = await listMyBeautyGoods()
}

const openDetail = (id) => {
  uni.navigateTo({ url: `/pages/beauty/detail?id=${id}` })
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

onShow(loadList)
</script>

<style scoped lang="scss">
.beauty-posts-page {
  padding-top: 20rpx;
}

.beauty-card-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.beauty-good-card {
  display: flex;
  gap: 14rpx;
  padding: 18rpx 16rpx;
  border-radius: 26rpx;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18rpx 44rpx rgba(236, 72, 153, 0.08);
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

.beauty-good-title,
.beauty-good-summary,
.beauty-good-price,
.beauty-good-time,
.beauty-empty-title,
.beauty-empty-desc {
  display: block;
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

.beauty-good-time {
  font-size: 22rpx;
  color: #94a3b8;
}

.beauty-empty-block {
  padding: 44rpx 28rpx;
  border-radius: 26rpx;
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
</style>
