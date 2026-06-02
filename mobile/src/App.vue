<script>
import { createPinia, setActivePinia } from 'pinia'
import { useUserStore } from './stores/user'
import { addSocketListener, connectSocket } from './utils/socket'

const pinia = createPinia()
setActivePinia(pinia)
const store = useUserStore()
let unsubscribe = null

export default {
  onLaunch() {
    const token = uni.getStorageSync('token')
    if (!token) {
      uni.reLaunch({ url: '/pages/login/index' })
      return
    }
    store.token = token
    connectSocket(token)
    store.fetchUnread()
    store.fetchProfile()
    store.syncAnnouncement()
    unsubscribe = addSocketListener((payload) => {
      store.applySocketEvent(payload)
    })
  },
  onHide() {
    if (!unsubscribe) {
      return
    }
    unsubscribe()
    unsubscribe = null
  },
}
</script>

<style lang="scss">
page {
  background: #f4f7fb;
  color: #1f2937;
  font-size: 28rpx;
}

.page-shell {
  min-height: 100vh;
  padding: 32rpx 28rpx 48rpx;
  box-sizing: border-box;
}

.card {
  background: #ffffff;
  border-radius: 24rpx;
  box-shadow: 0 12rpx 28rpx rgba(15, 23, 42, 0.06);
}

.section-title {
  font-size: 34rpx;
  font-weight: 700;
  margin-bottom: 20rpx;
}

.field-label {
  font-size: 26rpx;
  color: #64748b;
  margin-bottom: 10rpx;
}

.input {
  width: 100%;
  height: 84rpx;
  border-radius: 18rpx;
  background: #f8fafc;
  padding: 0 24rpx;
  box-sizing: border-box;
  margin-bottom: 24rpx;
}

.primary-btn {
  height: 88rpx;
  line-height: 88rpx;
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #fff;
  border-radius: 20rpx;
  text-align: center;
  font-weight: 600;
}

.ghost-btn {
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 20rpx;
  text-align: center;
  border: 2rpx solid #dbe4f0;
  color: #2d3748;
}
</style>
