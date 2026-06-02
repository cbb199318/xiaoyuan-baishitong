<template>
  <view class="uploader">
    <view v-if="modelValue?.url" class="preview-card">
      <image class="preview-image" :src="modelValue.url" mode="aspectFill" @click="handlePreview"></image>
      <view class="preview-actions">
        <view class="mini-btn" @click="handlePreview">预览</view>
        <view class="mini-btn ghost" @click="handleSelect">{{ loading ? '上传中...' : '重新上传' }}</view>
      </view>
    </view>
    <view v-else class="upload-empty" @click="handleSelect">
      <text class="upload-title">{{ loading ? '上传中...' : title }}</text>
      <text class="upload-tip">{{ tip }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { previewImage, uploadImage } from '../utils/upload'

const props = defineProps({
  modelValue: {
    type: Object,
    default: null,
  },
  bizType: {
    type: String,
    default: 'common',
  },
  title: {
    type: String,
    default: '点击上传图片',
  },
  tip: {
    type: String,
    default: '支持 JPG / PNG，上传后可预览',
  },
})

const emit = defineEmits(['update:modelValue'])
const loading = ref(false)

const handleSelect = async () => {
  if (loading.value) {
    return
  }
  loading.value = true
  try {
    const file = await uploadImage({ bizType: props.bizType })
    emit('update:modelValue', file)
  } finally {
    loading.value = false
  }
}

const handlePreview = () => {
  previewImage(props.modelValue?.url)
}
</script>

<style scoped lang="scss">
.uploader {
  margin-bottom: 24rpx;
}

.upload-empty,
.preview-card {
  border-radius: 20rpx;
  background: #f8fafc;
  border: 2rpx dashed #cbd5e1;
  overflow: hidden;
}

.upload-empty {
  min-height: 180rpx;
  padding: 28rpx 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.upload-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #0f172a;
}

.upload-tip {
  margin-top: 10rpx;
  color: #64748b;
  font-size: 24rpx;
}

.preview-image {
  width: 100%;
  height: 240rpx;
  display: block;
  background: #e2e8f0;
}

.preview-actions {
  display: flex;
  gap: 16rpx;
  padding: 18rpx 20rpx 20rpx;
}

.mini-btn {
  flex: 1;
  height: 70rpx;
  line-height: 70rpx;
  text-align: center;
  border-radius: 16rpx;
  color: #1677ff;
  background: rgba(22, 119, 255, 0.08);
  font-size: 26rpx;
}

.mini-btn.ghost {
  background: #fff;
  border: 2rpx solid #dbe4f0;
  color: #334155;
}
</style>
