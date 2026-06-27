<template>
  <div class="admin-layout">
    <aside class="admin-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <h2 class="sidebar-title">电商管理后台</h2>
      <el-menu :default-active="route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff" :collapse="sidebarCollapsed">
        <el-menu-item index="/admin"><el-icon><DataAnalysis /></el-icon><span>数据看板</span></el-menu-item>
        <el-sub-menu index="goods">
          <template #title><el-icon><Goods /></el-icon><span>商品管理</span></template>
          <el-menu-item index="/admin/products">商品列表</el-menu-item>
          <el-menu-item index="/admin/categories">分类管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/admin/orders"><el-icon><Document /></el-icon><span>订单管理</span></el-menu-item>
        <el-menu-item index="/admin/users"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
        <el-sub-menu index="site">
          <template #title><el-icon><Picture /></el-icon><span>网站管理</span></template>
          <el-menu-item index="/admin/banners">轮播图</el-menu-item>
          <el-menu-item index="/admin/announcements">公告管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/admin/promotions"><el-icon><Present /></el-icon><span>促销管理</span></el-menu-item>
        <el-menu-item index="/admin/feedbacks"><el-icon><ChatDotRound /></el-icon><span>反馈管理</span></el-menu-item>
        <el-menu-item index="/admin/reviews"><el-icon><Comment /></el-icon><span>评价管理</span></el-menu-item>
        <el-menu-item index="/admin/settings"><el-icon><Setting /></el-icon><span>系统设置</span></el-menu-item>
      </el-menu>
    </aside>
    <div class="admin-main">
      <header class="admin-header">
        <el-button class="menu-toggle" @click="sidebarCollapsed = !sidebarCollapsed" :icon="sidebarCollapsed ? Expand : Fold" text />
        <span class="header-user">{{ userStore.nickname }}</span>
        <el-button size="small" @click="goClient">返回商城</el-button>
        <el-button size="small" @click="logout" :loading="logoutLoading">退出</el-button>
      </header>
      <div class="admin-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Expand, Fold } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const sidebarCollapsed = ref(false)

const goClient = () => router.push('/')
const logoutLoading = ref(false)
const logout = async () => {
  logoutLoading.value = true
  try {
    await userStore.logout()
    router.push('/login')
  } catch (e) {} finally {
    logoutLoading.value = false
  }
}
</script>

<style scoped>
.admin-layout { display: flex; min-height: 100vh; }
.admin-sidebar { width: 220px; background: #304156; min-height: 100vh; transition: width 0.3s; overflow-x: hidden; }
.admin-sidebar.collapsed { width: 64px; }
.sidebar-title { padding: 15px; color: #409eff; text-align: center; font-size: 16px; white-space: nowrap; overflow: hidden; }
.admin-main { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.admin-header { height: 50px; background: #fff; display: flex; align-items: center; padding: 0 15px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); gap: 10px; }
.header-user { margin-left: auto; font-size: 14px; color: #606266; }
.menu-toggle { display: none; }
.admin-content { flex: 1; padding: 20px; background: #f0f2f5; overflow-x: auto; }

@media (max-width: 768px) {
  .menu-toggle { display: inline-flex; }
  .admin-sidebar { position: fixed; z-index: 200; height: 100vh; }
  .admin-sidebar:not(.collapsed) { width: 220px; }
  .admin-sidebar.collapsed { width: 0; padding: 0; }
  .admin-main { margin-left: 0 !important; }
  .admin-content { padding: 10px; }
  .admin-header { padding: 0 10px; }
}
</style>