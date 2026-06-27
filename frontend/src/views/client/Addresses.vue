<template>
  <div>
    <h3>收货地址</h3>
    <el-button type="primary" @click="showForm = true;editing=null;resetForm()" style="margin-bottom:15px">新增地址</el-button>

    <div v-loading="loading">
      <el-empty v-if="!addresses.length" description="暂无收货地址" />
      <div v-for="a in addresses" :key="a.id" class="address-card">
        <div class="address-main">
          <div class="address-top">
            <el-tag v-if="a.isDefault" type="danger" size="small" effect="dark">默认</el-tag>
            <span class="address-name">{{ a.receiverName }}</span>
            <span class="address-phone">{{ a.receiverPhone }}</span>
          </div>
          <div class="address-detail">{{ a.province }}{{ a.city }}{{ a.district }} {{ a.detail }}</div>
        </div>
        <div class="address-actions">
          <el-button size="small" @click="editAddr(a)">编辑</el-button>
          <el-button size="small" type="danger" @click="delAddr(a.id)">删除</el-button>
          <el-button size="small" v-if="!a.isDefault" type="warning" @click="setDefault(a.id)">设为默认</el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="showForm" :title="editing ? '编辑地址' : '新增地址'" width="500px">
      <el-form :model="addrForm" label-width="80px">
        <el-form-item label="收货人"><el-input v-model="addrForm.receiverName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="addrForm.receiverPhone" /></el-form-item>
        <el-form-item label="省份"><el-input v-model="addrForm.province" /></el-form-item>
        <el-form-item label="城市"><el-input v-model="addrForm.city" /></el-form-item>
        <el-form-item label="区县"><el-input v-model="addrForm.district" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="addrForm.detail" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForm = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="saveAddr">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { orderApi } from '@/api/endpoints'
import { ElMessage, ElMessageBox } from 'element-plus'

const addresses = ref([])
const showForm = ref(false)
const editing = ref(null)
const loading = ref(false)
const saveLoading = ref(false)
const addrForm = reactive({ receiverName: '', receiverPhone: '', province: '', city: '', district: '', detail: '' })

const resetForm = () => Object.assign(addrForm, { receiverName: '', receiverPhone: '', province: '', city: '', district: '', detail: '' })

const fetch = async () => {
  loading.value = true
  try {
    const res = await orderApi.getAddresses()
    addresses.value = res.data || []
  } catch (e) {} finally {
    loading.value = false
  }
}

const editAddr = (a) => { editing.value = a.id; Object.assign(addrForm, a); showForm.value = true }

const saveAddr = async () => {
  if (!addrForm.receiverName.trim() || !addrForm.receiverPhone.trim() || !addrForm.detail.trim()) {
    ElMessage.warning('请填写完整的地址信息')
    return
  }
  saveLoading.value = true
  try {
    if (editing.value) {
      await orderApi.updateAddress(editing.value, addrForm)
      ElMessage.success('地址更新成功')
    } else {
      await orderApi.addAddress(addrForm)
      ElMessage.success('地址添加成功')
    }
    showForm.value = false
    editing.value = null
    fetch()
  } catch (e) {} finally {
    saveLoading.value = false
  }
}

const delAddr = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该地址？', '确认删除', { type: 'warning' })
    await orderApi.deleteAddress(id)
    ElMessage.success('地址已删除')
    fetch()
  } catch (e) {}
}

const setDefault = async (id) => {
  try {
    await orderApi.setDefaultAddress(id)
    ElMessage.success('已设为默认地址')
    fetch()
  } catch (e) {}
}

onMounted(fetch)
</script>

<style scoped>
.address-card {
  border: 1px solid #ebeef5; padding: 16px; margin-bottom: 12px;
  border-radius: 8px; display: flex; justify-content: space-between;
  align-items: center; transition: all 0.3s;
}
.address-card:hover { border-color: #409eff; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.address-main { flex: 1; }
.address-top { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.address-name { font-weight: bold; color: #303133; }
.address-phone { color: #606266; }
.address-detail { color: #606266; font-size: 14px; }
.address-actions { display: flex; gap: 6px; flex-shrink: 0; }

@media (max-width: 768px) {
  .address-card { flex-direction: column; align-items: flex-start; gap: 10px; }
  .address-actions { width: 100%; }
}
</style>
