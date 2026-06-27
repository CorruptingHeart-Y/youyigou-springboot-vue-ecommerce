<template>
  <div class="client-layout">
    <header class="header">
      <div class="header-inner">
        <router-link to="/" class="logo">
          <img :src="logoImg" alt="电商平台" class="logo-img" />
        </router-link>

        <!-- ═══ 搜索框 ═══ -->
        <div class="header-search">
          <div class="search-pill" :class="{ focused: searchFocused }">
            <el-icon class="search-prefix-icon"><Search /></el-icon>
            <input
              v-model="keyword"
              type="text"
              class="search-input-native"
              placeholder="搜索你想要的宝贝"
              @keyup.enter="search"
              @focus="onSearchFocus"
              @blur="onSearchBlur"
            />
            <button class="search-submit-btn" @click="search">搜索</button>
          </div>
          <div class="hot-search-dropdown" v-if="showHotSearch && hotKeywords.length">
            <div class="hot-search-title">🔥 热门搜索</div>
            <div class="hot-search-tags">
              <el-tag v-for="(kw, i) in hotKeywords" :key="i" class="hot-tag" effect="plain" @mousedown.prevent="searchByKeyword(kw)">
                <span class="hot-rank" :class="{ 'hot-rank-top': i < 3 }">{{ i + 1 }}</span>
                {{ kw }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- ═══ 右侧操作区 ═══ -->
        <div class="header-nav">
          <template v-if="userStore.isLoggedIn()">
            <!-- 我的订单 -->
            <router-link to="/orders" class="nav-item">
              <div class="nav-icon-wrap">
                <el-icon><Document /></el-icon>
              </div>
              <span class="nav-label">我的订单</span>
            </router-link>

            <!-- 购物车 -->
            <router-link to="/cart" class="nav-item cart-nav">
              <el-badge :value="cartStore.count" :hidden="!cartStore.count" :max="99" class="cart-badge">
                <div class="nav-icon-wrap">
                  <el-icon><ShoppingCartFull /></el-icon>
                </div>
                <span class="nav-label">购物车</span>
              </el-badge>
            </router-link>

            <!-- 用户 -->
            <el-dropdown trigger="hover" :hide-timeout="100" :teleported="false">
              <div class="nav-user">
                <el-avatar :size="34" :src="userStore.avatar || undefined" class="nav-avatar">
                  {{ userStore.nickname?.charAt(0) }}
                </el-avatar>
                <span class="nav-nickname">{{ userStore.nickname }}</span>
                <el-icon class="nav-caret"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/profile')">
                    <el-icon><User /></el-icon> 个人中心
                  </el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/favorites')">
                    <el-icon><Star /></el-icon> 我的收藏
                  </el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/chat')">
                    <el-icon><Service /></el-icon> 在线客服
                  </el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin()" @click="$router.push('/admin')">
                    <el-icon><Setting /></el-icon> 管理后台
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout" :disabled="logoutLoading">
                    <el-icon><SwitchButton /></el-icon> 退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>

          <!-- 未登录 -->
          <template v-else>
            <router-link to="/login" class="nav-btn-outline">登录</router-link>
            <router-link to="/register" class="nav-btn-primary">注册</router-link>
          </template>
        </div>
      </div>
    </header>

    <main class="main-content">
      <router-view />
    </main>
    <Footer />
    <FloatingSidebar />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { productApi } from '@/api/endpoints'
import { ElMessage } from 'element-plus'
import { Search, Document, ShoppingCartFull, ArrowDown, User, Star, Service, Setting, SwitchButton } from '@element-plus/icons-vue'
import FloatingSidebar from '@/components/FloatingSidebar.vue'
import Footer from '@/components/Footer.vue'
import logoImg from '@/assets/images/logo.jpg'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const keyword = ref('')
const hotKeywords = ref([])
const showHotSearch = ref(false)
const searchFocused = ref(false)
const logoutLoading = ref(false)

if (userStore.isLoggedIn()) {
  cartStore.fetchCart()
}

onMounted(async () => {
  try {
    const res = await productApi.getHotSearch()
    hotKeywords.value = res.data || []
  } catch (e) {
    hotKeywords.value = ['手机', '电脑', '咖啡豆', '坚果', 'JK裙', '洛丽塔', 'T恤', 'A字短裙']
  }
})

const search = () => {
  if (keyword.value.trim()) {
    showHotSearch.value = false
    searchFocused.value = false
    router.push({ name: 'ProductList', query: { keyword: keyword.value.trim() } })
  }
}

const searchByKeyword = (kw) => {
  keyword.value = kw
  search()
}

const onSearchFocus = () => {
  searchFocused.value = true
  showHotSearch.value = true
}

const onSearchBlur = () => {
  searchFocused.value = false
  setTimeout(() => { showHotSearch.value = false }, 200)
}

const handleLogout = async () => {
  logoutLoading.value = true
  try {
    await userStore.logout()
    cartStore.clearCount()
    ElMessage.success('已安全退出')
    router.push('/')
  } catch (e) {
    ElMessage.error('退出失败，请重试')
  } finally {
    logoutLoading.value = false
  }
}
</script>

<style scoped>
.client-layout { min-height: 100vh; display: flex; flex-direction: column; background: #f4f4f4; }

/* ==================== Header ==================== */
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  position: sticky; top: 0; z-index: 100;
}
.header-inner {
  max-width: 1320px; margin: 0 auto;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 15px; height: 64px;
  transition: all 0.3s ease;
}

/* Logo */
.logo { display: flex; align-items: center; text-decoration: none; flex-shrink: 0; }
.logo-img { height: 50px; object-fit: contain; cursor: pointer; }

/* ==================== Search ==================== */
.header-search {
  flex: 1; margin: 0 60px; max-width: 600px;
  display: flex; flex-direction: column; position: relative;
}

/* Pill container — pure flex, no Element Plus border conflicts */
.search-pill {
  display: flex; align-items: center;
  height: 44px;
  border: 2px solid #e8e8e8;
  border-radius: 24px;
  background: #fff;
  overflow: hidden;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.search-pill:hover { border-color: #d0d0d0; }
.search-pill.focused {
  border-color: #e74c3c;
  box-shadow: 0 0 0 3px rgba(231, 76, 60, 0.12);
}

.search-prefix-icon {
  flex-shrink: 0; margin-left: 16px;
  font-size: 18px; color: #999;
}

.search-input-native {
  flex: 1;
  height: 100%; padding: 0 12px;
  border: none; outline: none; background: transparent;
  font-size: 14px; color: #333;
  min-width: 0;
}
.search-input-native::placeholder { color: #bbb; }

.search-submit-btn {
  flex-shrink: 0;
  height: 36px; padding: 0 24px; margin-right: 4px;
  border: none; border-radius: 20px;
  background: linear-gradient(135deg, #ff4757, #e74c3c);
  color: #fff; font-size: 14px; font-weight: 500;
  cursor: pointer; letter-spacing: 1px;
  transition: opacity 0.2s, transform 0.2s;
}
.search-submit-btn:hover { opacity: 0.9; transform: scale(1.03); }

/* Hot search dropdown */
.hot-search-dropdown {
  position: absolute; top: calc(100% + 8px); left: 0; right: 0; background: #fff;
  border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,0.1);
  padding: 16px 18px; z-index: 200;
}
.hot-search-title { font-size: 13px; color: #999; margin-bottom: 10px; letter-spacing: 0.5px; }
.hot-search-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.hot-tag { cursor: pointer; transition: all 0.2s; border-radius: 14px; padding: 0 12px; height: 28px; line-height: 28px; }
.hot-tag:hover { color: #e74c3c; border-color: #e74c3c; background: #fff5f5; }
.hot-rank {
  display: inline-block; width: 18px; height: 18px; line-height: 18px;
  text-align: center; border-radius: 4px; font-size: 11px; margin-right: 5px;
  background: #f0f2f5; color: #999; font-weight: 600;
}
.hot-rank-top { background: linear-gradient(135deg, #ff4757, #e74c3c); color: #fff; }

/* ==================== Right Nav ==================== */
.header-nav { display: flex; align-items: center; gap: 4px; flex-shrink: 0; }

.nav-item {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 4px;
  text-decoration: none; color: #555;
  padding: 6px 12px; border-radius: 10px;
  transition: all 0.2s ease;
}
.nav-item:hover { background: #fff5f5; color: #e74c3c; }
.nav-item:hover .nav-icon-wrap { background: #ffeded; }

.nav-icon-wrap {
  display: flex; align-items: center; justify-content: center;
  width: 30px; height: 30px; border-radius: 50%;
  transition: background 0.2s ease;
}
.nav-icon-wrap .el-icon { font-size: 20px; }

.nav-label { font-size: 12px; font-weight: 400; line-height: 1; }

/* Cart badge */
.cart-nav { position: relative; }
.cart-badge { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.cart-badge :deep(.el-badge__content) {
  font-size: 10px; height: 16px; line-height: 16px; padding: 0 5px;
  border-radius: 8px; top: 4px; right: 10px;
}

/* User */
.nav-user {
  display: flex; align-items: center; gap: 6px; cursor: pointer;
  padding: 4px 10px 4px 4px; border-radius: 24px;
  transition: background 0.2s ease;
  user-select: none; margin-left: 4px;
}
.nav-user:hover { background: #f5f5f5; }
.nav-avatar {
  flex-shrink: 0;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}
.nav-user:hover .nav-avatar { border-color: #e74c3c; }
.nav-nickname {
  font-size: 13px; color: #333;
  max-width: 72px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.nav-caret { font-size: 12px; color: #999; transition: transform 0.2s; }

:deep(.el-dropdown-menu__item .el-icon) { margin-right: 8px; font-size: 16px; }

/* Unauthenticated */
.nav-btn-outline, .nav-btn-primary {
  text-decoration: none; font-size: 13px; font-weight: 500;
  padding: 7px 18px; border-radius: 20px; transition: all 0.2s;
}
.nav-btn-outline {
  color: #e74c3c; border: 1.5px solid #e74c3c; background: #fff;
}
.nav-btn-outline:hover { background: #fff5f5; }
.nav-btn-primary {
  color: #fff; background: linear-gradient(135deg, #ff4757, #e74c3c); border: none;
}
.nav-btn-primary:hover { opacity: 0.9; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(231,76,60,0.3); }

/* ==================== Content ==================== */
.main-content { flex: 1; width: 100%; max-width: 1320px; margin: 20px auto 0; padding: 0 15px 40px; }

@media (max-width: 768px) {
  .header-inner { height: auto; padding: 10px 12px; flex-wrap: wrap; gap: 8px; }
  .header-search { order: 3; width: 100%; flex: none; max-width: none; margin: 0; }
  .header-nav { gap: 0; }
  .nav-item { padding: 4px 6px; }
  .nav-label { font-size: 10px; }
  .nav-icon-wrap { width: 24px; height: 24px; }
  .nav-icon-wrap .el-icon { font-size: 16px; }
  .logo-img { height: 36px; }
  .nav-nickname { display: none; }
  .nav-user { padding: 4px; margin-left: 0; }
  .main-content { padding: 0 10px; margin: 10px auto; }
  .search-pill { height: 38px; }
  .search-submit-btn { height: 30px; padding: 0 16px; font-size: 13px; }
  .search-prefix-icon { margin-left: 12px; font-size: 16px; }
}
</style>
