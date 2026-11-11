package com.orchard.service;

import com.orchard.common.BizException;
import com.orchard.entity.*;
import com.orchard.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** 订单核心服务：创建/支付/发货/确认收货/取消（PRD 2.3 订单发货物流规则） */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderLogRepository orderLogRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CartItemRepository cartItemRepository;
    private final CommissionRuleRepository ruleRepository;
    private final SettlementService settlementService;
    private final NotificationService notificationService;
    private final CouponService couponService;
    private final PointsService pointsService;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        OrderLogRepository orderLogRepository, ProductRepository productRepository,
                        StoreRepository storeRepository, CartItemRepository cartItemRepository,
                        CommissionRuleRepository ruleRepository, SettlementService settlementService,
                        NotificationService notificationService, CouponService couponService,
                        PointsService pointsService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderLogRepository = orderLogRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.cartItemRepository = cartItemRepository;
        this.ruleRepository = ruleRepository;
        this.settlementService = settlementService;
        this.notificationService = notificationService;
        this.couponService = couponService;
        this.pointsService = pointsService;
    }

    /** 创建订单：多店铺商品自动拆单；条件更新扣库存防超卖 */
    @Transactional
    public List<Order> create(Long uid, Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        String receiverName = (String) body.get("receiverName");
        String receiverPhone = (String) body.get("receiverPhone");
        String receiverAddress = (String) body.get("receiverAddress");
        String remark = (String) body.get("remark");

        if (items == null || items.isEmpty()) {
            throw new BizException("请选择商品");
        }
        // 按 productId 汇总数量
        Map<Long, Integer> qtyMap = new LinkedHashMap<>();        for (Map<String, Object> it : items) {
            Long pid = Long.valueOf(String.valueOf(it.get("productId")));
            int qty = Integer.parseInt(String.valueOf(it.getOrDefault("quantity", 1)));
            if (qty <= 0) throw new BizException("购买数量必须大于 0");
            qtyMap.merge(pid, qty, Integer::sum);
        }
        // 按店铺分组拆单
        Map<Long, List<Map.Entry<Long, Integer>>> byStore = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> e : qtyMap.entrySet()) {
            Product p = productRepository.findById(e.getKey())
                    .orElseThrow(() -> new BizException(40004, "商品不存在：" + e.getKey()));
            if (!"ON_SHELF".equals(p.getStatus())) {
                throw new BizException("商品「" + p.getName() + "」已下架");
            }
            byStore.computeIfAbsent(p.getStoreId(), k -> new ArrayList<>())
                    .add(new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue()));
        }

        List<Order> orders = new ArrayList<>();
        for (Map.Entry<Long, List<Map.Entry<Long, Integer>>> group : byStore.entrySet()) {
            Order o = new Order();
            o.setOrderNo("OR" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + String.format("%04d", new Random().nextInt(10000)));
            o.setUserId(uid);
            o.setStoreId(group.getKey());
            o.setReceiverName(receiverName);
            o.setReceiverPhone(receiverPhone);
            o.setReceiverAddress(receiverAddress);
            o.setRemark(remark);
            BigDecimal total = BigDecimal.ZERO;
            for (Map.Entry<Long, Integer> e : group.getValue()) {
                // 条件更新：库存足够且上架才扣减
                int updated = productRepository.deductStock(e.getKey(), e.getValue());
                if (updated == 0) {
                    throw new BizException("商品库存不足，下单失败");
                }
                Product p = productRepository.findById(e.getKey()).orElseThrow(() -> new BizException(40004, "商品不存在"));
                OrderItem oi = new OrderItem();
                oi.setOrder(o);
                oi.setProductId(p.getId());
                oi.setProductName(p.getName());
                oi.setProductCover(p.getCover());
                oi.setPrice(p.getPrice());
                oi.setQuantity(e.getValue());
                o.getItems().add(oi);
                total = total.add(p.getPrice().multiply(BigDecimal.valueOf(e.getValue())));
            }
            o.setTotalAmount(total);
            o.setFreight(BigDecimal.ZERO);   // 一期免运费，预留字段
            o.setPayAmount(total.add(o.getFreight()));
            // 抽成规则快照：优先店铺专属比例，其次全局生效规则
            Store store = storeRepository.findById(group.getKey()).orElseThrow(() -> new BizException(40004, "店铺不存在"));
            CommissionRule rule = ruleRepository.findByBizTypeAndEffective("NORMAL", 1).orElse(null);
            if (store.getCommissionRate() != null) {
                o.setCommissionMode("RATIO");
                o.setCommissionValue(store.getCommissionRate());
            } else if (rule != null) {
                o.setCommissionMode(rule.getMode());
                o.setCommissionValue("RATIO".equals(rule.getMode()) ? rule.getRatio() : rule.getFixedAmount());
            }
            Order saved = orderRepository.save(o);
            log(saved, "CREATE", "创建订单，金额 " + saved.getPayAmount(), uid);
            orders.add(saved);
        }
        // 清空购物车中对应商品
        cartItemRepository.deleteByUserIdAndProductIdIn(uid, new ArrayList<>(qtyMap.keySet()));
        // 二期营销：优惠券抵扣（一张券只抵扣其所属店铺的订单）
        applyCouponIfAny(uid, body, orders);
        return orders;
    }

    /** 下单用券：定位券所属店铺的订单并抵扣（PRD 二期营销） */
    private void applyCouponIfAny(Long uid, Map<String, Object> body, List<Order> orders) {
        Object c = body.get("userCouponId");
        if (c == null || String.valueOf(c).isEmpty()) return;
        Long userCouponId = Long.valueOf(String.valueOf(c));
        Long couponStoreId = couponService.storeOf(userCouponId);
        Order target = null;
        for (Order o : orders) {
            if (o.getStoreId().equals(couponStoreId)) {
                target = o;
                break;
            }
        }
        if (target == null) {
            throw new BizException("该优惠券仅限对应店铺使用");
        }
        BigDecimal deduction = couponService.applyToOrder(uid, userCouponId, target.getStoreId(),
                target.getTotalAmount(), target.getId());
        target.setCouponDeduction(deduction);
        target.setPayAmount(target.getTotalAmount().add(target.getFreight()).subtract(deduction).max(BigDecimal.ZERO));
        target.setUserCouponId(userCouponId);
        orderRepository.save(target);
    }

    /** 模拟支付回调（预留真实微信/支付宝扩展） */
    @Transactional
    public Order pay(Long uid, Long orderId) {
        Order o = getBuyerOrder(uid, orderId);
        if (!"UNPAID".equals(o.getStatus())) {
            throw new BizException("订单状态不支持支付");
        }
        o.setStatus("UNSHIPPED");
        o.setPaidAt(LocalDateTime.now());
        orderRepository.save(o);
        // 货款进入平台冻结期（PRD 2.2-4 资金流向）
        Store s = storeRepository.findById(o.getStoreId()).orElseThrow(() -> new BizException(40004, "店铺不存在"));
        s.setFrozenAmount(s.getFrozenAmount().add(o.getPayAmount()));
        storeRepository.save(s);
        log(o, "PAY", "支付成功，金额 " + o.getPayAmount(), uid);
        notificationService.push(s.getOwnerId(), "新订单待发货",
                "订单 " + o.getOrderNo() + " 已支付，请及时发货", "ORDER", o.getId());
        return o;
    }

    /** 自助发货：仅待发货状态可发货；同商家重复单号拦截（PRD 2.3-1/3/6） */
    @Transactional
    public Order ship(Long merchantUid, Long orderId, String expressCompany, String expressNo, String remark) {
        Store s = storeRepository.findByOwnerId(merchantUid)
                .orElseThrow(() -> new BizException(40004, "尚未开通店铺"));
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new BizException(40004, "订单不存在"));
        if (!o.getStoreId().equals(s.getId())) {
            throw new BizException(BizException.FORBIDDEN, "无权操作他人订单");
        }
        if (!"UNSHIPPED".equals(o.getStatus())) {
            throw new BizException("仅待发货订单支持发货");
        }
        if (o.getExpressNo() != null) {
            throw new BizException("该订单已发货，禁止重复录入物流信息");
        }
        if (orderRepository.existsByStoreIdAndExpressNo(s.getId(), expressNo)) {
            throw new BizException("该快递单号已存在，禁止重复录入");
        }
        o.setExpressCompany(expressCompany);
        o.setExpressNo(expressNo);
        o.setStatus("SHIPPED");
        o.setShippedAt(LocalDateTime.now());
        orderRepository.save(o);
        log(o, "SHIP", "发货：" + expressCompany + " " + expressNo + (remark == null ? "" : "（备注：" + remark + "）"), merchantUid);
        notificationService.push(o.getUserId(), "您的订单已发货",
                "订单 " + o.getOrderNo() + " 已由 " + expressCompany + " 揽收，单号 " + expressNo, "SHIP", o.getId());
        return o;
    }

    /** 修改物流：仅未签收（已发货）订单允许，修改留痕（PRD 2.3-4） */
    @Transactional
    public Order updateExpress(Long merchantUid, Long orderId, String expressCompany, String expressNo) {
        Store s = storeRepository.findByOwnerId(merchantUid)
                .orElseThrow(() -> new BizException(40004, "尚未开通店铺"));
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new BizException(40004, "订单不存在"));
        if (!o.getStoreId().equals(s.getId())) {
            throw new BizException(BizException.FORBIDDEN, "无权操作他人订单");
        }
        if (!"SHIPPED".equals(o.getStatus())) {
            throw new BizException("订单已签收/完成，禁止修改物流信息");
        }
        if (!expressNo.equals(o.getExpressNo())
                && orderRepository.existsByStoreIdAndExpressNo(s.getId(), expressNo)) {
            throw new BizException("该快递单号已存在，禁止重复录入");
        }
        String before = o.getExpressCompany() + " " + o.getExpressNo();
        o.setExpressCompany(expressCompany);
        o.setExpressNo(expressNo);
        orderRepository.save(o);
        log(o, "EXPRESS_CHANGE", "物流修改：" + before + " → " + expressCompany + " " + expressNo, merchantUid);
        return o;
    }

    /** 买家确认收货 → 触发结算 */
    @Transactional
    public Order confirm(Long uid, Long orderId) {
        Order o = getBuyerOrder(uid, orderId);
        if (!"SHIPPED".equals(o.getStatus())) {
            throw new BizException("订单状态不支持确认收货");
        }
        o.setStatus("COMPLETED");
        o.setCompletedAt(LocalDateTime.now());
        orderRepository.save(o);
        log(o, "CONFIRM", "买家确认收货", uid);
        settlementService.settle(o);
        // 二期会员积分：确认收货自动累计
        pointsService.earn(uid, o.getId(), o.getPayAmount());
        Store cs = storeRepository.findById(o.getStoreId()).orElse(null);
        if (cs != null) {
            notificationService.push(cs.getOwnerId(), "订单已完成结算",
                    "订单 " + o.getOrderNo() + " 买家已确认收货，货款已扣除抽成入账", "SETTLE", o.getId());
        }
        return o;
    }

    /** 取消订单：仅待付款；恢复库存 */
    @Transactional
    public Order cancel(Long uid, Long orderId) {
        Order o = getBuyerOrder(uid, orderId);
        if (!"UNPAID".equals(o.getStatus())) {
            throw new BizException("仅待付款订单可取消");
        }
        o.setStatus("CANCELLED");
        orderRepository.save(o);
        for (OrderItem oi : o.getItems()) {
            productRepository.restoreStock(oi.getProductId(), oi.getQuantity());
        }
        log(o, "CANCEL", "买家取消订单", uid);
        Store cs2 = storeRepository.findById(o.getStoreId()).orElse(null);
        if (cs2 != null) {
            notificationService.push(cs2.getOwnerId(), "订单已取消",
                    "订单 " + o.getOrderNo() + " 买家已取消（未支付）", "ORDER", o.getId());
        }
        return o;
    }

    /** 电子面单（二期模拟）：自动生成运单号并完成发货动作 */
    @Transactional
    public Order ewb(Long merchantUid, Long orderId, String expressCompany) {
        Store s = storeRepository.findByOwnerId(merchantUid)
                .orElseThrow(() -> new BizException(40004, "尚未开通店铺"));
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new BizException(40004, "订单不存在"));
        if (!o.getStoreId().equals(s.getId())) {
            throw new BizException(BizException.FORBIDDEN, "无权操作他人订单");
        }
        if (!"UNSHIPPED".equals(o.getStatus())) {
            throw new BizException("仅待发货订单支持电子面单发货");
        }
        String no = "EWB" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", new Random().nextInt(1000));
        o.setExpressCompany(expressCompany == null || expressCompany.isEmpty() ? "万亩果园电子面单" : expressCompany);
        o.setExpressNo(no);
        o.setStatus("SHIPPED");
        o.setShippedAt(LocalDateTime.now());
        orderRepository.save(o);
        log(o, "EWB_SHIP", "电子面单发货：" + o.getExpressCompany() + " " + no, merchantUid);
        notificationService.push(o.getUserId(), "您的订单已发货",
                "订单 " + o.getOrderNo() + " 已通过电子面单发货，运单号 " + no, "SHIP", o.getId());
        return o;
    }

    /** 物流预警：已支付超时未发货（默认 24 小时） */
    public List<Order> overdueOrders(Long merchantUid, int hours) {
        Store s = storeRepository.findByOwnerId(merchantUid)
                .orElseThrow(() -> new BizException(40004, "尚未开通店铺"));
        LocalDateTime deadline = LocalDateTime.now().minusHours(hours);
        List<Order> all = orderRepository.findByStatusAndPaidAtBefore("UNSHIPPED", deadline);
        List<Order> mine = new ArrayList<>();
        for (Order o : all) {
            if (o.getStoreId().equals(s.getId())) {
                mine.add(o);
            }
        }
        return mine;
    }

    /** 平台物流预警：全平台超时未发货 */
    public List<Order> overdueAll(int hours) {
        return orderRepository.findByStatusAndPaidAtBefore("UNSHIPPED", LocalDateTime.now().minusHours(hours));
    }

    // ---------- 查询 ----------

    public Page<Order> buyerOrders(Long uid, String status, int page, int size) {
        if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
            return orderRepository.findByUserIdAndStatus(uid, status, org.springframework.data.domain.PageRequest.of(page, size,
                    org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id")));
        }
        return orderRepository.findByUserId(uid, org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id")));
    }

    public Page<Order> merchantOrders(Long uid, String status, int page, int size) {
        Store s = storeRepository.findByOwnerId(uid)
                .orElseThrow(() -> new BizException(40004, "尚未开通店铺"));
        org.springframework.data.domain.PageRequest pr = org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
        if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
            return orderRepository.findByStoreIdAndStatus(s.getId(), status, pr);
        }
        return orderRepository.findByStoreId(s.getId(), pr);
    }

    public Map<String, Object> detail(Long uid, String role, Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new BizException(40004, "订单不存在"));
        boolean buyer = o.getUserId().equals(uid);
        boolean admin = "ADMIN".equals(role);
        boolean owner = "MERCHANT".equals(role)
                && storeRepository.findByOwnerId(uid).map(s -> s.getId().equals(o.getStoreId())).orElse(false);
        if (!buyer && !admin && !owner) {
            throw new BizException(BizException.FORBIDDEN, "无权查看该订单");
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("order", o);
        m.put("items", o.getItems());
        m.put("logs", orderLogRepository.findByOrderIdOrderByCreatedAtAsc(orderId));
        storeRepository.findById(o.getStoreId()).ifPresent(s -> m.put("storeName", s.getName()));
        return m;
    }

    private Order getBuyerOrder(Long uid, Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new BizException(40004, "订单不存在"));
        if (!o.getUserId().equals(uid)) {
            throw new BizException(BizException.FORBIDDEN, "无权操作该订单");
        }
        return o;
    }

    private void log(Order o, String action, String detail, Long operator) {
        OrderLog l = new OrderLog();
        l.setOrderId(o.getId());
        l.setAction(action);
        l.setDetail(detail);
        l.setOperator(operator);
        orderLogRepository.save(l);
    }

}
