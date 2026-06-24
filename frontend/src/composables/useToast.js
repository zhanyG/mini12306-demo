import { ref } from 'vue'

const toasts = ref([])
let seed = 0

export function useToast() {
  function show(message, type = 'info', duration = 3000) {
    const id = ++seed
    toasts.value.push({ id, message, type })
    if (duration > 0) {
      setTimeout(() => dismiss(id), duration)
    }
  }

  function success(message) {
    show(message, 'success')
  }

  function error(message) {
    show(message, 'error', 4000)
  }

  function dismiss(id) {
    toasts.value = toasts.value.filter(t => t.id !== id)
  }

  return { toasts, show, success, error, dismiss }
}
