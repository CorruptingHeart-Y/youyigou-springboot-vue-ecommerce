# 🛒 优易购 (YouYiGou) — 全栈微服务电商平台

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.3.6-brightgreen?logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3.4-4FC08D?logo=vuedotjs" alt="Vue">
  <img src="https://img.shields.io/badge/Java-23-orange?logo=openjdk" alt="Java 23">
  <img src="https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-7-red?logo=redis" alt="Redis">
  <img src="https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker" alt="Docker">
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License">
</p>

<p align="center"><b>Spring Boot 3 + Vue 3 全栈微服务电商平台</b> — 涵盖用户、商品、订单、网关四大微服务，<br>支持秒杀、优惠券、WebSocket 实时聊天、Docker 一键部署。</p>

---

## 📖 目录

- [项目简介](#-项目简介)
- [技术栈](#-技术栈)
- [微服务架构](#-微服务架构)
- [功能模块](#-功能模块)
- [快速开始](#-快速开始)
- [项目结构](#-项目结构)
- [数据库设计](#-数据库设计)
- [API 路由](#-api-路由)
- [配置说明](#-配置说明)
- [默认账号](#-默认账号)
- [需求实现对照](#-需求实现对照)

---

## 📖 项目简介

**优易购 (YouYiGou)** 是一个基于 Spring Boot 3 + Vue 3 的全栈电商平台，采用微服务架构将系统拆分为用户服务、商品服务、订单服务和 API 网关四个独立模块。前端使用 Vue 3 + Element Plus 构建响应式界面，后端集成 Spring Security + JWT 实现细粒度权限控制。

本项目为大学《Web 开发技术》课程设计，完整覆盖客户端购物全流程和管理后台全维度管理，并在基础需求之上实现了 Redis 缓存优化、微服务拆分、Docker 容器化等拓展功能。

### 亮点

- 🧩 **微服务架构** — 用户/商品/订单/网关四大服务独立部署，Spring Boot + RestTemplate 服务间通信
- 🔐 **JWT 动态权限** — Spring Security + JWT 无状态认证，RBAC 三级角色 + 细粒度 `@PreAuthorize` 注解控制
- ⚡ **Redis 秒杀** — Redis 原子 `DECR` 库存扣减 + 回滚机制，库存预热
- 🎫 **优惠券系统** — 领取 → 使用 → 核销完整流程，下单时验证领取状态和最低消费
- 💬 **WebSocket 聊天** — 网关透明代理 → 用户服务，端到端实时消息
- 🐳 **Docker 一键部署** — `docker-compose up -d` 启动全部服务 + MySQL + Redis
- 📊 **数据可视化** — ECharts 销售趋势、热销排行、订单状态统计，支持 Excel 导入导出

---

## 🛠 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Java + Spring Boot | 23 + 3.3.6 | 核心框架 |
| Spring Security | 6.x | 认证鉴权 |
| MyBatis-Plus | 3.5.9 | ORM（分页、逻辑删除、自动填充） |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7.x | Token 缓存、验证码、热门搜索、秒杀库存 |
| JWT (jjwt) | 0.12.6 | 无状态认证 |
| Knife4j | 4.5.0 | API 文档（OpenAPI 3） |
| WebSocket | — | 实时聊天 |
| Spring Boot Mail | — | 邮件验证码 |
| Apache POI | 5.3.0 | Excel 导入导出 |
| Hutool | 5.8.34 | 通用工具库 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.4 | 渐进式框架 |
| Vite | 5.4 | 构建工具 |
| Vue Router | 4.3 | 路由管理（History 模式） |
| Pinia | 2.1 | 状态管理 |
| Element Plus | 2.7 | UI 组件库 |
| Axios | 1.7 | HTTP 客户端 |
| ECharts | 5.5 | 数据可视化 |

---

## 🏗 微服务架构

```
                         ┌─────────────────────────────────────────────┐
                         │                  Gateway (:8080)             │
                         │   API 路由转发 · 静态资源 · SPA Fallback    │
                         └──┬──────────────┬──────────────┬────────────┘
                            │              │              │
                   /api/auth/**    /api/products/**   /api/client/**
                   /api/admin/users/**  /api/admin/products/**  /api/admin/order/**
                   /ws/chat/**          /api/admin/categories/** /api/admin/dashboard/**
                            │              │              │
                   ┌────────▼──┐  ┌───────▼──────┐  ┌─────▼────────┐
                   │ User Service │  │Product Service│  │ Order Service │
                   │    :8081     │  │    :8082      │  │    :8083      │
                   └──────┬───────┘  └──────┬────────┘  └──────┬────────┘
                          │                 │                  │
                          └─────────────────┼──────────────────┘
                                            │
                                   ┌────────▼───────┐
                                   │  MySQL  :3306   │
                                   │  Redis  :6379   │
                                   └────────────────┘
```

| 服务 | 端口 | 职责 |
|------|------|------|
| **Gateway** | 8080 | API 路由转发、前端静态资源托管、SPA History 模式 Fallback、WebSocket 代理 |
| **User Service** | 8081 | 用户注册/登录/JWT 认证、个人信息、地址管理、邮件验证码、WebSocket 聊天 |
| **Product Service** | 8082 | 商品 CRUD、分类管理、评价、收藏、轮播图、公告、促销活动、文件上传 |
| **Order Service** | 8083 | 购物车、订单（创建/支付/发货/退款）、仪表盘统计、反馈管理、物流、秒杀执行 |

---

## ✨ 功能模块

### 🛍 客户端（普通用户）

| 模块 | 功能 |
|------|------|
| **用户认证** | 注册（邮箱+验证码）、登录（记住我 7 天）、找回密码、个人信息修改 |
| **商品浏览** | 分类筛选、关键词搜索、价格/销量/新品排序、热门搜索词 |
| **商品详情** | 图片/名称/价格/库存/规格选择/详情介绍、评价查看 |
| **购物车** | 添加商品（选规格/数量）、修改数量、删除、勾选/全选、结算 |
| **订单流程** | 确认地址 → 选择优惠券 → 提交订单 → 模拟支付 → 查看物流 |
| **订单操作** | 取消订单、确认收货、申请退款 |
| **收藏 & 评价** | 收藏/取消收藏、1-5 星评分 + 文字评价 |
| **促销活动** | 秒杀活动（Redis 原子库存扣减）、优惠券领取/使用、折扣活动 |
| **客服聊天** | WebSocket 实时在线聊天 |
| **其他** | 收货地址管理、用户反馈、公告查看 |

### 🔧 管理后台（Admin / SuperAdmin）

| 模块 | 功能 |
|------|------|
| **数据看板** | 总用户数/订单数/销售额/今日订单、ECharts 趋势图/饼图、数据导出 Excel |
| **商品管理** | CRUD、上下架、Excel 批量导入导出 |
| **分类管理** | 树形多级分类，新增/修改/删除/排序 |
| **订单管理** | 查看/筛选、发货、取消、退款处理、订单导出 |
| **用户管理** | 查看/搜索（账号/手机号/昵称）、禁用/启用、角色修改 |
| **轮播图管理** | 上传图片、设置链接、排序 |
| **公告管理** | 发布/编辑/删除公告 |
| **促销管理** | 折扣活动、秒杀活动 CRUD |
| **评价 & 反馈** | 查看/删除违规评价、回复/标记已处理反馈 |
| **权限管理** | 三级角色（USER/ADMIN/SUPER_ADMIN）+ 动态权限分配 |

---

## 🚀 快速开始

### 环境要求

- **JDK 23** · **Maven 3.8+** · **MySQL 8.0+** · **Redis** · **Node.js 18+**

### 1. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

创建 `ecommerce` 数据库并导入基础数据（管理员账号 `admin`/`123456`）。如需完整种子数据：

```bash
mysql -u root -p ecommerce < sql/seed_full.sql
```

### 2. 启动 Redis

```bash
redis-server
```

### 3. 构建项目

```bash
mvn clean package -DskipTests
```

### 4. 启动微服务（按顺序）

```bash
java -jar ecommerce-user/target/ecommerce-user-1.0.0.jar        # ① 用户服务
java -jar ecommerce-product/target/ecommerce-product-1.0.0.jar  # ② 商品服务
java -jar ecommerce-order/target/ecommerce-order-1.0.0.jar      # ③ 订单服务
java -jar ecommerce-gateway/target/ecommerce-gateway-1.0.0.jar  # ④ API 网关
```

### 5. 构建前端

```bash
cd frontend && npm install && npm run build
```

构建产物输出到 `frontend/dist/`，Gateway 自动托管。

### 6. 访问

| 入口 | 地址 |
|------|------|
| 🛍 客户端 | http://localhost:8080/ |
| 🔧 管理后台 | http://localhost:8080/admin |
| 📘 API 文档（User） | http://localhost:8081/doc.html |
| 📘 API 文档（Product） | http://localhost:8082/doc.html |
| 📘 API 文档（Order） | http://localhost:8083/doc.html |

### 🐳 Docker 一键启动

```bash
docker-compose up -d
```

### 🔥 前端开发模式

```bash
cd frontend && npm run dev
```

Vite 开发服务器运行在 `http://localhost:5200`，API 自动代理到 Gateway `:8080`，支持 HMR 热更新。

---

## 📁 项目结构

```
youyigou-springboot-vue-ecommerce/
├── pom.xml                      # Maven 父 POM，统一版本管理
├── docker-compose.yml           # Docker 编排（MySQL + Redis + 4 服务）
├── sql/                         # 数据库脚本
│   ├── init.sql                 #   建表 + 管理员 + 分类
│   ├── seed_data.sql            #   种子数据（商品/轮播图/促销）
│   └── seed_full.sql            #   完整种子（含覆盖逻辑）
├── ecommerce-common/            # 🔧 共享模块
│   └── src/main/java/com/ecommerce/
│       ├── common/              #   Result、PageResult、全局异常处理
│       ├── config/              #   Security、CORS、Redis、Jackson、MyBatis-Plus、WebSocket
│       ├── dto/                 #   请求/响应 DTO
│       ├── entity/              #   数据库实体（11 张表）
│       ├── mapper/              #   MyBatis-Plus Mapper
│       ├── security/            #   JWT 工具、认证过滤器、@CurrentUser
│       └── websocket/           #   WebSocket 端点
├── ecommerce-gateway/           # 🚪 API 网关 (:8080)
│   └── src/main/java/com/ecommerce/gateway/
│       ├── GatewayController    #   API 路由转发（RestTemplate）
│       ├── IndexController      #   SPA index.html 返回
│       ├── SpaErrorController   #   SPA 路由 fallback
│       ├── StaticResourceConfig #   静态资源 /uploads 映射
│       ├── CorsFilter           #   全局 CORS
│       └── GatewaySecurityConfig#   安全配置（permitAll）
├── ecommerce-user/              # 👤 用户服务 (:8081)
│   └── src/main/java/com/ecommerce/user/
│       ├── controller/          #   Auth、AdminUser、Address
│       └── service/             #   注册/登录、邮件验证码、地址管理
├── ecommerce-product/           # 📦 商品服务 (:8082)
│   └── src/main/java/com/ecommerce/product/
│       ├── controller/client/   #   Product、Promotion（客户端）
│       ├── controller/admin/    #   商品/分类/轮播图/公告/促销管理
│       └── service/             #   商品、分类、收藏、评价、文件上传
├── ecommerce-order/             # 📋 订单服务 (:8083)
│   └── src/main/java/com/ecommerce/order/
│       ├── controller/          #   Order、Cart、Dashboard、Feedback、Logistics
│       └── service/             #   订单、购物车、秒杀执行、仪表盘
└── frontend/                    # 🎨 Vue 3 前端
    ├── vite.config.js           #   Vite 配置（代理 + 构建）
    └── src/
        ├── main.js              #   入口：Element Plus + Pinia + Router
        ├── router/index.js      #   路由（客户端 + 管理后台）
        ├── stores/              #   Pinia 状态（user、cart）
        ├── api/                 #   Axios 封装 + API 端点
        └── views/
            ├── client/          #   首页、商品列表/详情、购物车、订单、个人中心
            └── admin/           #   仪表盘、商品/订单/用户/内容管理
```

---

## 🗄 数据库设计

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| `e_user` | 用户表 | id, username, password, email, phone, role(USER/ADMIN/SUPER_ADMIN) |
| `e_category` | 商品分类 | id, name, parent_id（树形结构） |
| `e_product` | 商品表 | id, name, price, stock, specs(JSON), detail(富文本), images, cover_image |
| `e_cart_item` | 购物车 | id, user_id, product_id, spec, quantity |
| `e_address` | 收货地址 | id, user_id, name, phone, province/city/district, detail, is_default |
| `e_order` | 订单 | id, order_no, user_id, status, total, pay_type, logistics |
| `e_order_item` | 订单明细 | id, order_id, product_id, spec, quantity, price |
| `e_review` | 商品评价 | id, user_id, product_id, rating(1-5), content, images |
| `e_user_favorite` | 用户收藏 | id, user_id, product_id |
| `e_banner` | 轮播图 | id, title, image_url, link_url, sort |
| `e_announcement` | 公告 | id, title, content, publish_time |
| `e_feedback` | 用户反馈 | id, user_id, content, reply（管理员回复）, status |
| `e_promotion` | 促销活动 | id, name, type(SECKILL/DISCOUNT/COUPON), start_time, end_time, discount |
| `e_user_coupon` | 用户优惠券 | id, user_id, promotion_id, status(UNUSED/USED/EXPIRED) |
| `e_permission` | 权限表 | id, code, name |
| `e_role_permission` | 角色权限 | role, permission_id |

---

## 📡 API 路由

### 公共接口（无需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/send-code` | 发送邮箱验证码 |
| POST | `/api/auth/reset-password` | 重置密码 |
| GET | `/api/products` | 商品列表（分页+筛选+排序） |
| GET | `/api/products/{id}` | 商品详情 |
| GET | `/api/products/categories` | 分类树 |
| GET | `/api/products/hot` | 热门商品 |
| GET | `/api/products/new` | 新品推荐 |
| GET | `/api/promotions` | 进行中的促销 |
| GET | `/api/promotions/{id}` | 促销详情 |

### 用户接口（需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET/PUT | `/api/auth/profile` | 获取/修改个人信息 |
| PUT | `/api/auth/password` | 修改密码 |
| POST/GET/PUT/DELETE | `/api/client/cart/**` | 购物车 CRUD |
| POST/GET | `/api/client/order/**` | 订单创建/列表/详情 |
| POST | `/api/client/seckill/{id}` | 秒杀下单 |
| POST/DELETE | `/api/client/favorite/**` | 收藏/取消收藏 |
| POST | `/api/client/review` | 提交评价 |
| POST | `/api/client/feedback` | 提交反馈 |
| GET | `/api/client/logistics/{orderNo}` | 查询物流 |
| POST/GET | `/api/client/coupon/**` | 领取/查看优惠券 |

### 管理接口（需 ADMIN+）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/dashboard` | 仪表盘统计 |
| GET/POST/PUT/DELETE | `/api/admin/products/**` | 商品管理 |
| GET/POST/PUT/DELETE | `/api/admin/categories/**` | 分类管理 |
| GET/PUT | `/api/admin/order/**` | 订单管理 |
| GET/PUT | `/api/admin/users/**` | 用户管理 |
| GET/POST/PUT/DELETE | `/api/admin/site/banners/**` | 轮播图管理 |
| GET/POST/PUT/DELETE | `/api/admin/site/announcements/**` | 公告管理 |
| GET/POST/PUT/DELETE | `/api/admin/promotions/**` | 促销管理 |
| GET/DELETE | `/api/admin/content/reviews/**` | 评价管理 |
| GET/PUT | `/api/admin/content/feedbacks/**` | 反馈管理 |
| POST | `/api/admin/site/upload` | 文件上传 |

---

## ⚙ 配置说明

### 数据库连接

各服务默认连接 `localhost:3306/ecommerce`，用户名 `root`，密码 `123456`。修改以下文件同步配置：

- `ecommerce-user/src/main/resources/application.yml`
- `ecommerce-product/src/main/resources/application.yml`
- `ecommerce-order/src/main/resources/application.yml`

### JWT 密钥

所有微服务必须使用**相同的 JWT 密钥**：

```yaml
jwt:
  secret: ecommerce-platform-2025-secret-key-for-jwt-min-256-bits-length!!
  expiration: 86400000  # 24 小时
```

### 邮件服务

找回密码功能需要 SMTP 配置（`ecommerce-user`）：

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: your-email@qq.com
    password: your-smtp-password
```

### 文件上传

上传目录默认为项目根目录下的 `uploads/`，通过 Gateway 的 `/uploads/**` 路径对外提供访问。Gateway 启动时会自动搜索 `uploads/` 目录位置。

---

## 👥 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 🔴 超级管理员 | `admin` | `123456` |

新注册用户默认为 `USER` 角色，管理员可在后台升级为 `ADMIN` 或 `SUPER_ADMIN`。

---

## ✅ 需求实现对照

### 基本要求

- [x] **客户端** — 商品浏览/搜索、商品详情、购物车、订单流程（创建→支付→收货→退款）、收藏、评价
- [x] **管理后台** — 管理员登录、数据看板+图表、用户/商品/分类/订单管理、评价管理

### 进阶要求

- [x] **技术进阶** — Element Plus + Pinia + Spring Security JWT 认证鉴权
- [x] **界面设计** — Element Plus 统一风格、响应式布局（手机/电脑）
- [x] **客户端进阶** — 邮箱验证码注册、记住密码、找回密码、地址管理、规格选择、优惠券、秒杀、WebSocket 聊天、公告、反馈
- [x] **管理后台进阶** — 轮播图/公告/反馈管理、数据导出、商品 Excel 导入导出、动态权限分级

### 拓展要求

- [x] **Redis 缓存** — Token/验证码/商品详情/热门搜索/秒杀库存
- [x] **微服务架构** — User / Product / Order / Gateway 四服务独立部署
- [x] **Docker 容器化** — `docker-compose.yml` 一键启动全栈

---

## 📄 License

MIT License © 2025

---

<p align="center">
  <sub>Built with ❤️ as a university course project — Web Development Technology</sub>
</p>
