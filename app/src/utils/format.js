export function formatTime(t) {
  if (!t) return '—'
  return String(t).substring(11, 16)
}

export function formatDate(t) {
  if (!t) return '—'
  return String(t).substring(0, 10)
}

export function formatDateTime(t) {
  if (!t) return '—'
  return String(t).substring(0, 19).replace('T', ' ')
}

export function maskIdCard(card) {
  if (!card || card.length < 10) return card || ''
  return card.substring(0, 3) + '********' + card.substring(card.length - 4)
}

export function calcDuration(departure, arrival) {
  const start = new Date(String(departure).replace(' ', 'T'))
  const end = new Date(String(arrival).replace(' ', 'T'))
  const mins = Math.round((end - start) / 60000)
  if (mins < 60) return `${mins}分`
  const h = Math.floor(mins / 60)
  const m = mins % 60
  return m ? `${h}时${m}分` : `${h}时`
}
