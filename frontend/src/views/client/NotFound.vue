<template>
  <div class="not-found-page">
    <div class="not-found-card">
      <div class="not-found-code">404</div>
      <h2>页面不存在</h2>
      <p>您访问的页面可能已被移除或地址输入有误</p>
      <el-button type="primary" size="large" @click="$router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.not-found-page {
  display: flex; justify-content: center; align-items: center;
  min-height: 100vh; background: #f5f7fa;
}
.not-found-card {
  text-align: center; padding: 60px 40px; background: #fff;
  border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}
.not-found-code {
  font-size: 120px; font-weight: bold; color: #dcdfe6; line-height: 1;
  margin-bottom: 16px;
}
.not-found-card h2 { color: #303133; margin-bottom: 12px; }
.not-found-card p { color: #909399; margin-bottom: 24px; }
</style>