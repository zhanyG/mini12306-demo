/** 格式化 LocalDateTime 为 HH:mm */
export function formatTime(t) {
  if (!t) return '—'
  return String(t).substring(11, 16)
}

/** 格式化 LocalDateTime 为完整日期时间 */
export function formatDateTime(t) {
  if (!t) return '—'
  return String(t).substring(0, 19).replace('T', ' ')
}

/** 格式化 LocalDateTime 为日期 */
export function formatDate(t) {
  if (!t) return '—'
  return String(t).substring(0, 10)
}

/** 计算历时（分钟） */
export function calcDuration(departure, arrival) {
  if (!departure || !arrival) return ''
  const start = new Date(String(departure).replace(' ', 'T'))
  const end = new Date(String(arrival).replace(' ', 'T'))
  const mins = Math.round((end - start) / 60000)
  if (mins < 60) return `${mins}分`
  const h = Math.floor(mins / 60)
  const m = mins % 60
  return m ? `${h}时${m}分` : `${h}时`
}

/** 订单状态对应样式类 */
export function orderStatusClass(status) {
  const map = {
    '未支付': 'tag-warning',
    '已支付': 'tag-info',
    '已出票': 'tag-success',
    '已退票': 'tag-danger'
  }
  return 'tag ' + (map[status] || 'tag-info')
}
