import { ref, computed } from 'vue'

const token = ref(localStorage.getItem('token'))
const userId = ref(localStorage.getItem('userId'))
const username = ref(localStorage.getItem('username'))

export function useAuth() {
  const loggedIn = computed(() => !!token.value)

  function setAuth(data) {
    token.value = data.token
    userId.value = String(data.userId)
    username.value = data.username
    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', String(data.userId))
    localStorage.setItem('username', data.username)
  }

  function logout() {
    token.value = null
    userId.value = null
    username.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
  }

  return { token, userId, username, loggedIn, setAuth, logout }
}
