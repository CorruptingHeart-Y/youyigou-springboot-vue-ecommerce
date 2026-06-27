<template>
  <div class="auth-page">
    <div class="auth-bg">
      <div class="auth-shape shape1"></div>
      <div class="auth-shape shape2"></div>
      <div class="auth-shape shape3"></div>
    </div>
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">✨</div>
        <h2>创建账户</h2>
        <p>注册后即可享受购物乐趣</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱" prefix-icon="Message" />
        </el-form-item>
        <el-form-item prop="code">
          <div class="code-row">
            <el-input v-model="form.code" placeholder="验证码" prefix-icon="Key" style="flex:1" />
            <el-button :disabled="sendingCode || countdown > 0" :loading="sendingCode" @click="sendCode" class="code-btn" type="primary" plain>
              {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（6-20位）" prefix-icon="Lock" show-password @keyup.enter="doRegister" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="doRegister" class="auth-btn">注 册</el-button>
        </el-form-item>
      </el-form>
      <div class="auth-footer">
        已有账号？<router-link to="/login" class="auth-link">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/endpoints'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)

const form = reactive({ username: '', email: '', password: '', code: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, max: 20, message: '长度3-20位', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 20, message: '长度6-20位', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }, { min: 6, max: 6, message: '验证码为6位', trigger: 'blur' }],
}

const sendCode = async () => {
  if (!form.email) { ElMessage.warning('请先输入邮箱'); return }
  sendingCode.value = true
  try {
    await authApi.sendCode({ email: form.email, type: 'register' })
    ElMessage.success('验证码已发送至您的邮箱')
    countdown.value = 60
    const timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '发送失败')
  } finally {
    sendingCode.value = false
  }
}

const doRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authApi.register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
@import '@/assets/auth.css';

.code-row { display: flex; gap: 10px; width: 100%; }
.code-btn { flex-shrink: 0; min-width: 110px; }

@media (max-width: 480px) {
  .code-btn { min-width: 90px; font-size: 12px; }
}
</style>
