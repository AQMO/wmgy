package com.orchard.controller;

import com.orchard.annotation.RoleRequired;
import com.orchard.common.Result;
import com.orchard.entity.Order;
import com.orchard.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 二期物流增强：电子面单发货、物流预警（超时未发货） */
@RestController
public class LogisticsController {

    private final OrderService orderService;

    public LogisticsController(OrderService orderService) {
        this.orderService = orderService;
    }

    @RoleRequired("MERCHANT")
    @PostMapping("/api/merchant/orders/{id}/ewb")
    public Result<Order> ewb(@RequestAttribute("uid") Long uid, @PathVariable Long id,
                             @RequestBody(required = false) Map<String, String> body) {
        String company = body == null ? null : body.get("expressCompany");
        return Result.ok(orderService.ewb(uid, id, company));
    }

    @RoleRequired("MERCHANT")
    @GetMapping("/api/merchant/orders/overdue")
    public Result<List<Order>> overdue(@RequestAttribute("uid") Long uid,
                                       @RequestParam(defaultValue = "24") int hours) {
        return Result.ok(orderService.overdueOrders(uid, hours));
    }

    @RoleRequired("ADMIN")
    @GetMapping("/api/admin/orders/overdue")
    public Result<List<Order>> overdueAll(@RequestParam(defaultValue = "24") int hours) {
        return Result.ok(orderService.overdueAll(hours));
    }
}
