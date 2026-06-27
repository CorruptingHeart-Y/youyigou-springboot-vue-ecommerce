<template>
  <div class="profile-container">
    <h2 class="profile-title">个人中心</h2>

    <el-row :gutter="24">
      <el-col :xs="24" :sm="6">
        <el-card class="profile-card" shadow="hover">
          <div class="avatar-section">
            <el-upload
              class="avatar-uploader"
              :http-request="customUpload"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
            >
              <el-avatar :size="88" :src="form.avatar || undefined" class="avatar-preview">
                {{ userStore.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="avatar-overlay"><el-icon><Camera /></el-icon></div>
            </el-upload>
            <p class="avatar-tip">点击更换头像</p>
          </div>

          <div class="user-name">{{ form.nickname || form.username }}</div>
          <div class="user-role">
            <el-tag :type="userStore.isAdmin() ? 'danger' : 'primary'" effect="plain" size="small">
              {{ userStore.isAdmin() ? '超级管理员' : '普通会员' }}
            </el-tag>
          </div>

          <el-divider />

          <div class="user-info-list">
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span>{{ form.username }}</span>
            </div>
            <div class="info-item" v-if="form.email">
              <el-icon><Message /></el-icon>
              <span>{{ form.email }}</span>
            </div>
            <div class="info-item" v-if="form.phone">
              <el-icon><Phone /></el-icon>
              <span>{{ form.phone }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="18">
        <el-card class="settings-card" shadow="hover">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本设置" name="basic">
              <el-form :model="form" label-width="80px" v-loading="profileLoading" class="profile-form">
                <el-form-item label="用户名">
                  <el-input v-model="form.username" disabled />
                </el-form-item>
                <el-form-item label="昵称">
                  <el-input v-model="form.nickname" placeholder="请输入昵称" />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="form.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="form.phone" placeholder="请输入手机号" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="saveLoading" @click="saveProfile">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="安全设置" name="security">
              <el-form :model="pwForm" label-width="100px" class="profile-form">
                <el-form-item label="原密码">
                  <el-input v-model="pwForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
                </el-form-item>
                <el-form-item label="新密码">
                  <el-input v-model="pwForm.newPassword" type="password" show-password placeholder="请设置新密码（至少6位）" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="pwLoading" @click="savePassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { authApi } from '@/api/endpoints'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Camera, User, Message, Phone } from '@element-plus/icons-vue'
import api from '@/api/index'

const userStore = useUserStore()
const form = reactive({ username: '', nickname: '', email: '', phone: '', avatar: '' })
const pwForm = reactive({ oldPassword: '', newPassword: '' })
const profileLoading = ref(false)
const saveLoading = ref(false)
const pwLoading = ref(false)
const activeTab = ref('basic')

const customUpload = async (option) => {
  const formData = new FormData()
  formData.append('file', option.file)
  try {
    const res = await api.post('/admin/site/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200) {
      handleAvatarSuccess(res)
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
}

onMounted(async () => {
  profileLoading.value = true
  try {
    const res = await authApi.getProfile()
    Object.assign(form, res.data)
  } catch (e) {} finally {
    profileLoading.value = false
  }
})

const beforeAvatarUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) { ElMessage.error('仅支持 JPG/PNG/GIF/WEBP 格式'); return false }
  if (!isLt2M) { ElMessage.error('图片大小不能超过 2MB'); return false }
  return true
}

const handleAvatarSuccess = async (response) => {
  const avatarUrl = response.data || response
  try {
    await authApi.updateAvatar(avatarUrl)
    form.avatar = avatarUrl
    userStore.updateAvatar(avatarUrl)
    ElMessage.success('头像更新成功')
  } catch (e) {
    ElMessage.error('头像保存失败')
  }
}

const saveProfile = async () => {
  saveLoading.value = true
  try {
    await authApi.updateProfile(form)
    userStore.nickname = form.nickname
    localStorage.setItem('nickname', form.nickname)
    ElMessage.success('个人信息保存成功')
  } catch (e) {} finally {
    saveLoading.value = false
  }
}

const savePassword = async () => {
  if (!pwForm.oldPassword || !pwForm.newPassword) { ElMessage.warning('请填写完整'); return }
  if (pwForm.newPassword.length < 6) { ElMessage.warning('新密码至少6位'); return }
  pwLoading.value = true
  try {
    await authApi.updatePassword(pwForm)
    ElMessage.success('密码修改成功，请重新登录')
    pwForm.oldPassword = ''
    pwForm.newPassword = ''
  } catch (e) {} finally {
    pwLoading.value = false
  }
}
</script>

<style scoped>
.profile-container {
  max-width: 1320px;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

.profile-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 24px 0;
}

/* ===== 左侧名片卡 ===== */
.profile-card {
  border-radius: 8px;
  text-align: center;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 0 4px;
}
.avatar-uploader {
  position: relative;
  cursor: pointer;
  display: inline-block;
}
.avatar-preview {
  border: 3px solid #e4e7ed;
  transition: all 0.3s;
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  background: #ecf5ff;
}
.avatar-preview:hover {
  border-color: #409eff;
}
.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.3s;
  color: #fff;
  font-size: 22px;
}
.avatar-uploader:hover .avatar-overlay {
  opacity: 1;
}
.avatar-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
  margin-bottom: 0;
}

.user-name {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
  margin-top: 6px;
}
.user-role {
  margin-top: 8px;
}

.user-info-list {
  text-align: left;
  padding: 0 4px;
}
.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  font-size: 13px;
  color: #606266;
}
.info-item .el-icon {
  color: #909399;
  font-size: 15px;
  flex-shrink: 0;
}

/* ===== 右侧设置卡 ===== */
.settings-card {
  border-radius: 8px;
}

.profile-form {
  max-width: 520px;
  padding-top: 8px;
}

@media (max-width: 768px) {
  .profile-container {
    padding: 10px;
  }
  .profile-card {
    margin-bottom: 16px;
  }
}
</style>
