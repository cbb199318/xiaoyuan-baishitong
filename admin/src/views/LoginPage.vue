<template>
  <div class="login-shell">
    <div class="login-card">
      <h1>校园百事通后台</h1>
      <p>管理后台，支持用户、审核、举报和业务模块日常管理。</p>
      <el-form :model="form" label-position="top">
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入管理员手机号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
      </el-form>
      <el-button type="primary" class="submit" @click="submit">登录后台</el-button>
      <div class="tip">默认管理员：18800000000 / admin123</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'

const router = useRouter()
const form = reactive({
  phone: '18800000000',
  password: 'admin123',
})

const submit = async () => {
  try {
    const data: any = await api.post('/admin/login', form)
    localStorage.setItem('adminToken', data.token)
    router.push('/')
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败')
  }
}
</script>

<style scoped>
.login-shell {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: radial-gradient(circle at top, #dbeafe, #f3f6fb 55%);
}

.login-card {
  width: 460px;
  background: white;
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.1);
}

h1 {
  margin: 0 0 8px;
}

p {
  margin: 0 0 24px;
  color: #64748b;
}

.submit {
  width: 100%;
  height: 44px;
}

.tip {
  margin-top: 14px;
  color: #64748b;
  font-size: 13px;
}
</style>
