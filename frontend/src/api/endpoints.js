import api from './index'

export const authApi = {
  login: (data) => api.post('/auth/login', data),
  logout: () => api.post('/auth/logout'),
  register: (data) => api.post('/auth/register', data),
  sendCode: (data) => api.post('/auth/send-code', data),
  resetPassword: (data) => api.post('/auth/reset-password', data),
  getProfile: () => api.get('/auth/profile'),
  updateProfile: (data) => api.put('/auth/profile', data),
  updatePassword: (data) => api.put('/auth/password', data),
  updateAvatar: (avatar) => api.put('/auth/profile', { avatar }),
}

export const productApi = {
  getList: (params) => api.get('/products', { params }),
  getDetail: (id) => api.get(`/products/${id}`),
  getCategories: () => api.get('/products/categories'),
  getHot: (limit) => api.get('/products/hot', { params: { limit } }),
  getNew: (limit) => api.get('/products/new', { params: { limit } }),
  getReviews: (id, params) => api.get(`/products/${id}/reviews`, { params }),
  getHotSearch: () => api.get('/products/hot-search'),
}

export const cartApi = {
  add: (data) => api.post('/client/cart/add', data),
  getList: () => api.get('/client/cart/list'),
  updateQuantity: (id, quantity) => api.put(`/client/cart/${id}/quantity`, null, { params: { quantity } }),
  delete: (id) => api.delete(`/client/cart/${id}`),
  check: (id, checked) => api.put(`/client/cart/${id}/check`, null, { params: { checked } }),
  checkAll: (checked) => api.put('/client/cart/check-all', null, { params: { checked } }),
}

export const orderApi = {
  create: (data) => api.post('/client/order/create', data),
  pay: (data) => api.post('/client/order/pay', data),
  getDetail: (orderNo) => api.get(`/client/order/${orderNo}`),
  getList: (params) => api.get('/client/order/list', { params }),
  cancel: (orderNo) => api.put(`/client/order/${orderNo}/cancel`),
  confirm: (orderNo) => api.put(`/client/order/${orderNo}/confirm`),
  refund: (orderNo) => api.put(`/client/order/${orderNo}/refund`),
  getAddresses: () => api.get('/client/order/addresses'),
  addAddress: (data) => api.post('/client/order/address', data),
  updateAddress: (id, data) => api.put(`/client/order/address/${id}`, data),
  deleteAddress: (id) => api.delete(`/client/order/address/${id}`),
  setDefaultAddress: (id) => api.put(`/client/order/address/${id}/default`),
}

export const userApi = {
  favorite: (productId) => api.post(`/client/favorite/${productId}`),
  unfavorite: (productId) => api.delete(`/client/favorite/${productId}`),
  getFavorites: () => api.get('/client/favorite/list'),
  addReview: (data) => api.post('/client/review', data),
  submitFeedback: (data) => api.post('/client/feedback', data),
}

export const promotionApi = {
  getActive: () => api.get('/promotions'),
  getCoupons: () => api.get('/promotions/coupons'),
  getMyCoupons: () => api.get('/promotions/my-coupons'),
  claimCoupon: (id) => api.post(`/promotions/coupon/${id}/claim`),
  seckillOrder: (promotionId, data) => api.post(`/client/seckill/${promotionId}`, data),
}

export const logisticsApi = {
  query: (orderNo) => api.get(`/client/logistics/${orderNo}`),
}

export const adminApi = {
  getDashboard: () => api.get('/admin/dashboard'),
  exportDashboard: () => api.get('/admin/dashboard/export', { responseType: 'blob' }),

  getProducts: (params) => api.get('/admin/products', { params }),
  addProduct: (data) => api.post('/admin/products', data),
  updateProduct: (id, data) => api.put(`/admin/products/${id}`, data),
  deleteProduct: (id) => api.delete(`/admin/products/${id}`),
  toggleProduct: (id, status) => api.put(`/admin/products/${id}/status`, { status }),
  importProducts: (formData) => api.post('/admin/products/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),

  getCategories: () => api.get('/admin/categories'),
  addCategory: (data) => api.post('/admin/categories', data),
  updateCategory: (id, data) => api.put(`/admin/categories/${id}`, data),
  deleteCategory: (id) => api.delete(`/admin/categories/${id}`),

  getOrders: (params) => api.get('/admin/order/list', { params }),
  getOrderDetail: (no) => api.get(`/admin/order/${no}`),
  deliverOrder: (no, data) => api.put(`/admin/order/${no}/deliver`, data),
  cancelOrder: (no) => api.put(`/admin/order/${no}/cancel`),
  refundOrder: (no, approve) => api.put(`/admin/order/${no}/refund`, null, { params: { approve } }),

  getUsers: (params) => api.get('/admin/dashboard/users', { params }),
  toggleUser: (userId, status) => api.put(`/admin/dashboard/users/${userId}/status`, null, { params: { status } }),
  updateUserRole: (userId, role) => api.put(`/admin/dashboard/users/${userId}/role`, { role }),

  getFeedbacks: (params) => api.get('/admin/content/feedbacks', { params }),
  getReviews: (params) => api.get('/admin/content/reviews', { params }),
  replyFeedback: (id, reply) => api.put(`/admin/content/feedback/${id}/reply`, { reply }),
  resolveFeedback: (id) => api.put(`/admin/content/feedback/${id}/resolve`),
  deleteReview: (id) => api.delete(`/admin/content/review/${id}`),

  getBanners: () => api.get('/admin/site/banners'),
  addBanner: (data) => api.post('/admin/site/banner', data),
  updateBanner: (id, data) => api.put(`/admin/site/banner/${id}`, data),
  deleteBanner: (id) => api.delete(`/admin/site/banner/${id}`),

  getAnnouncements: () => api.get('/admin/site/announcements'),
  addAnnouncement: (data) => api.post('/admin/site/announcement', data),
  updateAnnouncement: (id, data) => api.put(`/admin/site/announcement/${id}`, data),
  deleteAnnouncement: (id) => api.delete(`/admin/site/announcement/${id}`),
  upload: (formData) => api.post('/admin/site/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),

  getPromotions: (params) => api.get('/admin/promotions', { params }),
  addPromotion: (data) => api.post('/admin/promotions', data),
  updatePromotion: (id, data) => api.put(`/admin/promotions/${id}`, data),
  deletePromotion: (id) => api.delete(`/admin/promotions/${id}`),
  togglePromotion: (id, status) => api.put(`/admin/promotions/${id}/status`, null, { params: { status } }),
}