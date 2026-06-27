<template>
  <div>
    <h3>评价管理</h3>
    <el-table :data="reviews" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户" width="100" />
      <el-table-column prop="productName" label="商品" min-width="120" show-overflow-tooltip />
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
      <el-table-column prop="rating" label="评分" width="80" align="center">
        <template #default="{row}">
          <el-rate v-model="row.rating" disabled size="small" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="160">
        <template #default="{row}">{{ row.createTime?.substring(0,19) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center">
        <template #default="{row}">
          <el-button size="small" type="danger" @click="doDel(row.id)">删除</el-button>
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

const reviews = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)

const fetch = async () => {
  loading.value = true
  try {
    const res = await adminApi.getReviews({ page: page.value, size: pageSize })
    reviews.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {} finally {
    loading.value = false
  }
}

const doDel = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该评价？此操作不可恢复', '确认删除', { type: 'warning' })
    await adminApi.deleteReview(id)
    ElMessage.success('评价已删除')
    fetch()
  } catch (e) {}
}

onMounted(fetch)
</script>
