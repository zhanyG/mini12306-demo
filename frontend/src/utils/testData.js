/**
 * 测试环境辅助：自动生成占位身份证号（后端字段最长18位，非必填）
 * 仅用于需要展示或兼容旧数据的场景，用户无需手动输入。
 */
export function generateTestIdCard() {
  const suffix = String(Date.now()).slice(-14)
  return `T${suffix}`.padEnd(18, '0').slice(0, 18)
}

/** 测试环境默认乘客姓名前缀 */
export function suggestPassengerName(index = 0) {
  const names = ['张三', '李四', '王五', '赵六', '测试乘客']
  return names[index % names.length]
}
