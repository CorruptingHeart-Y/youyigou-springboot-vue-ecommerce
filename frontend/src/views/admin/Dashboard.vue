<template>
  <div>
    <h3>数据看板</h3>
    <div v-loading="loading" style="min-height:200px">
      <el-row :gutter="15">
        <el-col :xs="12" :sm="6" v-for="stat in statCards" :key="stat.label">
          <el-card class="stat-card" :style="{ borderTop: `3px solid ${stat.color}` }">
            <div class="stat-icon" :style="{ color: stat.color }">{{ stat.icon }}</div>
            <div class="stat-info">
              <div class="stat-num" :style="{ color: stat.color }">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="15" style="margin-top:20px">
        <el-col :xs="24" :md="12">
          <el-card>
            <template #header><span style="font-weight:bold">订单状态统计</span></template>
            <div ref="pieChart" style="height:320px" />
          </el-card>
        </el-col>
        <el-col :xs="24" :md="12">
          <el-card>
            <template #header><span style="font-weight:bold">热销商品排行</span></template>
            <el-table :data="data.hotProducts" size="small" stripe>
              <el-table-column type="index" label="#" width="40" />
              <el-table-column prop="productName" label="商品" show-overflow-tooltip />
              <el-table-column prop="salesCount" label="销量" width="80" align="center" />
              <el-table-column prop="salesAmount" label="销售额" width="100" align="right">
                <template #default="{row}">¥{{ row.salesAmount?.toFixed(2) }}</template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!data.hotProducts?.length" description="暂无数据" :image-size="60" />
          </el-card>
        </el-col>
      </el-row>

      <el-row style="margin-top:20px">
        <el-col :span="24">
          <el-card>
            <template #header>
              <div style="display:flex;justify-content:space-between;align-items:center">
                <span style="font-weight:bold">销售趋势（近14天）</span>
                <el-button size="small" @click="exportDashboard" :loading="exportLoading">导出报表</el-button>
              </div>
            </template>
            <div ref="trendChart" style="height:320px" />
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { adminApi } from '@/api/endpoints'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts/core'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  ToolboxComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  LineChart,
  BarChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  ToolboxComponent,
  CanvasRenderer
])

const data = ref({ totalUsers: 0, totalOrders: 0, totalSales: 0, todayOrders: 0, todaySales: 0, hotProducts: [], saleTrend: [], orderStatusCount: {} })
const pieChart = ref(null)
const trendChart = ref(null)
const loading = ref(false)
const exportLoading = ref(false)
let pieInstance = null
let trendInstance = null

const statCards = computed(() => [
  { icon: '👥', label: '总用户数', value: data.value.totalUsers, color: '#409eff' },
  { icon: '📦', label: '总订单数', value: data.value.totalOrders, color: '#67c23a' },
  { icon: '💰', label: '总销售额', value: `¥${data.value.totalSales?.toFixed(2)}`, color: '#e6a23c' },
  { icon: '🛒', label: '今日订单', value: data.value.todayOrders, color: '#f56c6c' }
])

const statusNameMap = {
  PENDING_PAY: '待支付', PENDING_DELIVER: '待发货', DELIVERED: '待收货',
  COMPLETED: '已完成', CANCELLED: '已取消', REFUNDING: '退款中', REFUNDED: '已退款'
}

const initCharts = () => {
  if (pieChart.value) {
    pieInstance = echarts.init(pieChart.value)
    const d = data.value.orderStatusCount || {}
    pieInstance.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, type: 'scroll' },
      series: [{
        type: 'pie', radius: ['35%', '65%'], center: ['50%', '45%'],
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { formatter: '{b}\n{d}%' },
        data: Object.entries(d).map(([k, v]) => ({ name: statusNameMap[k] || k, value: v }))
      }]
    })
  }
  if (trendChart.value) {
    trendInstance = echarts.init(trendChart.value)
    const trends = data.value.saleTrend || []
    trendInstance.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
      grid: { left: 60, right: 30, top: 40, bottom: 40 },
      toolbox: {
        show: true,
        orient: 'horizontal',
        top: 5,
        right: 10,
        feature: {
          magicType: {
            show: true,
            type: ['line', 'bar'],
            title: { line: '切换为折线图', bar: '切换为柱状图' }
          }
        }
      },
      xAxis: { type: 'category', data: trends.map(t => t.date?.substring(5) || t.date), axisLabel: { fontSize: 11 } },
      yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
      series: [{
        name: '销售额', type: 'line', smooth: true,
        data: trends.map(t => t.amount),
        itemStyle: { color: '#409eff' },
        lineStyle: { color: '#409eff', width: 2.5 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.4)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.02)' }
          ])
        }
      }]
    })
  }
}

const handleResize = () => {
  pieInstance?.resize()
  trendInstance?.resize()
}

const exportDashboard = async () => {
  exportLoading.value = true
  try {
    const blob = await adminApi.exportDashboard()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `dashboard_${new Date().toISOString().slice(0, 10)}.xlsx`
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('报表导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await adminApi.getDashboard()
    data.value = res.data
  } catch (e) {} finally {
    loading.value = false
  }
  await nextTick()
  initCharts()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  pieInstance?.dispose()
  trendInstance?.dispose()
})
</script>

<style scoped>
.stat-card {
  display: flex; align-items: center; padding: 20px; margin-bottom: 15px;
  border-radius: 8px; transition: all 0.3s;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.1); }
.stat-icon { font-size: 36px; margin-right: 16px; }
.stat-info { flex: 1; }
.stat-num { font-size: 26px; font-weight: bold; line-height: 1.2; }
.stat-label { color: #909399; font-size: 13px; margin-top: 4px; }

@media (max-width: 768px) {
  .stat-card { padding: 14px; }
  .stat-icon { font-size: 28px; margin-right: 10px; }
  .stat-num { font-size: 20px; }
}
</style>
