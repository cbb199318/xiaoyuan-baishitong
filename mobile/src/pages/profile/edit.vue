<template>
  <view class="page-shell">
    <view class="card panel">
      <view class="section-title">编辑资料</view>
      <view class="field-label">昵称</view>
      <input v-model="form.nickname" class="input" placeholder="请输入昵称" />
      <view class="field-label">头像</view>
      <ImageUploader v-model="avatarFile" biz-type="avatar" title="上传头像" tip="上传后会自动回填头像地址" />
      <view class="field-label">性别</view>
      <input v-model="form.gender" class="input" placeholder="男 / 女 / 保密" />
      <view class="field-label">联系方式</view>
      <input v-model="form.contactPhone" class="input" placeholder="请输入联系方式" />
      <view class="field-label">个人简介</view>
      <textarea v-model="form.bio" class="input textarea" placeholder="介绍一下自己"></textarea>
      <view class="field-label">勤工发布身份</view>
      <view class="role-switch">
        <view
          v-for="item in roleOptions"
          :key="item.value"
          :class="['role-chip', publishRole === item.value ? 'active' : '']"
          @click="publishRole = item.value"
        >
          <text class="role-chip-title">{{ item.label }}</text>
          <text class="role-chip-desc">{{ item.desc }}</text>
        </view>
      </view>
      <view class="role-tip">
        学生身份可直接发布线上互助需求，商家身份发布线下岗位时需上传资质并进入审核流程。
      </view>
      <view class="primary-btn" @click="submit">保存资料</view>
    </view>
  </view>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { onBackPress, onLoad } from '@dcloudio/uni-app'
import ImageUploader from '../../components/ImageUploader.vue'
import request from '../../utils/request'
import { useUserStore } from '../../stores/user'
import { handleJobsMineBackPress, isFromJobsMine } from '../../utils/jobsNavigation'

const store = useUserStore()
let pageOptions = {}
const form = reactive({
  nickname: '',
  avatarUrl: '',
  gender: '',
  contactPhone: '',
  bio: '',
})
const avatarFile = ref(null)
const publishRole = ref('STUDENT')
const roleOptions = [
  {
    label: '学生身份',
    value: 'STUDENT',
    desc: '发布线上互助、家教、文案等校园协助需求',
  },
  {
    label: '商家身份',
    value: 'BUSINESS',
    desc: '发布线下兼职岗位，提交后需等待资质审核',
  },
]

onMounted(async () => {
  const data = await request.get('/user/profile')
  Object.assign(form, data)
  publishRole.value = String(store.profile?.publishRole || data.publishRole || '').toUpperCase() === 'BUSINESS' ? 'BUSINESS' : 'STUDENT'
  avatarFile.value = data.avatarUrl
    ? {
        fileId: null,
        url: data.avatarUrl,
        originalName: '头像',
      }
    : null
})

onLoad((options) => {
  pageOptions = options || {}
})

const submit = async () => {
  form.avatarUrl = avatarFile.value?.url || ''
  await request.put('/user/profile', {
    ...form,
    publishRole: publishRole.value,
  })
  store.setProfile({
    ...(store.profile || {}),
    ...form,
    publishRole: publishRole.value,
  })
  uni.showToast({ title: '保存成功', icon: 'success' })
  setTimeout(() => {
    if (isFromJobsMine(pageOptions)) {
      uni.redirectTo({ url: '/pages/jobs/index?tab=mine' })
      return
    }
    uni.switchTab({ url: '/pages/profile/index' })
  }, 400)
}

onBackPress(() => handleJobsMineBackPress(pageOptions))
</script>

<style scoped lang="scss">
.panel {
  padding: 34rpx 28rpx;
}

.textarea {
  height: 180rpx;
  padding-top: 20rpx;
}

.role-switch {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-bottom: 18rpx;
}

.role-chip {
  padding: 22rpx 24rpx;
  border-radius: 22rpx;
  background: #f8fafc;
  border: 2rpx solid transparent;
}

.role-chip.active {
  border-color: #1677ff;
  background: #eef6ff;
  box-shadow: 0 12rpx 24rpx rgba(22, 119, 255, 0.12);
}

.role-chip-title,
.role-chip-desc {
  display: block;
}

.role-chip-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}

.role-chip-desc,
.role-tip {
  font-size: 24rpx;
  line-height: 1.7;
  color: #64748b;
}

.role-chip-desc {
  margin-top: 10rpx;
}

.role-tip {
  margin: -4rpx 0 24rpx;
}
</style>
