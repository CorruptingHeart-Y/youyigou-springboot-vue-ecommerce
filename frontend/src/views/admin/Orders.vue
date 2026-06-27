<template>
  <div>
    <h3>订单管理</h3>
    <div style="display:flex;gap:10px;margin-bottom:15px;flex-wrap:wrap">
      <el-input v-model="searchNo" placeholder="订单号" style="width:200px" @keyup.enter="fetch" />
      <el-select v-model="searchStatus" placeholder="状态" clearable @change="fetch" style="width:150px">
        <el-option v-for="(v,k) in statusMap" :key="k" :label="v" :value="k" />
      </el-select>
      <el-button @click="fetch">搜索</el-button>
    </div>
    <el-table :data="orders" border stripe v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="username" label="用户" width="120" />
      <el-table-column prop="totalAmount" label="金额" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{row}"><el-tag>{{ statusMap[row.status] }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="160">
        <template #default="{row}">{{ row.createTime?.substring(0,19) }}</template>
      </el-table-column>
      <el-table-column label="操作" min-width="200">
        <template #default="{row}">
          <el-button size="small" @click="$router.push('/admin/order/'+row.orderNo)">详情</el-button>
          <el-button v-if="row.status==='PENDING_DELIVER'" size="small" type="primary" @click="showDeliver(row)">发货</el-button>
          <el-button v-if="row.status==='REFUNDING'" size="small" type="warning" @click="doRefund(row.orderNo,true)">同意退款</el-button>
          <el-button v-if="row.status==='PENDING_PAY' || row.status==='PENDING_DELIVER'" size="small" type="danger" @click="doCancel(row.orderNo)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-if="total > pageSize"
      layout="total, prev, pager, next"
      :total="total"
      :page-size="pageSize"
      v-model:current-page="page"
      @current-change="fetch"
      style="margin-top:15px;justify-content:center"
    />

    <el-dialog v-model="deliverVisible" title="发货" width="400px">
      <el-form :model="deliverForm">
        <el-form-item label="物流单号"><el-input v-model="deliverForm.logisticsNo" /></el-form-item>
        <el-form-item label="物流公司"><el-input v-model="deliverForm.logisticsCompany" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deliverVisible=false">取消</el-button>
        <el-button type="primary" :loading="deliverLoading" @click="confirmDeliver">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '@/api/endpoints'
import { ORDER_STATUS_MAP } from '@/constants/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const orders = ref([])
const searchNo = ref('')
const searchStatus = ref('')
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)
const deliverVisible = ref(false)
const deliverNo = ref('')
const deliverLoading = ref(false)
const deliverForm = reactive({ logisticsNo: '', logisticsCompany: '' })
const statusMap = ORDER_STATUS_MAP

const fetch = async () => {
  loading.value = true
  try {
    const res = await adminApi.getOrders({ page: page.value, size: pageSize, orderNo: searchNo.value || undefined, status: searchStatus.value || undefined })
    orders.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {} finally {
    loading.value = false
  }
}

const showDeliver = (row) => { deliverNo.value = row.orderNo; deliverForm.logisticsNo = ''; deliverForm.logisticsCompany = '顺丰速运'; deliverVisible.value = true }
const confirmDeliver = async () => {
  deliverLoading.value = true
  try { await adminApi.deliverOrder(deliverNo.value, deliverForm); deliverVisible.value = false; ElMessage.success('发货成功'); fetch() } catch (e) {} finally { deliverLoading.value = false }
}
const doCancel = async (no) => {
  try { await ElMessageBox.confirm('确定取消该订单？', '确认取消', { type: 'warning' }); await adminApi.cancelOrder(no); ElMessage.success('订单已取消'); fetch() } catch (e) {}
}
const doRefund = async (no, approve) => {
  try { await ElMessageBox.confirm('确定同意退款？', '确认退款', { type: 'warning' }); await adminApi.refundOrder(no, approve); ElMessage.success('退款已处理'); fetch() } catch (e) {}
}

onMounted(fetch)
</script>
