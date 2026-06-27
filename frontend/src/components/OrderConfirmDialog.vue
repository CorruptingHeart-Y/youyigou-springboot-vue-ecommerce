<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    width="660px"
    :close-on-click-modal="false"
    @open="onOpen"
    custom-class="checkout-dialog"
  >
    <template #header>
      <div class="chk-header">
        <span class="chk-header-title">确认订单</span>
        <span class="chk-header-sub">请核对以下信息后提交</span>
      </div>
    </template>

    <!-- ═══ 商品明细 ═══ -->
    <div class="chk-section">
      <div class="chk-section-label">商品明细</div>
      <div class="chk-items-box">
        <div v-for="(item, i) in displayItems" :key="i" class="chk-item-row">
          <img :src="item.image || '/uploads/products/default.svg'" class="chk-item-img" />
          <div class="chk-item-info">
            <div class="chk-item-name">{{ item.name }}</div>
            <div class="chk-item-spec" v-if="item.spec && item.spec !== '默认'">{{ item.spec }}</div>
          </div>
          <div class="chk-item-right">
            <span class="chk-item-price">¥{{ item.price }}</span>
            <span class="chk-item-mul">×{{ item.quantity }}</span>
            <span class="chk-item-subtotal">¥{{ item.subtotal }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ═══ 收货地址 ═══ -->
    <div class="chk-section">
      <div class="chk-section-label">收货地址</div>

      <!-- Selected address card -->
      <div class="addr-card" @click="showAddressDialog = true" v-if="selectedAddress">
        <div class="addr-card-body">
          <div class="addr-card-line1">
            <span class="addr-card-name">{{ selectedAddress.receiverName }}</span>
            <span class="addr-card-phone">{{ selectedAddress.receiverPhone }}</span>
            <el-tag v-if="selectedAddress.isDefault" size="small" type="danger" effect="plain">默认</el-tag>
          </div>
          <div class="addr-card-line2">
            {{ selectedAddress.province }}{{ selectedAddress.city }}{{ selectedAddress.district }} {{ selectedAddress.detail }}
          </div>
        </div>
        <div class="addr-card-arrow">
          <span class="addr-switch-text">修改</span>
          <span class="addr-switch-icon">›</span>
        </div>
      </div>

      <!-- Empty state -->
      <div class="addr-empty" v-else @click="showAddressForm = true">
        <span class="addr-empty-icon">+</span>
        <span>添加收货地址</span>
      </div>

      <!-- Address selection dialog -->
      <el-dialog v-model="showAddressDialog" title="选择收货地址" width="520px" append-to-body>
        <div class="addr-select-list">
          <div
            v-for="a in addresses"
            :key="a.id"
            class="addr-select-item"
            :class="{ active: selectedAddressId === a.id }"
            @click="selectedAddressId = a.id; showAddressDialog = false"
          >
            <div class="addr-select-body">
              <div class="addr-select-line1">
                <span class="addr-select-name">{{ a.receiverName }}</span>
                <span class="addr-select-phone">{{ a.receiverPhone }}</span>
                <el-tag v-if="a.isDefault" size="small" type="danger" effect="plain">默认</el-tag>
              </div>
              <div class="addr-select-line2">
                {{ a.province }}{{ a.city }}{{ a.district }} {{ a.detail }}
              </div>
            </div>
            <span class="addr-select-check" v-if="selectedAddressId === a.id">✓</span>
          </div>
        </div>
        <div class="addr-select-add" @click="showAddressDialog = false; showAddressForm = true">
          <span>+ 新增地址</span>
        </div>
        <template #footer>
          <el-button @click="showAddressDialog = false">取消</el-button>
        </template>
      </el-dialog>
    </div>

    <!-- ═══ 支付方式 ═══ -->
    <div class="chk-section">
      <div class="chk-section-label">支付方式</div>
      <div class="pay-cards">
        <div
          v-for="opt in payOptions"
          :key="opt.value"
          class="pay-card"
          :class="{ active: payMethod === opt.value }"
          @click="payMethod = opt.value"
        >
          <!-- Alipay SVG -->
          <svg v-if="opt.value === 'ALIPAY'" class="pay-card-icon-svg" width="28" height="28" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <rect width="24" height="24" rx="5" fill="#1677FF"/>
            <text x="12" y="16.5" text-anchor="middle" font-size="12" font-weight="bold" fill="white" font-family="Arial, 'Microsoft YaHei', sans-serif">支</text>
          </svg>
          <!-- WeChat Pay SVG -->
          <svg v-else-if="opt.value === 'WECHAT'" class="pay-card-icon-svg" width="28" height="28" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <rect width="24" height="24" rx="5" fill="#07C160"/>
            <ellipse cx="8" cy="10" rx="3.5" ry="3" fill="white"/>
            <ellipse cx="15.5" cy="11.5" rx="4" ry="3.5" fill="white" opacity="0.75"/>
          </svg>
          <!-- Balance emoji fallback -->
          <span v-else class="pay-card-icon-emoji">{{ opt.icon }}</span>
          <div class="pay-card-body">
            <div class="pay-card-name">{{ opt.label }}</div>
            <div class="pay-card-desc">{{ opt.desc }}</div>
          </div>
          <span class="pay-card-check" v-if="payMethod === opt.value">✓</span>
        </div>
      </div>
    </div>

    <!-- ═══ 优惠券 (normal only) ═══ -->
    <div class="chk-section" v-if="!isSeckill">
      <div class="chk-section-label">优惠券</div>
      <div class="coupon-scroll" v-if="coupons.length">
        <div
          v-for="c in coupons"
          :key="c.id"
          class="coupon-chip"
          :class="{ active: selectedCoupon?.id === c.id }"
          @click="selectCoupon(c)"
        >
          <span class="coupon-chip-left">¥{{ c.discountValue }}</span>
          <span class="coupon-chip-right">满{{ c.minAmount || 0 }}可用</span>
          <span class="coupon-chip-check" v-if="selectedCoupon?.id === c.id">✓</span>
        </div>
      </div>
      <div class="chk-empty-hint" v-else>暂无可用优惠券</div>
    </div>

    <!-- ═══ 备注 ═══ -->
    <div class="chk-section">
      <div class="chk-section-label">备注</div>
      <el-input v-model="remark" placeholder="选填：如有特殊需求请在此说明" :rows="2" type="textarea" class="chk-remark" />
    </div>

    <!-- ═══ Footer: total + submit ═══ -->
    <div class="chk-footer">
      <div class="chk-footer-left">
        <span class="chk-footer-label">应付金额</span>
        <span class="chk-footer-price">¥{{ finalAmount.toFixed(2) }}</span>
        <span class="chk-footer-discount" v-if="couponDiscount > 0">已优惠 ¥{{ couponDiscount.toFixed(2) }}</span>
      </div>
      <el-button type="danger" size="large" round @click="doSubmit" :loading="submitting" class="chk-submit-btn">
        提交订单
      </el-button>
    </div>

    <!-- ═══ 新增地址子对话框 ═══ -->
    <el-dialog v-model="showAddressForm" title="新增地址" width="480px" append-to-body>
      <el-form :model="addressForm" label-width="80px">
        <el-form-item label="收货人"><el-input v-model="addressForm.receiverName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="addressForm.receiverPhone" /></el-form-item>
        <el-form-item label="省份"><el-input v-model="addressForm.province" /></el-form-item>
        <el-form-item label="城市"><el-input v-model="addressForm.city" /></el-form-item>
        <el-form-item label="区县"><el-input v-model="addressForm.district" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="addressForm.detail" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddressForm = false">取消</el-button>
        <el-button type="primary" :loading="addrSaving" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { orderApi, promotionApi } from '@/api/endpoints'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: { type: Boolean, default: false },
  isSeckill: { type: Boolean, default: false },
  items: { type: Array, default: () => [] },
  totalAmount: { type: Number, default: 0 }
})

const emit = defineEmits(['update:visible', 'submit'])

const addresses = ref([])
const coupons = ref([])
const selectedAddressId = ref('')
const payMethod = ref('ALIPAY')
const remark = ref('')
const selectedCoupon = ref(null)
const couponDiscount = ref(0)
const submitting = ref(false)
const showAddressForm = ref(false)
const addrSaving = ref(false)
const showAddressDialog = ref(false)

const addressForm = reactive({ receiverName: '', receiverPhone: '', province: '', city: '', district: '', detail: '' })

const displayItems = computed(() => props.items)
const finalAmount = computed(() => Math.max(0, props.totalAmount - couponDiscount.value))

const selectedAddress = computed(() => {
  if (!selectedAddressId.value) return null
  return addresses.value.find(a => a.id === selectedAddressId.value) || null
})

const payOptions = [
  { value: 'ALIPAY', label: '支付宝', desc: '推荐安装支付宝用户使用' },
  { value: 'WECHAT', label: '微信支付', desc: '微信安全支付' },
  { value: 'BALANCE', label: '余额支付', desc: '使用账户余额支付', icon: '💰' }
]

const onOpen = async () => {
  selectedAddressId.value = ''
  payMethod.value = 'ALIPAY'
  remark.value = ''
  selectedCoupon.value = null
  couponDiscount.value = 0
  showAddressDialog.value = false

  try {
    const [addrRes, promRes] = await Promise.all([
      orderApi.getAddresses(),
      props.isSeckill ? Promise.resolve({ data: [] }) : promotionApi.getActive()
    ])
    addresses.value = addrRes.data || []
    if (addresses.value.length) selectedAddressId.value = addresses.value[0].id
    coupons.value = (promRes.data || []).filter(p => p.type === 'COUPON')
  } catch (e) {}
}

const selectCoupon = (c) => {
  if (selectedCoupon.value?.id === c.id) {
    selectedCoupon.value = null
    couponDiscount.value = 0
    return
  }
  if (props.totalAmount < (c.minAmount || 0)) {
    ElMessage.warning(`需满¥${c.minAmount}才可使用该优惠券`)
    return
  }
  selectedCoupon.value = c
  couponDiscount.value = c.discountValue
}

const saveAddress = async () => {
  if (!addressForm.receiverName.trim() || !addressForm.detail.trim()) {
    ElMessage.warning('请填写完整的地址信息')
    return
  }
  addrSaving.value = true
  try {
    await orderApi.addAddress(addressForm)
    const res = await orderApi.getAddresses()
    addresses.value = res.data || []
    // Auto-select the newly added address (last in the list)
    if (addresses.value.length) {
      const newAddr = addresses.value[addresses.value.length - 1]
      selectedAddressId.value = newAddr.id
    }
    showAddressForm.value = false
    showAddressDialog.value = false
    ElMessage.success('地址添加成功')
  } catch (e) {} finally {
    addrSaving.value = false
  }
}

const doSubmit = () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  const payload = {
    addressId: selectedAddressId.value,
    payMethod: payMethod.value,
    remark: remark.value
  }
  if (selectedCoupon.value) {
    payload.couponId = selectedCoupon.value.id
    payload.couponDiscount = selectedCoupon.value.discountValue
  }
  emit('submit', payload)
}
</script>

<style scoped>
/* ═══════════════════════════════════════════
   Dialog shell overrides
   ═══════════════════════════════════════════ */
:deep(.checkout-dialog) {
  border-radius: 16px !important;
  overflow: hidden;
}
:deep(.checkout-dialog .el-dialog__header) {
  margin: 0;
  padding: 20px 28px 0;
}
:deep(.checkout-dialog .el-dialog__body) {
  padding: 8px 28px 20px;
}
:deep(.checkout-dialog .el-dialog__footer) {
  display: none; /* we use custom footer */
}

/* ── Header ── */
.chk-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.chk-header-title {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}
.chk-header-sub {
  font-size: 13px;
  color: #c0c4cc;
}

/* ═══════════════════════════════════════════
   Sections
   ═══════════════════════════════════════════ */
.chk-section {
  margin-top: 20px;
}
.chk-section-label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.chk-section-label::before {
  content: '';
  display: inline-block;
  width: 3px;
  height: 14px;
  background: #e74c3c;
  border-radius: 2px;
}

/* ═══════════════════════════════════════════
   Order items box
   ═══════════════════════════════════════════ */
.chk-items-box {
  background: #fafafa;
  border-radius: 10px;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.chk-item-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 8px 0;
}
.chk-item-row + .chk-item-row {
  border-top: 1px dashed #e4e7ed;
}

.chk-item-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
  border: 1px solid #f0f0f0;
}

.chk-item-info {
  flex: 1;
  min-width: 0;
}
.chk-item-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.chk-item-spec {
  font-size: 12px;
  color: #909399;
  margin-top: 3px;
  background: #f0f2f5;
  display: inline-block;
  padding: 1px 8px;
  border-radius: 4px;
}

.chk-item-right {
  display: flex;
  align-items: baseline;
  gap: 6px;
  flex-shrink: 0;
}
.chk-item-price {
  font-size: 14px;
  color: #606266;
}
.chk-item-mul {
  font-size: 12px;
  color: #c0c4cc;
}
.chk-item-subtotal {
  font-size: 15px;
  color: #f56c6c;
  font-weight: 700;
  min-width: 60px;
  text-align: right;
}

/* ═══════════════════════════════════════════
   Address card
   ═══════════════════════════════════════════ */
.addr-card {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border: 2px solid transparent;
  border-radius: 10px;
  padding: 16px 18px;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.addr-card:hover {
  border-color: #c6d0e0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.addr-card-body {
  flex: 1;
  min-width: 0;
}
.addr-card-line1 {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.addr-card-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.addr-card-phone {
  font-size: 14px;
  color: #606266;
}
.addr-card-line2 {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}

.addr-card-arrow {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  margin-left: 10px;
}
.addr-switch-text {
  font-size: 13px;
  color: #409eff;
}
.addr-switch-icon {
  font-size: 18px;
  color: #409eff;
  font-weight: 600;
}

/* Empty address */
.addr-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 64px;
  background: #f5f7fa;
  border: 2px dashed #dcdfe6;
  border-radius: 10px;
  cursor: pointer;
  color: #909399;
  font-size: 14px;
  transition: border-color 0.2s;
}
.addr-empty:hover {
  border-color: #409eff;
  color: #409eff;
}
.addr-empty-icon {
  font-size: 20px;
  font-weight: 300;
}

/* Address select dialog */
.addr-select-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.addr-select-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  cursor: pointer;
  border: 2px solid #e4e7ed;
  border-radius: 10px;
  transition: all 0.15s;
}
.addr-select-item:hover {
  border-color: #c6d0e0;
  background: #f5f7fa;
}
.addr-select-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}
.addr-select-body {
  flex: 1;
  min-width: 0;
}
.addr-select-line1 {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}
.addr-select-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.addr-select-phone {
  font-size: 14px;
  color: #606266;
}
.addr-select-line2 {
  font-size: 13px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.addr-select-check {
  color: #409eff;
  font-weight: 700;
  font-size: 18px;
  margin-left: 12px;
  flex-shrink: 0;
}
.addr-select-add {
  margin-top: 12px;
  padding: 12px;
  cursor: pointer;
  color: #409eff;
  font-size: 14px;
  font-weight: 500;
  text-align: center;
  border: 2px dashed #dcdfe6;
  border-radius: 10px;
  transition: all 0.15s;
}
.addr-select-add:hover {
  border-color: #409eff;
  background: #ecf5ff;
}

/* ═══════════════════════════════════════════
   Payment cards
   ═══════════════════════════════════════════ */
.pay-cards {
  display: flex;
  gap: 12px;
}
.pay-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border: 2px solid #e4e7ed;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  background: #fff;
}
.pay-card:hover {
  border-color: #c6d0e0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.pay-card.active {
  border-color: #409eff;
  background: #ecf5ff;
  box-shadow: 0 2px 12px rgba(64,158,255,0.12);
}

.pay-card-icon-svg {
  flex-shrink: 0;
  display: block;
}
.pay-card-icon-emoji {
  font-size: 24px;
  flex-shrink: 0;
}

.pay-card-body {
  flex: 1;
}
.pay-card-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 2px;
}
.pay-card.active .pay-card-name {
  color: #1a1a2e;
}
.pay-card-desc {
  font-size: 11px;
  color: #c0c4cc;
}
.pay-card.active .pay-card-desc {
  color: #909399;
}

.pay-card-check {
  position: absolute;
  top: 8px;
  right: 10px;
  width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  background: #409eff;
  border-radius: 50%;
}

/* ═══════════════════════════════════════════
   Coupon chips
   ═══════════════════════════════════════════ */
.coupon-scroll {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.coupon-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border: 2px solid #e6e6e6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  background: #fff;
}
.coupon-chip:hover {
  border-color: #f56c6c;
}
.coupon-chip.active {
  border-color: #f56c6c;
  background: #fff5f5;
}
.coupon-chip-left {
  font-size: 16px;
  font-weight: 700;
  color: #f56c6c;
}
.coupon-chip-right {
  font-size: 12px;
  color: #909399;
}
.coupon-chip-check {
  color: #f56c6c;
  font-weight: 700;
  font-size: 13px;
}
.chk-empty-hint {
  color: #c0c4cc;
  font-size: 13px;
}

/* ═══════════════════════════════════════════
   Remark
   ═══════════════════════════════════════════ */
.chk-remark {
  --el-input-border-radius: 8px;
}

/* ═══════════════════════════════════════════
   Footer
   ═══════════════════════════════════════════ */
.chk-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24px;
  padding: 16px 0 4px;
  border-top: 2px solid #f0f2f5;
}

.chk-footer-left {
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.chk-footer-label {
  font-size: 14px;
  color: #606266;
}
.chk-footer-price {
  font-size: 28px;
  font-weight: 800;
  color: #e74c3c;
  letter-spacing: -1px;
}
.chk-footer-discount {
  font-size: 12px;
  color: #e6a23c;
  background: #fef6e6;
  padding: 2px 10px;
  border-radius: 12px;
}

.chk-submit-btn {
  min-width: 140px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
}

/* ═══════════════════════════════════════════
   Responsive
   ═══════════════════════════════════════════ */
@media (max-width: 600px) {
  .pay-cards {
    flex-direction: column;
    gap: 8px;
  }
  .chk-footer {
    flex-direction: column;
    gap: 14px;
    align-items: stretch;
  }
  .chk-footer-left {
    justify-content: center;
  }
  .chk-submit-btn {
    width: 100%;
  }
  .chk-footer-price {
    font-size: 24px;
  }
}
</style>
