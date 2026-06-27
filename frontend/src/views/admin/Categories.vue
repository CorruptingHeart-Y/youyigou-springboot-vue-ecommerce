<template>
  <div>
    <h3>分类管理</h3>
    <el-button type="primary" @click="showAdd" style="margin-bottom:15px">新增分类</el-button>
    <el-table :data="flatCategories" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="parentId" label="父级ID" width="80" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button size="small" @click="editCat(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="delCat(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing?'编辑分类':'新增分类'" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="父级"><el-input-number v-model="form.parentId" :min="0" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { adminApi } from '@/api/endpoints'
import { ElMessage, ElMessageBox } from 'element-plus'

const allCategories = ref([])
const dialogVisible = ref(false)
const editing = ref(false)
const loading = ref(false)
const saveLoading = ref(false)
const form = reactive({ name: '', parentId: 0, sortOrder: 0 })

const flatCategories = computed(() => allCategories.value.flatMap(c => [c, ...(c.children || [])]))

const fetch = async () => {
  loading.value = true
  try {
    const res = await adminApi.getCategories()
    allCategories.value = res.data || []
  } catch (e) {} finally {
    loading.value = false
  }
}

const showAdd = () => { editing.value = false; Object.assign(form, { name: '', parentId: 0, sortOrder: 0 }); dialogVisible.value = true }
const editCat = (row) => { editing.value = true; Object.assign(form, row); dialogVisible.value = true }

const save = async () => {
  if (!form.name.trim()) { ElMessage.warning('请输入分类名称'); return }
  saveLoading.value = true
  try {
    if (editing.value) { await adminApi.updateCategory(form.id, form) }
    else { await adminApi.addCategory(form) }
    dialogVisible.value = false
    ElMessage.success(editing.value ? '分类更新成功' : '分类创建成功')
    fetch()
  } catch (e) {} finally {
    saveLoading.value = false
  }
}

const delCat = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该分类？删除后不可恢复', '确认删除', { type: 'warning' })
    await adminApi.deleteCategory(id)
    ElMessage.success('分类已删除')
    fetch()
  } catch (e) {}
}

onMounted(fetch)
</script>
