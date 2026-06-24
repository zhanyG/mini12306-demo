import axios from 'axios'

const api = axios.create({ baseURL: '', timeout: 15000 })

api.interceptors.request.use(config => {
  const token = localStorage.getItem('adminToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  res => res,
  err => {
    const msg = err.response?.data?.error || err.message || '请求失败'
    if (err.response?.status === 401) {
      localStorage.removeItem('adminToken')
      localStorage.removeItem('adminUsername')
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(new Error(msg))
  }
)

export const adminApi = {
  login(data) {
    return api.post('/api/admin/login', data)
  },
  logout() {
    return api.post('/api/admin/logout')
  },
  getStats() {
    return api.get('/api/admin/dashboard/stats')
  },
  getTrains() {
    return api.get('/api/admin/trains')
  },
  createTrain(data) {
    return api.post('/api/admin/trains', data)
  },
  updateTrain(id, data) {
    return api.put(`/api/admin/trains/${id}`, data)
  },
  deleteTrain(id) {
    return api.delete(`/api/admin/trains/${id}`)
  },
  getUsers() {
    return api.get('/api/admin/users')
  },
  getUser(id) {
    return api.get(`/api/admin/users/${id}`)
  },
  createUser(data) {
    return api.post('/api/admin/users', data)
  },
  updateUser(id, data) {
    return api.put(`/api/admin/users/${id}`, data)
  },
  deleteUser(id) {
    return api.delete(`/api/admin/users/${id}`)
  },
  getOrders() {
    return api.get('/api/admin/orders')
  },
  getOrder(id) {
    return api.get(`/api/admin/orders/${id}`)
  },
  createOrder(data) {
    return api.post('/api/admin/orders', data)
  },
  updateOrder(id, data) {
    return api.put(`/api/admin/orders/${id}`, data)
  },
  deleteOrder(id) {
    return api.delete(`/api/admin/orders/${id}`)
  },
  getTrain(id) {
    return api.get(`/api/trains/${id}`)
  },
  getPassenger(id) {
    return api.get(`/api/passengers/${id}`)
  },
  getPassengersByUser(userId) {
    return api.get(`/api/passengers/user/${userId}`)
  },
  getNotifications() {
    return api.get('/api/admin/notifications')
  },
  createNotification(data) {
    return api.post('/api/admin/notifications', data)
  },
  deleteNotification(id) {
    return api.delete(`/api/admin/notifications/${id}`)
  }
}

export default api
