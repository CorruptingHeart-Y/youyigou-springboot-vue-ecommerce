<template>
  <div>
    <h3>用户反馈</h3>
    <el-card style="max-width:600px">
      <el-form :model="form">
        <el-form-item label="反馈内容">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入您的反馈或建议" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="submit">提交反馈</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { userApi } from '@/api/endpoints'
import { ElMessage } from 'element-plus'

const form = reactive({ content: '' })
const submitLoading = ref(false)

const submit = async () => {
  if (!form.content.trim()) { ElMessage.warning('请输入反馈内容'); return }
  submitLoading.value = true
  try {
    await userApi.submitFeedback(form)
    ElMessage.success('感谢您的反馈，我们会尽快处理')
    form.content = ''
  } catch (e) {} finally {
    submitLoading.value = false
  }
}
</script>
