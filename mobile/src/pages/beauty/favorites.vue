<template>
  <view class="page-shell beauty-favorites-page">
    <view v-if="list.length" class="beauty-card-list">
      <view v-for="item in list" :key="item.id" class="beauty-good-card" @click="openDetail(item.id)">
        <image class="beauty-good-image" :src="item.imageUrl || item.image" mode="aspectFill"></image>
        <view class="beauty-good-main">
          <text class="beauty-good-title">{{ item.title }}</text>
          <text class="beauty-good-summary">{{ item.summary }}</text>
          <view class="beauty-good-meta">
            <text class="beauty-good-price">￥{{ item.price }}</text>
            <text class="beauty-good-tag">{{ item.skinTags?.[0] || '平价好物' }}</text>
          </view>
        </view>
      </view>
    </view>

    <view v-else class="card beauty-empty-block">
      <text class="beauty-empty-title">暂时没有收藏内容</text>
      <text class="beauty-empty-desc">在好物详情页点击收藏后，这里会同步展示你的平价种草清单。</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { listMyBeautyFavorites } from '../../utils/beautyApi'

const list = ref([])

const loadList = async () => {
  list.value = await listMyBeautyFavorites()
}

const openDetail = (id) => {
  uni.navigateTo({ url: `/pages/beauty/detail?id=${id}` })
}

onShow(loadList)
</script>

<style scoped lang="scss">
.beauty-favorites-page {
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

.beauty-good-title,
.beauty-good-summary,
.beauty-good-price,
.beauty-good-tag,
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

.beauty-good-summary {
  margin-top: 8rpx;
  font-size: 23rpx;
  line-height: 1.65;
  color: #64748b;
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

.beauty-good-tag {
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
