export function formatTime(t) {
  if (!t) return '—'
  return String(t).substring(11, 16)
}

export function formatDateTime(t) {
  if (!t) return '—'
  return String(t).substring(0, 19).replace('T', ' ')
}

export function toDatetimeLocal(t) {
  if (!t) return ''
  return String(t).substring(0, 16)
}

export function orderStatusClass(status) {
  const map = { '未支付': 'tag-warning', '已支付': 'tag-info', '已出票': 'tag-success', '已退票': 'tag-danger' }
  return 'tag ' + (map[status] || 'tag-info')
}
