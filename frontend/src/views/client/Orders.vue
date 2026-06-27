<template>
  <div class="orders-page">
    <div class="orders-container">
      <h3 class="orders-title">我的订单</h3>

      <el-tabs v-model="activeTab" @tab-change="fetchOrders" class="orders-tabs">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="待支付" name="PENDING_PAY" />
        <el-tab-pane label="待发货" name="PENDING_DELIVER" />
        <el-tab-pane label="待收货" name="DELIVERED" />
        <el-tab-pane label="已完成" name="COMPLETED" />
        <el-tab-pane label="已取消" name="CANCELLED" />
      </el-tabs>

      <el-empty v-if="!orders.length && !loading" description="暂无订单" />

      <div v-for="o in orders" :key="o.orderNo" class="order-card">
        <!-- 卡片头部 -->
        <div class="order-card-header">
          <div class="order-card-meta">
            <span class="order-card-no">订单号：{{ o.orderNo }}</span>
            <span class="order-card-time">{{ o.createTime?.substring(0, 19) }}</span>
          </div>
          <el-tag :type="statusTagType(o.status)" effect="light" size="small">
            {{ statusMap[o.status] || o.status }}
          </el-tag>
        </div>

        <!-- 商品列表 -->
        <div class="order-card-body">
          <div
            v-for="item in (o.items || [])"
            :key="item.id"
            class="order-item"
            @click="$router.push('/product/' + item.productId)"
          >
            <img
              :src="item.productImage || 'https://via.placeholder.com/120'"
              class="order-item-img"
            />
            <div class="order-item-info">
              <div class="order-item-name">{{ item.productName }}</div>
              <div class="order-item-spec" v-if="item.spec && item.spec !== '默认'">{{ item.spec }}</div>
            </div>
            <div class="order-item-price">
              <span class="oip-amount">¥{{ item.price }}</span>
              <span class="oip-qty">× {{ item.quantity }}</span>
            </div>
          </div>
        </div>

        <!-- 底部操作栏 -->
        <div class="order-card-footer">
          <div class="order-card-total">
            合计：<span class="total-price">¥{{ o.payAmount || o.totalAmount }}</span>
          </div>
          <div class="order-card-actions">
            <el-button v-if="o.status === 'PENDING_PAY'" type="primary" round size="small" @click="doPay(o)">立即支付</el-button>
            <el-button v-if="o.status === 'PENDING_PAY'" type="info" plain round size="small" @click="doCancel(o.orderNo)">取消订单</el-button>
            <el-button v-if="o.status === 'DELIVERED'" type="danger" round size="small" @click="doConfirm(o.orderNo)">确认收货</el-button>
            <el-button v-if="o.status === 'DELIVERED' && o.logisticsNo" type="primary" plain round size="small" @click="$router.push('/order/' + o.orderNo)">查看物流</el-button>
            <el-button v-if="o.status === 'COMPLETED'" type="warning" plain round size="small" @click="doRefund(o.orderNo)">申请售后</el-button>
            <el-button v-if="o.status === 'COMPLETED'" type="danger" round size="small" @click="$router.push('/product/' + (o.items?.[0]?.productId || ''))">去评价</el-button>
            <el-button plain round size="small" @click="$router.push('/order/' + o.orderNo)">订单详情</el-button>
          </div>
        </div>
      </div>

      <div class="orders-pagination" v-if="total > size">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="size" v-model:current-page="page" @current-change="fetchOrders" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { orderApi } from '@/api/endpoints'
import { ORDER_STATUS_MAP } from '@/constants/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const orders = ref([])
const loading = ref(false)
const activeTab = ref('')
const page = ref(1)
const size = 10
const total = ref(0)
const statusMap = ORDER_STATUS_MAP

const statusTagType = (status) => {
  const map = {
    PENDING_PAY: 'warning',
    PENDING_DELIVER: 'primary',
    DELIVERED: 'success',
    COMPLETED: 'info',
    CANCELLED: 'info',
    REFUNDING: 'danger',
    REFUNDED: 'info',
  }
  return map[status] || 'info'
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await orderApi.getList({ page: page.value, size, status: activeTab.value || undefined })
    orders.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {} finally {
    loading.value = false
  }
}

const doPay = async (o) => {
  try {
    await orderApi.pay({ orderNo: o.orderNo, payMethod: o.payMethod || 'ALIPAY' })
    ElMessage.success('支付成功')
    fetchOrders()
  } catch (e) {}
}

const doCancel = async (no) => {
  try {
    await ElMessageBox.confirm('确定取消该订单？')
    await orderApi.cancel(no)
    ElMessage.success('已取消')
    fetchOrders()
  } catch (e) {}
}

const doConfirm = async (no) => {
  try {
    await ElMessageBox.confirm('确定已收到商品？')
    await orderApi.confirm(no)
    ElMessage.success('已确认收货')
    fetchOrders()
  } catch (e) {}
}

const doRefund = async (no) => {
  try {
    await ElMessageBox.confirm('确定申请退款？')
    await orderApi.refund(no)
    ElMessage.success('退款申请已提交')
    fetchOrders()
  } catch (e) {}
}

onMounted(fetchOrders)
</script>

<style scoped>
.orders-page {
  min-height: 100vh;
  background: #f5f5f7;
}

.orders-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px 15px 40px;
}

.orders-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px;
}

/* ===== Tabs ===== */
.orders-tabs {
  margin-bottom: 20px;
}
.orders-tabs :deep(.el-tabs__header) {
  background: #fff;
  border-radius: 12px;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin: 0 0 20px;
}
.orders-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

/* ===== Order Card ===== */
.order-card {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  border: 1px solid #ebeef5;
  overflow: hidden;
}

/* Header */
.order-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
}
.order-card-meta {
  display: flex;
  align-items: center;
  gap: 20px;
}
.order-card-no {
  font-size: 13px;
  color: #909399;
}
.order-card-time {
  font-size: 13px;
  color: #909399;
}

/* Body */
.order-card-body {
  padding: 12px 20px;
}
.order-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 0;
  cursor: pointer;
  transition: background 0.15s;
}
.order-item:not(:last-child) {
  border-bottom: 1px solid #f5f5f5;
}
.order-item:hover {
  background: #fafafa;
  margin: 0 -20px;
  padding: 12px 20px;
}
.order-item-img {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  flex-shrink: 0;
  background: #fafafa;
}
.order-item-info {
  flex: 1;
  min-width: 0;
}
.order-item-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.order-item-spec {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.order-item-price {
  text-align: right;
  flex-shrink: 0;
}
.oip-amount {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}
.oip-qty {
  font-size: 12px;
  color: #909399;
  margin-left: 4px;
}

/* Footer */
.order-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-top: 1px solid #f0f0f0;
  background: #fafbfc;
}
.order-card-total {
  font-size: 14px;
  color: #606266;
}
.total-price {
  font-size: 18px;
  font-weight: 700;
  color: #f56c6c;
}
.order-card-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

/* Pagination */
.orders-pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* Empty */
.orders-container :deep(.el-empty) {
  padding: 60px 0;
}

@media (max-width: 768px) {
  .orders-container {
    padding: 12px 10px 30px;
  }
  .order-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  .order-card-meta {
    flex-direction: column;
    gap: 4px;
  }
  .order-card-footer {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  .order-card-actions {
    justify-content: flex-start;
  }
  .order-item:hover {
    margin: 0 -12px;
    padding: 12px;
  }
  .order-card-body {
    padding: 8px 12px;
  }
  .order-card-header, .order-card-footer {
    padding: 12px;
  }
}
</style>
