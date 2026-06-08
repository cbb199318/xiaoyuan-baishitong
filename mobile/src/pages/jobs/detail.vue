<template>
  <view class="jobs-detail-page">
    <view class="jobs-navbar">
      <view class="jobs-nav-side jobs-nav-left" hover-class="jobs-nav-hover" @click="goBack">‹</view>
      <view class="jobs-nav-center">
        <text class="jobs-nav-title">岗位详情</text>
      </view>
      <view class="jobs-nav-placeholder"></view>
    </view>

    <scroll-view scroll-y class="jobs-detail-scroll">
      <view class="jobs-detail-content">
        <view v-if="detail" class="detail-card">
          <view :class="['hero-card', detail.roleType === 'BUSINESS' ? 'business' : 'student']">
            <view class="detail-head">
              <text :class="['role-tag', detail.roleType === 'BUSINESS' ? 'business' : 'student']">
                {{ jobsRoleMap[detail.roleType] || '学生发布' }}
              </text>
              <text class="detail-salary">{{ detail.salaryText }}</text>
            </view>

            <text class="detail-title">{{ detail.title }}</text>
            <text class="hero-subtitle">
              {{ detail.roleType === 'BUSINESS' ? '面向学生开放的线下岗位信息' : '学生线上互助与技能接单需求' }}
            </text>
          </view>

          <view class="info-card">
            <text class="section-title">岗位 / 需求信息</text>
            <view class="info-grid">
              <view class="info-item">
                <text class="info-label">分类</text>
                <text class="info-value">{{ jobsCategoryMap[detail.category] || detail.category }}</text>
              </view>
              <view class="info-item">
                <text class="info-label">发布身份</text>
                <text class="info-value">{{ jobsRoleMap[detail.roleType] || '学生发布' }}</text>
              </view>
              <view class="info-item">
                <text class="info-label">地点</text>
                <text class="info-value">{{ detail.location }}</text>
              </view>
              <view class="info-item">
                <text class="info-label">时间</text>
                <text class="info-value">{{ detail.timeText }}</text>
              </view>
              <view class="info-item">
                <text class="info-label">人数</text>
                <text class="info-value">{{ detail.filledCount || 0 }} / {{ detail.headCount || 1 }}</text>
              </view>
              <view class="info-item">
                <text class="info-label">薪资 / 酬劳</text>
                <text class="info-value accent">{{ detail.salaryText }}</text>
              </view>
              <view class="info-item">
                <text class="info-label">发布者</text>
                <text class="info-value">{{ detail.publisherName }}</text>
              </view>
            </view>
          </view>

          <view class="summary-card">
            <text class="section-title">工作 / 服务内容</text>
            <text class="detail-summary">{{ detail.content || detail.summary }}</text>
          </view>

          <view class="summary-card">
            <text class="section-title">岗位要求 / 需求标准</text>
            <text class="detail-summary">{{ detail.requirement || '请与发布者进一步沟通具体要求。' }}</text>
          </view>

          <view v-if="detail.credentialFile?.url" class="credential-block">
            <text class="credential-title">商家资质材料</text>
            <image class="credential-image" :src="detail.credentialFile.url" mode="aspectFill" @click="previewCredential"></image>
            <text class="credential-tip">点击图片可预览商家提交的资质照片</text>
          </view>

          <view class="tip-card">
            <text class="section-title">平台提示</text>
            <text class="detail-summary">
              {{
                detail.roleType === 'BUSINESS'
                  ? '商家岗位需通过平台审核后公开展示，请同学注意核实岗位地点、时间和报酬说明。'
                  : '学生互助需求由用户自主发布，无需资质审核，建议沟通时确认交付要求与时间节点。'
              }}
            </text>
          </view>

          <view class="contact-card">
            <text class="section-title">联系与反馈</text>
            <text class="detail-summary">如需进一步确认岗位细节，可直接联系对方；遇到异常情况可提交举报反馈，由后台人工处理。</text>
            <view class="contact-actions">
              <view :class="['contact-btn', isOwnJob ? 'disabled' : '']" @click="callPhone">
                {{ isOwnJob ? '本人发布' : '电话联系' }}
              </view>
              <view class="contact-btn danger" @click="openReport">举报反馈</view>
            </view>
          </view>

        </view>

        <view v-else class="empty-card">
          <text class="empty-title">未找到该岗位信息</text>
        </view>
      </view>
    </scroll-view>

    <view v-if="detail" class="bottom-action-bar">
      <view class="action-btn ghost" @click="toggleFavorite">
        {{ favoriteActive ? '已收藏' : '收藏岗位' }}
      </view>
      <view :class="['action-btn ghost', actionDisabled ? 'disabled' : '']" @click="openChat">
        {{ isOwnJob ? '无需私聊' : '在线私聊' }}
      </view>
      <view :class="['action-btn', actionDisabled ? 'disabled' : '']" @click="applyNow">
        {{ actionDisabled ? disabledText : '立即报名/接单' }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import {
  createJobApplication,
  ensureJobConversation,
  favoriteJob,
  getJobDetail,
  listFavoriteJobs,
  unfavoriteJob,
} from '../../utils/jobsApi'
import { jobsCategoryMap, jobsRoleMap } from '../../utils/jobsMeta'

const store = useUserStore()
const detail = ref(null)
const favoriteIds = ref([])
const currentJobId = ref('')

const resolveCurrentJobId = (options) => {
  if (options?.id) {
    return String(options.id)
  }
  const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : []
  const current = pages[pages.length - 1]
  if (current?.options?.id) {
    return String(current.options.id)
  }
  if (typeof window !== 'undefined') {
    const hash = window.location.hash || ''
    const query = hash.includes('?') ? hash.split('?')[1] : ''
    const id = new URLSearchParams(query).get('id')
    if (id) {
      return id
    }
  }
  return ''
}

const loadDetail = async (options) => {
  const jobId = resolveCurrentJobId(options)
  currentJobId.value = jobId
  if (!jobId) {
    detail.value = null
    return
  }
  const [jobDetail, favorites] = await Promise.all([getJobDetail(jobId), listFavoriteJobs()])
  detail.value = jobDetail
  favoriteIds.value = (favorites || []).map((item) => item.id)
}

const favoriteActive = computed(() => favoriteIds.value.includes(detail.value?.id))
const isOwnJob = computed(() => Number(detail.value?.publisherId || 0) === Number(store.profile?.userId || 0))
const actionDisabled = computed(() => {
  if (!detail.value) {
    return true
  }
  return (
    isOwnJob.value ||
    detail.value.expired ||
    Number(detail.value.filledCount || 0) >= Number(detail.value.headCount || 1) ||
    detail.value.status === 'COMPLETED'
  )
})
const disabledText = computed(() => {
  if (!detail.value) {
    return '不可操作'
  }
  if (isOwnJob.value) {
    return '我发布的'
  }
  if (detail.value.expired) {
    return '已过期'
  }
  if (Number(detail.value.filledCount || 0) >= Number(detail.value.headCount || 1)) {
    return '已招满'
  }
  if (detail.value.status === 'COMPLETED') {
    return '已完结'
  }
  return '不可操作'
})

const goBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.redirectTo({ url: '/pages/jobs/index?tab=home' })
    },
  })
}

const previewCredential = () => {
  if (!detail.value?.credentialFile?.url) {
    return
  }
  uni.previewImage({
    current: detail.value.credentialFile.url,
    urls: [detail.value.credentialFile.url],
  })
}

const toggleFavorite = async () => {
  if (!detail.value?.id) {
    return
  }
  const added = !favoriteIds.value.includes(detail.value.id)
  detail.value = added ? await favoriteJob(detail.value.id) : await unfavoriteJob(detail.value.id)
  favoriteIds.value = added
    ? [...favoriteIds.value, detail.value.id]
    : favoriteIds.value.filter((id) => id !== detail.value.id)
  uni.showToast({ title: added ? '已加入收藏' : '已取消收藏', icon: 'none' })
}

const openChat = async () => {
  if (isOwnJob.value) {
    uni.showToast({ title: '自己发布的岗位无需私聊', icon: 'none' })
    return
  }
  if (actionDisabled.value) {
    uni.showToast({ title: disabledText.value, icon: 'none' })
    return
  }
  const conversation = await ensureJobConversation(detail.value?.id)
  if (!conversation) {
    uni.showToast({ title: '暂时无法创建会话', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: `/pages/jobs/chat?id=${conversation.id}&title=${encodeURIComponent(conversation.counterpartyName || '兼职沟通')}&jobId=${detail.value?.id || ''}&phone=${conversation.counterpartyPhone || ''}`,
  })
}

const callPhone = () => {
  if (isOwnJob.value) {
    uni.showToast({ title: '自己发布的岗位无需联系自己', icon: 'none' })
    return
  }
  const phone = detail.value?.publisherPhone || ''
  if (!phone) {
    uni.showToast({ title: '暂无可拨打号码', icon: 'none' })
    return
  }
  uni.makePhoneCall({ phoneNumber: phone })
}

const openReport = () => {
  if (!detail.value?.id) {
    return
  }
  uni.navigateTo({
    url: `/pages/report/create?module=jobs&targetType=job&targetId=${detail.value.id}`,
  })
}

const applyNow = async () => {
  if (isOwnJob.value) {
    uni.showToast({ title: '自己发布的岗位无需报名', icon: 'none' })
    return
  }
  if (actionDisabled.value) {
    uni.showToast({ title: disabledText.value, icon: 'none' })
    return
  }
  const conversation = await ensureJobConversation(detail.value?.id)
  await createJobApplication({
    postId: detail.value?.id,
    conversationId: conversation?.id ? String(conversation.id) : '',
  })
  uni.showToast({ title: '已提交报名/接单意向', icon: 'success' })
  setTimeout(() => {
    if (!conversation) {
      return
    }
    uni.navigateTo({
      url: `/pages/jobs/chat?id=${conversation.id}&title=${encodeURIComponent(conversation.counterpartyName || '兼职沟通')}&jobId=${detail.value?.id || ''}&phone=${conversation.counterpartyPhone || ''}`,
    })
  }, 240)
}

onLoad((options) => {
  currentJobId.value = resolveCurrentJobId(options)
})

onShow(async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  await loadDetail({ id: currentJobId.value })
})
</script>

<style scoped lang="scss">
.jobs-detail-page {
  min-height: 100vh;
  background: #f4f7fb;
  padding-bottom: calc(164rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.jobs-navbar {
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

.jobs-nav-side {
  min-height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18rpx;
  color: #1f2937;
}

.jobs-nav-left {
  font-size: 42rpx;
}

.jobs-nav-center {
  text-align: center;
}

.jobs-nav-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
}

.jobs-nav-placeholder {
  min-height: 72rpx;
}

.jobs-nav-hover {
  background: #f1f5f9;
}

.jobs-detail-scroll {
  height: calc(100vh - 112rpx - env(safe-area-inset-top) - 144rpx - env(safe-area-inset-bottom));
}

.jobs-detail-content {
  padding: 24rpx;
  box-sizing: border-box;
}

.detail-card {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.hero-card,
.info-card,
.summary-card,
.credential-block,
.contact-card,
.tip-card,
.empty-card {
  padding: 26rpx 24rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.06);
}

.hero-card.student {
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
}

.hero-card.business {
  background: linear-gradient(135deg, #ecfdf5, #dcfce7);
}

.detail-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
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

.detail-salary {
  font-size: 28rpx;
  font-weight: 800;
  color: #ef4444;
}

.section-title,
.detail-title,
.credential-title,
.empty-title {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
}

.detail-title {
  margin-top: 18rpx;
}

.hero-subtitle,
.detail-summary,
.credential-tip {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.8;
  color: #64748b;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx 14rpx;
  margin-top: 18rpx;
}

.info-item {
  padding: 18rpx;
  border-radius: 22rpx;
  background: #f8fafc;
}

.info-label,
.info-value {
  display: block;
}

.info-label {
  font-size: 22rpx;
  color: #94a3b8;
}

.info-value {
  margin-top: 8rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: #0f172a;
}

.info-value.accent {
  color: #ef4444;
  font-weight: 700;
}

.credential-image {
  width: 100%;
  height: 320rpx;
  margin-top: 14rpx;
  border-radius: 22rpx;
  background: #e2e8f0;
}

.contact-actions {
  display: flex;
  gap: 14rpx;
  margin-top: 18rpx;
}

.contact-btn {
  flex: 1;
  min-height: 78rpx;
  border-radius: 20rpx;
  background: #eff6ff;
  color: #2563eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 23rpx;
  font-weight: 700;
}

.contact-btn.danger {
  background: #fef2f2;
  color: #dc2626;
}

.contact-btn.disabled {
  background: #e2e8f0;
  color: #94a3b8;
}

.bottom-action-bar {
  position: fixed;
  left: 24rpx;
  right: 24rpx;
  bottom: calc(24rpx + env(safe-area-inset-bottom));
  display: flex;
  gap: 14rpx;
  padding: 16rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.08);
}

.action-btn {
  flex: 1;
  min-height: 88rpx;
  border-radius: 22rpx;
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 700;
}

.action-btn.ghost {
  background: #e2e8f0;
  color: #334155;
}

.action-btn.active {
  background: #0f172a;
}

.action-btn.disabled {
  background: #cbd5e1;
  color: #ffffff;
}
</style>
