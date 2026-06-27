<template>
  <div class="cart-page">
    <h3 class="cart-title">我的购物车</h3>
    <el-empty v-if="!cartStore.items.length" description="购物车是空的">
      <el-button type="primary" @click="$router.push('/')">去逛逛</el-button>
    </el-empty>
    <template v-else>
      <div class="cart-card">
        <div class="cart-toolbar">
          <el-checkbox :model-value="allChecked" @change="cartStore.checkAll($event)">全选</el-checkbox>
          <el-button type="danger" size="small" text @click="batchRemove" :disabled="!anyChecked">批量删除</el-button>
        </div>

        <div v-loading="cartLoading" class="cart-list">
          <div v-for="item in cartStore.items" :key="item.id" class="cart-item">
            <el-checkbox :model-value="!!item.checked" @change="cartStore.checkItem(item.id, $event)" class="cart-check" />
            <div class="cart-img-wrap">
              <img v-if="item.productImage" :src="item.productImage" class="cart-img" @click="$router.push('/product/'+item.productId)" />
              <div v-else class="cart-img-placeholder"></div>
            </div>
            <div class="cart-info">
              <div class="cart-name" @click="$router.push('/product/'+item.productId)">{{ item.productName }}</div>
              <div class="cart-spec" v-if="item.spec">{{ item.spec }}</div>
            </div>
            <div class="cart-price">¥{{ item.price }}</div>
            <div class="cart-qty">
              <el-input-number v-model="item.quantity" :min="1" size="small" @change="cartStore.updateQuantity(item.id, item.quantity)" />
            </div>
            <div class="cart-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
            <el-button type="danger" size="small" text @click="cartStore.removeItem(item.id)" class="cart-del">删除</el-button>
          </div>
        </div>
      </div>

      <div class="cart-footer">
        <div class="cart-summary">
          已选 <span class="highlight">{{ checkedCount }}</span> 件商品，合计:
          <span class="cart-total">¥{{ total.toFixed(2) }}</span>
        </div>
        <el-button type="danger" size="large" round @click="checkout" :disabled="!anyChecked" class="checkout-btn">去结算</el-button>
      </div>
    </template>

    <OrderConfirmDialog
      v-model:visible="dialogVisible"
      :items="dialogItems"
      :total-amount="total"
      @submit="handleOrderSubmit"
    />
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { orderApi } from '@/api/endpoints'
import OrderConfirmDialog from '@/components/OrderConfirmDialog.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()
const dialogVisible = ref(false)
const submitting = ref(false)
const cartLoading = ref(false)

const allChecked = computed(() => cartStore.items.length > 0 && cartStore.items.every(i => i.checked))
const anyChecked = computed(() => cartStore.items.some(i => i.checked))
const checkedCount = computed(() => cartStore.items.filter(i => i.checked).length)
const total = computed(() => cartStore.items.filter(i => i.checked).reduce((sum, i) => sum + i.price * i.quantity, 0))

const dialogItems = computed(() =>
  cartStore.items
    .filter(i => i.checked)
    .map(i => ({
      name: i.productName,
      image: i.productImage,
      price: i.price,
      quantity: i.quantity,
      spec: i.spec || '默认',
      subtotal: (i.price * i.quantity).toFixed(2)
    }))
)

const batchRemove = async () => {
  const checkedItems = cartStore.items.filter(i => i.checked)
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${checkedItems.length} 件商品？`, '确认删除', { type: 'warning' })
    for (const item of checkedItems) { await cartStore.removeItem(item.id) }
    ElMessage.success('已删除')
  } catch (e) {}
}

onMounted(() => { cartStore.fetchCart() })

const checkout = () => {
  if (!anyChecked.value) { ElMessage.warning('请选择商品'); return }
  dialogVisible.value = true
}

const handleOrderSubmit = async (payload) => {
  submitting.value = true
  try {
    const res = await orderApi.create(payload)
    ElMessage.success('订单创建成功')
    dialogVisible.value = false
    cartStore.fetchCart()
    router.push('/order/' + res.data.orderNo)
  } catch (e) {} finally {
    submitting.value = false
  }
}
</script>

<style scoped>
/* ===== Page shell ===== */
.cart-page {
  min-height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
}

.cart-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
}

/* ===== Main card ===== */
.cart-card {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  padding: 0 20px;
  flex: 1;
}

/* ===== Toolbar ===== */
.cart-toolbar {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 14px 0;
  border-bottom: 1px solid #ebeef5;
}

/* ===== List items ===== */
.cart-list { min-height: 80px; }

.cart-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}
.cart-item:hover { background: #fafcff; }

.cart-check { flex-shrink: 0; }

.cart-img-wrap { flex-shrink: 0; }

.cart-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  display: block;
}

.cart-img-placeholder {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ===== Info ===== */
.cart-info { flex: 1; min-width: 0; }

.cart-name {
  font-size: 14px;
  color: #303133;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cart-name:hover { color: #409eff; }

.cart-spec {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* ===== Price / Qty / Subtotal ===== */
.cart-price {
  width: 80px;
  text-align: center;
  color: #606266;
  font-size: 14px;
  flex-shrink: 0;
}

.cart-qty { width: 130px; flex-shrink: 0; }

.cart-subtotal {
  width: 100px;
  text-align: right;
  color: #f56c6c;
  font-weight: 600;
  font-size: 15px;
  flex-shrink: 0;
}

.cart-del { flex-shrink: 0; }

/* ===== Sticky footer ===== */
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 24px;
  margin-top: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.04);
  position: sticky;
  bottom: 0;
  z-index: 10;
}

.cart-summary { font-size: 14px; color: #606266; }
.highlight { color: #409eff; font-weight: bold; }
.cart-total { color: #f56c6c; font-size: 24px; font-weight: bold; margin-left: 5px; }

.checkout-btn {
  border-radius: 24px;
  padding: 10px 32px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1px;
  background: #e1251b;
  border-color: #e1251b;
}
.checkout-btn:hover {
  background: #c11c14;
  border-color: #c11c14;
}

@media (max-width: 768px) {
  .cart-card { padding: 0 10px; }
  .cart-item { flex-wrap: wrap; gap: 8px; }
  .cart-price, .cart-subtotal { width: auto; }
  .cart-qty { width: 110px; }
  .cart-img, .cart-img-placeholder { width: 60px; height: 60px; }
  .cart-footer { flex-direction: column; gap: 10px; }
  .cart-total { font-size: 20px; }
}
</style>
