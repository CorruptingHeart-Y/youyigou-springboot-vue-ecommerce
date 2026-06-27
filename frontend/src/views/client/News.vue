<template>
  <div class="news-page">
    <div class="news-hero">
      <h1 class="news-hero-title">平台快讯</h1>
      <p class="news-hero-sub">掌握最新电商动态与平台公告</p>
    </div>
    <div class="news-card" v-loading="loading">
      <el-timeline v-if="newsList.length">
        <el-timeline-item
          v-for="(item, i) in newsList"
          :key="item.id"
          :timestamp="item.date"
          :color="item.color"
          placement="top"
          size="large"
        >
          <h3 class="tl-title">{{ item.title }}</h3>
          <p class="tl-content">{{ item.content }}</p>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else-if="!loading" description="暂无快讯" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/endpoints'

const COLORS = ['#e74c3c', '#f39c12', '#409eff', '#67c23a', '#909399', '#8b5cf6']

const FALLBACK_NEWS = [
  {
    id: 'fallback-1', date: '2026-06-01',
    title: '618年中大促狂欢即将开启',
    content: '一年一度的618年中大促将于6月18日正式开启，全场好物低至5折，更有百万优惠券等你来抢！本次大促涵盖手机数码、家电、服饰、食品等全品类，满减、秒杀、拼团多重优惠叠加，敬请期待！',
    color: '#e74c3c',
  },
  {
    id: 'fallback-2', date: '2026-05-28',
    title: '【规则更新】关于生鲜类目售后时效的通知',
    content: '为保障消费者权益，自2026年6月1日起，生鲜类商品售后申请时效调整为签收后24小时内。如遇商品质量问题，请及时拍照取证并联系客服处理。超时将无法受理，敬请谅解。',
    color: '#f39c12',
  },
  {
    id: 'fallback-3', date: '2026-05-20',
    title: '优选购PLUS会员权益升级说明',
    content: 'PLUS会员权益全面升级！新增专属客服通道、每月8张运费券、品牌折扣专享价等超值权益。老会员自动升级，新用户开通即享首月0.1元体验价。详情请前往个人中心查看。',
    color: '#409eff',
  },
  {
    id: 'fallback-4', date: '2026-05-15',
    title: '物流时效保障计划上线',
    content: '平台联合顺丰、京东物流推出"极速达"服务，覆盖全国300+城市。承诺当日达、次日达超时赔付，每单最高赔付50元优惠券。让你的每一次购物都快人一步！',
    color: '#67c23a',
  },
  {
    id: 'fallback-5', date: '2026-05-08',
    title: '关于严厉打击虚假交易的公告',
    content: '为维护平台公平有序的交易环境，我们将持续严厉打击刷单炒信、虚假评价等违规行为。一经查实，将予以商品下架、店铺扣分乃至封禁处理。欢迎广大用户积极举报。',
    color: '#909399',
  },
  {
    id: 'fallback-6', date: '2026-04-25',
    title: 'App 5.0 全新版本发布',
    content: '优选购App 5.0版本正式上线！全新首页布局、智能推荐算法升级、AR试妆功能、直播购物体验优化。iOS与Android各大应用商店同步更新，建议尽快升级体验全新功能。',
    color: '#8b5cf6',
  },
]

const newsList = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await adminApi.getAnnouncements()
    const apiData = (res.data || [])
      .filter(a => a.status === 1)
      .sort((a, b) => (b.priority || 0) - (a.priority || 0))
      .map((a, i) => ({
        id: a.id,
        date: a.createTime?.substring(0, 10) || '',
        title: a.title,
        content: a.content,
        color: COLORS[i % COLORS.length],
      }))
    newsList.value = [...FALLBACK_NEWS, ...apiData].sort((a, b) => b.date.localeCompare(a.date))
  } catch (e) {
    newsList.value = [...FALLBACK_NEWS]
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.news-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 15px;
}
.news-hero {
  text-align: center;
  padding: 40px 0 32px;
}
.news-hero-title {
  font-size: 28px;
  font-weight: 700;
  color: #222;
  margin: 0 0 8px;
}
.news-hero-sub {
  font-size: 14px;
  color: #999;
  margin: 0;
}
.news-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px 40px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.tl-title {
  font-size: 16px;
  font-weight: 600;
  color: #222;
  margin: 0 0 8px;
}
.tl-content {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  margin: 0 0 10px;
}
:deep(.el-timeline-item__node) {
  border-radius: 50%;
  box-shadow: 0 0 0 4px rgba(0,0,0,0.04);
}

@media (max-width: 768px) {
  .news-card {
    padding: 20px 16px;
  }
  .news-hero {
    padding: 24px 0 20px;
  }
  .news-hero-title {
    font-size: 22px;
  }
  .tl-title {
    font-size: 14px;
  }
  .tl-content {
    font-size: 13px;
  }
}
</style>
