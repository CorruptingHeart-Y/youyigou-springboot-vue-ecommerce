<template>
  <div class="od-page" v-loading="loading">
    <div class="od-container">
      <!-- 顶部导航 -->
      <el-page-header @back="$router.push('/orders')" class="od-back">
        <template #content>
          <span class="od-back-title">订单详情</span>
        </template>
      </el-page-header>

      <template v-if="order">
        <!-- 状态步骤条 -->
        <div class="od-card od-steps-card">
          <el-steps :active="stepActive" :process-status="stepProcessStatus" finish-status="success" align-center>
            <el-step title="提交订单" :description="order.createTime?.substring(0,10)" />
            <el-step title="支付成功" :description="order.payTime?.substring(0,10) || ''" />
            <el-step title="商家发货" :description="order.deliveryTime?.substring(0,10) || ''" />
            <el-step title="交易完成" :description="order.finishTime?.substring(0,10) || ''" />
          </el-steps>
        </div>

        <!-- 收货信息 -->
        <div class="od-card">
          <div class="od-card-title">
            <el-icon><LocationFilled /></el-icon> 收货信息
          </div>
          <div class="od-info-grid">
            <div class="od-info-item">
              <span class="od-info-label">收货人</span>
              <span class="od-info-value">{{ order.receiverName }}</span>
            </div>
            <div class="od-info-item">
              <span class="od-info-label">联系电话</span>
              <span class="od-info-value">{{ order.receiverPhone }}</span>
            </div>
            <div class="od-info-item od-info-full">
              <span class="od-info-label">收货地址</span>
              <span class="od-info-value">{{ order.addressDetail }}</span>
            </div>
            <div class="od-info-item" v-if="order.logisticsNo">
              <span class="od-info-label">物流单号</span>
              <span class="od-info-value">{{ order.logisticsNo }}</span>
            </div>
            <div class="od-info-item" v-if="order.logisticsCompany">
              <span class="od-info-label">物流公司</span>
              <span class="od-info-value">{{ order.logisticsCompany }}</span>
            </div>
          </div>
        </div>

        <!-- 订单信息 -->
        <div class="od-card">
          <div class="od-card-title">
            <el-icon><Document /></el-icon> 订单信息
          </div>
          <div class="od-info-grid">
            <div class="od-info-item">
              <span class="od-info-label">订单编号</span>
              <span class="od-info-value od-mono">{{ order.orderNo }}</span>
            </div>
            <div class="od-info-item">
              <span class="od-info-label">订单状态</span>
              <span class="od-info-value">
                <el-tag :type="statusType" effect="light" size="small">{{ statusMap[order.status] }}</el-tag>
              </span>
            </div>
            <div class="od-info-item">
              <span class="od-info-label">下单时间</span>
              <span class="od-info-value">{{ order.createTime?.substring(0,19) }}</span>
            </div>
            <div class="od-info-item" v-if="order.payTime">
              <span class="od-info-label">支付时间</span>
              <span class="od-info-value">{{ order.payTime?.substring(0,19) }}</span>
            </div>
            <div class="od-info-item" v-if="order.remark">
              <span class="od-info-label">备注</span>
              <span class="od-info-value">{{ order.remark }}</span>
            </div>
          </div>
        </div>

        <!-- 商品列表 -->
        <div class="od-card">
          <div class="od-card-title">
            <el-icon><Goods /></el-icon> 商品列表
          </div>
          <div class="od-goods-list">
            <div class="od-goods-item" v-for="item in (order.items || [])" :key="item.id" @click="$router.push('/product/'+item.productId)">
              <img :src="item.productImage || 'https://via.placeholder.com/80'" class="od-goods-img" />
              <div class="od-goods-info">
                <div class="od-goods-name">{{ item.productName }}</div>
                <div class="od-goods-spec" v-if="item.spec && item.spec !== '默认'">{{ item.spec }}</div>
              </div>
              <div class="od-goods-price">¥{{ item.price }}</div>
              <div class="od-goods-qty">×{{ item.quantity }}</div>
              <div class="od-goods-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
            </div>
          </div>

          <!-- 合计 + 操作 -->
          <div class="od-footer">
            <div class="od-footer-total">
              合计：<span class="od-total-price">¥{{ order.payAmount || order.totalAmount }}</span>
            </div>
            <div class="od-footer-actions">
              <el-button v-if="order.status==='CANCELLED' || order.status==='REFUNDED'" plain round size="small" disabled>订单已关闭</el-button>
              <el-button v-if="order.status==='PENDING_PAY'" type="danger" round size="large" :loading="payLoading" @click="doPay" class="od-btn-primary">立即支付</el-button>
              <el-button v-if="order.status==='PENDING_PAY'" plain round size="large" :loading="cancelLoading" @click="doCancel">取消订单</el-button>
              <el-button v-if="order.status==='PENDING_DELIVER'" plain round size="large" :loading="cancelLoading" @click="doCancel">取消订单</el-button>
              <el-button v-if="order.status==='DELIVERED'" type="success" round size="large" :loading="confirmLoading" @click="doConfirm">确认收货</el-button>
              <el-button v-if="order.status==='COMPLETED'" type="danger" plain round size="large" @click="$router.push('/product/'+(order.items?.[0]?.productId||''))">再次购买</el-button>
              <el-button v-if="order.status==='COMPLETED'" type="warning" plain round size="large" :loading="refundLoading" @click="doRefund">申请退款</el-button>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi } from '@/api/endpoints'
import { ORDER_STATUS_MAP, ORDER_STATUS_TYPE_MAP } from '@/constants/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import { LocationFilled, Document, Goods } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const loading = ref(false)
const payLoading = ref(false)
const confirmLoading = ref(false)
const cancelLoading = ref(false)
const refundLoading = ref(false)

const statusMap = ORDER_STATUS_MAP
const statusType = computed(() => ORDER_STATUS_TYPE_MAP[order.value?.status] || '')

const stepActive = computed(() => {
  const s = order.value?.status
  if (!s) return 0
  if (s === 'PENDING_PAY' || s === 'CANCELLED') return 1
  if (s === 'PENDING_DELIVER') return 2
  if (s === 'DELIVERED') return 3
  if (s === 'COMPLETED' || s === 'REFUNDING' || s === 'REFUNDED') return 4
  return 1
})

const stepProcessStatus = computed(() => {
  const s = order.value?.status
  if (s === 'CANCELLED') return 'error'
  return 'process'
})

const fetch = async () => {
  loading.value = true
  try {
    const res = await orderApi.getDetail(route.params.orderNo)
    order.value = res.data
  } catch (e) {} finally {
    loading.value = false
  }
}

const doPay = async () => {
  try {
    await ElMessageBox.confirm('确认支付该订单？', '确认支付', { type: 'info' })
    payLoading.value = true
    await orderApi.pay({ orderNo: order.value.orderNo, payMethod: order.value.payMethod || 'ALIPAY' })
    ElMessage.success('支付成功')
    fetch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('支付失败')
  } finally {
    payLoading.value = false
  }
}

const doConfirm = async () => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'info' })
    confirmLoading.value = true
    await orderApi.confirm(order.value.orderNo)
    ElMessage.success('已确认收货')
    fetch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  } finally {
    confirmLoading.value = false
  }
}

const doCancel = async () => {
  try {
    await ElMessageBox.confirm('确定取消该订单？', '确认取消', { type: 'warning' })
    cancelLoading.value = true
    await orderApi.cancel(order.value.orderNo)
    ElMessage.success('订单已取消')
    fetch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('取消失败')
  } finally {
    cancelLoading.value = false
  }
}

const doRefund = async () => {
  try {
    await ElMessageBox.confirm('确定申请退款？', '申请退款', { type: 'warning' })
    refundLoading.value = true
    await orderApi.refund(order.value.orderNo)
    ElMessage.success('退款申请已提交')
    fetch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('申请失败')
  } finally {
    refundLoading.value = false
  }
}

onMounted(fetch)
</script>

<style scoped>
.od-page {
  min-height: 100vh;
  background: #f5f5f7;
}

.od-container {
  max-width: 860px;
  margin: 0 auto;
  padding: 20px 15px 40px;
}

/* Back nav */
.od-back {
  margin-bottom: 20px;
}
.od-back-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

/* Card */
.od-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
  padding: 20px 24px;
  margin-bottom: 16px;
}

/* Steps card */
.od-steps-card {
  padding: 28px 32px 20px;
}

.od-card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

/* Info grid — label left, value right */
.od-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px 32px;
}
.od-info-item {
  display: flex;
  align-items: baseline;
  gap: 10px;
  font-size: 14px;
}
.od-info-full {
  grid-column: 1 / -1;
}
.od-info-label {
  color: #909399;
  flex-shrink: 0;
  min-width: 70px;
}
.od-info-value {
  color: #303133;
  word-break: break-all;
}
.od-mono {
  font-family: 'SF Mono', 'Menlo', monospace;
  font-size: 13px;
  color: #606266;
}

/* Goods list */
.od-goods-list {
  display: flex;
  flex-direction: column;
}
.od-goods-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 0;
  cursor: pointer;
  transition: background 0.15s;
}
.od-goods-item:not(:last-child) {
  border-bottom: 1px solid #f5f5f5;
}
.od-goods-item:hover {
  background: #fafafa;
  margin: 0 -12px;
  padding: 12px;
}
.od-goods-img {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  flex-shrink: 0;
  background: #fafafa;
}
.od-goods-info {
  flex: 1;
  min-width: 0;
}
.od-goods-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  line-height: 1.4;
}
.od-goods-spec {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.od-goods-price {
  font-size: 14px;
  color: #606266;
  width: 80px;
  text-align: center;
  flex-shrink: 0;
}
.od-goods-qty {
  font-size: 13px;
  color: #909399;
  width: 50px;
  text-align: center;
  flex-shrink: 0;
}
.od-goods-subtotal {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  width: 90px;
  text-align: right;
  flex-shrink: 0;
}

/* Footer */
.od-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
.od-footer-total {
  font-size: 14px;
  color: #606266;
}
.od-total-price {
  font-size: 22px;
  font-weight: 700;
  color: #f56c6c;
}
.od-footer-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.od-btn-primary {
  background: #e1251b;
  border-color: #e1251b;
}
.od-btn-primary:hover {
  background: #c11c14;
  border-color: #c11c14;
}

@media (max-width: 768px) {
  .od-container {
    padding: 12px 10px 30px;
  }
  .od-card {
    padding: 14px 16px;
  }
  .od-steps-card {
    padding: 20px 16px 16px;
  }
  .od-info-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }
  .od-goods-item:hover {
    margin: 0 -8px;
    padding: 12px 8px;
  }
  .od-footer {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  .od-footer-actions {
    justify-content: flex-start;
  }
}
</style>
