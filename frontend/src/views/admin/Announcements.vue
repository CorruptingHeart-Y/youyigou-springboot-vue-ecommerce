<template>
  <div>
    <h3>公告管理</h3>
    <el-button type="primary" @click="showAdd" style="margin-bottom:15px">新增公告</el-button>
    <el-table :data="announcements" border stripe v-loading="loading">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{row}">
          <el-tag :type="row.status===1?'success':'info'" effect="dark">{{ row.status===1?'已发布':'草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template #default="{row}">{{ row.createTime?.substring(0,19) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button size="small" @click="editRow(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="doDel(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing?'编辑公告':'新增公告'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="5" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="发布" inactive-text="草稿" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '@/api/endpoints'
import { ElMessage, ElMessageBox } from 'element-plus'

const announcements = ref([])
const dialogVisible = ref(false)
const editing = ref(false)
const loading = ref(false)
const saveLoading = ref(false)
const form = reactive({ title: '', content: '', status: 0 })

const fetch = async () => {
  loading.value = true
  try {
    const res = await adminApi.getAnnouncements()
    announcements.value = res.data || []
  } catch (e) {} finally {
    loading.value = false
  }
}

const showAdd = () => { editing.value = false; Object.assign(form, { title: '', content: '', status: 0 }); dialogVisible.value = true }
const editRow = (row) => { editing.value = true; Object.assign(form, row); dialogVisible.value = true }

const save = async () => {
  if (!form.title.trim()) { ElMessage.warning('请输入公告标题'); return }
  if (!form.content.trim()) { ElMessage.warning('请输入公告内容'); return }
  saveLoading.value = true
  try {
    if (editing.value) { await adminApi.updateAnnouncement(form.id, form) }
    else { await adminApi.addAnnouncement(form) }
    dialogVisible.value = false
    ElMessage.success(editing.value ? '公告更新成功' : '公告创建成功')
    fetch()
  } catch (e) {} finally {
    saveLoading.value = false
  }
}

const doDel = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该公告？', '确认删除', { type: 'warning' })
    await adminApi.deleteAnnouncement(id)
    ElMessage.success('公告已删除')
    fetch()
  } catch (e) {}
}

onMounted(fetch)
</script>
