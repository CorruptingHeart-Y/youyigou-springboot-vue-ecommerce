<template>
  <div>
    <h3>反馈管理</h3>
    <el-table :data="feedbacks" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{row}"><el-tag :type="row.status===1?'success':'warning'" effect="dark">{{ row.status===1?'已处理':'待处理' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="reply" label="回复" min-width="150" show-overflow-tooltip />
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button v-if="row.status===0" size="small" type="primary" @click="showReply(row)">回复</el-button>
          <el-button v-if="row.status===0" size="small" type="success" @click="doResolve(row.id)">标记已处理</el-button>
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

    <el-dialog v-model="replyVisible" title="回复反馈" width="400px">
      <el-input v-model="replyText" type="textarea" :rows="3" placeholder="请输入回复内容" />
      <template #footer>
        <el-button @click="replyVisible=false">取消</el-button>
        <el-button type="primary" :loading="replyLoading" @click="doReply">确认回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/endpoints'
import { ElMessage, ElMessageBox } from 'element-plus'

const feedbacks = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)
const replyVisible = ref(false)
const replyId = ref(0)
const replyText = ref('')
const replyLoading = ref(false)

const fetch = async () => {
  loading.value = true
  try {
    const res = await adminApi.getFeedbacks({ page: page.value, size: pageSize })
    feedbacks.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {} finally {
    loading.value = false
  }
}

const showReply = (row) => { replyId.value = row.id; replyText.value = ''; replyVisible.value = true }
const doReply = async () => {
  if (!replyText.value.trim()) { ElMessage.warning('请输入回复内容'); return }
  replyLoading.value = true
  try {
    await adminApi.replyFeedback(replyId.value, replyText.value)
    replyVisible.value = false
    ElMessage.success('回复成功')
    fetch()
  } catch (e) {} finally {
    replyLoading.value = false
  }
}

const doResolve = async (id) => {
  try {
    await ElMessageBox.confirm('确定标记为已处理？', '确认操作', { type: 'info' })
    await adminApi.resolveFeedback(id)
    ElMessage.success('已标记为已处理')
    fetch()
  } catch (e) {}
}

onMounted(fetch)
</script>
