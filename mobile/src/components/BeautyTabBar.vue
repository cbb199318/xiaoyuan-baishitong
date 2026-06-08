<template>
  <view class="beauty-tabbar-shell">
    <view class="beauty-tabbar">
      <view
        :class="['beauty-tab-item', current === 'home' ? 'active' : '']"
        @click="emit('change', 'home')"
      >
        <text class="beauty-tab-icon">◦</text>
        <text class="beauty-tab-label">首页</text>
      </view>

      <view class="beauty-tab-publish-wrap">
        <view
          :class="['beauty-tab-publish', current === 'publish' ? 'active' : '']"
          @click="emit('change', 'publish')"
        >
          <text class="beauty-tab-plus">+</text>
        </view>
        <text :class="['beauty-tab-publish-label', current === 'publish' ? 'active' : '']">发布</text>
      </view>

      <view
        :class="['beauty-tab-item', current === 'mine' ? 'active' : '']"
        @click="emit('change', 'mine')"
      >
        <text class="beauty-tab-icon">◦</text>
        <text class="beauty-tab-label">我的</text>
      </view>
    </view>
  </view>
</template>

<script setup>
defineProps({
  current: {
    type: String,
    default: 'home',
  },
})

const emit = defineEmits(['change'])
</script>

<style scoped lang="scss">
.beauty-tabbar-shell {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 50;
  padding: 0 18rpx calc(14rpx + env(safe-area-inset-bottom));
  background: linear-gradient(180deg, rgba(244, 247, 251, 0) 0%, rgba(244, 247, 251, 0.78) 36%, #f4f7fb 100%);
}

.beauty-tabbar {
  position: relative;
  display: grid;
  grid-template-columns: 1fr 176rpx 1fr;
  align-items: end;
  min-height: 118rpx;
  padding: 12rpx 12rpx 10rpx;
  border-radius: 36rpx;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 22rpx 50rpx rgba(190, 24, 93, 0.12);
}

.beauty-tab-item,
.beauty-tab-publish-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.beauty-tab-item {
  min-height: 88rpx;
  color: #94a3b8;
}

.beauty-tab-item.active {
  color: #be185d;
}

.beauty-tab-icon {
  font-size: 28rpx;
  line-height: 1;
}

.beauty-tab-label,
.beauty-tab-publish-label {
  margin-top: 8rpx;
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1;
}

.beauty-tab-publish-wrap {
  position: relative;
  transform: translateY(-20rpx);
}

.beauty-tab-publish {
  width: 108rpx;
  height: 108rpx;
  border-radius: 32rpx;
  background: linear-gradient(135deg, #ec4899, #fb7185);
  box-shadow: 0 20rpx 38rpx rgba(236, 72, 153, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
}

.beauty-tab-publish.active {
  box-shadow: 0 24rpx 44rpx rgba(225, 29, 72, 0.34);
}

.beauty-tab-plus {
  color: #ffffff;
  font-size: 56rpx;
  line-height: 1;
  font-weight: 400;
  transform: translateY(-2rpx);
}

.beauty-tab-publish-label {
  color: #94a3b8;
}

.beauty-tab-publish-label.active {
  color: #be185d;
}
</style>
