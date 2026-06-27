<template>
  <div v-loading="loading">
    <h3>订单详情</h3>
    <template v-if="order">
      <el-descriptions border :column="isMobile ? 1 : 2">
        <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="statusType">{{ statusMap[order.status] }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="用户">{{ order.username }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ order.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="实付金额"><span style="color:#f56c6c;font-weight:bold">¥{{ order.payAmount }}</span></el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ order.payMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ order.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ order.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ order.addressDetail }}</el-descriptions-item>
        <el-descriptions-item label="物流单号">{{ order.logisticsNo || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="物流公司">{{ order.logisticsCompany || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ order.remark || '无' }}</el-descriptions-item>
      </el-descriptions>

      <h4 style="margin-top:20px">商品列表</h4>
      <el-table :data="order.items || []" border stripe>
        <el-table-column label="商品" prop="productName" />
        <el-table-column label="单价" prop="price" width="100" align="right" />
        <el-table-column label="数量" prop="quantity" width="80" align="center" />
        <el-table-column label="小计" prop="subtotal" width="100" align="right" />
      </el-table>

      <div style="margin-top:20px;display:flex;gap:10px;flex-wrap:wrap" v-if="order.status !== 'CANCELLED' && order.status !== 'COMPLETED' && order.status !== 'REFUNDED'">
        <el-button v-if="order.status==='PENDING_DELIVER'" type="primary" :loading="deliverLoading" @click="doDeliver">发货</el-button>
        <el-button v-if="order.status==='REFUNDING'" type="warning" :loading="refundLoading" @click="doRefund(true)">同意退款</el-button>
        <el-button type="danger" :loading="cancelLoading" @click="doCancel">取消订单</el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { adminApi } from '@/api/endpoints'
import { ORDER_STATUS_MAP, ORDER_STATUS_TYPE_MAP } from '@/constants/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const order = ref(null)
const loading = ref(false)
const deliverLoading = ref(false)
const refundLoading = ref(false)
const cancelLoading = ref(false)
const isMobile = computed(() => window.innerWidth < 768)

const statusMap = ORDER_STATUS_MAP
const statusType = computed(() => ORDER_STATUS_TYPE_MAP[order.value?.status] || '')

const fetch = async () => {
  loading.value = true
  try {
    const res = await adminApi.getOrderDetail(route.params.orderNo)
    order.value = res.data
  } catch (e) {} finally {
    loading.value = false
  }
}

const doDeliver = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入物流单号', '发货', { inputPattern: /\S+/, inputErrorMessage: '请输入物流单号' })
    deliverLoading.value = true
    await adminApi.deliverOrder(order.value.orderNo, { logisticsNo: value, logisticsCompany: '顺丰速运' })
    ElMessage.success('发货成功')
    fetch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('发货失败')
  } finally {
    deliverLoading.value = false
  }
}

const doRefund = async (approve) => {
  try {
    await ElMessageBox.confirm('确定同意退款？', '确认退款', { type: 'warning' })
    refundLoading.value = true
    await adminApi.refundOrder(order.value.orderNo, approve)
    ElMessage.success('退款已处理')
    fetch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('退款处理失败')
  } finally {
    refundLoading.value = false
  }
}

const doCancel = async () => {
  try {
    await ElMessageBox.confirm('确定取消该订单？', '确认取消', { type: 'warning' })
    cancelLoading.value = true
    await adminApi.cancelOrder(order.value.orderNo)
    ElMessage.success('订单已取消')
    fetch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('取消失败')
  } finally {
    cancelLoading.value = false
  }
}

onMounted(fetch)
</script>
