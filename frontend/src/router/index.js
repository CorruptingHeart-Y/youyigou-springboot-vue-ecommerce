import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../views/client/Layout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('../views/client/Home.vue') },
      { path: 'products', name: 'ProductList', component: () => import('../views/client/ProductList.vue') },
      { path: 'product/:id', name: 'ProductDetail', component: () => import('../views/client/ProductDetail.vue') },
      { path: 'cart', name: 'Cart', component: () => import('../views/client/Cart.vue'), meta: { requiresAuth: true } },
      { path: 'orders', name: 'Orders', component: () => import('../views/client/Orders.vue'), meta: { requiresAuth: true } },
      { path: 'order/:orderNo', name: 'OrderDetail', component: () => import('../views/client/OrderDetail.vue'), meta: { requiresAuth: true } },
      { path: 'profile', name: 'Profile', component: () => import('../views/client/Profile.vue'), meta: { requiresAuth: true } },
      { path: 'addresses', name: 'Addresses', component: () => import('../views/client/Addresses.vue'), meta: { requiresAuth: true } },
      { path: 'favorites', name: 'Favorites', component: () => import('../views/client/Favorites.vue'), meta: { requiresAuth: true } },
      { path: 'feedback', name: 'Feedback', component: () => import('../views/client/Feedback.vue'), meta: { requiresAuth: true } },
      { path: 'chat', name: 'Chat', component: () => import('../views/client/Chat.vue'), meta: { requiresAuth: true } },
      { path: 'news', name: 'News', component: () => import('../views/client/News.vue') },
    ]
  },
  {
    path: '/login', name: 'Login', component: () => import('../views/client/Login.vue'), meta: { guest: true }
  },
  {
    path: '/register', name: 'Register', component: () => import('../views/client/Register.vue'), meta: { guest: true }
  },
  {
    path: '/forgot-password', name: 'ForgotPassword', component: () => import('../views/client/ForgotPassword.vue'), meta: { guest: true }
  },
  {
    path: '/admin',
    component: () => import('../views/admin/Layout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', name: 'Dashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'products', name: 'AdminProducts', component: () => import('../views/admin/Products.vue') },
      { path: 'categories', name: 'AdminCategories', component: () => import('../views/admin/Categories.vue') },
      { path: 'orders', name: 'AdminOrders', component: () => import('../views/admin/Orders.vue') },
      { path: 'order/:orderNo', name: 'AdminOrderDetail', component: () => import('../views/admin/OrderDetail.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/Users.vue') },
      { path: 'banners', name: 'AdminBanners', component: () => import('../views/admin/Banners.vue') },
      { path: 'announcements', name: 'AdminAnnouncements', component: () => import('../views/admin/Announcements.vue') },
      { path: 'feedbacks', name: 'AdminFeedbacks', component: () => import('../views/admin/Feedbacks.vue') },
      { path: 'promotions', name: 'AdminPromotions', component: () => import('../views/admin/Promotions.vue') },
      { path: 'reviews', name: 'AdminReviews', component: () => import('../views/admin/Reviews.vue') },
      { path: 'settings', name: 'AdminSettings', component: () => import('../views/admin/Settings.vue') },
    ]
  },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('../views/client/NotFound.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (to.meta.requiresAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.meta.requiresAdmin && role !== 'ADMIN' && role !== 'SUPER_ADMIN') {
    next('/')
  } else if (to.meta.guest && token) {
    next('/')
  } else {
    next()
  }
})

export default router
