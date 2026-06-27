<template>
  <footer class="amz-footer">
    <!-- ═══ 第一部分：回到顶部 ═══ -->
    <div class="footer-back-top" @click="scrollToTop">
      <span>回到顶部</span>
    </div>

    <!-- ═══ 第二部分：链接矩阵 ═══ -->
    <div class="footer-links">
      <div class="footer-links-inner">
        <div class="footer-col" v-for="col in linkColumns" :key="col.title">
          <h4 class="footer-col-title">{{ col.title }}</h4>
          <ul class="footer-col-list">
            <li v-for="link in col.links" :key="link.text">
              <a :href="link.href || '#'" @click.prevent="link.href ? null : onLinkClick(link.text)">
                {{ link.text }}
              </a>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- ═══ 分割线 ═══ -->
    <div class="footer-divider"></div>

    <!-- ═══ 第三部分：底部品牌与设置 ═══ -->
    <div class="footer-bottom">
      <div class="footer-bottom-inner">
        <div class="footer-brand">
          <span class="footer-logo">优选购</span>
        </div>
        <div class="footer-settings">
          <span class="setting-pill">🌐 简体中文</span>
          <span class="setting-pill">¥ CNY - 人民币</span>
          <span class="setting-pill">🇨🇳 中国</span>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const onLinkClick = (text) => {
  // Placeholder — no-op for demo links
}

const linkColumns = [
  {
    title: '了解我们',
    links: [
      { text: '关于优选购' },
      { text: '人才招聘' },
      { text: '品牌故事' },
      { text: '新闻中心' },
      { text: '投资者关系' }
    ]
  },
  {
    title: '合作信息',
    links: [
      { text: '我要开店' },
      { text: '供应商入驻' },
      { text: '广告合作' },
      { text: '推广联盟' },
      { text: '合作招商' }
    ]
  },
  {
    title: '支付服务',
    links: [
      { text: '支付方式' },
      { text: '优选购卡' },
      { text: '分期支付' },
      { text: '账户充值' },
      { text: '支付安全' }
    ]
  },
  {
    title: '帮助中心',
    links: [
      { text: '我的订单' },
      { text: '退货与换货' },
      { text: '配送信息' },
      { text: '联系客服' },
      { text: '用户协议' }
    ]
  }
]
</script>

<style scoped>
/* ═══ 回到顶部栏 ═══ */
.footer-back-top {
  background: #37475A;
  color: #fff;
  text-align: center;
  padding: 15px 0;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.2s ease;
  user-select: none;
}
.footer-back-top:hover {
  background: #485769;
}

/* ═══ 链接矩阵 ═══ */
.footer-links {
  background: #232F3E;
  padding: 40px 0 32px;
}
.footer-links-inner {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 20px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}
.footer-col-title {
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 14px;
}
.footer-col-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.footer-col-list li {
  margin-bottom: 8px;
}
.footer-col-list a {
  color: #CCC;
  font-size: 13px;
  text-decoration: none;
  transition: all 0.15s ease;
}
.footer-col-list a:hover {
  color: #fff;
  text-decoration: underline;
}

/* ═══ 分割线 ═══ */
.footer-divider {
  border-top: 1px solid #3a4553;
  background: #131A22;
}

/* ═══ 底部栏 ═══ */
.footer-bottom {
  background: #131A22;
  padding: 28px 0 32px;
}
.footer-bottom-inner {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 20px;
}
.footer-logo {
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 2px;
}
.footer-settings {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
}
.setting-pill {
  display: inline-flex;
  align-items: center;
  padding: 5px 14px;
  border: 1px solid #848688;
  border-radius: 3px;
  color: #CCC;
  font-size: 12px;
  cursor: default;
  white-space: nowrap;
  transition: border-color 0.2s, color 0.2s;
}
.setting-pill:hover {
  border-color: #bbb;
  color: #fff;
}

/* ═══ 响应式 ═══ */
@media (max-width: 768px) {
  .footer-links-inner {
    grid-template-columns: repeat(2, 1fr);
    gap: 20px 16px;
  }
  .footer-col-title { font-size: 14px; }
  .footer-col-list a { font-size: 12px; }
  .footer-bottom-inner { flex-direction: column; gap: 14px; }
  .footer-logo { font-size: 18px; }
}
</style>
