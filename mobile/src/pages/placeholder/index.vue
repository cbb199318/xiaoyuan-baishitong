<template>
  <view class="page-shell">
    <view class="card placeholder">
      <text class="title">{{ title }}</text>
      <text class="desc">这个模块的通用能力已经预留完成，具体业务流程将在下一阶段接入。</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const title = ref('功能建设中')

onLoad((query) => {
  title.value = query.title || '功能建设中'
})
</script>

<style scoped lang="scss">
.placeholder {
  padding: 60rpx 34rpx;
  text-align: center;
}

.title {
  display: block;
  font-size: 42rpx;
  font-weight: 800;
  margin-bottom: 18rpx;
}

.desc {
  color: #64748b;
  line-height: 1.7;
}
</style>
