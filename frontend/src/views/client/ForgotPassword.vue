<template>
  <div class="auth-page">
    <div class="auth-bg">
      <div class="auth-shape shape1"></div>
      <div class="auth-shape shape2"></div>
      <div class="auth-shape shape3"></div>
    </div>
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">🔐</div>
        <h2>找回密码</h2>
        <p>通过邮箱验证码重置您的密码</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="注册邮箱" prefix-icon="Message" />
        </el-form-item>
        <el-form-item prop="code">
          <div class="code-row">
            <el-input v-model="form.code" placeholder="验证码" prefix-icon="Key" style="flex:1" />
            <el-button :disabled="sendingCode || countdown > 0" :loading="sendingCode" @click="sendCode" class="code-btn" type="primary" plain>
              {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input v-model="form.newPassword" type="password" placeholder="新密码（6-20位）" prefix-icon="Lock" show-password @keyup.enter="doReset" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="doReset" class="auth-btn">重置密码</el-button>
        </el-form-item>
      </el-form>
      <div class="auth-footer">
        <router-link to="/login" class="auth-link">← 返回登录</router-link>
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

const form = reactive({ email: '', code: '', newPassword: '' })

const rules = {
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, max: 20, message: '长度6-20位', trigger: 'blur' }]
}

const sendCode = async () => {
  if (!form.email) { ElMessage.warning('请先输入邮箱'); return }
  sendingCode.value = true
  try {
    await authApi.sendCode({ email: form.email, type: 'reset' })
    ElMessage.success('验证码已发送至您的邮箱')
    countdown.value = 60
    const timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '发送失败')
  } finally {
    sendingCode.value = false
  }
}

const doReset = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authApi.resetPassword(form)
    ElMessage.success('密码重置成功，请使用新密码登录')
    router.push('/login')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '重置失败')
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
