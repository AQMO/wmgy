package com.orchard.controller;

import com.orchard.annotation.RoleRequired;
import com.orchard.common.Result;
import com.orchard.entity.Order;
import com.orchard.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** 创建订单（多店铺自动拆单） */
    @PostMapping
    @RoleRequired({"USER"})
    public Result<List<Order>> create(@RequestAttribute("uid") Long uid, @RequestBody Map<String, Object> body) {
        return Result.ok(orderService.create(uid, body));
    }

    /** 买家订单列表 */
    @GetMapping
    @RoleRequired({"USER"})
    public Result<Page<Order>> list(@RequestAttribute("uid") Long uid,
                                    @RequestParam(required = false, defaultValue = "ALL") String status,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return Result.ok(orderService.buyerOrders(uid, status, page, size));
    }

    /** 订单详情（买家/店主/管理员，Service 内鉴权） */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@RequestAttribute("uid") Long uid,
                                              @RequestAttribute("role") String role,
                                              @PathVariable Long id) {
        return Result.ok(orderService.detail(uid, role, id));
    }

    /** 模拟支付（预留真实支付通道扩展） */
    @PutMapping("/{id}/pay")
    @RoleRequired({"USER"})
    public Result<Order> pay(@RequestAttribute("uid") Long uid, @PathVariable Long id) {
        return Result.ok(orderService.pay(uid, id));
    }

    /** 取消订单（仅待付款） */
    @PutMapping("/{id}/cancel")
    @RoleRequired({"USER"})
    public Result<Order> cancel(@RequestAttribute("uid") Long uid, @PathVariable Long id) {
        return Result.ok(orderService.cancel(uid, id));
    }

    /** 确认收货（触发结算） */
    @PutMapping("/{id}/confirm")
    @RoleRequired({"USER"})
    public Result<Order> confirm(@RequestAttribute("uid") Long uid, @PathVariable Long id) {
        return Result.ok(orderService.confirm(uid, id));
    }
}
