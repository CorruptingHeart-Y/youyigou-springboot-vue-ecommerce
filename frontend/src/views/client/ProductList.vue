<template>
  <div>
    <!-- ═══ 一体化高能筛选条 ═══ -->
    <div class="filter-bar">
      <!-- 左侧：排序文本标签 -->
      <div class="sort-tabs">
        <span
          class="sort-tab"
          :class="{ active: sortMode === 'default' }"
          @click="setSort('default')"
        >综合</span>
        <span
          class="sort-tab"
          :class="{ active: sortMode === 'sales' }"
          @click="setSort('sales')"
        >销量</span>
        <span
          class="sort-tab"
          :class="{ active: sortMode === 'price' }"
          @click="setSort('price')"
        >
          价格
          <span class="price-arrows" v-if="sortMode === 'price'">
            <el-icon :size="10"><component :is="priceOrder === 'asc' ? CaretTop : CaretBottom" /></el-icon>
          </span>
        </span>
      </div>

      <!-- 右侧：分类 + 搜索 -->
      <div class="filter-right">
        <el-select
          v-model="categoryId"
          placeholder="全部分类"
          @change="doSearch"
          clearable
          class="filter-cate"
          :teleported="false"
        >
          <el-option
            v-for="c in categories"
            :key="c.id"
            :label="c._isChild ? '  ' + c.name : c.name"
            :value="c.id"
            :class="c._isChild ? 'opt-child' : 'opt-parent'"
          />
        </el-select>
        <div class="search-pill">
          <el-icon class="search-pill-icon"><Search /></el-icon>
          <input
            v-model="searchKeyword"
            type="text"
            class="search-pill-input"
            placeholder="搜索商品"
            @keyup.enter="doSearch"
          />
          <button class="search-pill-btn" @click="doSearch">
            <el-icon :size="16"><Search /></el-icon>
          </button>
        </div>
      </div>
    </div>

    <!-- 秒杀筛选提示 -->
    <div v-if="promotionFilter === 'SECKILL'" class="promo-filter-tip">
      <el-tag type="danger" effect="dark" closable @close="clearPromotionFilter">⚡ 当前筛选：限时秒杀</el-tag>
    </div>

    <div v-if="loading" style="text-align:center;padding:40px">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <p style="color:#909399;margin-top:10px">加载中...</p>
    </div>

    <el-empty v-else-if="!products.length" description="暂无商品" />

    <el-row :gutter="15" v-else>
      <el-col :xs="12" :sm="8" :md="6" :lg="6" v-for="p in products" :key="p.id">
        <el-card shadow="hover" @click="$router.push('/product/'+p.id)" class="product-card">
          <div class="product-img-wrap">
            <img :src="p.coverImage || 'https://via.placeholder.com/200'" class="product-img" />
            <div class="product-tag" v-if="p.originalPrice && p.originalPrice > p.price">
              <el-tag type="danger" size="small" effect="dark">特惠</el-tag>
            </div>
          </div>
          <div class="product-info">
            <div class="product-name">{{ p.name }}</div>
            <div class="product-price-row">
              <span class="product-price">¥{{ p.price }}</span>
              <span class="product-orig-price" v-if="p.originalPrice">¥{{ p.originalPrice }}</span>
            </div>
            <div class="product-sales">已售{{ p.sales || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div style="display:flex;justify-content:center;margin:24px 0" v-if="total > size">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi, promotionApi } from '@/api/endpoints'
import { Search, Loading, CaretTop, CaretBottom } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const products = ref([])
const categories = ref([])
const searchKeyword = ref(route.query.keyword || '')
const sortBy = ref(route.query.sort || '')
const categoryId = ref(route.query.categoryId ? Number(route.query.categoryId) : null)
const promotionFilter = ref(route.query.promotion || '')
const seckillProductIds = ref([])
const page = ref(1)
const size = 20
const total = ref(0)
const loading = ref(false)

const sortMode = ref('default')
const priceOrder = ref('desc')

const setSort = (mode) => {
  if (mode === 'price') {
    if (sortMode.value === 'price') {
      priceOrder.value = priceOrder.value === 'asc' ? 'desc' : 'asc'
    } else {
      priceOrder.value = 'desc'
    }
    sortBy.value = priceOrder.value === 'asc' ? 'price_asc' : 'price_desc'
  } else if (mode === 'sales') {
    sortBy.value = 'sales'
  } else {
    sortBy.value = ''
  }
  sortMode.value = mode
  doSearch()
}

const fetchData = async () => {
  loading.value = true
  try {
    const requests = [productApi.getList({ page: page.value, size, categoryId: categoryId.value, keyword: searchKeyword.value, sort: sortBy.value })]

    // If seckill filter active, also fetch promotions to get seckill product IDs
    if (promotionFilter.value === 'SECKILL' && !seckillProductIds.value.length) {
      requests.push(promotionApi.getActive())
    }

    const results = await Promise.all(requests)
    const prodRes = results[0]

    let allProducts = prodRes.data?.records || []
    total.value = prodRes.data?.total || 0

    // Extract seckill product IDs from promotion response
    if (results[1]) {
      const promotions = results[1].data || []
      seckillProductIds.value = promotions
        .filter(p => p.type === 'SECKILL')
        .map(p => p.productId || p.product_id)
        .filter(Boolean)
    }

    // Client-side filter: only keep products that are in seckill
    if (promotionFilter.value === 'SECKILL' && seckillProductIds.value.length) {
      allProducts = allProducts.filter(p => seckillProductIds.value.includes(p.id))
      total.value = allProducts.length
    }

    products.value = allProducts

    // Always fetch categories
    const catRes = await productApi.getCategories()
    categories.value = Array.isArray(catRes.data)
      ? catRes.data.flatMap(c => [{ ...c, _isChild: false }, ...(c.children || []).map(ch => ({ ...ch, _isChild: true, _parentName: c.name }))])
      : []
  } catch (e) {} finally {
    loading.value = false
  }
}

const clearPromotionFilter = () => {
  promotionFilter.value = ''
  seckillProductIds.value = []
  page.value = 1
  router.replace({ query: { ...route.query, promotion: undefined } })
  fetchData()
}

const doSearch = () => { page.value = 1; fetchData() }
const onPageChange = (p) => { page.value = p; fetchData() }

watch(() => route.query, (q) => {
  if (q.keyword) searchKeyword.value = q.keyword
  if (q.categoryId) categoryId.value = Number(q.categoryId)
  if (q.promotion) {
    promotionFilter.value = q.promotion
    seckillProductIds.value = []
  } else if (!q.promotion && promotionFilter.value) {
    promotionFilter.value = ''
    seckillProductIds.value = []
  }
  fetchData()
})

onMounted(fetchData)
</script>

<style scoped>
/* ===== Promotion filter tip ===== */
.promo-filter-tip {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}
.promo-filter-tip .el-tag {
  font-size: 14px;
  padding: 6px 14px;
}

/* ===== Filter bar ===== */
.filter-bar {
  background: #fff;
  border-radius: 8px;
  padding: 12px 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.02);
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  gap: 16px;
}

/* Sort text tabs */
.sort-tabs {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}
.sort-tab {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  transition: color 0.2s, background 0.2s;
  user-select: none;
  border: 1px solid transparent;
}
.sort-tab:hover {
  color: #e74c3c;
  background: #fef0f0;
}
.sort-tab.active {
  color: #e74c3c;
  font-weight: 600;
  background: #fef0f0;
  border-color: #fcd5d5;
}
.price-arrows {
  display: inline-flex;
  flex-direction: column;
  line-height: 0;
  margin-left: 1px;
}

/* Right side */
.filter-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

/* Category select — lightweight */
.filter-cate {
  width: 130px;
}
.filter-cate :deep(.el-input__wrapper) {
  border: none;
  box-shadow: none;
  background: #f5f5f7;
  border-radius: 8px;
  padding: 0 10px;
}
.filter-cate :deep(.el-input__wrapper:hover),
.filter-cate :deep(.el-input__wrapper.is-focus) {
  background: #f0f0f2;
  box-shadow: none;
}
.filter-cate :deep(.el-input__inner) {
  font-size: 13px;
  color: #606266;
}

/* Search pill */
.search-pill {
  display: flex;
  align-items: center;
  height: 36px;
  border: 1.5px solid #e8e8e8;
  border-radius: 20px;
  background: #fff;
  overflow: hidden;
  transition: border-color 0.2s;
}
.search-pill:focus-within {
  border-color: #e74c3c;
}
.search-pill-icon {
  flex-shrink: 0;
  margin-left: 12px;
  font-size: 14px;
  color: #999;
}
.search-pill-input {
  width: 140px;
  height: 100%;
  padding: 0 8px;
  border: none;
  outline: none;
  background: transparent;
  font-size: 13px;
  color: #333;
}
.search-pill-input::placeholder { color: #bbb; font-size: 12px; }
.search-pill-btn {
  flex-shrink: 0;
  width: 34px;
  height: 30px;
  margin-right: 3px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff4757, #e74c3c);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.2s;
}
.search-pill-btn:hover { opacity: 0.85; }

/* ===== Product cards ===== */
.product-card {
  cursor: pointer; margin-bottom: 16px; border-radius: 12px; overflow: hidden;
  transition: all 0.3s ease; border: 1px solid #f0f0f0;
}
.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 28px rgba(0,0,0,0.1) !important;
  border-color: #e0e0e0;
}

.product-img-wrap { position: relative; }
.product-img { width: 100%; height: 200px; object-fit: cover; display: block; }
.product-tag { position: absolute; top: 10px; right: 10px; }

.product-info { padding: 14px 14px 16px; }
.product-name {
  font-size: 14px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  color: #303133; margin-bottom: 4px;
}
.product-price-row { margin-top: 6px; display: flex; align-items: baseline; gap: 6px; }
.product-price { color: #e74c3c; font-size: 18px; font-weight: 700; }
.product-orig-price { color: #c0c4cc; font-size: 12px; text-decoration: line-through; }
.product-sales { color: #909399; font-size: 12px; margin-top: 4px; }

@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    gap: 10px;
    padding: 10px 14px;
  }
  .filter-right {
    width: 100%;
    gap: 8px;
  }
  .filter-cate {
    width: 100px;
  }
  .search-pill-input {
    width: 100px;
  }
  .sort-tab {
    padding: 5px 10px;
    font-size: 13px;
  }
  .product-img { height: 140px; }
  .product-price { font-size: 16px; }
}
</style>

<style>
/* Category dropdown — parent vs child hierarchy (non-scoped: options are teleported) */
.opt-parent {
  font-weight: 600 !important;
  font-size: 15px !important;
  color: #333 !important;
}
.opt-child {
  font-weight: 400 !important;
  font-size: 13px !important;
  color: #888 !important;
  padding-left: 28px !important;
}
.opt-child::before {
  content: '— ';
  color: #ccc;
}
</style>
