<template>
  <div class="auth-page">
    <div class="auth-bg">
      <div class="auth-shape shape1"></div>
      <div class="auth-shape shape2"></div>
      <div class="auth-shape shape3"></div>
    </div>
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">🛒</div>
        <h2>欢迎回来</h2>
        <p>登录您的账户继续购物</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password @keyup.enter="doLogin" />
        </el-form-item>
        <el-form-item>
          <div class="auth-options">
            <el-checkbox v-model="form.rememberMe">记住密码</el-checkbox>
            <router-link to="/forgot-password" class="auth-link">忘记密码？</router-link>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="doLogin" class="auth-btn">登 录</el-button>
        </el-form-item>
      </el-form>
      <div class="auth-footer">
        还没有账号？<router-link to="/register" class="auth-link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '', rememberMe: false })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const doLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功，欢迎回来！')
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {} finally {
    loading.value = false
  }
}
</script>

<style scoped>
@import '@/assets/auth.css';

.auth-options { display: flex; justify-content: space-between; align-items: center; width: 100%; }
</style>
