<template>
  <div>
    <h3>用户管理</h3>
    <div style="display:flex;gap:10px;margin-bottom:15px;flex-wrap:wrap">
      <el-input v-model="keyword" placeholder="搜索用户" style="width:250px" @keyup.enter="fetch" />
      <el-button @click="fetch">搜索</el-button>
    </div>
    <el-table :data="users" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column label="角色" width="140" align="center">
        <template #default="scope">
          <template v-if="scope.row.role === 'SUPER_ADMIN'">
            <el-tag type="danger" effect="dark">超级管理员</el-tag>
          </template>
          <div v-else-if="scope.row.isEditingRole" style="display: inline-block;">
            <el-select
              v-model="scope.row.role"
              size="small"
              placement="bottom"
              automatic-dropdown
              @change="handleRoleChange(scope.row)"
              @visible-change="(v) => { if (!v) scope.row.isEditingRole = false }"
              style="width: 100px;"
            >
              <el-option label="普通用户" value="USER" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
          </div>
          <el-tag
            v-else
            :type="scope.row.role === 'ADMIN' ? 'danger' : 'info'"
            effect="plain"
            style="cursor: pointer; width: 80px;"
            @click="scope.row.isEditingRole = true"
          >
            {{ scope.row.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{row}">
          <el-tag :type="row.status===1?'success':'danger'" effect="dark">{{ row.status===1?'正常':'已禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{row}">
          <el-button v-if="row.role!=='SUPER_ADMIN'" size="small" :type="row.status===1?'warning':'success'" @click="toggleStatus(row)">{{ row.status===1?'禁用':'启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-if="total > pageSize"
      layout="total, prev, pager, next"
      :total="total"
      :page-size="pageSize"
      v-model:current-page="page"
      @current-change="fetch"
      style="margin-top:15px;justify-content:center"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/endpoints'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const keyword = ref('')
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)

const fetch = async () => {
  loading.value = true
  try {
    const res = await adminApi.getUsers({ page: page.value, size: pageSize, keyword: keyword.value || undefined })
    users.value = (res.data?.records || []).map(u => ({ ...u, isEditingRole: false }))
    total.value = res.data?.total || 0
  } catch (e) {} finally {
    loading.value = false
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定${action}用户 ${row.username}？`, '确认操作', { type: 'warning' })
    await adminApi.toggleUser(row.id, newStatus)
    ElMessage.success(`已${action}用户 ${row.username}`)
    fetch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleRoleChange = async (row) => {
  const newRole = row.role
  const oldRole = newRole === 'ADMIN' ? 'USER' : 'ADMIN'
  try {
    await ElMessageBox.confirm(`确定将用户 ${row.username} 的角色修改为 ${newRole === 'ADMIN' ? '管理员' : '普通用户'}？`, '确认修改', { type: 'warning' })
    await adminApi.updateUserRole(row.id, newRole)
    ElMessage.success('角色已更新')
  } catch (e) {
    row.role = oldRole
    if (e !== 'cancel') ElMessage.error('角色修改失败')
  }
  row.isEditingRole = false
}

onMounted(fetch)
</script>
