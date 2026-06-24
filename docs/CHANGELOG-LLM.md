# mini12306 全栈重构记录（LLM 辅助）

> 基于 `beta1.0.3` 版本的全方位优化 + Vue 3 前端，由 Claude Code（LLM）辅助完成。

---

## 改动总览

```
后端：新增 3 个文件，修改 8 个文件 → 编译 0 error
前端：新增 12 个文件，Vue 3 + Vite 4 + Vue Router → 构建 0 error
前后端完全分离，通过 REST API 通信
```

---

## 新增文件

### `config/GlobalExceptionHandler.java`
全局异常处理器，所有 `RuntimeException` 统一返回 JSON 格式：

```json
{ "error": "错误描述" }
```

避免直接抛 HTTP 500 白页，对前端调用更友好。

### `config/WebConfig.java`
CORS 跨域配置，允许来自前端的跨域请求（为后续接前端做准备）。

### `controller/PassengerController.java`
**乘客管理 API**（完整 CRUD）：

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | `/api/passengers` | 添加常用乘客 |
| GET | `/api/passengers/user/{userId}` | 查询某用户的所有乘客 |
| GET | `/api/passengers/{id}` | 查询单个乘客 |
| PUT | `/api/passengers/{id}` | 更新乘客信息 |
| DELETE | `/api/passengers/{id}` | 删除乘客 |

> `Passenger` 实体和 `PassengerRepository` 之前已存在，但缺少 Controller 层，导致数据表有却无法通过 API 操作。

---

## 修改文件

### `pom.xml`
- 新增依赖 `spring-security-crypto`，引入 BCrypt 密码加密

### `request/LoginRequest.java`
- 为 `username` 和 `password` 添加 `@NotBlank` 校验注解
- 配合 `@Valid` 自动校验请求参数

### `entity/User.java`
- password 字段添加 `@JsonIgnore`，**API 返回不再暴露密码**（即使是加密后的也不应返回）

### `repository/TrainRepository.java`
- 新增三个 JPA 方法命名查询：
  - `findByStartStationContainingAndEndStationContaining`
  - `findByStartStationContaining`
  - `findByEndStationContaining`
- 搜索从**内存过滤**升级为**数据库层查询**

### `repository/PassengerRepository.java`
- 新增 `findByUserId`，支持按用户查询常用乘客

### `service/DataService.java`
- **种子数据扩充**：从 1 趟车（G1001）增加到 5 趟，覆盖 G/D/K 字头和多条线路
- 新增 `searchTrains()`：数据库层搜索，替代原来的全量内存过滤
- 新增 `getUserById()`：替代原来 `getUsers().stream().filter(...)` 的低效方式
- 新增 `getTrainById()`：单条车次查询
- 新增 `getPassengersByUserId()`、`getPassengerById()`、`deletePassenger()`：乘客 CRUD 支持

### `controller/UserController.java`
- **注册加密**：密码使用 `BCryptPasswordEncoder` 加密后入库
- **唯一性校验**：注册时检查用户名是否已存在
- **密码强度检查**：密码长度不少于 6 位
- **登录验证**：使用 `passwordEncoder.matches()` 比对 BCrypt 密文
- **查询优化**：`getUser()` 改用 `getUserById()` 直接查库

### `controller/TrainController.java`
- 搜索方法改为调用 `DataService.searchTrains()`（数据库层）
- 新增 `GET /api/trains/{id}` 车次详情接口

### `service/OrderService.java`
- **动态座位分配**：根据已售座位数自动计算座位号（如"01车01A""02车05D"等），替换原来的硬编码 `"01车01A"`
- **事务保证**：`buyTicket()` 和 `cancelTicket()` 添加 `@Transactional(rollbackFor = Exception.class)`，保证扣余票 + 创建订单 + 出票要么全成功，要么全回滚

---

## 前端（Vue 3 + Vite）

### 技术栈
- **Vue 3**（Composition API + `<script setup>`）
- **Vite 5** 构建工具
- **Vue Router 4** 前端路由
- **Axios** HTTP 请求封装
- **纯 CSS** 手写样式，无 UI 框架依赖

### 项目结构

```
frontend/
├── index.html
├── package.json
├── vite.config.js              开发代理 → localhost:8080
└── src/
    ├── main.js                  入口
    ├── App.vue                  根组件 + 导航栏
    ├── api/index.js             Axios 封装 + 所有 API 方法
    ├── router/index.js          路由配置 + 登录守卫
    ├── assets/style.css         全局样式
    ├── components/
    │   └── NavBar.vue           顶部导航栏
    └── views/
        ├── Login.vue            登录页
        ├── Register.vue         注册页
        ├── TrainList.vue        车次查询/列表/搜索
        ├── BuyTicket.vue        购票确认（选择乘车人）
        ├── OrderList.vue        我的订单 + 退票
        └── Passengers.vue       常用乘客管理（增删）
```

### 页面功能说明

| 页面 | 路由 | 功能 |
|------|------|------|
| 登录 | `/login` | 用户名密码登录，存储 token |
| 注册 | `/register` | 用户注册，成功后跳转登录 |
| 车次查询 | `/` | 搜索 + 列表展示，含余票/时间/价格，点击购票 |
| 购票确认 | `/buy/:trainId` | 展示车次信息，选择乘车人，确认购票 |
| 我的订单 | `/orders` | 订单列表，已出票可退票 |
| 常用乘客 | `/passengers` | 列表 + 弹窗添加 + 删除 |

### 前后端交互

```
vite.config.js proxy: /api → http://localhost:8080
```

开发时前端运行在 `localhost:5173`，API 请求通过 Vite 代理转发到后端 `localhost:8080`，不涉及跨域问题。生产部署时后端已配置 CORS（`WebConfig.java`）。

### 使用方式

```bash
# 启动后端（Spring Boot）
cd demo && mvn spring-boot:run

# 启动前端（另一个终端）
cd frontend && npm run dev
```

---

```
用户管理
  POST   /api/users/register              注册（BCrypt 加密）
  POST   /api/users/login                 登录（BCrypt 验证）
  GET    /api/users/{id}                  查询用户

乘客管理
  POST   /api/passengers                  添加乘客
  GET    /api/passengers/user/{userId}    查询某用户的所有乘客
  GET    /api/passengers/{id}             查询单个乘客
  PUT    /api/passengers/{id}             更新乘客
  DELETE /api/passengers/{id}             删除乘客

车次查询
  GET    /api/trains                      所有车次
  GET    /api/trains/search               按起止站搜索
  GET    /api/trains/{id}                 车次详情

订单
  POST   /api/orders/buy                  购票（动态座位 + 事务）
  POST   /api/orders/{id}/cancel          退票
  GET    /api/orders/user/{userId}        查询用户订单

健康检查
  GET    /hello                           服务状态检测
```

---

## 课程展示亮点

| 知识点 | 对应实现 |
|--------|----------|
| 三层架构 | Controller → Service → Repository |
| RESTful API 设计 | 12 个接口覆盖用户/乘客/车次/订单 |
| 密码安全 | BCrypt 加密存储 + 验证 |
| 数据库查询 | JPA 方法命名查询，避免内存过滤 |
| 事务管理 | `@Transactional` 保证购票流程原子性 |
| 全局异常处理 | `@RestControllerAdvice` 统一异常响应 |
| 参数校验 | `@Valid` + `@NotBlank` 注解 |
| 动态算法 | 基于余票计算座位号 |
| 状态机 | 订单未支付→已支付→已出票→已退票 |
| 数据初始化 | `@PostConstruct` 预置演示数据 |

---

*文档生成于 2026-05-22。*
