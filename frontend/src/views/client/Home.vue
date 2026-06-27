<template>
  <div class="home">
    <!-- ========== 首屏：左侧分类导航 + 右侧大轮播 ========== -->
    <div class="hero-section">
      <div class="hero-inner">
        <nav class="category-nav">
          <div class="category-nav-header">全部商品分类</div>
          <ul class="category-nav-list">
            <li
              v-for="cat in topCategories"
              :key="cat.id"
              class="category-nav-item"
              @mouseenter="activeCateId = cat.id"
              @mouseleave="activeCateId = null"
              @click="$router.push({name:'ProductList',query:{categoryId:cat.id}})"
            >
              <span class="cate-name">{{ cat.name }}</span>
              <span class="cate-arrow">›</span>
              <div class="cate-sub-popup" v-if="activeCateId === cat.id && cat.children && cat.children.length">
                <div
                  v-for="child in cat.children"
                  :key="child.id"
                  class="cate-sub-item"
                  @click.stop="$router.push({name:'ProductList',query:{categoryId:child.id}})"
                >{{ child.name }}</div>
              </div>
            </li>
          </ul>
        </nav>
        <div class="hero-carousel-wrap">
          <el-skeleton animated v-if="!banners.length" class="hero-skeleton">
            <template #template>
              <el-skeleton-item variant="image" style="width:100%;height:440px" />
            </template>
          </el-skeleton>
          <el-carousel
            v-else
            :interval="5000"
            height="440px"
            arrow="hover"
            indicator-position="outside"
            class="hero-carousel"
          >
            <el-carousel-item v-for="b in banners" :key="b.id">
              <img
                :src="b.imageUrl"
                class="hero-banner-img"
                @click="$router.push(b.linkUrl || '/')"
              />
            </el-carousel-item>
          </el-carousel>
          <div class="hero-hot-tags" v-if="hotTags.length">
            <router-link
              v-for="(tag, i) in hotTags"
              :key="tag.id"
              :to="{name:'ProductList',query:{categoryId:tag.id}}"
              class="hero-tag"
            >
              <span class="hero-tag-rank" :class="{'top3': i < 3}">{{ i + 1 }}</span>
              {{ tag.name }}
            </router-link>
          </div>
        </div>

        <!-- ═══ 右侧面板 ═══ -->
        <div class="hero-right-panel">
          <!-- 用户卡片 -->
          <div class="right-user-card">
            <template v-if="userStore.isLoggedIn()">
              <div class="ruc-top">
                <el-avatar :size="46" :src="userStore.avatar || undefined" class="ruc-avatar">
                  {{ userStore.nickname?.charAt(0) }}
                </el-avatar>
                <div class="ruc-greeting"><span class="ruc-hi">Hi，</span><span class="ruc-name">{{ userStore.nickname }}</span></div>
              </div>
              <div class="ruc-actions">
                <router-link to="/orders" class="ruc-action-item">
                  <el-icon><Document /></el-icon>
                  <span>我的订单</span>
                </router-link>
                <router-link to="/favorites" class="ruc-action-item">
                  <el-icon><Star /></el-icon>
                  <span>我的收藏</span>
                </router-link>
                <router-link to="/profile" class="ruc-action-item">
                  <el-icon><User /></el-icon>
                  <span>个人中心</span>
                </router-link>
              </div>
            </template>
            <template v-else>
              <div class="ruc-top">
                <el-avatar :size="46" class="ruc-avatar">👤</el-avatar>
                <div class="ruc-greeting">Hi，欢迎来到优选购</div>
              </div>
              <div class="ruc-actions ruc-unauth">
                <router-link to="/login" class="ruc-btn ruc-btn-outline">登录</router-link>
                <router-link to="/register" class="ruc-btn ruc-btn-primary">注册</router-link>
              </div>
            </template>
          </div>

          <!-- 平台快讯 -->
          <div class="right-news">
            <div class="right-news-header">
              <span class="rnh-title">平台快讯</span>
              <span class="rnh-more" @click="$router.push('/news')">更多 ›</span>
            </div>
            <div class="right-news-list">
              <div
                v-for="(item, i) in announcements.slice(0, 3)"
                :key="item.id"
                class="right-news-item"
                @click="$router.push('/news')"
              >
                <span class="rni-dot" :class="'rni-dot-' + (i % 3)"></span>
                <span class="rni-text">{{ item.title }}</span>
              </div>
              <div v-if="!announcements.length" class="right-news-empty">暂无快讯</div>
            </div>
          </div>

          <!-- 促销入口 -->
          <div class="right-promo">
            <div class="rp-block rp-coupon" @click="scrollToCoupon">
              <div class="rp-icon">🎫</div>
              <div class="rp-info">
                <div class="rp-title">领券中心</div>
                <div class="rp-desc">超值优惠等你</div>
              </div>
            </div>
            <div class="rp-block rp-seckill" @click="scrollToSeckill">
              <div class="rp-icon">⚡</div>
              <div class="rp-info">
                <div class="rp-title">限时秒杀</div>
                <div class="rp-desc">手慢无</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 限时秒杀 ========== -->
    <div class="zone seckill-zone" v-if="seckillPromotions.length">
      <div class="zone-header">
        <div class="zone-title">
          <span class="zone-icon">⚡</span>
          <span class="zone-title-text zone-title-text--seckill">限时秒杀</span>
          <span class="zone-sub">FLASH SALE</span>
        </div>
        <div class="zone-more" @click="$router.push({name:'ProductList',query:{promotion:'SECKILL'}})">
          查看更多 <span class="arrow">›</span>
        </div>
      </div>
      <div class="seckill-scroll">
        <div
          class="seckill-item"
          v-for="p in seckillPromotions"
          :key="p.id"
          @click="goSeckill(p)"
        >
          <div class="seckill-item-img">
            <img :src="p.coverImage || p.image || '/uploads/products/default.svg'" />
            <span class="seckill-badge">秒杀</span>
          </div>
          <div class="seckill-item-info">
            <div class="seckill-item-name">{{ p.title }}</div>
            <div class="seckill-item-price">
              <span class="s-price-curr">¥{{ p.discountValue != null ? p.discountValue : (p.seckillPrice != null ? p.seckillPrice : (p.price != null ? p.price : '?')) }}</span>
              <span class="s-price-orig" v-if="p.originalPrice">¥{{ p.originalPrice }}</span>
            </div>
            <div class="seckill-item-countdown" v-if="p._countdown !== undefined">
              <span class="cd-block">{{ p._hours }}</span><span class="cd-colon">:</span>
              <span class="cd-block">{{ p._minutes }}</span><span class="cd-colon">:</span>
              <span class="cd-block">{{ p._seconds }}</span>
            </div>
            <div class="seckill-item-progress">
              <div class="s-progress-bar">
                <div class="s-progress-fill" :style="{width: (p._progress || 0) + '%'}"></div>
              </div>
              <span class="s-progress-text">已抢{{ p._progress || 0 }}%</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 优惠券中心 ========== -->
    <div class="zone coupon-zone" v-if="couponPromotions.length">
      <div class="zone-header">
        <div class="zone-title">
          <span class="zone-icon">🎫</span>
          <span class="zone-title-text zone-title-text--coupon">优惠券中心</span>
          <span class="zone-sub">COUPON CENTER</span>
        </div>
      </div>
      <div class="coupon-grid">
        <div
          class="coupon-card-new"
          v-for="p in couponPromotions"
          :key="p.id"
          @click="claimCoupon(p)"
        >
          <div class="cc-left">
            <div class="cc-amount">
              <span class="cc-symbol">¥</span>
              <span class="cc-num">{{ p.discountValue }}</span>
            </div>
            <div class="cc-condition">满{{ p.minAmount || 0 }}可用</div>
          </div>
          <div class="cc-divider"></div>
          <div class="cc-right">
            <div class="cc-name">{{ p.title }}</div>
            <div class="cc-expire">有效期至 {{ (p.endTime || '').substring(0, 10) }}</div>
            <el-button
              :type="claimedCouponIds.has(p.id) ? 'info' : 'primary'"
              size="small"
              round
              :loading="p._claiming"
              :disabled="claimedCouponIds.has(p.id)"
              class="cc-claim-btn"
              @click.stop="claimCoupon(p)"
            >{{ claimedCouponIds.has(p.id) ? '已领取' : '立即领取' }}</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 热门商品 ========== -->
    <div class="zone product-zone">
      <div class="zone-header">
        <div class="zone-title">
          <span class="zone-title-text zone-title-text--hot">热门商品</span>
          <span class="zone-sub">HOT PRODUCTS</span>
        </div>
        <div class="zone-more" @click="$router.push({name:'ProductList',query:{sort:'hot'}})">
          查看更多 <span class="arrow">›</span>
        </div>
      </div>
      <div class="product-grid">
        <template v-if="hotProducts.length">
          <div
            class="product-card"
            v-for="p in hotProducts"
            :key="p.id"
            @click="$router.push('/product/'+p.id)"
          >
            <div class="pc-image">
              <img :src="p.coverImage || 'https://placehold.co/300x300/f5f5f5/ccc?text=No+Image'" />
            </div>
            <div class="pc-body">
              <div class="pc-name">{{ p.name }}</div>
              <div class="pc-tags" v-if="p.tags">
                <span class="pc-tag" v-for="t in p.tags" :key="t">{{ t }}</span>
              </div>
              <div class="pc-bottom">
                <div class="pc-price">
                  <span class="pc-price-curr">¥{{ p.price }}</span>
                  <span class="pc-price-orig" v-if="p.originalPrice && p.originalPrice > p.price">¥{{ p.originalPrice }}</span>
                </div>
                <span class="pc-sales" v-if="p.sales">已售{{ p.sales }}+</span>
              </div>
            </div>
          </div>
        </template>
        <el-skeleton v-else animated :count="8" :throttle="500">
          <template #template>
            <div class="product-card-skeleton">
              <el-skeleton-item variant="image" style="width:100%;height:180px" />
              <div style="padding:12px">
                <el-skeleton-item variant="text" style="width:80%" />
                <el-skeleton-item variant="text" style="width:40%;margin-top:8px" />
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>

    <!-- ========== 新品推荐 ========== -->
    <div class="zone product-zone">
      <div class="zone-header">
        <div class="zone-title">
          <span class="zone-title-text zone-title-text--new">新品推荐</span>
          <span class="zone-sub">NEW ARRIVALS</span>
        </div>
        <div class="zone-more" @click="$router.push({name:'ProductList',query:{sort:'new'}})">
          查看更多 <span class="arrow">›</span>
        </div>
      </div>
      <div class="product-grid">
        <template v-if="newProducts.length">
          <div
            class="product-card"
            v-for="p in newProducts"
            :key="p.id"
            @click="$router.push('/product/'+p.id)"
          >
            <div class="pc-image">
              <img :src="p.coverImage || 'https://placehold.co/300x300/f5f5f5/ccc?text=No+Image'" />
              <span class="pc-badge-new">NEW</span>
            </div>
            <div class="pc-body">
              <div class="pc-name">{{ p.name }}</div>
              <div class="pc-bottom">
                <div class="pc-price">
                  <span class="pc-price-curr">¥{{ p.price }}</span>
                </div>
              </div>
            </div>
          </div>
        </template>
        <el-skeleton v-else animated :count="8" :throttle="500">
          <template #template>
            <div class="product-card-skeleton">
              <el-skeleton-item variant="image" style="width:100%;height:180px" />
              <div style="padding:12px">
                <el-skeleton-item variant="text" style="width:80%" />
                <el-skeleton-item variant="text" style="width:40%;margin-top:8px" />
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>

    <!-- ========== 其他促销 ========== -->
    <div class="zone" v-if="otherPromotions.length">
      <div class="zone-header">
        <div class="zone-title">
          <span class="zone-icon">🎁</span>
          <span class="zone-title-text zone-title-text--promo">促销活动</span>
          <span class="zone-sub">PROMOTIONS</span>
        </div>
      </div>
      <div class="promo-simple-grid">
        <div class="promo-simple-card" v-for="p in otherPromotions" :key="p.id">
          <div class="ps-tag" :class="p.type==='DISCOUNT'?'tag-discount':'tag-promo'">
            {{ p.type === 'DISCOUNT' ? '折扣' : '促销' }}
          </div>
          <div class="ps-title">{{ p.title }}</div>
          <div class="ps-desc">{{ p.description || '优惠值: ' + p.discountValue }}</div>
          <div class="ps-expire">有效期至 {{ (p.endTime || '').substring(0,10) }}</div>
        </div>
      </div>
    </div>

    <!-- ========== 平台公告 ========== -->
    <div class="zone announce-zone" v-if="announcements.length">
      <div class="zone-header">
        <div class="zone-title">
          <span class="zone-icon">📢</span>
          <span class="zone-title-text zone-title-text--announce">平台公告</span>
          <span class="zone-sub">ANNOUNCEMENTS</span>
        </div>
      </div>
      <div class="announce-list">
        <div class="announce-item" v-for="a in announcements" :key="a.id" @click="openAnnouncement(a)">
          <span class="announce-tag">公告</span>
          <span class="announce-title">{{ a.title }}</span>
          <span class="announce-content">{{ a.content }}</span>
        </div>
      </div>
    </div>
  </div>

    <!-- 公告详情弹窗 -->
    <el-dialog v-model="announceDialogVisible" :title="currentAnnouncement?.title || '公告详情'" width="640px" destroy-on-close center>
      <div class="announce-dialog-body">{{ currentAnnouncement?.content }}</div>
    </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { productApi, promotionApi } from '@/api/endpoints'
import { adminApi } from '@/api/endpoints'
import { ElMessage } from 'element-plus'
import { Document, Star, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const banners = ref([])
const categories = ref([])
const hotProducts = ref([])
const newProducts = ref([])
const promotions = ref([])
const announcements = ref([])
const announceDialogVisible = ref(false)
const currentAnnouncement = ref(null)
const activeCateId = ref(null)
let countdownTimer = null

const topCategories = computed(() => {
  return categories.value.filter(c => !c.parentId && c.parentId !== 0)
})

const hotTags = computed(() => {
  return topCategories.value.slice(0, 8).map(c => ({ id: c.id, name: c.name }))
})

const seckillPromotions = computed(() => promotions.value.filter(p => p.type === 'SECKILL'))
const couponPromotions = computed(() => promotions.value.filter(p => p.type === 'COUPON'))
const otherPromotions = computed(() => promotions.value.filter(p => p.type !== 'SECKILL' && p.type !== 'COUPON'))

const startCountdown = () => {
  countdownTimer = setInterval(() => {
    const now = Date.now()
    seckillPromotions.value.forEach(p => {
      const end = new Date(p.endTime).getTime()
      const diff = Math.max(0, end - now)
      p._hours = String(Math.floor(diff / 3600000)).padStart(2, '0')
      p._minutes = String(Math.floor((diff % 3600000) / 60000)).padStart(2, '0')
      p._seconds = String(Math.floor((diff % 60000) / 1000)).padStart(2, '0')
      p._countdown = diff
      const total = p.totalStock || p.stock || 100
      const sold = p.soldCount || p.sold || 0
      p._progress = Math.min(100, Math.round((sold / total) * 100))
    })
  }, 1000)
}

onMounted(async () => {
  try {
    const [catRes, hotRes, newRes, banRes, promRes, annRes] = await Promise.all([
      productApi.getCategories(),
      productApi.getHot(10),
      productApi.getNew(10),
      adminApi.getBanners(),
      promotionApi.getActive(),
      adminApi.getAnnouncements()
    ])
    const rawCats = Array.isArray(catRes.data) ? catRes.data : []
    categories.value = rawCats
    hotProducts.value = hotRes.data || []
    newProducts.value = newRes.data || []
    banners.value = (banRes.data || []).filter(b => b.status === 1)
    promotions.value = (promRes.data || []).map(p => ({ ...p, _claiming: false }))
    announcements.value = (annRes.data || []).filter(a => a.status === 1)
    startCountdown()
  } catch (e) {}
})

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})

const openAnnouncement = (a) => {
  currentAnnouncement.value = a
  announceDialogVisible.value = true
}

const goSeckill = (promo) => {
  if (!userStore.isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/product/' + (promo.productId || promo.product_id))
}

const claimedCouponIds = ref(new Set(JSON.parse(localStorage.getItem('claimedCoupons') || '[]')))

const scrollToCoupon = () => {
  const el = document.querySelector('.coupon-zone')
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}

const scrollToSeckill = () => {
  const el = document.querySelector('.seckill-zone')
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}

const claimCoupon = async (promo) => {
  if (!userStore.isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (claimedCouponIds.value.has(promo.id)) {
    ElMessage.warning('您已经领取过该优惠券啦~')
    return
  }
  promo._claiming = true
  try {
    await promotionApi.claimCoupon(promo.id)
    ElMessage.success('优惠券领取成功！')
    claimedCouponIds.value.add(promo.id)
    localStorage.setItem('claimedCoupons', JSON.stringify([...claimedCouponIds.value]))
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || ''
    if (msg.includes('已领取') || msg.includes('已经领取')) {
      claimedCouponIds.value.add(promo.id)
      localStorage.setItem('claimedCoupons', JSON.stringify([...claimedCouponIds.value]))
    }
  } finally {
    promo._claiming = false
  }
}
</script>

<style scoped>
/* ===== 首屏 Hero ===== */
.hero-section {
  background: transparent;
  margin-bottom: 24px;
}
.hero-inner {
  max-width: 1320px;
  margin: 0 auto;
  display: flex;
  height: 440px;
  position: relative;
  gap: 20px;
  align-items: stretch;
}
.hero-skeleton {
  flex: 1;
  padding: 0;
}

/* 左侧分类导航 — 白底轻量风 */
.category-nav {
  width: 242px;
  flex-shrink: 0;
  height: 100%;
  background: #fff;
  color: #333;
  position: relative;
  z-index: 50;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}
.category-nav-header {
  padding: 0 16px;
  height: 46px;
  line-height: 46px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #e74c3c, #c0392b);
  color: #fff;
  letter-spacing: 1px;
}
.category-nav-list {
  list-style: none;
  margin: 0;
  padding: 4px 0;
}
.category-nav-item {
  padding: 0 16px;
  height: 40px;
  line-height: 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.15s ease;
  color: #333;
  margin: 1px 6px;
  border-radius: 6px;
}
.category-nav-item:first-child { margin-top: 6px; }
.category-nav-item:last-child { margin-bottom: 6px; }
.category-nav-item:hover {
  background: #fef0f0;
  color: #e74c3c;
}
.cate-arrow {
  font-size: 14px;
  color: #bbb;
  transition: color 0.15s;
}
.category-nav-item:hover .cate-arrow {
  color: #e74c3c;
}
.cate-sub-popup {
  position: absolute;
  left: 242px;
  top: 0;
  bottom: 0;
  width: 560px;
  background: #fff;
  color: #333;
  box-shadow: 4px 8px 28px rgba(0,0,0,0.08);
  border-radius: 0 12px 12px 0;
  padding: 20px;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  gap: 8px;
  z-index: 60;
}
.cate-sub-item {
  padding: 6px 14px 6px 22px;
  background: #f8f8f8;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 400;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  position: relative;
}
.cate-sub-item::before {
  content: '';
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #d0d0d0;
  transition: background 0.2s;
}
.cate-sub-item:hover {
  background: #fef0f0;
  color: #e74c3c;
}
.cate-sub-item:hover::before {
  background: #e74c3c;
}

/* 右侧轮播 */
.hero-carousel-wrap {
  flex: 1;
  position: relative;
  overflow: hidden;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}
.hero-carousel {
  height: 440px;
}
.hero-carousel :deep(.el-carousel__container) {
  height: 440px;
}

/* ===== 右侧面板（统一白卡片） ===== */
.hero-right-panel {
  width: 275px;
  flex-shrink: 0;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 0;
  overflow: hidden;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
  padding: 16px 15px;
}

/* 用户卡片 */
.right-user-card {
  flex-shrink: 0;
  padding-bottom: 14px;
  margin: -16px -15px 0;
  padding: 16px 15px 14px;
  background: linear-gradient(180deg, #fcebeb 0%, #ffffff 100%);
  border-radius: 12px 12px 0 0;
  border-bottom: 1px solid #f0f0f0;
}
.ruc-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}
.ruc-avatar {
  flex-shrink: 0;
  border: 2px solid #f0f0f0;
}
.ruc-greeting {
  font-size: 18px;
  font-weight: 700;
  color: #1f2329;
  letter-spacing: 0.5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ruc-hi {
  font-weight: 500;
  color: #8b949e;
}
.ruc-name {
  font-weight: 700;
  color: #1f2329;
}
.ruc-actions {
  display: flex;
  gap: 0;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}
.ruc-action-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 4px;
  text-decoration: none;
  color: #666;
  font-size: 11px;
  transition: all 0.2s;
  position: relative;
}
.ruc-action-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 20%;
  height: 60%;
  width: 1px;
  background: #f0f0f0;
}
.ruc-action-item:hover {
  background: #fef0f0;
  color: #e74c3c;
}
.ruc-action-item .el-icon {
  font-size: 18px;
}
.ruc-unauth {
  display: flex;
  gap: 8px;
  border: none;
}
.ruc-btn {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  border-radius: 18px;
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s;
}
.ruc-btn-outline {
  color: #e74c3c;
  border: 1.5px solid #e74c3c;
  background: #fff;
}
.ruc-btn-outline:hover {
  background: #fff5f5;
}
.ruc-btn-primary {
  color: #fff;
  background: linear-gradient(135deg, #ff4757, #e74c3c);
}
.ruc-btn-primary:hover {
  opacity: 0.9;
}

/* 平台快讯 */
.right-news {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.right-news-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  flex-shrink: 0;
}
.rnh-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}
.rnh-more {
  font-size: 12px;
  color: #999;
  cursor: pointer;
  transition: color 0.2s;
}
.rnh-more:hover {
  color: #e74c3c;
}
.right-news-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
}
.right-news-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 0;
  cursor: pointer;
  transition: all 0.2s;
}
.right-news-item:hover {
  color: #e74c3c;
}
.rni-dot {
  flex-shrink: 0;
  width: 6px;
  height: 6px;
  border-radius: 50%;
}
.rni-dot-0 { background: #e74c3c; }
.rni-dot-1 { background: #f39c12; }
.rni-dot-2 { background: #409eff; }
.rni-text {
  font-size: 12px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}
.right-news-empty {
  text-align: center;
  color: #bbb;
  font-size: 12px;
  padding: 12px 0;
}

/* 促销入口 */
.right-promo {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  padding-top: 12px;
  width: 100%;
  box-sizing: border-box;
}
.rp-block {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 8px 5px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
}
.rp-block:hover {
  transform: translateY(-2px);
}
.rp-coupon {
  background: linear-gradient(135deg, #fdf6f0, #fef0e6);
}
.rp-coupon:hover {
  box-shadow: 0 4px 12px rgba(200,140,80,0.25);
}
.rp-seckill {
  background: linear-gradient(135deg, #fef0f0, #fde8e8);
}
.rp-seckill:hover {
  box-shadow: 0 4px 12px rgba(231,76,60,0.25);
}
.rp-icon {
  font-size: 18px;
  flex-shrink: 0;
}
.rp-info {
  min-width: 0;
  overflow: hidden;
}
.rp-title {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rp-desc {
  font-size: 11px;
  color: #999;
  line-height: 1.2;
  margin-top: 1px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.hero-banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  cursor: pointer;
}
.hero-hot-tags {
  position: absolute;
  bottom: 12px;
  left: 12px;
  right: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  z-index: 10;
}
.hero-tag {
  background: rgba(0,0,0,0.4);
  color: #fff;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 4px;
  backdrop-filter: blur(8px);
  transition: all 0.3s ease;
}
.hero-tag:hover {
  background: rgba(231,76,60,0.85);
}
.hero-tag-rank {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: rgba(255,255,255,0.3);
  font-size: 11px;
  font-weight: bold;
}
.hero-tag-rank.top3 {
  background: #e74c3c;
}

/* ===== 通用 Zone ===== */
.zone {
  max-width: 1320px;
  margin: 0 auto 30px;
}
.zone-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}
.zone-title {
  font-size: 22px;
  font-weight: 700;
  color: #222;
  display: flex;
  align-items: center;
  gap: 8px;
}
.zone-title-text {
  display: inline-block;
}
.zone-title-text--seckill {
  background: linear-gradient(90deg, #ff4d4f, #ff9000) !important;
  -webkit-background-clip: text !important;
  background-clip: text !important;
  -webkit-text-fill-color: transparent !important;
  color: transparent !important;
}
.zone-title-text--coupon {
  background: linear-gradient(90deg, #f39c12, #e74c3c) !important;
  -webkit-background-clip: text !important;
  background-clip: text !important;
  -webkit-text-fill-color: transparent !important;
  color: transparent !important;
}
.zone-title-text--hot {
  background: linear-gradient(90deg, #ff4d4f, #ff6b81) !important;
  -webkit-background-clip: text !important;
  background-clip: text !important;
  -webkit-text-fill-color: transparent !important;
  color: transparent !important;
}
.zone-title-text--new {
  background: linear-gradient(90deg, #00b09b, #96c93d) !important;
  -webkit-background-clip: text !important;
  background-clip: text !important;
  -webkit-text-fill-color: transparent !important;
  color: transparent !important;
}
.zone-title-text--promo {
  background: linear-gradient(90deg, #c0392b, #8e44ad) !important;
  -webkit-background-clip: text !important;
  background-clip: text !important;
  -webkit-text-fill-color: transparent !important;
  color: transparent !important;
}
.zone-title-text--announce {
  background: linear-gradient(90deg, #1e3c72, #2a5298) !important;
  -webkit-background-clip: text !important;
  background-clip: text !important;
  -webkit-text-fill-color: transparent !important;
  color: transparent !important;
}
.zone-icon {
  font-size: 24px;
}
.zone-sub {
  font-size: 13px;
  font-weight: 400;
  color: #bbb;
  text-transform: uppercase;
  letter-spacing: 2px;
  margin-left: 4px;
}
.zone-more {
  font-size: 13px;
  color: #999;
  cursor: pointer;
  transition: color 0.3s;
  display: flex;
  align-items: center;
  gap: 2px;
}
.zone-more:hover {
  color: #e74c3c;
}
.zone-more .arrow {
  font-size: 16px;
}

/* ===== 秒杀区 ===== */
.seckill-zone {
  background: #fff;
  border-radius: 12px;
  padding: 24px 28px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}
.seckill-scroll {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
  padding-bottom: 4px;
}
.seckill-scroll::-webkit-scrollbar { display: none; }
.seckill-item {
  flex: 0 0 220px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}
.seckill-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(231,76,60,0.1);
  border-color: #fde2e2;
}
.seckill-item-img {
  position: relative;
  height: 140px;
  overflow: hidden;
}
.seckill-item-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}
.seckill-item:hover .seckill-item-img img {
  transform: scale(1.08);
}
.seckill-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: linear-gradient(135deg, #ff3131 0%, #ff8917 100%);
  border: none;
  color: #fff;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}
.seckill-item-info {
  padding: 12px;
}
.seckill-item-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6px;
}
.seckill-item-price {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 8px;
}
.s-price-curr {
  color: #e74c3c;
  font-size: 20px;
  font-weight: 700;
}
.s-price-orig {
  color: #bbb;
  font-size: 12px;
  text-decoration: line-through;
}
.seckill-item-countdown {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
}
.cd-block {
  background: #333;
  color: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 700;
  font-family: 'SF Mono', 'Menlo', 'Courier New', monospace;
}
.cd-colon {
  color: #333;
  font-weight: 700;
  font-size: 13px;
}
.seckill-item-progress {
  display: flex;
  align-items: center;
  gap: 6px;
}
.s-progress-bar {
  flex: 1;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
}
.s-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #e74c3c, #ff6b6b);
  border-radius: 3px;
  transition: width 0.5s ease;
}
.s-progress-text {
  font-size: 11px;
  color: #999;
  white-space: nowrap;
}

/* ===== 优惠券区 ===== */
.coupon-zone {
  background: #fff;
  border-radius: 12px;
  padding: 24px 28px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}
.coupon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.coupon-card-new {
  display: flex;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #fdf6f0 0%, #fef9f3 50%, #fff 100%);
  border: 1px solid #f0e0d0;
}
.coupon-card-new:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(180,120,60,0.15);
}
.cc-left {
  width: 110px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #d4a574, #c89664);
  color: #fff;
  padding: 20px 8px;
}
.cc-amount {
  display: flex;
  align-items: baseline;
}
.cc-symbol {
  font-size: 16px;
  font-weight: 600;
}
.cc-num {
  font-size: 36px;
  font-weight: 800;
  line-height: 1;
}
.cc-condition {
  font-size: 12px;
  margin-top: 6px;
  opacity: 0.85;
}
.cc-divider {
  width: 0;
  border-left: 2px dashed #e0d0c0;
  margin: 12px 0;
}
.cc-right {
  flex: 1;
  padding: 16px 14px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
}
.cc-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}
.cc-expire {
  font-size: 12px;
  color: #aaa;
}
.cc-claim-btn {
  align-self: flex-start;
}

/* ===== 商品区 ===== */
.product-zone {
  background: #fff;
  border-radius: 12px;
  padding: 24px 28px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}
.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 24px rgba(225, 37, 27, 0.12);
  border-color: #e0e0e0;
}
.pc-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f8f9fa;
  position: relative;
}
.pc-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}
.product-card:hover .pc-image img {
  transform: scale(1.06);
}
.pc-badge-new {
  position: absolute;
  top: 10px;
  left: 10px;
  background: linear-gradient(135deg, #2ecc71, #27ae60);
  color: #fff;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 1px;
}
.pc-body {
  padding: 14px 14px 16px;
}
.pc-name {
  font-size: 13px;
  color: #333;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 8px;
  min-height: 38px;
}
.pc-tags {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}
.pc-tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  background: #fef0f0;
  color: #e74c3c;
  border: 1px solid #fde2e2;
}
.pc-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.pc-price {
  display: flex;
  align-items: baseline;
  gap: 6px;
}
.pc-price-curr {
  color: #e74c3c;
  font-size: 18px;
  font-weight: 700;
}
.pc-price-orig {
  color: #bbb;
  font-size: 12px;
  text-decoration: line-through;
}
.pc-sales {
  font-size: 11px;
  color: #bbb;
}

.product-card-skeleton {
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  overflow: hidden;
}

/* ===== 促销活动简单卡片 ===== */
.promo-simple-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
}
.promo-simple-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  border: 1px solid #eee;
  transition: all 0.3s ease;
  cursor: default;
}
.promo-simple-card:hover {
  box-shadow: 0 6px 20px rgba(0,0,0,0.06);
  transform: translateY(-2px);
}
.ps-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  margin-bottom: 10px;
}
.tag-discount {
  background: #fef6e6;
  color: #e6a23c;
}
.tag-promo {
  background: #e8f8f0;
  color: #27ae60;
}
.ps-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}
.ps-desc {
  font-size: 13px;
  color: #888;
  margin-bottom: 8px;
}
.ps-expire {
  font-size: 12px;
  color: #bbb;
}

/* ===== 公告区 ===== */
.announce-list {
  display: flex;
  flex-direction: column;
  gap: 1px;
  background: #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
}
.announce-item {
  background: #fff;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: background 0.2s;
}
.announce-item:hover {
  background: #f9f9f9;
}
.announce-item:hover .announce-title {
  color: #e74c3c;
}
.announce-tag {
  flex-shrink: 0;
  padding: 2px 8px;
  background: #e8f4fd;
  color: #409eff;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}
.announce-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
}
.announce-content {
  flex: 1;
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.announce-dialog-body {
  white-space: pre-wrap;
  line-height: 1.9;
  font-size: 14px;
  color: #333;
  padding: 8px 0;
}

/* ===== 响应式 ===== */
@media (max-width: 1200px) {
  .product-grid { grid-template-columns: repeat(4, 1fr); }
  .category-nav { width: 190px; }
  .cate-sub-popup { left: 190px; width: 440px; }
  .hero-right-panel { width: 230px; }
  .right-news-item { padding: 5px 0; }
  .rni-text { font-size: 11px; }
}
@media (max-width: 992px) {
  .hero-inner { height: auto; flex-direction: column; }
  .category-nav { display: none; }
  .hero-right-panel { display: none; }
  .hero-carousel { height: 260px; }
  .hero-carousel :deep(.el-carousel__container) { height: 260px; }
  .hero-carousel-wrap { width: 100%; border-radius: 8px; }
  .product-grid { grid-template-columns: repeat(3, 1fr); }
  .seckill-item { flex: 0 0 187px; }
  .seckill-item-img { height: 120px; }
  .cc-num { font-size: 28px; }
}
@media (max-width: 768px) {
  .zone { border-radius: 8px; padding: 16px; }
  .hero-carousel { height: 200px; }
  .hero-carousel :deep(.el-carousel__container) { height: 200px; }
  .hero-carousel-wrap { border-radius: 8px; }
  .product-grid { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .pc-image { height: 150px; }
  .seckill-item { flex: 0 0 165px; }
  .seckill-item-img { height: 110px; }
  .coupon-grid { grid-template-columns: 1fr; }
  .zone-title { font-size: 18px; }
  .cc-num { font-size: 24px; }
  .cc-left { width: 90px; }
  .hero-hot-tags { display: none; }
}
@media (max-width: 480px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); gap: 8px; }
  .pc-image { height: 130px; }
  .pc-name { font-size: 12px; min-height: 32px; }
  .pc-price-curr { font-size: 16px; }
  .seckill-zone, .coupon-zone, .product-zone { padding: 12px; border-radius: 8px; }
  .announce-item { flex-direction: column; align-items: flex-start; gap: 4px; }
  .announce-content { white-space: normal; }
}
</style>