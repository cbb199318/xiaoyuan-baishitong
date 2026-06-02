<template>
  <view class="partner-mine-page">
    <view class="partner-navbar">
      <view class="partner-nav-side partner-nav-left" hover-class="partner-nav-hover" @click="goBack">‹</view>
      <view class="partner-nav-center">
        <text class="partner-nav-title">个人中心</text>
      </view>
      <view class="partner-nav-side partner-nav-placeholder"></view>
    </view>

    <scroll-view scroll-y class="partner-mine-scroll">
      <view class="partner-mine-content">
        <view class="card partner-profile-card">
          <view class="profile-head">
            <view class="profile-avatar">{{ (profile?.nickname || '搭').slice(0, 1) }}</view>
            <view class="profile-info">
              <text class="profile-name">{{ profile?.nickname || '搭子用户' }}</text>
              <text class="profile-role">跑腿身份：全员通用用户</text>
              <text class="profile-meta">手机号：{{ profile?.phone || '-' }}</text>
            </view>
          </view>

          <view class="tag-editor">
            <text class="section-title">个人特点标签</text>
            <view class="tag-grid">
              <view
                v-for="tag in partnerPresetTags"
                :key="tag"
                :class="['tag-chip', profileTags.includes(tag) ? 'active' : '']"
                @click="toggleTag(tag)"
              >
                {{ tag }}
              </view>
            </view>

            <view class="tag-custom-row">
              <input
                v-model.trim="customTag"
                class="tag-input"
                maxlength="8"
                placeholder="自定义标签，最多 8 个"
                @confirm="addCustomTag"
              />
              <view class="tag-add-btn" @click="addCustomTag">添加</view>
            </view>

            <view v-if="profileTags.length" class="saved-tags">
              <view v-for="tag in profileTags" :key="tag" class="saved-tag-chip">
                <text>{{ tag }}</text>
                <text class="saved-tag-remove" @click="removeTag(tag)">×</text>
              </view>
            </view>
            <text class="helper-text">保存后会同步到首页卡片、聊天资料等对外页面展示。</text>
            <view class="save-btn" @click="saveTags">保存标签</view>
          </view>
        </view>

        <view class="card tab-card">
          <view class="tab-row">
            <view
              v-for="item in tabOptions"
              :key="item.value"
              :class="['tab-chip', activeTab === item.value ? 'active' : '']"
              @click="activeTab = item.value"
            >
              {{ item.label }}
            </view>
          </view>

          <view v-if="activeTab === 'published'" class="record-list">
            <view v-if="publishedList.length" class="record-stack">
              <view v-for="item in publishedList" :key="item.id" class="record-card">
                <text class="record-title">{{ categoryMap[item.type] || item.type }} · {{ item.location }}</text>
                <text class="record-desc">{{ item.description }}</text>
                <text class="record-meta">状态：{{ item.status === 'OFFLINE' ? '已下架' : '展示中' }}</text>
                <text class="record-meta">申请人数：{{ getApplicants(item.id).length }}</text>
                <view class="mini-action-row">
                  <view class="mini-action" @click="openDemandDetail(item.id)">查看详情</view>
                  <view class="mini-action" @click="viewApplicants(item)">申请记录</view>
                  <view class="mini-action" @click="editDemand(item)">编辑需求</view>
                  <view class="mini-action danger" @click="offlineDemand(item)">下架需求</view>
                </view>
              </view>
            </view>
            <view v-else class="empty-block">还没有发布过搭子需求。</view>
          </view>

          <view v-else-if="activeTab === 'applied'" class="record-list">
            <view v-if="appliedList.length" class="record-stack">
              <view v-for="item in appliedList" :key="item.id" class="record-card">
                <text class="record-title">{{ item.demandTitle }}</text>
                <text class="record-desc">{{ item.demandSummary || '已发起搭子申请，等待对方回复。' }}</text>
                <text class="record-meta">申请状态：{{ statusLabelMap[item.status] || item.status }}</text>
                <view class="mini-action-row">
                  <view class="mini-action" @click="openDemandDetail(item.demandId)">查看详情</view>
                  <view class="mini-action" @click="openChat(item)">进入聊天</view>
                </view>
              </view>
            </view>
            <view v-else class="empty-block">你还没有主动申请过搭子需求。</view>
          </view>

          <view v-else class="record-list">
            <view v-if="matchedList.length" class="record-stack">
              <view v-for="item in matchedList" :key="item.id" class="record-card">
                <text class="record-title">{{ item.demandTitle }}</text>
                <text class="record-desc">已匹配搭子：{{ getCounterpartyName(item) }}</text>
                <text class="record-meta">状态：已匹配成功</text>
                <view class="mini-action-row">
                  <view class="mini-action" @click="openDemandDetail(item.demandId)">查看详情</view>
                  <view class="mini-action" @click="openChat(item)">进入聊天</view>
                </view>
              </view>
            </view>
            <view v-else class="empty-block">当前还没有匹配成功的搭子。</view>
          </view>
        </view>

        <view class="card partner-report-card">
          <view class="report-head">
            <view>
              <text class="section-title">举报记录</text>
              <text class="helper-text">查看本人所有举报工单、举证信息与处理进度。</text>
            </view>
            <view class="report-link" @click="go('/pages/partner/reports')">查看全部</view>
          </view>
        </view>
      </view>
    </scroll-view>

    <view v-if="applicantPanelVisible" class="applicant-panel-mask" @click="closeApplicantPanel">
      <view class="applicant-panel" @click.stop>
        <view class="applicant-panel-header">
          <view class="applicant-panel-title-wrap">
            <text class="applicant-panel-title">申请记录</text>
            <text class="applicant-panel-subtitle">
              {{ selectedDemand?.location || '当前需求' }} · {{ activeApplicants.length }} 人
            </text>
          </view>
          <text class="applicant-panel-close" @click="closeApplicantPanel">关闭</text>
        </view>

        <scroll-view scroll-y class="applicant-panel-scroll">
          <view v-if="activeApplicants.length" class="applicant-stack">
            <view v-for="item in activeApplicants" :key="item.id" class="applicant-card">
              <view class="applicant-head">
                <view class="applicant-avatar">{{ (item.applicantName || '搭').slice(0, 1) }}</view>
                <view class="applicant-main">
                  <view class="applicant-name-row">
                    <text class="applicant-name">{{ item.applicantName || '搭子同学' }}</text>
                    <text :class="['applicant-status', `status-${String(item.status || 'PENDING').toLowerCase()}`]">
                      {{ statusLabelMap[item.status] || item.status }}
                    </text>
                  </view>
                  <text class="applicant-time">申请时间：{{ formatTime(item.updatedAt) }}</text>
                </view>
              </view>

              <view v-if="getApplicantTags(item).length" class="applicant-tag-row">
                <view v-for="tag in getApplicantTags(item)" :key="tag" class="applicant-tag">{{ tag }}</view>
              </view>

              <text class="applicant-preview">{{ item.latestMessage || '已发起搭子申请，等待你处理。' }}</text>

              <view class="applicant-action-row">
                <view class="applicant-action ghost" @click="openChat(item)">进入聊天</view>
                <view class="applicant-action reject" @click="handleApplicantStatus(item, 'REJECTED')">拒绝</view>
                <view class="applicant-action" @click="handleApplicantStatus(item, 'ACCEPTED')">同意</view>
              </view>
            </view>
          </view>
          <view v-else class="empty-block applicant-empty">当前还没有申请记录。</view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import {
  getAllPartnerDemands,
  getPartnerProfileTags,
  partnerCategoryMap,
  partnerPresetTags,
  savePartnerProfileTags,
  updatePartnerDemandStatus,
} from '../../utils/partnerMock'
import { getPartnerConversations, updatePartnerConversationStatus } from '../../utils/partnerChat'

const store = useUserStore()
const profile = computed(() => store.profile)
const profileTags = ref([])
const customTag = ref('')
const activeTab = ref('published')
const publishedList = ref([])
const conversationList = ref([])
const applicantPanelVisible = ref(false)
const selectedDemand = ref(null)

const tabOptions = [
  { label: '我的发布', value: 'published' },
  { label: '我的申请', value: 'applied' },
  { label: '已匹配搭子', value: 'matched' },
]

const statusLabelMap = {
  PENDING: '待回复',
  ACCEPTED: '已同意',
  REJECTED: '已拒绝',
}

const categoryMap = partnerCategoryMap

const appliedList = computed(() =>
  conversationList.value.filter((item) => Number(item.applicantId) === Number(profile.value?.userId || 0))
)

const matchedList = computed(() =>
  conversationList.value.filter(
    (item) =>
      item.status === 'ACCEPTED' &&
      (Number(item.applicantId) === Number(profile.value?.userId || 0) ||
        Number(item.publisherId) === Number(profile.value?.userId || 0))
  )
)

const activeApplicants = computed(() => {
  if (!selectedDemand.value?.id) {
    return []
  }
  return getApplicants(selectedDemand.value.id).sort(
    (a, b) => new Date(b.updatedAt || 0).getTime() - new Date(a.updatedAt || 0).getTime()
  )
})

const loadMineData = () => {
  const userId = Number(profile.value?.userId || 0)
  profileTags.value = getPartnerProfileTags(userId)
  publishedList.value = getAllPartnerDemands().filter((item) => Number(item.publisherId || 0) === userId)
  conversationList.value = getPartnerConversations(userId)
}

const toggleTag = (tag) => {
  if (profileTags.value.includes(tag)) {
    profileTags.value = profileTags.value.filter((item) => item !== tag)
    return
  }
  if (profileTags.value.length >= 8) {
    uni.showToast({ title: '最多添加 8 个标签', icon: 'none' })
    return
  }
  profileTags.value = [...profileTags.value, tag]
}

const addCustomTag = () => {
  const value = customTag.value.trim()
  if (!value) {
    return
  }
  if (profileTags.value.includes(value)) {
    uni.showToast({ title: '标签已存在', icon: 'none' })
    return
  }
  if (profileTags.value.length >= 8) {
    uni.showToast({ title: '最多添加 8 个标签', icon: 'none' })
    return
  }
  profileTags.value = [...profileTags.value, value]
  customTag.value = ''
}

const removeTag = (tag) => {
  profileTags.value = profileTags.value.filter((item) => item !== tag)
}

const saveTags = () => {
  savePartnerProfileTags(profile.value?.userId, profileTags.value)
  uni.showToast({ title: '标签已保存', icon: 'success' })
  loadMineData()
}

const getApplicants = (demandId) =>
  conversationList.value.filter((item) => String(item.demandId) === String(demandId))

const getApplicantTags = (item) => getPartnerProfileTags(item.applicantId).slice(0, 4)

const getCounterpartyName = (item) =>
  Number(item.publisherId) === Number(profile.value?.userId || 0) ? item.applicantName : item.publisherName

const openChat = (item) => {
  uni.navigateTo({ url: `/pages/partner/chat?id=${item.id}` })
}

const openDemandDetail = (id) => {
  uni.navigateTo({ url: `/pages/partner/detail?id=${id}` })
}

const editDemand = (item) => {
  uni.navigateTo({ url: `/pages/partner/publish?id=${item.id}` })
}

const offlineDemand = (item) => {
  updatePartnerDemandStatus(item.id, 'OFFLINE')
  uni.showToast({ title: '需求已下架', icon: 'success' })
  loadMineData()
}

const viewApplicants = (item) => {
  if (!getApplicants(item.id).length) {
    uni.showToast({ title: '当前还没有申请记录', icon: 'none' })
    return
  }
  selectedDemand.value = item
  applicantPanelVisible.value = true
}

const closeApplicantPanel = () => {
  applicantPanelVisible.value = false
  selectedDemand.value = null
}

const handleApplicantStatus = (item, status) => {
  if (item.status === status) {
    uni.showToast({ title: status === 'ACCEPTED' ? '该申请已同意' : '该申请已拒绝', icon: 'none' })
    return
  }
  updatePartnerConversationStatus({
    conversationId: item.id,
    status,
    operatorName: profile.value?.nickname || '发布者',
  })
  uni.showToast({ title: status === 'ACCEPTED' ? '已同意申请' : '已拒绝申请', icon: 'success' })
  loadMineData()
}

const formatTime = (value) => (value ? value.replace('T', ' ').slice(0, 16) : '')

const go = (url) => {
  uni.navigateTo({ url })
}

const goBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.navigateTo({ url: '/pages/partner/index' })
    },
  })
}

onShow(async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  loadMineData()
})
</script>

<style lang="scss">
.partner-mine-page {
  min-height: 100vh;
  background: #f4f7fb;
}

.partner-navbar {
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

.partner-nav-side {
  min-height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1f2937;
  font-size: 28rpx;
  border-radius: 18rpx;
}

.partner-nav-left {
  font-size: 42rpx;
}

.partner-nav-placeholder {
  pointer-events: none;
}

.partner-nav-center {
  text-align: center;
}

.partner-nav-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
}

.partner-nav-hover {
  background: #f1f5f9;
}

.partner-mine-scroll {
  height: calc(100vh - 112rpx - env(safe-area-inset-top));
}

.partner-mine-content {
  padding: 24rpx;
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  overflow-x: hidden;
}

.partner-profile-card,
.tab-card,
.partner-report-card {
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  overflow: hidden;
}

.profile-head {
  display: flex;
  gap: 18rpx;
  margin-bottom: 24rpx;
}

.profile-avatar {
  width: 92rpx;
  height: 92rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb, #38bdf8);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34rpx;
  font-weight: 700;
  flex-shrink: 0;
}

.profile-info text {
  display: block;
}

.profile-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #0f172a;
}

.profile-role,
.profile-meta,
.helper-text {
  margin-top: 8rpx;
  font-size: 22rpx;
  line-height: 1.7;
  color: #64748b;
}

.section-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 14rpx;
}

.tag-grid,
.saved-tags,
.tab-row,
.mini-action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.tag-chip,
.saved-tag-chip,
.tab-chip,
.mini-action,
.report-link,
.save-btn,
.tag-add-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.tag-chip,
.saved-tag-chip {
  padding: 12rpx 18rpx;
  border-radius: 999rpx;
  background: #edf2f7;
  color: #475569;
  font-size: 22rpx;
}

.tag-chip.active {
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
}

.tag-custom-row {
  display: flex;
  gap: 12rpx;
  margin-top: 16rpx;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.tag-input {
  flex: 1;
  min-width: 0;
  min-height: 84rpx;
  padding: 0 22rpx;
  border-radius: 20rpx;
  background: #f8fafc;
  box-sizing: border-box;
}

.tag-add-btn,
.save-btn {
  min-height: 84rpx;
  padding: 0 24rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 700;
  box-sizing: border-box;
}

.saved-tags {
  margin-top: 16rpx;
}

.saved-tag-remove {
  margin-left: 10rpx;
}

.save-btn {
  margin-top: 18rpx;
  display: flex;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.tab-row {
  margin-bottom: 18rpx;
}

.tab-chip {
  padding: 14rpx 20rpx;
  border-radius: 999rpx;
  background: #e2e8f0;
  color: #475569;
  font-size: 23rpx;
}

.tab-chip.active {
  background: #1677ff;
  color: #ffffff;
}

.record-stack {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.record-card {
  padding: 22rpx;
  border-radius: 22rpx;
  background: #f8fafc;
}

.record-title {
  display: block;
  font-size: 26rpx;
  font-weight: 700;
  color: #0f172a;
}

.record-desc,
.record-meta,
.empty-block {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.7;
  color: #64748b;
}

.mini-action-row {
  margin-top: 14rpx;
}

.mini-action {
  min-height: 68rpx;
  padding: 0 18rpx;
  border-radius: 18rpx;
  background: #e8eef8;
  color: #315172;
  font-size: 22rpx;
}

.mini-action.danger {
  background: #fee2e2;
  color: #dc2626;
}

.report-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.report-link {
  min-height: 68rpx;
  padding: 0 18rpx;
  border-radius: 18rpx;
  background: #eff6ff;
  color: #2563eb;
  font-size: 22rpx;
  flex-shrink: 0;
}

.applicant-panel-mask {
  position: fixed;
  inset: 0;
  z-index: 80;
  background: rgba(15, 23, 42, 0.36);
  display: flex;
  align-items: flex-end;
}

.applicant-panel {
  width: 100%;
  max-height: 78vh;
  border-radius: 28rpx 28rpx 0 0;
  background: #ffffff;
  overflow: hidden;
}

.applicant-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding: 24rpx 28rpx;
  border-bottom: 1px solid #eef2f7;
}

.applicant-panel-title-wrap text {
  display: block;
}

.applicant-panel-title {
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
}

.applicant-panel-subtitle,
.applicant-time,
.applicant-preview {
  margin-top: 8rpx;
  font-size: 22rpx;
  line-height: 1.7;
  color: #64748b;
}

.applicant-panel-close {
  font-size: 24rpx;
  color: #64748b;
}

.applicant-panel-scroll {
  max-height: calc(78vh - 110rpx);
}

.applicant-stack {
  padding: 20rpx 24rpx calc(24rpx + env(safe-area-inset-bottom));
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  box-sizing: border-box;
}

.applicant-card {
  padding: 22rpx;
  border-radius: 24rpx;
  background: #f8fafc;
}

.applicant-head {
  display: flex;
  gap: 14rpx;
}

.applicant-avatar {
  width: 76rpx;
  height: 76rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb, #38bdf8);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 700;
  flex-shrink: 0;
}

.applicant-main {
  flex: 1;
  min-width: 0;
}

.applicant-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.applicant-name {
  font-size: 26rpx;
  font-weight: 700;
  color: #0f172a;
}

.applicant-status {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 21rpx;
  font-weight: 700;
  white-space: nowrap;
}

.applicant-status.status-pending {
  background: #dbeafe;
  color: #1d4ed8;
}

.applicant-status.status-accepted {
  background: #dcfce7;
  color: #15803d;
}

.applicant-status.status-rejected {
  background: #fee2e2;
  color: #dc2626;
}

.applicant-tag-row,
.applicant-action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.applicant-tag-row {
  margin-top: 14rpx;
}

.applicant-tag {
  padding: 10rpx 16rpx;
  border-radius: 999rpx;
  background: #e0f2fe;
  color: #0284c7;
  font-size: 21rpx;
}

.applicant-action-row {
  margin-top: 16rpx;
}

.applicant-action {
  flex: 1;
  min-width: 160rpx;
  min-height: 72rpx;
  border-radius: 18rpx;
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 700;
}

.applicant-action.ghost {
  background: #e2e8f0;
  color: #334155;
}

.applicant-action.reject {
  background: #fee2e2;
  color: #dc2626;
}

.applicant-empty {
  margin: 24rpx;
}
</style>
