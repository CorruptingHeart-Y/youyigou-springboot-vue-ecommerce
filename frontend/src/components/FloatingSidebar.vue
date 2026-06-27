<template>
  <div class="floating-sidebar" :class="{ 'sidebar-hidden': !visible }">
    <!-- ═══ 返回顶部 ═══ -->
    <transition name="fade">
      <el-tooltip content="返回顶部" placement="left" :teleported="false" v-if="showBackTop">
        <div class="sidebar-item" @click="scrollToTop">
          <el-icon :size="22"><Top /></el-icon>
        </div>
      </el-tooltip>
    </transition>

    <!-- ═══ 领券中心 ═══ -->
    <el-tooltip content="领券中心" placement="left" :teleported="false">
      <div class="sidebar-item" @click="openCouponDialog">
        <el-icon :size="22"><Ticket /></el-icon>
      </div>
    </el-tooltip>

    <!-- ═══ 在线客服 ═══ -->
    <el-tooltip content="在线客服" placement="left" :teleported="false">
      <div class="sidebar-item" @click="openChat">
        <el-icon :size="22"><Service /></el-icon>
      </div>
    </el-tooltip>

    <!-- ═══ 购物车 ═══ -->
    <el-popover
      placement="left-start"
      :width="320"
      trigger="hover"
      :show-after="200"
      :hide-after="100"
      :teleported="false"
    >
      <template #reference>
        <div class="sidebar-item cart-item">
          <el-badge :value="cartStore.count" :hidden="!cartStore.count" :max="99">
            <el-icon :size="22"><ShoppingCart /></el-icon>
          </el-badge>
        </div>
      </template>
      <div class="cart-popover">
        <div class="cart-popover-title">购物车</div>
        <div v-if="!cartStore.items.length" class="cart-empty">购物车是空的</div>
        <template v-else>
          <div class="cart-popover-list">
            <div v-for="item in cartStore.items.slice(0, 5)" :key="item.id" class="cart-popover-item">
              <img v-if="item.productImage" :src="item.productImage" class="cart-popover-img" />
              <div v-else class="cart-popover-img-placeholder"></div>
              <div class="cart-popover-info">
                <div class="cart-popover-name">{{ item.productName }}</div>
                <div class="cart-popover-meta">x{{ item.quantity }} · ¥{{ item.price }}</div>
              </div>
            </div>
          </div>
          <div v-if="cartStore.items.length > 5" class="cart-popover-more">
            还有 {{ cartStore.items.length - 5 }} 件商品...
          </div>
        </template>
        <div class="cart-popover-actions" v-if="cartStore.items.length">
          <el-button size="small" @click="$router.push('/cart')">查看购物车</el-button>
          <el-button size="small" type="primary" @click="$router.push('/cart')">去结算</el-button>
        </div>
      </div>
    </el-popover>
  </div>

  <!-- ═══ 客服聊天抽屉 ═══ -->
  <el-drawer v-model="chatDrawer" title="在线客服" direction="rtl" :size="420" :z-index="3000">
    <template #header>
      <div class="chat-drawer-header">
        <span class="chat-drawer-icon">💬</span>
        <span>在线客服</span>
        <span class="chat-drawer-status" :class="wsConnected ? 'online' : 'offline'">
          {{ wsConnected ? '已连接' : '未连接' }}
        </span>
      </div>
    </template>
    <div class="chat-drawer-body">
      <div class="chat-drawer-messages" ref="chatMsgBox">
        <div v-if="!chatMessages.length && wsConnected" class="chat-drawer-empty">
          <div class="chat-empty-icon">👋</div>
          <p>你好！有什么可以帮到你的吗？</p>
          <div class="chat-quick-btns">
            <el-button size="small" round @click="sendQuickMsg('我想查询订单状态')">查询订单</el-button>
            <el-button size="small" round @click="sendQuickMsg('我想退换货')">退换货</el-button>
            <el-button size="small" round @click="sendQuickMsg('商品有问题')">商品问题</el-button>
          </div>
        </div>
        <div v-if="!wsConnected && !chatMessages.length" class="chat-drawer-empty">
          <p>正在连接客服系统...</p>
        </div>
        <div v-for="(msg, i) in chatMessages" :key="i" class="chat-msg-wrapper">
          <div v-if="msg.type === 'system'" class="chat-msg-system">{{ msg.content }}</div>
          <div v-else :class="['chat-msg', msg.fromUserId === chatUserId ? 'chat-msg-self' : 'chat-msg-other']">
            <span class="chat-msg-user">{{ msg.fromUserId === chatUserId ? '我' : '客服' }}</span>
            <div class="chat-msg-bubble">{{ msg.content }}</div>
          </div>
        </div>
      </div>
      <div class="chat-drawer-input">
        <el-input
          v-model="chatInput"
          placeholder="输入消息..."
          @keyup.enter="sendChatMsg"
          :disabled="!wsConnected"
          maxlength="500"
        >
          <template #append>
            <el-button @click="sendChatMsg" :disabled="!wsConnected || !chatInput.trim()" type="primary">发送</el-button>
          </template>
        </el-input>
      </div>
    </div>
  </el-drawer>

  <!-- ═══ 领券对话框 ═══ -->
  <el-dialog v-model="couponDialog" title="领券中心" width="560px" :z-index="3000">
    <div v-loading="couponLoading" class="coupon-dialog-body">
      <el-empty v-if="!couponLoading && !availableCoupons.length" description="暂无可用优惠券" />
      <div class="coupon-dialog-grid" v-else>
        <div v-for="c in availableCoupons" :key="c.id" class="coupon-dialog-card" :class="{ claimed: claimedSet.has(c.id) }">
          <div class="coupon-card-left">
            <div class="coupon-card-value">¥{{ c.discountValue }}</div>
            <div class="coupon-card-condition">满{{ c.minAmount || 0 }}可用</div>
          </div>
          <div class="coupon-card-right">
            <div class="coupon-card-name">{{ c.name }}</div>
            <div class="coupon-card-time">{{ c.startTime?.slice(0, 10) || '' }} ~ {{ c.endTime?.slice(0, 10) || '' }}</div>
            <el-button
              size="small"
              :type="claimedSet.has(c.id) ? 'info' : 'primary'"
              :disabled="claimedSet.has(c.id)"
              @click="doClaimCoupon(c)"
              style="margin-top:8px"
            >
              {{ claimedSet.has(c.id) ? '已领取' : '立即领取' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { promotionApi } from '@/api/endpoints'
import { ElMessage } from 'element-plus'
import { Top, Ticket, Service, ShoppingCart } from '@element-plus/icons-vue'

const router = useRouter()
const cartStore = useCartStore()

const visible = ref(true)
const showBackTop = ref(false)
let lastScrollY = 0
let visibilityTimer = null

// ==================== Scroll handling ====================
const onScroll = () => {
  const y = window.scrollY
  showBackTop.value = y > 400
  if (y > lastScrollY + 30) {
    visible.value = false
  } else if (y < lastScrollY - 10) {
    visible.value = true
    if (visibilityTimer) clearTimeout(visibilityTimer)
    visibilityTimer = setTimeout(() => { visible.value = true }, 1500)
  }
  lastScrollY = y
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// ==================== Customer service / WebSocket ====================
const chatDrawer = ref(false)
const chatInput = ref('')
const chatMessages = ref([])
const chatMsgBox = ref(null)
const chatUserId = ref(Number(localStorage.getItem('userId') || 0))
const wsConnected = ref(false)
let ws = null
let reconnectTimer = null
let reconnectAttempts = 0
const MAX_RECONNECT = 10

const connectWs = () => {
  const token = localStorage.getItem('token')
  if (!token) return
  const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
  const url = `${protocol}//${location.host}/ws/chat/${token}`
  ws = new WebSocket(url)

  ws.onopen = () => {
    wsConnected.value = true
    reconnectAttempts = 0
    if (!chatMessages.value.length) {
      chatMessages.value.push({ type: 'system', content: '已连接到客服，请描述您的问题' })
    }
    scrollChatBottom()
  }

  ws.onmessage = (e) => {
    try {
      const data = JSON.parse(e.data)
      chatMessages.value.push(data)
      scrollChatBottom()
    } catch (err) {}
  }

  ws.onclose = () => {
    wsConnected.value = false
    if (reconnectAttempts < MAX_RECONNECT) {
      const delay = Math.min(5000, 1000 * Math.pow(2, reconnectAttempts))
      reconnectAttempts++
      reconnectTimer = setTimeout(connectWs, delay)
    }
  }

  ws.onerror = () => {
    wsConnected.value = false
  }
}

const scrollChatBottom = () => {
  nextTick(() => {
    if (chatMsgBox.value) {
      chatMsgBox.value.scrollTop = chatMsgBox.value.scrollHeight
    }
  })
}

const openChat = () => {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    return
  }
  chatDrawer.value = true
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    connectWs()
  }
}

const sendChatMsg = () => {
  if (!chatInput.value.trim()) return
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    ElMessage.warning('连接未建立，请稍后重试')
    return
  }
  ws.send(JSON.stringify({ content: chatInput.value.trim() }))
  chatInput.value = ''
}

const sendQuickMsg = (text) => {
  chatInput.value = text
  sendChatMsg()
}

const closeWs = () => {
  if (reconnectTimer) clearTimeout(reconnectTimer)
  if (ws) ws.close()
}

// ==================== Coupon ====================
const couponDialog = ref(false)
const couponLoading = ref(false)
const availableCoupons = ref([])
const claimedSet = ref(new Set(JSON.parse(localStorage.getItem('claimedCoupons') || '[]')))

const openCouponDialog = async () => {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    return
  }
  couponDialog.value = true
  couponLoading.value = true
  try {
    const res = await promotionApi.getActive()
    availableCoupons.value = (res.data || []).filter(p => p.type === 'COUPON')
  } catch (e) {
    availableCoupons.value = []
  } finally {
    couponLoading.value = false
  }
}

const doClaimCoupon = async (promo) => {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    await promotionApi.claimCoupon(promo.id)
    ElMessage.success('优惠券领取成功！')
    claimedSet.value.add(promo.id)
    localStorage.setItem('claimedCoupons', JSON.stringify([...claimedSet.value]))
  } catch (e) {
    ElMessage.error('领取失败，请重试')
  }
}

onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  if (localStorage.getItem('token')) {
    cartStore.fetchCart()
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
  if (visibilityTimer) clearTimeout(visibilityTimer)
  closeWs()
})
</script>

<style scoped>
.floating-sidebar {
  position: fixed;
  right: 20px;
  bottom: 15%;
  z-index: 999;
  display: flex;
  flex-direction: column;
  gap: 4px;
  transition: opacity 0.3s ease, transform 0.3s ease;
}
.sidebar-hidden {
  opacity: 0.3;
  pointer-events: none;
}

.sidebar-item {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  color: #606266;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.2s ease;
  position: relative;
}
.sidebar-item:hover {
  background: #409eff;
  color: #fff;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.3);
  transform: translateX(-2px);
}
.cart-item {
  padding: 0;
}

/* Cart popover */
.cart-popover { max-height: 360px; display: flex; flex-direction: column; }
.cart-popover-title { font-size: 15px; font-weight: 600; margin-bottom: 10px; color: #303133; }
.cart-empty { text-align: center; color: #909399; padding: 20px 0; font-size: 13px; }
.cart-popover-list { flex: 1; overflow-y: auto; max-height: 220px; }
.cart-popover-item {
  display: flex; align-items: center; gap: 10px; padding: 8px 0;
  border-bottom: 1px solid #f2f3f5;
}
.cart-popover-item:last-child { border-bottom: none; }
.cart-popover-img { width: 40px; height: 40px; object-fit: cover; border-radius: 4px; flex-shrink: 0; }
.cart-popover-img-placeholder { width: 40px; height: 40px; background: #f5f7fa; border-radius: 4px; flex-shrink: 0; }
.cart-popover-info { flex: 1; min-width: 0; }
.cart-popover-name { font-size: 13px; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.cart-popover-meta { font-size: 12px; color: #909399; margin-top: 2px; }
.cart-popover-more { text-align: center; font-size: 12px; color: #909399; padding: 8px 0; }
.cart-popover-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 10px; padding-top: 10px; border-top: 1px solid #f2f3f5; }

/* Chat drawer */
.chat-drawer-header { display: flex; align-items: center; gap: 8px; }
.chat-drawer-icon { font-size: 20px; }
.chat-drawer-status { font-size: 12px; margin-left: auto; padding: 2px 8px; border-radius: 10px; }
.chat-drawer-status.online { background: #e8f8e8; color: #67c23a; }
.chat-drawer-status.offline { background: #fef0f0; color: #f56c6c; }

.chat-drawer-body { display: flex; flex-direction: column; height: 100%; }
.chat-drawer-messages { flex: 1; overflow-y: auto; padding: 12px 4px; background: #f5f7fa; border-radius: 8px; }
.chat-drawer-messages::-webkit-scrollbar { width: 4px; }
.chat-drawer-messages::-webkit-scrollbar-thumb { background: #dcdfe6; border-radius: 2px; }
.chat-drawer-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #909399; font-size: 14px; }
.chat-empty-icon { font-size: 40px; margin-bottom: 8px; }
.chat-quick-btns { display: flex; gap: 6px; margin-top: 12px; flex-wrap: wrap; justify-content: center; }

.chat-msg-wrapper { margin-bottom: 12px; }
.chat-msg-system { text-align: center; font-size: 12px; color: #909399; background: #ebeef5; display: inline-block; padding: 4px 12px; border-radius: 10px; width: 100%; }
.chat-msg { display: flex; flex-direction: column; gap: 2px; }
.chat-msg-other { align-items: flex-start; }
.chat-msg-self { align-items: flex-end; }
.chat-msg-user { font-size: 11px; color: #909399; }
.chat-msg-bubble { max-width: 75%; padding: 10px 14px; border-radius: 14px; font-size: 13px; line-height: 1.5; word-break: break-word; }
.chat-msg-other .chat-msg-bubble { background: #fff; color: #303133; border-top-left-radius: 4px; }
.chat-msg-self .chat-msg-bubble { background: #409eff; color: #fff; border-top-right-radius: 4px; }

.chat-drawer-input { margin-top: 10px; }

/* Coupon dialog */
.coupon-dialog-body { min-height: 200px; }
.coupon-dialog-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.coupon-dialog-card {
  display: flex; border-radius: 10px; overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06); transition: all 0.2s;
}
.coupon-dialog-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.1); transform: translateY(-2px); }
.coupon-dialog-card.claimed { opacity: 0.6; }
.coupon-card-left {
  width: 90px; background: linear-gradient(135deg, #f56c6c, #e74c3c); color: #fff;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 16px 8px; flex-shrink: 0;
}
.coupon-card-value { font-size: 22px; font-weight: bold; }
.coupon-card-condition { font-size: 11px; margin-top: 4px; opacity: 0.9; }
.coupon-card-right { flex: 1; padding: 12px; display: flex; flex-direction: column; justify-content: center; background: #fff; }
.coupon-card-name { font-size: 14px; font-weight: 600; color: #303133; }
.coupon-card-time { font-size: 11px; color: #909399; margin-top: 4px; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

@media (max-width: 768px) {
  .floating-sidebar { right: 8px; bottom: 10%; gap: 2px; }
  .sidebar-item { width: 36px; height: 36px; border-radius: 6px; }
  .sidebar-item .el-icon { font-size: 18px !important; }
  .coupon-dialog-grid { grid-template-columns: 1fr; }
}
</style>
