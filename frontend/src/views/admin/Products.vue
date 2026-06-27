<template>
  <div>
    <h3>商品管理</h3>
    <div style="display:flex;gap:10px;margin-bottom:15px;flex-wrap:wrap">
      <el-input v-model="keyword" placeholder="搜索商品" style="width:200px" @keyup.enter="fetch" />
      <el-button @click="fetch">搜索</el-button>
      <el-button type="primary" @click="showAdd">新增商品</el-button>
      <el-upload :show-file-list="false" :before-upload="handleImport" accept=".xlsx,.xls">
        <el-button>批量导入</el-button>
      </el-upload>
    </div>
    <el-table :data="products" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="封面" width="80">
        <template #default="{row}"><img :src="row.coverImage || 'https://via.placeholder.com/40'" style="width:40px;height:40px;object-fit:cover" /></template>
      </el-table-column>
      <el-table-column prop="name" label="名称" min-width="120" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="price" label="价格" width="80" />
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="sales" label="销量" width="80" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{row}"><el-tag :type="row.status===1?'success':'danger'">{{ row.status===1?'上架':'下架' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button size="small" @click="editProduct(row)">编辑</el-button>
          <el-button size="small" :type="row.status===1?'warning':'success'" @click="toggleStatus(row)">{{ row.status===1?'下架':'上架' }}</el-button>
          <el-button size="small" type="danger" @click="delProduct(row.id)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editing?'编辑商品':'新增商品'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="原价"><el-input-number v-model="form.originalPrice" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="封面图"><el-input v-model="form.coverImage" placeholder="图片URL或上传" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="详情"><el-input v-model="form.detail" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="关键词"><el-input v-model="form.keywords" /></el-form-item>
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

const products = ref([])
const categories = ref([])
const dialogVisible = ref(false)
const editing = ref(false)
const loading = ref(false)
const saveLoading = ref(false)
const keyword = ref('')
const page = ref(1)
const pageSize = 10
const total = ref(0)
const form = reactive({ name: '', categoryId: null, price: 0, originalPrice: null, stock: 0, coverImage: '', description: '', detail: '', keywords: '' })

const fetch = async () => {
  loading.value = true
  try {
    const res = await adminApi.getProducts({ page: page.value, size: pageSize, keyword: keyword.value || undefined })
    products.value = res.data?.records || []
    total.value = res.data?.total || 0
    const catRes = await adminApi.getCategories()
    categories.value = Array.isArray(catRes.data) ? catRes.data.flatMap(c => [c, ...(c.children || [])]) : []
  } catch (e) {} finally {
    loading.value = false
  }
}

const showAdd = () => { editing.value = false; Object.assign(form, { name: '', categoryId: null, price: 0, originalPrice: null, stock: 0, coverImage: '', description: '', detail: '', keywords: '' }); dialogVisible.value = true }
const editProduct = (row) => { editing.value = true; Object.assign(form, row); dialogVisible.value = true }

const save = async () => {
  saveLoading.value = true
  try {
    if (editing.value) { await adminApi.updateProduct(form.id, form) }
    else { await adminApi.addProduct(form) }
    dialogVisible.value = false; ElMessage.success('保存成功'); fetch()
  } catch (e) {} finally {
    saveLoading.value = false
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(`确定${action}商品 ${row.name}？`, '确认操作', { type: 'warning' })
    await adminApi.toggleProduct(row.id, newStatus)
    ElMessage.success(`已${action}`)
    fetch()
  } catch (e) {}
}

const delProduct = async (id) => {
  try { await ElMessageBox.confirm('确定删除该商品？', '确认删除', { type: 'warning' }); await adminApi.deleteProduct(id); ElMessage.success('删除成功'); fetch() } catch (e) {}
}

const handleImport = async (file) => {
  const fd = new FormData(); fd.append('file', file)
  try { await adminApi.importProducts(fd); ElMessage.success('导入成功'); fetch() } catch (e) {}
  return false
}

onMounted(fetch)
</script>
