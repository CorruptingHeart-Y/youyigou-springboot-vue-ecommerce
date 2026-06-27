<template>
  <div class="detail-container" v-if="product">
    <el-row :gutter="30">
      <el-col :xs="24" :sm="10">
        <div class="main-img-wrapper">
          <img :src="product.coverImage || 'https://via.placeholder.com/400'" alt="商品主图" />
        </div>
        <div class="thumbnails-row" v-if="product.images && product.images.length">
          <img
            v-for="(img, i) in product.images"
            :key="i"
            :src="img"
            @click="product.coverImage = img"
            class="thumbnail-img"
            :class="{ active: product.coverImage === img }"
          />
        </div>
      </el-col>

      <el-col :xs="24" :sm="14">
        <h2 class="product-name">{{ product.name }}</h2>

        <div class="seckill-banner" v-if="activeSeckill">
          <div class="seckill-left">
            <div class="seckill-tag-row">
              <el-tag type="danger" effect="dark" size="large">⚡ 限时秒杀</el-tag>
              <span class="seckill-banner-title">{{ activeSeckill.title }}</span>
            </div>
            <div class="seckill-countdown">
              <span class="countdown-label">距结束</span>
              <span class="countdown-block">{{ seckillCountdown.hours }}</span>
              <span class="countdown-sep">:</span>
              <span class="countdown-block">{{ seckillCountdown.minutes }}</span>
              <span class="countdown-sep">:</span>
              <span class="countdown-block">{{ seckillCountdown.seconds }}</span>
            </div>
            <div class="seckill-prices">
              <span class="price-seckill">秒杀价 ¥{{ activeSeckill.discountValue || activeSeckill.seckillPrice }}</span>
              <span class="price-orig">原价 ¥{{ product.price }}</span>
            </div>
          </div>
          <div class="seckill-right">
            <el-progress :percentage="seckillProgress" :stroke-width="12" color="#f56c6c" :format="() => `已抢${seckillProgress}%`" />
          </div>
        </div>

        <div class="price-box" v-else>
          <span class="price-current">¥{{ product.price }}</span>
          <span class="price-original" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
        </div>

        <div class="coupon-badges" v-if="activeCoupons.length">
          <span class="coupon-badge" v-for="c in activeCoupons" :key="c.id">
            🎫 满{{ c.minAmount || 0 }}减{{ c.discountValue }}
          </span>
        </div>

        <div class="product-meta">库存: {{ product.stock }} | 销量: {{ product.sales }} | 分类: {{ product.categoryName }}</div>

        <div class="spec-row" v-if="specOptions.length">
          <span class="spec-label">规格选择</span>
          <el-select v-model="selectedSpec" placeholder="请选择规格" style="width:200px">
            <el-option v-for="s in specOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </div>

        <div class="qty-row">
          <el-input-number v-model="quantity" :min="1" :max="product.stock" />
        </div>

        <div class="action-row">
          <el-button v-if="activeSeckill" type="danger" size="large" @click="openSeckillDialog">⚡ 秒杀抢购</el-button>
          <el-button v-if="!activeSeckill" type="danger" size="large" @click="addCart">加入购物车</el-button>
          <el-button v-if="!activeSeckill" type="warning" size="large" @click="buyNow">立即购买</el-button>
          <el-button size="large" :icon="product.isFavorited ? 'StarFilled' : 'Star'" @click="toggleFavorite" :type="product.isFavorited ? 'warning' : 'default'" />
        </div>
      </el-col>
    </el-row>

    <div class="detail-tabs-section">
      <el-tabs>
        <el-tab-pane label="商品详情">
          <div class="detail-content" v-html="product.detail || product.description || '暂无详情'" />
        </el-tab-pane>
        <el-tab-pane label="商品评价">
          <div v-for="r in reviews" :key="r.id" class="review-item">
            <div class="review-left">
              <img :src="r.avatar || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(r.nickname || r.username || 'User')" class="review-avatar" />
            </div>
            <div class="review-right">
              <div class="review-nickname">{{ r.nickname || r.username || '匿名用户' }}</div>
              <div class="review-meta">
                <span class="review-rating">{{ '★'.repeat(r.rating) }}</span>
                <span class="review-date">{{ r.createTime?.substring(0, 10) }}</span>
              </div>
              <div class="review-body">{{ r.content }}</div>
              <div v-if="r.images" class="review-images">
                <img v-for="(img, i) in parseImages(r.images)" :key="i" :src="img" class="review-img" />
              </div>
            </div>
          </div>
          <el-empty v-if="!reviews.length" description="暂无评价" />
          <div class="review-form-section" v-if="userStore.isLoggedIn()">
            <h4>发表评价</h4>
            <el-rate v-model="reviewForm.rating" />
            <el-input v-model="reviewForm.content" type="textarea" :rows="3" placeholder="请输入评价内容" class="review-input" />
            <el-upload v-model:file-list="reviewImages" :action="uploadUrl" :headers="uploadHeaders" list-type="picture-card" :limit="5" :on-success="onReviewImgSuccess" class="review-upload">
              <el-icon><Plus /></el-icon>
            </el-upload>
            <el-button type="primary" :loading="reviewLoading" @click="submitReview" class="review-submit-btn">提交评价</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <OrderConfirmDialog
      v-model:visible="seckillDialogVisible"
      :is-seckill="true"
      :items="seckillDialogItems"
      :total-amount="seckillDialogTotal"
      @submit="handleSeckillSubmit"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi, cartApi, userApi, promotionApi } from '@/api/endpoints'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import OrderConfirmDialog from '@/components/OrderConfirmDialog.vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()
const product = ref(null)
const reviews = ref([])
const quantity = ref(1)
const selectedSpec = ref('默认')
const specOptions = ref([])
const reviewForm = reactive({ rating: 5, content: '' })
const reviewImages = ref([])
const reviewImgUrls = ref([])
const reviewLoading = ref(false)
const seckillLoading = ref(false)
const seckillDialogVisible = ref(false)
const seckillDialogItems = ref([])
const seckillDialogTotal = ref(0)
const uploadUrl = '/api/admin/site/upload'
const uploadHeaders = { Authorization: `Bearer ${localStorage.getItem('token')}` }

const promotions = ref([])
const seckillCountdown = reactive({ hours: '00', minutes: '00', seconds: '00' })
let countdownTimer = null

const activeSeckill = computed(() => promotions.value.find(p => p.type === 'SECKILL'))
const activeCoupons = computed(() => promotions.value.filter(p => p.type === 'COUPON'))
const seckillProgress = computed(() => {
  const s = activeSeckill.value
  if (!s) return 0
  const total = s.totalStock || s.stock || 100
  const sold = s.soldCount || s.sold || 0
  return Math.min(100, Math.round((sold / total) * 100))
})

const startCountdown = () => {
  countdownTimer = setInterval(() => {
    if (!activeSeckill.value) return
    const end = new Date(activeSeckill.value.endTime).getTime()
    const diff = Math.max(0, end - Date.now())
    seckillCountdown.hours = String(Math.floor(diff / 3600000)).padStart(2, '0')
    seckillCountdown.minutes = String(Math.floor((diff % 3600000) / 60000)).padStart(2, '0')
    seckillCountdown.seconds = String(Math.floor((diff % 60000) / 1000)).padStart(2, '0')
  }, 1000)
}

onMounted(async () => {
  try {
    const [prodRes, revRes, promRes] = await Promise.all([
      productApi.getDetail(route.params.id),
      productApi.getReviews(route.params.id, { page: 1, size: 10 }),
      promotionApi.getActive()
    ])
    product.value = prodRes.data
    reviews.value = revRes.data?.records || []
    promotions.value = (promRes.data || []).filter(p => {
      if (p.productId && p.productId != route.params.id) return false
      return true
    })
    parseSpecs()
    startCountdown()
  } catch (e) {}
})

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})

const parseSpecs = () => {
  if (product.value?.specs) {
    try {
      const s = typeof product.value.specs === 'string' ? JSON.parse(product.value.specs) : product.value.specs
      if (Array.isArray(s)) specOptions.value = s
      else if (s.options) specOptions.value = s.options
    } catch (e) { specOptions.value = [] }
  }
}

const addCart = async () => {
  if (!product.value) return
  try {
    await cartApi.add({ productId: product.value.id, quantity: quantity.value, spec: selectedSpec.value })
    ElMessage.success('已加入购物车')
    cartStore.fetchCart()
  } catch (e) {}
}

const buyNow = async () => {
  await addCart()
  router.push('/cart')
}

const openSeckillDialog = () => {
  if (!userStore.isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!activeSeckill.value || !product.value) return
  const seckillPrice = activeSeckill.value.discountValue || activeSeckill.value.seckillPrice || product.value.price
  seckillDialogItems.value = [{
    name: product.value.name,
    image: product.value.coverImage,
    price: seckillPrice,
    quantity: quantity.value,
    spec: selectedSpec.value,
    subtotal: (seckillPrice * quantity.value).toFixed(2)
  }]
  seckillDialogTotal.value = seckillPrice * quantity.value
  seckillDialogVisible.value = true
}

const handleSeckillSubmit = async (payload) => {
  if (!activeSeckill.value || !product.value) return
  seckillLoading.value = true
  try {
    await promotionApi.seckillOrder(activeSeckill.value.id, {
      productId: product.value.id,
      quantity: quantity.value,
      spec: selectedSpec.value,
      addressId: payload.addressId,
      payMethod: payload.payMethod
    })
    ElMessage.success('秒杀成功！订单已生成')
    seckillDialogVisible.value = false
    router.push('/orders')
  } catch (e) {} finally {
    seckillLoading.value = false
  }
}

const toggleFavorite = async () => {
  if (!product.value) return
  try {
    if (product.value.isFavorited) {
      await userApi.unfavorite(product.value.id)
    } else {
      await userApi.favorite(product.value.id)
    }
    product.value.isFavorited = !product.value.isFavorited
    ElMessage.success(product.value.isFavorited ? '已收藏' : '已取消收藏')
  } catch (e) {}
}

const onReviewImgSuccess = (res) => {
  if (res.data) reviewImgUrls.value.push(res.data)
}

const parseImages = (data) => {
  if (!data) return []
  try { return typeof data === 'string' ? JSON.parse(data) : data } catch (e) { return [] }
}

const submitReview = async () => {
  if (!reviewForm.content.trim()) { ElMessage.warning('请输入评价内容'); return }
  reviewLoading.value = true
  try {
    await userApi.addReview({
      productId: product.value.id,
      content: reviewForm.content,
      rating: reviewForm.rating,
      images: JSON.stringify(reviewImgUrls.value)
    })
    ElMessage.success('评价提交成功')
    reviewForm.content = ''
    reviewForm.rating = 5
    reviewImages.value = []
    reviewImgUrls.value = []
    const revRes = await productApi.getReviews(route.params.id, { page: 1, size: 10 })
    reviews.value = revRes.data?.records || []
  } catch (e) {} finally {
    reviewLoading.value = false
  }
}
</script>

<style scoped>
.detail-container {
  max-width: 1320px;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

/* ===== 主图区 ===== */
.main-img-wrapper {
  width: 100%;
  aspect-ratio: 1 / 1;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}
.main-img-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.thumbnails-row {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  justify-content: center;
  flex-wrap: wrap;
}
.thumbnail-img {
  width: 56px;
  height: 56px;
  object-fit: cover;
  cursor: pointer;
  border: 2px solid #dcdfe6;
  border-radius: 4px;
  transition: border-color 0.2s;
}
.thumbnail-img:hover {
  border-color: #409eff;
}
.thumbnail-img.active {
  border-color: #f56c6c;
}

.product-name {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  line-height: 1.4;
}

/* ===== 价格区 ===== */
.price-box {
  background: #fef0f0;
  padding: 15px 18px;
  border-radius: 6px;
  margin: 12px 0;
}
.price-current {
  color: #f56c6c;
  font-size: 30px;
  font-weight: bold;
}
.price-original {
  color: #909399;
  font-size: 14px;
  margin-left: 10px;
  text-decoration: line-through;
}

/* ===== 秒杀横幅 - 紧凑版 ===== */
.seckill-banner {
  background: linear-gradient(135deg, #fff1f0 0%, #ffccc7 100%);
  border: 1px solid #f89898;
  border-radius: 8px;
  padding: 12px 16px;
  margin: 12px 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 60px;
}
.seckill-left {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.seckill-tag-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.seckill-banner-title {
  font-size: 14px;
  font-weight: bold;
  color: #f56c6c;
}
.seckill-countdown {
  display: flex;
  align-items: center;
  gap: 2px;
}
.countdown-label {
  font-size: 12px;
  color: #909399;
  margin-right: 4px;
}
.countdown-block {
  background: #303133;
  color: #fff;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 14px;
  font-weight: bold;
  font-family: 'Courier New', monospace;
}
.countdown-sep {
  color: #303133;
  font-weight: bold;
  font-size: 14px;
}
.seckill-prices {
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.price-seckill {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}
.price-orig {
  color: #c0c4cc;
  font-size: 13px;
  text-decoration: line-through;
}
.seckill-right {
  flex: 1 1 auto;
  min-width: 120px;
}

/* ===== 优惠券 ===== */
.coupon-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 10px 0;
}
.coupon-badge {
  background: linear-gradient(135deg, #fff7e6, #ffe7ba);
  border: 1px dashed #e6a23c;
  color: #e6a23c;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: bold;
}

.product-meta {
  color: #909399;
  font-size: 13px;
  margin: 10px 0;
}
.spec-row {
  margin: 12px 0;
}
.spec-label {
  display: block;
  color: #606266;
  margin-bottom: 6px;
  font-size: 14px;
}
.qty-row {
  margin: 16px 0;
}
.action-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 16px;
}

/* ===== 底部详情 Tabs ===== */
.detail-tabs-section {
  margin-top: 36px;
}
.detail-content {
  min-height: 200px;
  line-height: 1.8;
  color: #303133;
  font-size: 15px;
}
.detail-content :deep(img) {
  max-width: 100%;
  border-radius: 4px;
}

/* ===== 评价 ===== */
.review-item {
  display: flex;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}
.review-left {
  flex-shrink: 0;
  margin-right: 12px;
}
.review-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}
.review-right {
  flex: 1;
  min-width: 0;
}
.review-nickname {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}
.review-meta {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}
.review-rating {
  color: #f7ba2a;
}
.review-date {
  margin-left: 8px;
}
.review-body {
  margin-top: 8px;
  color: #333;
  line-height: 1.6;
}
.review-images {
  margin-top: 6px;
  display: flex;
  gap: 8px;
}
.review-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}
.review-form-section {
  margin-top: 20px;
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
}
.review-input {
  margin-top: 10px;
}
.review-upload {
  margin-top: 10px;
}
.review-submit-btn {
  margin-top: 10px;
}

@media (max-width: 768px) {
  .detail-container {
    padding: 10px;
  }
  .seckill-banner {
    flex-direction: column;
    align-items: stretch;
  }
  .seckill-prices {
    flex-direction: column;
    gap: 2px;
  }
  .price-seckill {
    font-size: 18px;
  }
  .product-name {
    margin-top: 14px;
  }
}
</style>
