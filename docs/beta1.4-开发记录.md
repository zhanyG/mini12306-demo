# beta1.4 开发记录（Agent 协作迭代）

> 本文档记录 mini12306 项目在 beta1.4 版本中的全部改动，涵盖本次对话中 Agent 协助完成的前后端开发与迭代。

---

## 一、版本概览

| 模块 | 目录 | 端口 | 说明 |
|------|------|------|------|
| 后端 | `demo/` | 8080 | Spring Boot + MySQL + JPA |
| 乘客端 | `frontend/` | 5173 | 用户购票 Web 端 |
| 管理端 | `front-manage/` | 5174 | 管理员后台 Web 端（新建） |

---

## 二、迭代时间线（本次对话）

### 阶段 1：乘客端 Web 重构

**背景**：原 `frontend/` 功能简单，测试环境需输入身份证，体验不佳。

**主要改动**：

1. **测试环境友好**
   - 注册页移除身份证必填，仅需用户名 + 密码
   - 常用乘客添加只需姓名，身份证由 `generateTestIdCard()` 自动生成
   - 购票页支持「快速添加乘车人」

2. **功能完善**
   - 车次查询：起止站搜索、站点 datalist 联想、交换起止站、历时显示、余票状态
   - 购票：车次详情、乘客单选、确认购票
   - 订单：状态筛选、展示车次/乘车人/路线、退票
   - 乘客：增删改（编辑功能新增）
   - 个人中心：用户信息、快捷入口（新页面 `/profile`）

3. **基础设施**
   - `src/utils/format.js` — 时间/状态格式化
   - `src/utils/testData.js` — 测试数据辅助
   - `src/composables/useAuth.js` — 登录态
   - `src/composables/useToast.js` — 全局 Toast
   - `src/components/ToastContainer.vue`、`AppModal.vue`
   - `src/api/index.js` — 增加 `loadOrderEnrichment` 批量加载关联数据

4. **UI**
   - 12306 风格蓝色主题、车次卡片、响应式布局、页面过渡动画

**未改动**：后端乘客 API 接口路径与语义保持不变。

---

### 阶段 2：管理员端与后端（新建）

**背景**：车次/用户/订单仅能通过 MySQL 或 Java 初始化代码维护，需要独立管理界面。

#### 2.1 后端改动

**User 实体**
- 新增 `role` 字段（`USER` / `ADMIN`）
- `@JsonProperty(READ_ONLY)` 防止注册接口提权
- `UserController.register` 强制 `role = "USER"`

**认证与鉴权**
| 文件 | 作用 |
|------|------|
| `AdminAuthService.java` | 管理员登录、内存 token 存储 |
| `AdminAuthInterceptor.java` | 拦截 `/api/admin/**`（排除 login/logout） |
| `WebConfig.java` | CORS 增加 `5174`；注册拦截器 |

**初始化**
- `DataService.initAdminUser()`：首次启动创建 `admin / admin123`

**管理 API**

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/login` | 管理员登录 |
| POST | `/api/admin/logout` | 退出 |
| GET | `/api/admin/dashboard/stats` | 仪表盘统计 |
| GET/POST/PUT/DELETE | `/api/admin/trains` | 车次 CRUD |
| GET | `/api/admin/users` | 用户列表（只读） |
| GET | `/api/admin/orders` | 订单列表（只读） |

#### 2.2 管理端前端（`front-manage/`）

**技术栈**：Vue 3 + Vue Router + Axios + Vite（端口 5174）

**页面**
| 路由 | 页面 | 功能 |
|------|------|------|
| `/login` | Login | 管理员登录 |
| `/` | Dashboard | 统计卡片、快捷入口 |
| `/trains` | Trains | 车次增删改查 |
| `/orders` | Orders | 订单列表、状态筛选 |
| `/users` | Users | 用户列表 |

**布局**：侧边栏 + 顶栏（`AdminLayout.vue`）

---

### 阶段 3：用户/订单 CRUD 与管理端完善（本次迭代）

**背景**：管理端用户、订单模块仅支持查看，需升级为完整 CRUD。

#### 3.1 后端新增

**DTO**
| 文件 | 用途 |
|------|------|
| `AdminUserRequest.java` | 创建/更新用户 |
| `AdminOrderCreateRequest.java` | 代客下单 |
| `AdminOrderUpdateRequest.java` | 更新订单状态/金额 |

**AdminService 扩展**

| 操作 | 用户 | 订单 |
|------|------|------|
| 查 | `getUserById`、列表 | `getOrderById`、列表 |
| 增 | `createUser`（BCrypt 加密密码） | `createOrder`（复用 `OrderService.buyTicket`） |
| 改 | `updateUser`（可选改密码/角色） | `updateOrder`（状态变更同步余票/车票） |
| 删 | `deleteUser`（禁止删 `admin`） | `deleteOrder`（已出票自动释放余票） |

**订单状态同步逻辑**
- `已出票 → 已退票`：释放余票，车票标记已退
- `非已出票 → 已出票`：扣减余票，若无车票则生成
- 删除已出票订单：释放余票并删除关联车票

**API 扩展**

| 方法 | 路径 |
|------|------|
| GET/POST/PUT/DELETE | `/api/admin/users`、`/api/admin/users/{id}` |
| GET/POST/PUT/DELETE | `/api/admin/orders`、`/api/admin/orders/{id}` |

#### 3.2 管理端前端更新

**Users.vue**
- 新增用户、编辑（用户名/密码/姓名/手机/角色）、删除
- 默认 `admin` 账号不可删除

**Orders.vue**
- 代客下单：选用户 → 加载乘车人 → 选车次
- 编辑：修改状态、金额
- 删除：确认后删除（已出票自动释票）

**api/index.js**
- 新增 `createUser`、`updateUser`、`deleteUser`
- 新增 `createOrder`、`updateOrder`、`deleteOrder`、`getPassengersByUser`

---

## 三、文件变更清单

### 后端（`demo/src/main/java/com/example/demo/`）

```
entity/User.java                          # +role 字段
request/TrainRequest.java                 # 车次 DTO
request/AdminUserRequest.java             # 用户管理 DTO（新）
request/AdminOrderCreateRequest.java      # 代客下单 DTO（新）
request/AdminOrderUpdateRequest.java      # 订单更新 DTO（新）
service/DataService.java                  # initAdminUser、initTestData
service/AdminAuthService.java             # 管理员认证（新）
service/AdminService.java                 # 管理 CRUD（新/扩展）
service/OrderService.java                 # 购票流程（被 Admin 复用）
config/WebConfig.java                     # CORS + 拦截器
config/AdminAuthInterceptor.java          # 鉴权拦截（新）
config/GlobalExceptionHandler.java        # 全局异常
controller/UserController.java            # 注册强制 USER 角色
controller/admin/AdminAuthController.java
controller/admin/AdminDashboardController.java
controller/admin/AdminTrainController.java
controller/admin/AdminUserController.java     # CRUD（扩展）
controller/admin/AdminOrderController.java    # CRUD（扩展）
```

### 乘客端（`frontend/src/`）

```
api/index.js
composables/useAuth.js, useToast.js
utils/format.js, testData.js
components/NavBar.vue, ToastContainer.vue, AppModal.vue
views/Login.vue, Register.vue, TrainList.vue, BuyTicket.vue
views/OrderList.vue, Passengers.vue, Profile.vue（新）
router/index.js, App.vue, assets/style.css
```

### 管理端（`front-manage/`）— 全新目录

```
package.json, vite.config.js, index.html
src/main.js, App.vue, router/index.js, api/index.js
src/composables/useAuth.js, useToast.js
src/utils/format.js
src/components/ToastContainer.vue, AppModal.vue
src/layouts/AdminLayout.vue
src/views/Login.vue, Dashboard.vue, Trains.vue
src/views/Orders.vue, Users.vue
src/assets/style.css
```

---

## 四、数据与权限说明

### 测试数据初始化
- **存储**：MySQL（`mini12306` 库），非内存
- **车次**：`DataService.initTestData()` 在 `trains` 表为空时插入 5 条示例车次
- **管理员**：`initAdminUser()` 自动创建 `admin / admin123`
- **用户/乘客/订单**：通过 Web 端或管理端操作产生

### 权限模型
| 角色 | 登录入口 | Token 存储 |
|------|----------|------------|
| 普通用户 | `/api/users/login` | `localStorage.token` |
| 管理员 | `/api/admin/login` | `localStorage.adminToken`（服务端内存校验） |

管理员 token 在服务重启后失效，需重新登录。

---

## 五、使用指南

### 启动
```bash
# 1. 确保 MySQL 已启动，库 mini12306 可连接（见 application.properties）

# 2. 后端
cd demo
mvn spring-boot:run

# 3. 乘客端
cd frontend
npm install && npm run dev    # http://localhost:5173

# 4. 管理端
cd front-manage
npm install && npm run dev    # http://localhost:5174
```

### 管理端典型操作
1. 使用 `admin / admin123` 登录
2. **车次管理**：新增/编辑/删除车次及余票
3. **用户管理**：创建测试用户、修改角色、删除用户
4. **订单管理**：代客下单、修改状态/金额、删除异常订单

---

## 六、已知限制与后续可优化项

1. 管理员 token 存于内存，多实例部署或重启后需重新登录
2. 订单手动改状态时，部分边界情况（如反复切换状态）未做完整业务校验
3. 删除用户未级联删除其乘客/订单（可能产生孤儿数据）
4. 管理端未做分页，数据量大时表格性能待优化
5. 乘客端与管理端样式独立维护，尚未抽取共享组件库

---

## 七、版本对比摘要

| 能力 | beta1.3 | beta1.4 |
|------|---------|---------|
| 乘客 Web 端 | 无/简陋 | 完整购票流程 + 测试友好 |
| 管理 Web 端 | 无 | 独立 `front-manage` |
| 车次维护 | SQL / 代码初始化 | 管理端 CRUD |
| 用户管理 | 仅注册 API | 管理端 CRUD |
| 订单管理 | 用户自助 | 用户自助 + 管理端 CRUD |
| 角色权限 | 无 | USER / ADMIN |

---

*文档生成于 beta1.4 迭代，对应 Agent 协作开发会话的全部改动。*
