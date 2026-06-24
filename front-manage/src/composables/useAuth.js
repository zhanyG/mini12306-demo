import { ref, computed } from 'vue'

const token = ref(localStorage.getItem('adminToken'))
const username = ref(localStorage.getItem('adminUsername'))

export function useAuth() {
  const loggedIn = computed(() => !!token.value)

  function setAuth(data) {
    token.value = data.token
    username.value = data.username
    localStorage.setItem('adminToken', data.token)
    localStorage.setItem('adminUsername', data.username)
  }

  async function logout(apiLogout) {
    try {
      if (apiLogout) await apiLogout()
    } catch { /* ignore */ }
    token.value = null
    username.value = null
    localStorage.removeItem('adminToken')
    localStorage.removeItem('adminUsername')
  }

  return { token, username, loggedIn, setAuth, logout }
}
