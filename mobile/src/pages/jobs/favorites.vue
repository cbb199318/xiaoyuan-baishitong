<template>
  <view class="page-shell jobs-favorites-page">
    <view v-if="list.length" class="card-list">
      <view v-for="item in list" :key="item.id" class="card favorite-card" @click="openDetail(item.id)">
        <view class="favorite-head">
          <text class="favorite-title">{{ item.title }}</text>
          <text class="favorite-salary">{{ item.salaryText }}</text>
        </view>
        <text class="favorite-meta">{{ item.location }} · {{ item.timeText }}</text>
        <text class="favorite-summary">{{ item.summary || '暂无补充说明' }}</text>
      </view>
    </view>

    <view v-else class="card empty-card">
      <text class="empty-title">暂无收藏内容</text>
      <text class="empty-desc">你收藏过的岗位和需求会在这里集中展示。</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onBackPress, onLoad, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { handleJobsMineBackPress } from '../../utils/jobsNavigation'
import { listFavoriteJobs } from '../../utils/jobsApi'

const store = useUserStore()
const list = ref([])
let pageOptions = {}

const loadList = async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  list.value = await listFavoriteJobs()
}

const openDetail = (id) => {
  uni.navigateTo({ url: `/pages/jobs/detail?id=${id}` })
}

onLoad((options) => {
  pageOptions = options || {}
})

onShow(loadList)

onBackPress(() => handleJobsMineBackPress(pageOptions))
</script>

<style scoped lang="scss">
.jobs-favorites-page {
  padding-top: 20rpx;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.favorite-card {
  padding: 24rpx;
}

.favorite-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.favorite-title,
.empty-title {
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
}

.favorite-salary {
  font-size: 24rpx;
  font-weight: 700;
  color: #ef4444;
}

.favorite-meta,
.favorite-summary,
.empty-desc {
  display: block;
  margin-top: 10rpx;
  color: #64748b;
  font-size: 23rpx;
  line-height: 1.7;
}

.empty-card {
  padding: 44rpx 28rpx;
  text-align: center;
}
</style>
