<template>
  <view :class="[embedded ? 'embedded-page' : 'page-shell']">
    <view class="search-panel">
      <view class="search-box">
        <input
          v-model.trim="keyword"
          class="search-input"
          confirm-type="search"
          placeholder="搜索岗位、兼职需求、服务类型"
          @confirm="handleSearch"
          @input="handleInput"
        />
        <view class="search-button" @click="handleSearch">搜索</view>
      </view>
    </view>

    <scroll-view scroll-x class="category-scroll" show-scrollbar="false">
      <view class="category-row">
        <view
          v-for="item in jobsCategoryOptions"
          :key="item.value"
          :class="['category-chip', activeCategory === item.value ? 'active' : '']"
          @click="activeCategory = item.value"
        >
          {{ item.label }}
        </view>
      </view>
    </scroll-view>

    <view class="jobs-list-wrap">
      <view v-if="displayList.length" class="jobs-list">
        <view v-for="item in displayList" :key="item.id" class="job-card" @click="openDetail(item.id)">
          <view class="job-card-top">
            <view class="job-top-left">
              <text :class="['role-tag', item.roleType === 'BUSINESS' ? 'business' : 'student']">
                {{ jobsRoleMap[item.roleType] || '学生发布' }}
              </text>
              <text v-if="item.jobTypeLabel" class="job-type">{{ item.jobTypeLabel }}</text>
            </view>
            <text class="job-salary">{{ item.salaryText }}</text>
          </view>

          <text class="job-title">{{ item.title }}</text>
          <text class="job-meta">地点：{{ item.location }}</text>
          <text class="job-meta">时间：{{ item.timeText }}</text>
          <text class="job-meta">发布方：{{ item.publisherName || '岗位发布者' }}</text>
          <text class="job-summary">{{ item.summary }}</text>

          <view class="job-bottom">
            <view class="job-bottom-btn ghost" @click.stop="openDetail(item.id)">查看详情</view>
            <view
              :class="['job-bottom-btn', isFavorite(item.id) ? 'active' : '']"
              @click.stop="toggleFavorite(item)"
            >
              {{ isFavorite(item.id) ? '已收藏' : '收藏' }}
            </view>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <view class="empty-illustration"></view>
        <text class="empty-title">暂无兼职及求助需求，稍后再来查看</text>
      </view>

      <view v-if="displayList.length" class="list-footer">
        <text v-if="finished" class="footer-text">没有更多历史岗位了</text>
        <text v-else class="footer-text load-more-action" @click="handleLoadMore">点击加载更多</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { onReachBottom, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import {
  favoriteJob,
  listFavoriteJobs,
  listJobs,
  unfavoriteJob,
} from '../../utils/jobsApi'
import { jobsCategoryMap, jobsCategoryOptions, jobsRoleMap } from '../../utils/jobsMeta'

defineProps({
  embedded: {
    type: Boolean,
    default: false,
  },
})

const store = useUserStore()
const allList = ref([])
const keyword = ref('')
const activeCategory = ref('ALL')
const page = ref(1)
const pageSize = 6
const favoriteIds = ref([])

const loadFavorites = async () => {
  const favorites = await listFavoriteJobs()
  favoriteIds.value = (favorites || []).map((item) => item.id)
}

const loadList = async () => {
  allList.value = await listJobs({
    keyword: keyword.value.trim(),
    category: activeCategory.value,
  })
}

const normalizedKeyword = computed(() => keyword.value.trim().toLowerCase())

const filteredList = computed(() => {
  return (allList.value || [])
    .filter((item) => {
      if (!normalizedKeyword.value) {
        return true
      }
      const text = [
        item.title,
        item.jobTypeLabel || '',
        jobsCategoryMap[item.category] || '',
        item.location,
        item.summary,
      ]
        .join(' ')
        .toLowerCase()
      return text.includes(normalizedKeyword.value)
    })
})

const displayList = computed(() => filteredList.value.slice(0, page.value * pageSize))
const finished = computed(() => displayList.value.length >= filteredList.value.length)

const isFavorite = (id) => favoriteIds.value.includes(id)

const handleSearch = () => {
  page.value = 1
  loadList().then(() => {
    uni.showToast({
      title: filteredList.value.length ? `找到 ${filteredList.value.length} 条结果` : '暂无匹配结果',
      icon: 'none',
    })
  })
}

const handleInput = () => {
  if (!keyword.value) {
    page.value = 1
  }
}

const handleLoadMore = () => {
  if (finished.value) {
    return
  }
  page.value += 1
}

const handleShellRefresh = () => {
  page.value = 1
  keyword.value = ''
  activeCategory.value = 'ALL'
  Promise.all([loadList(), loadFavorites()]).finally(() => {
    uni.stopPullDownRefresh()
  })
}

const toggleFavorite = async (item) => {
  const added = !favoriteIds.value.includes(item.id)
  const updated = added ? await favoriteJob(item.id) : await unfavoriteJob(item.id)
  favoriteIds.value = added
    ? [...favoriteIds.value, item.id]
    : favoriteIds.value.filter((id) => id !== item.id)
  const index = allList.value.findIndex((current) => current.id === item.id)
  if (index >= 0 && updated) {
    allList.value.splice(index, 1, updated)
  }
  uni.showToast({ title: added ? '已加入收藏' : '已取消收藏', icon: 'none' })
}

const openDetail = (id) => {
  uni.navigateTo({ url: `/pages/jobs/detail?id=${id}` })
}

onShow(async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  await Promise.all([loadList(), loadFavorites()])
})

onMounted(async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  await Promise.all([loadList(), loadFavorites()])
})

onReachBottom(() => {
  handleLoadMore()
})

watch(activeCategory, () => {
  page.value = 1
  loadList()
})

uni.$off('jobs-home-refresh', handleShellRefresh)
uni.$on('jobs-home-refresh', handleShellRefresh)

onBeforeUnmount(() => {
  uni.$off('jobs-home-refresh', handleShellRefresh)
})
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
  display: grid;
  grid-template-columns: 1fr 112rpx;
  gap: 14rpx;
  min-height: 88rpx;
  padding: 14rpx;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 28rpx rgba(15, 23, 42, 0.06);
}

.search-input {
  height: 78rpx;
  padding: 0 22rpx;
  border-radius: 20rpx;
  background: #f8fafc;
}

.search-button {
  height: 78rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 700;
}

.category-scroll {
  white-space: nowrap;
  padding: 6rpx 0 10rpx;
}

.category-row {
  display: inline-flex;
  gap: 14rpx;
}

.category-chip {
  padding: 14rpx 22rpx;
  border-radius: 999rpx;
  background: #e7eef8;
  color: #475569;
  font-size: 23rpx;
}

.category-chip.active {
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
}

.jobs-list-wrap {
  padding-top: 14rpx;
}

.jobs-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.job-card {
  padding: 24rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.06);
}

.job-card-top,
.job-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
}

.job-top-left {
  display: flex;
  align-items: center;
  gap: 10rpx;
  flex-wrap: wrap;
}

.role-tag {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 700;
}

.role-tag.student {
  background: #dbeafe;
  color: #1d4ed8;
}

.role-tag.business {
  background: #dcfce7;
  color: #15803d;
}

.job-salary {
  font-size: 28rpx;
  font-weight: 800;
  color: #ef4444;
}

.job-title {
  display: block;
  margin-top: 16rpx;
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
}

.job-type {
  display: inline-flex;
  align-items: center;
  min-height: 40rpx;
  padding: 0 12rpx;
  border-radius: 999rpx;
  background: #eff6ff;
  font-size: 22rpx;
  color: #1677ff;
}

.job-meta,
.job-summary {
  display: block;
  margin-top: 10rpx;
  font-size: 23rpx;
  line-height: 1.7;
  color: #64748b;
}

.job-bottom {
  margin-top: 20rpx;
}

.job-bottom-btn {
  flex: 1;
  min-height: 76rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 23rpx;
  font-weight: 700;
}

.job-bottom-btn.ghost {
  background: #e2e8f0;
  color: #334155;
}

.job-bottom-btn.active {
  background: #0f172a;
}

.empty-state {
  margin-top: 48rpx;
  padding: 56rpx 30rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.06);
  text-align: center;
}

.empty-illustration {
  width: 120rpx;
  height: 120rpx;
  margin: 0 auto 18rpx;
  border-radius: 36rpx;
  background: linear-gradient(135deg, #dbeafe, #eff6ff);
}

.empty-title,
.footer-text {
  display: block;
  font-size: 24rpx;
  color: #64748b;
  text-align: center;
}

.list-footer {
  padding: 24rpx 0 10rpx;
}

.load-more-action {
  color: #1677ff;
}
</style>
