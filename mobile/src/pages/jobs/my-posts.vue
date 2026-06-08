<template>
  <view class="page-shell jobs-manage-page">
    <view class="filter-row">
      <view
        v-for="item in tabOptions"
        :key="item.value"
        :class="['filter-chip', activeTab === item.value ? 'active' : '']"
        @click="activeTab = item.value"
      >
        {{ item.label }}
      </view>
    </view>

    <view v-if="filteredList.length" class="card-list">
      <view v-for="item in filteredList" :key="item.id" class="card item-card">
        <view class="item-head">
          <text class="item-title">{{ item.title }}</text>
          <text :class="['status-tag', item.statusMeta.tone]">{{ item.statusMeta.label }}</text>
        </view>
        <text class="item-meta">{{ item.location }} · {{ item.timeText }}</text>
        <text class="item-meta">{{ item.salaryText }} · {{ roleLabel(item.roleType) }}</text>
        <text class="item-summary">{{ item.summary || '暂无补充说明' }}</text>
        <view class="action-row">
          <view class="mini-btn ghost" @click="openDetail(item.id)">查看详情</view>
          <view class="mini-btn ghost" @click="editPost(item)">编辑</view>
          <view class="mini-btn ghost" @click="toggleVisible(item)">
            {{ isOffShelf(item) ? '重新上架' : '下架' }}
          </view>
          <view class="mini-btn danger" @click="removePost(item)">删除</view>
        </view>
      </view>
    </view>

    <view v-else class="card empty-card">
      <text class="empty-title">暂无对应发布内容</text>
      <text class="empty-desc">切换状态标签后，这里会展示你在勤工助学模块下的发布记录。</text>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onBackPress, onLoad, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { handleJobsMineBackPress } from '../../utils/jobsNavigation'
import { deleteJobPost, listMyJobs, updateJobVisibility } from '../../utils/jobsApi'
import { jobsRoleMap } from '../../utils/jobsMeta'

const store = useUserStore()
const list = ref([])
const activeTab = ref('ACTIVE')
let pageOptions = {}

const isBusiness = computed(() =>
  String(store.profile?.publishRole || store.profile?.roleType || '').toUpperCase() === 'BUSINESS'
)

const tabOptions = computed(() =>
  isBusiness.value
    ? [
        { label: '审核中', value: 'PENDING_REVIEW' },
        { label: '已通过', value: 'APPROVED' },
        { label: '已驳回', value: 'REJECTED' },
        { label: '招聘中', value: 'RECRUITING' },
      ]
    : [
        { label: '进行中', value: 'ACTIVE' },
        { label: '已完结', value: 'COMPLETED' },
        { label: '已下架', value: 'OFFLINE' },
      ]
)

const buildStatusMeta = (item) => {
  if (isBusiness.value) {
    if (item.status === 'PENDING_REVIEW') {
      return { key: 'PENDING_REVIEW', label: '审核中', tone: 'amber' }
    }
    if (item.status === 'REJECTED') {
      return { key: 'REJECTED', label: '已驳回', tone: 'red' }
    }
    if ((item.status === 'APPROVED' || item.status === 'ACTIVE') && item.publicVisible) {
      return { key: 'RECRUITING', label: '招聘中', tone: 'green' }
    }
    return { key: 'APPROVED', label: '已通过', tone: 'blue' }
  }
  if (item.status === 'COMPLETED') {
    return { key: 'COMPLETED', label: '已完结', tone: 'green' }
  }
  if (!item.publicVisible || item.status === 'OFFLINE' || item.status === 'CANCELLED') {
    return { key: 'OFFLINE', label: '已下架', tone: 'slate' }
  }
  return { key: 'ACTIVE', label: '进行中', tone: 'blue' }
}

const filteredList = computed(() =>
  list.value
    .map((item) => ({
      ...item,
      statusMeta: buildStatusMeta(item),
    }))
    .filter((item) => item.statusMeta.key === activeTab.value)
)

const roleLabel = (roleType) => jobsRoleMap[roleType] || '学生发布'
const isOffShelf = (item) => !item.publicVisible || item.status === 'OFFLINE'

const loadList = async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  list.value = await listMyJobs()
  if (!tabOptions.value.some((item) => item.value === activeTab.value)) {
    activeTab.value = tabOptions.value[0]?.value || 'ACTIVE'
  }
}

const openDetail = (id) => {
  uni.navigateTo({ url: `/pages/jobs/detail?id=${id}` })
}

const editPost = (item) => {
  uni.navigateTo({ url: `/pages/jobs/publish?editId=${item.id}` })
}

const toggleVisible = async (item) => {
  const nextVisible = isOffShelf(item)
  await updateJobVisibility(item.id, nextVisible)
  uni.showToast({ title: nextVisible ? '已重新上架' : '已下架', icon: 'success' })
  loadList()
}

const removePost = (item) => {
  uni.showModal({
    title: '删除发布',
    content: '删除后将无法恢复，是否继续？',
    success: (result) => {
      if (!result.confirm) {
        return
      }
      deleteJobPost(item.id)
      uni.showToast({ title: '已删除', icon: 'success' })
      loadList()
    },
  })
}

onLoad((options) => {
  pageOptions = options || {}
})

onShow(loadList)

onBackPress(() => handleJobsMineBackPress(pageOptions))
</script>

<style scoped lang="scss">
.jobs-manage-page {
  padding-top: 20rpx;
}

.filter-row {
  display: flex;
  gap: 14rpx;
  overflow-x: auto;
  margin-bottom: 20rpx;
}

.filter-chip {
  flex-shrink: 0;
  min-height: 68rpx;
  padding: 0 24rpx;
  border-radius: 999rpx;
  background: #e2e8f0;
  color: #475569;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
}

.filter-chip.active {
  background: #1677ff;
  color: #ffffff;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.item-card {
  padding: 24rpx;
}

.item-head,
.action-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
}

.item-head {
  justify-content: space-between;
}

.item-title,
.empty-title {
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
}

.item-meta,
.item-summary,
.empty-desc {
  display: block;
  margin-top: 10rpx;
  color: #64748b;
  font-size: 23rpx;
  line-height: 1.7;
}

.action-row {
  margin-top: 18rpx;
}

.mini-btn,
.status-tag {
  border-radius: 18rpx;
  font-size: 22rpx;
}

.mini-btn {
  min-height: 68rpx;
  padding: 0 18rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #1677ff;
  color: #ffffff;
}

.mini-btn.ghost {
  background: #e2e8f0;
  color: #334155;
}

.mini-btn.danger {
  background: #fee2e2;
  color: #dc2626;
}

.status-tag {
  padding: 8rpx 14rpx;
  font-weight: 700;
}

.status-tag.blue {
  background: #dbeafe;
  color: #1d4ed8;
}

.status-tag.green {
  background: #dcfce7;
  color: #15803d;
}

.status-tag.amber {
  background: #fef3c7;
  color: #d97706;
}

.status-tag.red {
  background: #fee2e2;
  color: #dc2626;
}

.status-tag.slate {
  background: #e2e8f0;
  color: #475569;
}

.empty-card {
  padding: 44rpx 28rpx;
  text-align: center;
}
</style>
