<template>
  <div>
    <h3>轮播图管理</h3>
    <el-button type="primary" @click="showAdd" style="margin-bottom:15px">新增轮播图</el-button>
    <el-table :data="banners" border stripe v-loading="loading">
      <el-table-column label="图片" width="120">
        <template #default="{row}"><img :src="row.imageUrl" style="width:100px;height:50px;object-fit:cover;border-radius:4px" /></template>
      </el-table-column>
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="linkUrl" label="链接" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{row}">
          <el-tag :type="row.status===1?'success':'danger'" effect="dark">{{ row.status===1?'启用':'禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button size="small" @click="editRow(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="doDel(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing?'编辑轮播图':'新增轮播图'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="图片URL"><el-input v-model="form.imageUrl" /></el-form-item>
        <el-form-item label="链接"><el-input v-model="form.linkUrl" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
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

const banners = ref([])
const dialogVisible = ref(false)
const editing = ref(false)
const loading = ref(false)
const saveLoading = ref(false)
const form = reactive({ title: '', imageUrl: '', linkUrl: '', sortOrder: 0, status: 1 })

const fetch = async () => {
  loading.value = true
  try {
    const res = await adminApi.getBanners()
    banners.value = res.data || []
  } catch (e) {} finally {
    loading.value = false
  }
}

const clearForm = () => { Object.keys(form).forEach(k => delete form[k]); }
const showAdd = () => {
  editing.value = false
  clearForm()
  Object.assign(form, { title: '', imageUrl: '', linkUrl: '', sortOrder: 0, status: 1 })
  dialogVisible.value = true
}
const editRow = (row) => {
  editing.value = true
  clearForm()
  Object.assign(form, { id: row.id, title: row.title, imageUrl: row.imageUrl, linkUrl: row.linkUrl, sortOrder: row.sortOrder, status: row.status })
  dialogVisible.value = true
}

const save = async () => {
  if (!form.title.trim()) { ElMessage.warning('请输入标题'); return }
  saveLoading.value = true
  try {
    if (editing.value) { await adminApi.updateBanner(form.id, form) }
    else { await adminApi.addBanner(form) }
    dialogVisible.value = false
    ElMessage.success(editing.value ? '轮播图更新成功' : '轮播图创建成功')
    fetch()
  } catch (e) {} finally {
    saveLoading.value = false
  }
}

const doDel = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该轮播图？', '确认删除', { type: 'warning' })
    await adminApi.deleteBanner(id)
    ElMessage.success('轮播图已删除')
    fetch()
  } catch (e) {}
}

onMounted(fetch)
</script>
