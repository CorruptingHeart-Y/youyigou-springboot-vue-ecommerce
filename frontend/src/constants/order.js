export const ORDER_STATUS_MAP = {
  PENDING_PAY: '待支付',
  PENDING_DELIVER: '待发货',
  DELIVERED: '待收货',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  REFUNDING: '退款中',
  REFUNDED: '已退款'
}

export const ORDER_STATUS_TYPE_MAP = {
  PENDING_PAY: 'warning',
  PENDING_DELIVER: '',
  DELIVERED: 'info',
  COMPLETED: 'success',
  CANCELLED: 'danger',
  REFUNDING: 'warning',
  REFUNDED: 'info'
}