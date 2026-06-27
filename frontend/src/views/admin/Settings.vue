<template>
  <div class="settings-page">
    <h3>系统设置</h3>

    <el-card style="margin-bottom:20px">
      <h4>管理员信息</h4>
      <el-form :model="adminForm" label-width="100px" v-loading="profileLoading" style="max-width:500px">
        <el-form-item label="用户名"><el-input v-model="adminForm.username" disabled /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="adminForm.nickname" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="adminForm.email" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="adminForm.phone" /></el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saveProfileLoading" @click="saveAdminProfile">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-bottom:20px">
      <h4>权限分级管理</h4>
      <el-table :data="rolePermissions" border stripe style="margin-bottom:16px">
        <el-table-column prop="role" label="角色" width="150">
          <template #default="{row}">
            <el-tag :type="row.role==='SUPER_ADMIN'?'danger':row.role==='ADMIN'?'warning':'info'" effect="dark">
              {{ row.role === 'SUPER_ADMIN' ? '超级管理员' : row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="商品管理" width="100" align="center">
          <template #default="{row}">
            <el-icon color="#67c23a" v-if="row.product"><CircleCheckFilled /></el-icon>
            <el-icon color="#f56c6c" v-else><CircleCloseFilled /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="订单管理" width="100" align="center">
          <template #default="{row}">
            <el-icon color="#67c23a" v-if="row.order"><CircleCheckFilled /></el-icon>
            <el-icon color="#f56c6c" v-else><CircleCloseFilled /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="用户管理" width="100" align="center">
          <template #default="{row}">
            <el-icon color="#67c23a" v-if="row.user"><CircleCheckFilled /></el-icon>
            <el-icon color="#f56c6c" v-else><CircleCloseFilled /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="内容管理" width="100" align="center">
          <template #default="{row}">
            <el-icon color="#67c23a" v-if="row.content"><CircleCheckFilled /></el-icon>
            <el-icon color="#f56c6c" v-else><CircleCloseFilled /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="权限管理" width="100" align="center">
          <template #default="{row}">
            <el-icon color="#67c23a" v-if="row.role === 'SUPER_ADMIN'"><CircleCheckFilled /></el-icon>
            <el-icon color="#f56c6c" v-else><CircleCloseFilled /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="说明" min-width="200">
          <template #default="{row}">{{ row.desc }}</template>
        </el-table-column>
      </el-table>

      <div v-if="isSuperAdmin">
        <el-divider />
        <h4>用户角色管理</h4>
        <el-table :data="users" border stripe v-loading="usersLoading" style="margin-top:10px">
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="nickname" label="昵称" width="120" />
          <el-table-column label="角色" width="160">
            <template #default="{row}">
              <el-tag :type="row.role==='SUPER_ADMIN'?'danger':row.role==='ADMIN'?'warning':'info'">
                {{ row.role === 'SUPER_ADMIN' ? '超级管理员' : row.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{row}">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="dark">
                {{ row.status === 1 ? '正常' : '已禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="200">
            <template #default="{row}">
              <el-select v-model="row.role" size="small" style="width:120px" @change="updateRole(row)">
                <el-option label="普通用户" value="USER" />
                <el-option label="管理员" value="ADMIN" />
                <el-option label="超级管理员" value="SUPER_ADMIN" />
              </el-select>
              <el-button size="small" :type="row.status === 1 ? 'danger' : 'success'" @click="toggleUserStatus(row)" style="margin-left:8px">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-if="usersTotal > 10"
          layout="prev, pager, next"
          :total="usersTotal"
          :page-size="10"
          v-model:current-page="usersPage"
          @current-change="fetchUsers"
          style="margin-top:12px;justify-content:center"
        />
      </div>
    </el-card>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="12">
        <el-card style="margin-bottom:20px">
          <h4>修改密码</h4>
          <el-form :model="pwForm" label-width="100px">
            <el-form-item label="原密码"><el-input v-model="pwForm.oldPassword" type="password" show-password /></el-form-item>
            <el-form-item label="新密码"><el-input v-model="pwForm.newPassword" type="password" show-password /></el-form-item>
            <el-form-item><el-button type="primary" :loading="pwLoading" @click="savePassword">修改密码</el-button></el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-card style="margin-bottom:20px">
          <h4>图片上传</h4>
          <el-upload :show-file-list="true" :on-success="onUploadSuccess" :action="uploadUrl" :headers="uploadHeaders" list-type="picture">
            <el-button type="primary">上传图片</el-button>
          </el-upload>
          <div v-if="uploadedUrl" style="margin-top:10px">
            <p>上传成功: {{ uploadedUrl }}</p>
            <img :src="uploadedUrl" style="max-width:200px" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { authApi, adminApi } from '@/api/endpoints'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const isSuperAdmin = computed(() => userStore.role === 'SUPER_ADMIN')

const adminForm = reactive({ username: '', nickname: '', email: '', phone: '' })
const profileLoading = ref(false)
const saveProfileLoading = ref(false)
const pwForm = reactive({ oldPassword: '', newPassword: '' })
const pwLoading = ref(false)
const uploadedUrl = ref('')
const uploadUrl = '/api/admin/site/upload'
const uploadHeaders = { Authorization: `Bearer ${localStorage.getItem('token')}` }

const users = ref([])
const usersLoading = ref(false)
const usersTotal = ref(0)
const usersPage = ref(1)

const rolePermissions = [
  { role: 'SUPER_ADMIN', product: true, order: true, user: true, content: true, desc: '拥有系统全部权限，可管理所有资源和用户角色' },
  { role: 'ADMIN', product: true, order: true, user: true, content: true, desc: '可管理商品/订单/用户/内容，但不能修改其他管理员角色' },
  { role: 'USER', product: false, order: false, user: false, content: false, desc: '仅可使用客户端功能：浏览商品、购物、下单、评价' }
]

onMounted(async () => {
  profileLoading.value = true
  try {
    const res = await authApi.getProfile()
    Object.assign(adminForm, res.data)
  } catch (e) {} finally {
    profileLoading.value = false
  }
  if (isSuperAdmin.value) {
    fetchUsers()
  }
})

const fetchUsers = async () => {
  usersLoading.value = true
  try {
    const res = await adminApi.getUsers({ page: usersPage.value, size: 10 })
    users.value = res.data?.records || res.data || []
    usersTotal.value = res.data?.total || users.value.length
  } catch (e) {} finally {
    usersLoading.value = false
  }
}

const saveAdminProfile = async () => {
  saveProfileLoading.value = true
  try {
    await authApi.updateProfile(adminForm)
    userStore.nickname = adminForm.nickname
    localStorage.setItem('nickname', adminForm.nickname)
    ElMessage.success('管理员信息保存成功')
  } catch (e) {} finally {
    saveProfileLoading.value = false
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

const updateRole = async (user) => {
  try {
    await ElMessageBox.confirm(`确定将用户 ${user.username} 的角色修改为 ${user.role}？`, '确认修改角色', { type: 'warning' })
    await adminApi.updateUserRole(user.id, user.role)
    ElMessage.success('角色修改成功')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('角色修改失败')
  }
}

const toggleUserStatus = async (user) => {
  const newStatus = user.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定${action}用户 ${user.username}？`, `确认${action}`, { type: 'warning' })
    await adminApi.toggleUser(user.id, newStatus)
    user.status = newStatus
    ElMessage.success(`已${action}用户 ${user.username}`)
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(`${action}失败`)
  }
}

const onUploadSuccess = (res) => {
  if (res.data) uploadedUrl.value = res.data
  else uploadedUrl.value = res.message || '上传成功'
}
</script>

<style scoped>
.settings-page { padding: 0; }
</style>
