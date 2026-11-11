# 万亩果园多商户果园小店平台（一期核心闭环 + 二期增强）

基于 PRD V1.2 实现的全栈项目：**Vue 3 + Spring Boot（Spring MVC + Hibernate/JPA）+ MySQL 8 + JWT 认证**，
并包含 **uni-app 微信小程序消费者端**、**WebSocket 实时聊天与消息推送**、**MinIO 文件存储（可切换）**、
**二期功能（采摘预约/优惠券营销/商品溯源/会员积分/电子面单/物流预警）**、**小店流量监控**、**平台聊天监控**。

## 项目结构

```
orchard-platform/
├── docs/                     # 概要设计文档、详细设计文档
├── sql/init.sql              # 数据库初始化脚本（建库）
├── backend/                  # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/orchard/
│       ├── common/           # Result/JWT/拦截器/全局异常
│       ├── config/           # Web配置、种子数据
│       ├── entity/           # 16 张表实体（Hibernate，含聊天/通知/流量日志）
│       ├── repository/       # Spring Data JPA
│       ├── service/          # 业务逻辑（认证/微信登录/店铺/商品/订单/结算/聊天/通知/管理）
│       └── controller/       # REST 接口（三端统一 /api）
├── frontend/                 # Vue 3 H5/PC 前端
│   └── src/
│       ├── views/consumer/   # 消费者端：首页/店铺/商品/购物车/下单/订单/聊天
│       ├── views/merchant/   # 商家端：入驻/工作台/装修/商品/发货/结算/买家聊天
│       ├── views/admin/      # 平台后台：大盘/商家审核/抽成/订单监控/聊天监控/流量监控
│       └── views/common/     # 消息中心（三端共用）
└── miniprogram/              # uni-app 微信小程序（消费者端）
    └── src/pages/            # 首页/店铺/商品/购物车/下单/订单/聊天/消息/我的/登录
```

## 快速启动

### 1. 数据库
```sql
-- 执行 sql/init.sql 或直接:
CREATE DATABASE orchard_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
表结构由 Hibernate `ddl-auto: update` 启动时自动创建。

### 2. 后端（JDK 8+，Maven 3.8+）
```bash
cd backend
# 先修改 src/main/resources/application.yml 中的 MySQL 密码
mvn spring-boot:run          # 启动于 http://localhost:8080
```
首启自动创建种子账号与演示数据。

### 3. H5/PC 前端（Node 16+）
```bash
cd frontend
npm install
npm run dev                  # 启动于 http://localhost:5173（/api 已代理到 8080）
```

### 4. 微信小程序端（uni-app）
```bash
cd miniprogram
npm install
npm run build:mp-weixin      # 产出 dist/build/mp-weixin
# 微信开发者工具导入 dist/build/mp-weixin 目录即可预览
```
- 后端 `application.yml` 的 `wx.appid/secret` 留空时，小程序登录走**开发模式（mock openid）**，无需正式 AppID 即可调试；填入后自动走真实 `wx.login` + jscode2session
- 开发者工具需勾选「不校验合法域名」（本地调试 localhost:8080）

#### 单果园定位（重要）
小程序是**单个果园的自营店铺**，不是平台商城：首页即果园主页（园名/产地/营业状态 + 本园商品流），不出现「附近店铺」「多店拆单」等平台元素，聊天是用户与**园主**直聊。
果园由 `utils/store.js` 的 `DEFAULT_STORE_ID`（默认 1）或扫码进入时的 `?storeId=` 参数确定，缓存于本地。
平台侧的多商户隔离、抽成、资金结算仍在后台与商家端生效，只是不在消费者界面暴露。

## 演示账号

| 角色 | 账号 | 密码 | 入口 |
|---|---|---|---|
| 平台管理员 | admin | admin123 | `/admin/dashboard` |
| 果园商家 | demo | merchant123 | `/merchant/workbench`（万亩一号果园） |
| 消费者 | guohr | user123 | `/shop/home` |

## 已实现功能（一期核心闭环）

- **用户认证**：注册/登录/JWT 鉴权/三角色权限（ADMIN/MERCHANT/USER），BCrypt 密码加密
- **多商户隔离**：商家仅可访问自有店铺的商品/订单/资金数据（Service 层强制过滤）
- **商家入驻**：提交资质 → 平台审核 → 缴保证金开通；支持冻结/解冻/关停、单店保证金差异化
- **商品体系**：店铺分类 CRUD、商品 CRUD、上下架、库存防超卖扣减
- **交易闭环**：购物车 → 多店铺自动拆单 → 模拟支付（预留微信/支付宝扩展）→ 状态机流转
- **自助发货**（PRD 2.3 核心规则）：仅待发货可发货、快递公司+单号必填、同店重复单号拦截、签收前可修改并留痕、一次发货限制
- **资金结算**：抽成规则版本化（比例/固定金额、运费基数开关、单店专属比例）、货款冻结→扣抽成→入余额→提现申请，全量流水可导出 CSV
- **平台管理**：数据大盘（GMV/抽成/店铺/订单）、商家审核、抽成规则配置、全局配置、全平台订单监控导出
- **微信小程序消费者端**：uni-app Vue3 实现，wx.login 登录（开发模式可无 AppID 调试）、浏览/下单/支付/订单/聊天/消息全套
- **聊天**：买家↔商家一对一图文消息，5 秒轮询拉取（WebSocket 为演进项），未读角标
- **消息推送**：支付/发货/收货/取消/入驻审核/结算等事件自动写入站内通知，三端消息中心 + 未读角标轮询
- **聊天监控**：平台后台可查看全平台会话记录，支持纠纷取证（PRD 5.4）
- **小店流量监控**：店铺/商品访问埋点，商家工作台今日 PV/UV，平台端全站 PV + 店铺流量 Top10 排行
- **留痕审计**：订单日志 + 全局操作日志永久留存

## 二期增强功能

- **文件存储（MinIO/本地双模式）**：`storage.type=minio` 走 MinIO 对象存储（endpoint/密钥/桶可配置，支持迁移变更），默认 `local` 本地盘模式零依赖即可用；所有文件统一登记 `t_file` 记录表，可按业务（PRODUCT/CHAT/TRACE/AVATAR）维护、删除；上传入口：商品封面、溯源凭证
  ```yaml
  # application.yml 示例（均可通过环境变量覆盖）
  storage:
    type: minio                # minio / local
    local-dir: ./uploads
    public-url:                # 文件外链前缀，留空走后端代理 /api/files/preview/**
    minio:
      endpoint: http://localhost:9000
      access-key: minioadmin
      secret-key: minioadmin
      bucket: orchard
  ```
- **WebSocket 实时通道**：`/ws/chat` 聊天、`/ws/notify` 站内通知，握手 `?token=` JWT 鉴权；聊天消息双端实时推送（发送经 WS 或 REST 均可），断线 5s 自动重连，离线由历史拉取兜底；通知事件（支付/发货/收货/审核/结算/预警）实时弹提醒
- **采摘预约**：商品页「预约到园采摘」→ 商家确认/拒绝/完成接待，全流程站内通知
- **优惠券营销**：商家发券（满减、限量、有效期、可停用）→ 店铺页领券 → 结算自动匹配店铺订单抵扣
- **商品溯源**：商家录入 种植/施肥/采摘/质检/包装 全链路记录（含图片凭证，走 MinIO 上传）；商品页公开查询 `/shop/trace/:id`
- **会员积分**：订单确认收货自动累计（1 元 = 1 分），「我的」页积分明细
- **电子面单（模拟）**：待发货订单一键生成运单号并完成发货，推送买家
- **物流预警**：已支付超 24 小时未发货自动预警——每小时定时任务推送商家通知 + 商家/平台后台预警列表页

## 已知限制

- 支付为模拟实现（小程序端可无缝替换为 `wx.requestPayment`，`OrderService.pay` 已预留扩展点）
- MinIO 未部署时默认走本地盘存储；文件预览经后端代理（未直连桶域名），生产建议配置 `public-url`
- 小程序未配置正式 AppID/合法域名前，真机预览需在开发者工具勾选「不校验合法域名」；小程序内 WS 地址 `utils/ws.js` 的 `wsBase()` 需按环境替换
- 电子面单为模拟运单号生成，对接快递 100/快递鸟等真实面单服务时替换 `OrderService.ewb` 实现即可
