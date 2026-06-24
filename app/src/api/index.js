import axios from 'axios'

const api = axios.create({ timeout: 10000 })

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  res => res,
  err => {
    const msg = err.response?.data?.error || err.message || '请求失败'
    return Promise.reject(new Error(msg))
  }
)

export const userApi = {
  register(data) { return api.post('/api/users/register', data) },
  login(data) { return api.post('/api/users/login', data) },
  getById(id) { return api.get(`/api/users/${id}`) },
  updateProfile(id, data) { return api.put(`/api/users/${id}`, data) },
  changePassword(id, data) { return api.put(`/api/users/${id}/password`, data) },
  verifyIdCard(id, data) { return api.post(`/api/users/${id}/verify-idcard`, data) }
}

export const trainApi = {
  getAll() { return api.get('/api/trains') },
  search(params) { return api.get('/api/trains/search', { params }) },
  getById(id) { return api.get(`/api/trains/${id}`) }
}

export const passengerApi = {
  list(userId) { return api.get(`/api/passengers/user/${userId}`) },
  getById(id) { return api.get(`/api/passengers/${id}`) },
  add(data) { return api.post('/api/passengers', data) },
  update(id, data) { return api.put(`/api/passengers/${id}`, data) },
  remove(id) { return api.delete(`/api/passengers/${id}`) }
}

export const orderApi = {
  buy(params) { return api.post('/api/orders/buy', null, { params }) },
  create(params) { return api.post('/api/orders/create', null, { params }) },
  simulatePay(orderId) { return api.post(`/api/orders/${orderId}/simulate-pay`) },
  confirmPay(orderId, payType, payTradeNo) { return api.post(`/api/orders/${orderId}/confirm-pay`, null, { params: { payType, payTradeNo } }) },
  cancel(orderId) { return api.post(`/api/orders/${orderId}/cancel`) },
  getById(orderId) { return api.get(`/api/orders/${orderId}`) },
  list(userId) { return api.get(`/api/orders/user/${userId}`) },
  reschedule(orderId, newTrainId) { return api.post(`/api/orders/${orderId}/reschedule`, null, { params: { newTrainId } }) },
  payUpgrade(orderId, newTrainId) { return api.post(`/api/orders/${orderId}/pay-upgrade`, null, { params: { newTrainId } }) }
}

export const billApi = {
  listByUser(userId) { return api.get(`/api/bills/user/${userId}`) },
  listByOrder(orderId) { return api.get(`/api/bills/order/${orderId}`) }
}

export const notificationApi = {
  list(userId) { return api.get('/api/notifications', { params: { userId } }) },
  getById(id) { return api.get(`/api/notifications/${id}`) },
  markRead(id, userId) { return api.post(`/api/notifications/${id}/read`, null, { params: { userId } }) },
  unreadCount(userId) { return api.get('/api/notifications/unread-count', { params: { userId } }) }
}
