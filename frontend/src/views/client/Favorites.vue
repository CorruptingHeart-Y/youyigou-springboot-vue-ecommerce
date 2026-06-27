<template>
  <div class="favorites-page">
    <h3 class="page-title">我的收藏</h3>
    <div v-loading="loading">
      <el-empty v-if="!favorites.length" description="暂无收藏商品" />
      <div v-else class="fav-grid">
        <div
          v-for="item in favorites"
          :key="item.id"
          class="fav-card"
          @click="$router.push('/product/' + item.id)"
        >
          <div class="fav-img-wrap">
            <img
              :src="item.coverImage || '/uploads/products/default.svg'"
              :alt="item.name"
              class="fav-img"
            />
          </div>
          <div class="fav-body">
            <div class="fav-name">{{ item.name }}</div>
            <div class="fav-meta">
              <span class="fav-price">¥{{ item.price }}</span>
              <span v-if="item.originalPrice && item.originalPrice > item.price" class="fav-original">¥{{ item.originalPrice }}</span>
            </div>
            <el-button class="fav-remove" size="small" text @click.stop="doRemove(item.id)">
              <span class="fav-remove-icon">✕</span> 取消收藏
            </el-button>
          </div>
        </div>
      </div>
      <div class="fav-pagination" v-if="total > pageSize">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize" v-model:current-page="page" @current-change="fetch" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/api/endpoints'
import { ElMessage, ElMessageBox } from 'element-plus'

const favorites = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)

const fetch = async () => {
  loading.value = true
  try {
    const res = await userApi.getFavorites()
    favorites.value = res.data?.records || res.data || []
    total.value = res.data?.total || favorites.value.length
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const doRemove = async (productId) => {
  try {
    await ElMessageBox.confirm('确定取消收藏？', '提示', { type: 'warning' })
    await userApi.unfavorite(productId)
    ElMessage.success('已取消收藏')
    fetch()
  } catch (e) {
    // cancelled or error, both handled
  }
}

onMounted(fetch)
</script>

<style scoped>
.favorites-page {
  padding: 4px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
}

/* ── Card grid ── */
.fav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(206px, 1fr));
  gap: 16px;
}

/* ── Card ── */
.fav-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.25s, box-shadow 0.25s;
  display: flex;
  flex-direction: column;
}

.fav-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.10);
}

/* ── Image ── */
.fav-img-wrap {
  width: 100%;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  background: #f5f7fa;
  flex-shrink: 0;
}

.fav-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.35s;
}

.fav-card:hover .fav-img {
  transform: scale(1.06);
}

/* ── Body ── */
.fav-body {
  padding: 12px 14px 14px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.fav-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 6px;
}

/* ── Meta ── */
.fav-meta {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 10px;
}

.fav-price {
  color: #f56c6c;
  font-weight: 700;
  font-size: 17px;
}

.fav-original {
  color: #c0c4cc;
  font-size: 12px;
  text-decoration: line-through;
}

/* ── Remove button ── */
.fav-remove {
  margin-top: auto;
  align-self: flex-end;
  padding: 4px 10px;
  font-size: 12px;
  color: #909399;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  transition: color 0.2s, border-color 0.2s, background 0.2s;
}

.fav-remove:hover {
  color: #f56c6c;
  border-color: #f56c6c;
  background: #fef0f0;
}

.fav-remove-icon {
  margin-right: 2px;
  font-size: 10px;
}

/* ── Pagination ── */
.fav-pagination {
  margin-top: 28px;
  text-align: center;
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .fav-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .fav-body {
    padding: 10px;
  }

  .fav-name {
    font-size: 13px;
  }

  .fav-price {
    font-size: 15px;
  }
}
</style>
