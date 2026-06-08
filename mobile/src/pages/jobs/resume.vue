<template>
  <view class="page-shell">
    <view class="card resume-card">
      <text class="section-title">我的简历</text>
      <view class="field-label">基础信息</view>
      <input v-model.trim="form.realName" class="input" placeholder="请输入姓名" />
      <input v-model.trim="form.major" class="input" placeholder="请输入专业 / 班级" />
      <input v-model.trim="form.grade" class="input" placeholder="请输入年级" />

      <view class="field-label">擅长技能</view>
      <textarea v-model.trim="form.skills" class="input textarea" placeholder="例如：PPT 制作、文案修改、家教辅导"></textarea>

      <view class="field-label">可服务时间</view>
      <textarea v-model.trim="form.availableTime" class="input textarea small" placeholder="例如：周一到周五晚上、周末全天"></textarea>

      <view class="field-label">联系方式</view>
      <input v-model.trim="form.contactPhone" class="input" placeholder="请输入常用联系方式" />

      <view class="field-label">自我介绍</view>
      <textarea v-model.trim="form.introduction" class="input textarea" placeholder="补充你的服务风格、经验或优势"></textarea>

      <view class="primary-btn" @click="submit">保存简历</view>
    </view>
  </view>
</template>

<script setup>
import { reactive } from 'vue'
import { onBackPress, onLoad, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { handleJobsMineBackPress } from '../../utils/jobsNavigation'
import { getJobsResume, saveJobsResume } from '../../utils/jobsApi'

const store = useUserStore()
let pageOptions = {}
const form = reactive({
  realName: '',
  major: '',
  grade: '',
  skills: '',
  availableTime: '',
  contactPhone: '',
  introduction: '',
})

const loadResume = async () => {
  if (!store.profile) {
    await store.fetchProfile()
  }
  Object.assign(form, await getJobsResume())
}

const submit = async () => {
  await saveJobsResume({ ...form })
  uni.showToast({ title: '简历已保存', icon: 'success' })
}

onLoad((options) => {
  pageOptions = options || {}
})

onShow(loadResume)

onBackPress(() => handleJobsMineBackPress(pageOptions))
</script>

<style scoped lang="scss">
.resume-card {
  padding: 32rpx 28rpx;
}

.section-title {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 18rpx;
}

.textarea {
  height: 180rpx;
  padding-top: 20rpx;
}

.textarea.small {
  height: 140rpx;
}
</style>
