# beta1.5 开发记录

> 本文档记录 mini12306 项目在 beta1.5 版本中围绕后端能力补齐与前端页面适配完成的改动。

---

## 一、版本目标

本次版本分两部分完成：

1. 后端：补齐车次查询的时间维度，并实现普通用户改签能力。
2. 前端：适配按日期查询车次，并为乘客端订单页新增改签操作入口。

实名注册保持后置，不在 beta1.5 中实现，以便继续兼容当前测试环境下的简化录入流程。

---

## 二、后端接口变更

### 1. 车次查询接口增强

原接口路径保持不变：

```http
GET /api/trains/search
```

新增可选参数：

- `start`：始发站，模糊匹配
- `end`：终点站，模糊匹配
- `date`：出发日期，格式固定为 `yyyy-MM-dd`

示例：

```http
GET /api/trains/search?start=北京&end=上海&date=2025-04-06
GET /api/trains/search?date=2025-04-06
```

查询规则：

- 仅传 `start` / `end` 时，保持原有行为不变。
- 传入 `date` 时，按该自然日内发车的车次筛选。
- 同时传 `start`、`end`、`date` 时，按三项共同过滤。
- `date` 格式错误时，返回明确异常信息，不做静默忽略。

### 2. 新增改签接口

新增普通用户改签接口：

```http
POST /api/orders/{orderId}/reschedule?newTrainId={id}
```

示例：

```http
POST /api/orders/1/reschedule?newTrainId=3
```

返回：改签后的订单对象。

---

## 三、改签业务规则

beta1.5 第一版改签规则固定如下：

- 仅允许"已出票"订单改签。
- 原订单必须存在。
- 新车次必须存在。
- 新车次不能与原车次相同。
- 新车次必须有余票。
- 改签后乘客不变、用户不变。
- 原订单保留原 `orderNo`、`userId`、`passengerId`。
- 订单状态继续保持"已出票"，不新增中间状态。

改签成功后，系统会完成：

1. 释放原车次余票。
2. 扣减新车次余票。
3. 更新订单的 `trainId` 与 `price`。
4. 更新关联车票的座位号、出票时间与票号。
5. 保持车票状态为有效。

整个改签流程放在事务中执行，任一步失败都会回滚。

---

## 四、前端适配

### 1. 车次搜索页 (`TrainList.vue`)

- 原有输入框 `travelDate`（类型为 `date`）在搜索时已经未传递到后端。
- 现在 `handleSearch()` 将 `travelDate.value` 作为 `date` 参数传给 `trainApi.search()`。
- 日期默认值为当天，用户可按需修改。
- 搜索条件无变化：start、end、date 三项均可选，向后兼容。

### 2. 订单页改签操作 (`OrderList.vue`)

- 已出票订单新增"改签"按钮，位于"退票"按钮左侧。
- 点击后弹出 `AppModal` 弹窗，展示所有可改签的车次列表。
- 可改签车次条件：
  - 有余票（`availableSeats > 0`）
  - 不是当前已购车次
- 每项展示车次号、路线、时间和价格。
- 用户点击"选择改签"调用后端改签接口。
- 改签成功后自动刷新订单列表。

### 3. API 调用层 (`api/index.js`)

- `trainApi.search(start, end)` 扩展为 `trainApi.search(start, end, date)`。
- 新增 `orderApi.reschedule(orderId, newTrainId)` 方法。

---

## 五、兼容性说明

本次改动在后端和前端均保持了向后兼容：

- 旧前端如果不传 `date`，查询行为不变。
- 旧后端接口不受前端改动影响。
- 原有购票、退票、查询用户订单接口路径与语义均未改变。
- 订单页原有退票功能不受影响。

---

## 六、涉及文件清单

### 后端

| 文件 | 改动类型 |
|------|---------|
| `demo/src/main/java/com/example/demo/repository/TrainRepository.java` | 扩展：新增时间区间查询方法 |
| `demo/src/main/java/com/example/demo/service/DataService.java` | 修改：`searchTrains` 支持 date 参数 |
| `demo/src/main/java/com/example/demo/controller/TrainController.java` | 修改：`/search` 接口暴露 date 参数 |
| `demo/src/main/java/com/example/demo/service/OrderService.java` | 扩展：新增 `rescheduleTicket` 方法 |
| `demo/src/main/java/com/example/demo/controller/OrderController.java` | 扩展：新增 `/reschedule` 接口 |

### 前端

| 文件 | 改动类型 |
|------|---------|
| `frontend/src/api/index.js` | 扩展：`trainApi.search` 支持 date，新增 `orderApi.reschedule` |
| `frontend/src/views/TrainList.vue` | 修改：搜索时传递 `travelDate` 到后端 |
| `frontend/src/views/OrderList.vue` | 扩展：新增改签按钮、弹窗与业务流程 |

---

## 七、已知限制

1. 改签未处理补差价或退差价，仅更新订单票价字段。
2. 改签未加入发车前时效校验。
3. 改签仅支持普通用户订单接口，未单独扩展管理员改签入口。
4. 实名注册仍未补齐，测试环境继续使用简化注册流程。
5. 改签弹窗目前加载全部车次进行过滤，数据量大时建议优化为后端搜索接口。

---

## 八、建议测试场景

### 车次查询

- 仅传 `start`
- 仅传 `end`
- 仅传 `date`
- 同时传 `start/end/date`
- 传入非法 `date`
- 空日期（默认当天）查询

### 改签

- 已出票订单改签到另一趟有余票车次
- 改签到同一车次（应被弹窗过滤）
- 对未出票/已退票订单（改签按钮应不显示）
- 改签成功后校验订单 `trainId` 和 `price` 已更新
- 改签成功后订单列表自动刷新
- 改签失败时弹窗不关闭，可继续选择其他车次

---

## 九、版本结论

beta1.5 完成后，mini12306 在用户完整流程上比 beta1.4 更进一步：

- 查询车次具备时间维度
- 订单从仅支持购票/退票，新增改签操作
- 前后端新增功能均向后兼容
