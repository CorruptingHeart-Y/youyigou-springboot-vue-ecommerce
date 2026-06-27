<template>
  <div>
    <h3>促销管理</h3>
    <el-button type="primary" @click="showAdd" style="margin-bottom:15px">新增促销</el-button>
    <el-table :data="promotions" border stripe v-loading="loading">
      <el-table-column prop="title" label="标题" min-width="120" show-overflow-tooltip />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{row}">
          <el-tag :type="row.type==='SECKILL'?'danger':row.type==='DISCOUNT'?'warning':'success'" effect="dark">
            {{ row.type==='SECKILL'?'秒杀':row.type==='DISCOUNT'?'折扣':'优惠券' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="discountValue" label="优惠值" width="80" />
      <el-table-column label="时间" min-width="200">
        <template #default="{row}">{{ row.startTime?.substring(0,10) }} ~ {{ row.endTime?.substring(0,10) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="{row}">
          <el-tag :type="row.status===1?'success':'danger'" effect="dark">{{ row.status===1?'启用':'停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{row}">
          <el-button size="small" @click="editRow(row)">编辑</el-button>
          <el-button size="small" :type="row.status===1?'warning':'success'" @click="toggleStatus(row)">{{ row.status===1?'停用':'启用' }}</el-button>
          <el-button size="small" type="danger" @click="doDel(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing?'编辑促销':'新增促销'" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" style="width:100%">
            <el-option label="秒杀" value="SECKILL" />
            <el-option label="折扣" value="DISCOUNT" />
            <el-option label="优惠券" value="COUPON" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="商品ID"><el-input-number v-model="form.productId" :min="0" placeholder="留空为全场" style="width:100%" /></el-form-item>
        <el-form-item label="优惠值"><el-input-number v-model="form.discountValue" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="最低消费" v-if="form.type==='COUPON'"><el-input-number v-model="form.minAmount" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="秒杀库存" v-if="form.type==='SECKILL'"><el-input-number v-model="form.seckillStock" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="开始时间"><el-input v-model="form.startTime" placeholder="yyyy-MM-dd HH:mm:ss" /></el-form-item>
        <el-form-item label="结束时间"><el-input v-model="form.endTime" placeholder="yyyy-MM-dd HH:mm:ss" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '@/api/endpoints'
import { ElMessage, ElMessageBox } from 'element-plus'

const promotions = ref([])
const dialogVisible = ref(false)
const editing = ref(false)
const loading = ref(false)
const saveLoading = ref(false)
const form = reactive({ title: '', type: 'DISCOUNT', description: '', productId: null, discountValue: 0, minAmount: 0, seckillStock: 100, startTime: '', endTime: '', status: 1 })

const fetch = async () => {
  loading.value = true
  try {
    const res = await adminApi.getPromotions()
    promotions.value = res.data || []
  } catch (e) {} finally {
    loading.value = false
  }
}

const showAdd = () => {
  editing.value = false
  Object.assign(form, { title: '', type: 'DISCOUNT', description: '', productId: null, discountValue: 0, minAmount: 0, seckillStock: 100, startTime: '', endTime: '', status: 1 })
  dialogVisible.value = true
}
const editRow = (row) => { editing.value = true; Object.assign(form, row); dialogVisible.value = true }

const save = async () => {
  if (!form.title.trim()) { ElMessage.warning('请输入促销标题'); return }
  saveLoading.value = true
  try {
    if (editing.value) { await adminApi.updatePromotion(form.id, form) }
    else { await adminApi.addPromotion(form) }
    dialogVisible.value = false
    ElMessage.success(editing.value ? '促销更新成功' : '促销创建成功')
    fetch()
  } catch (e) {} finally {
    saveLoading.value = false
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(`确定${action}促销「${row.title}」？`, '确认操作', { type: 'warning' })
    await adminApi.togglePromotion(row.id, newStatus)
    ElMessage.success(`已${action}`)
    fetch()
  } catch (e) {}
}

const doDel = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该促销活动？', '确认删除', { type: 'warning' })
    await adminApi.deletePromotion(id)
    ElMessage.success('促销已删除')
    fetch()
  } catch (e) {}
}

onMounted(fetch)
</script>
