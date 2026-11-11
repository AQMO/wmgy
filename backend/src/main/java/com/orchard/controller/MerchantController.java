package com.orchard.controller;

import com.orchard.annotation.RoleRequired;
import com.orchard.common.Result;
import com.orchard.dto.ShipReq;
import com.orchard.dto.WithdrawReq;
import com.orchard.entity.Order;
import com.orchard.entity.Store;
import com.orchard.entity.Withdraw;
import com.orchard.service.OrderService;
import com.orchard.service.SettlementService;
import com.orchard.service.StoreService;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 商家端：订单处理、自助发货、资金结算 */
@RestController
@RequestMapping("/api/merchant")
@RoleRequired({"MERCHANT"})
public class MerchantController {

    private final OrderService orderService;
    private final SettlementService settlementService;
    private final StoreService storeService;

    public MerchantController(OrderService orderService, SettlementService settlementService, StoreService storeService) {
        this.orderService = orderService;
        this.settlementService = settlementService;
        this.storeService = storeService;
    }

    @GetMapping("/orders")
    public Result<Page<Order>> orders(@RequestAttribute("uid") Long uid,
                                      @RequestParam(required = false, defaultValue = "ALL") String status,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return Result.ok(orderService.merchantOrders(uid, status, page, size));
    }

    /** 自助发货（核心，PRD 4.5 发货弹窗） */
    @PutMapping("/orders/{id}/ship")
    public Result<Order> ship(@RequestAttribute("uid") Long uid, @PathVariable Long id,
                              @Validated @RequestBody ShipReq req) {
        return Result.ok(orderService.ship(uid, id, req.getExpressCompany(), req.getExpressNo(), req.getRemark()));
    }

    /** 修改物流（签收前） */
    @PutMapping("/orders/{id}/express")
    public Result<Order> updateExpress(@RequestAttribute("uid") Long uid, @PathVariable Long id,
                                       @Validated @RequestBody ShipReq req) {
        return Result.ok(orderService.updateExpress(uid, id, req.getExpressCompany(), req.getExpressNo()));
    }

    /** 工作台统计 */
    @GetMapping("/workbench")
    public Result<Map<String, Object>> workbench(@RequestAttribute("uid") Long uid) {
        Store s = storeService.myStore(uid);
        Page<Order> unshipped = orderService.merchantOrders(uid, "UNSHIPPED", 0, 1);
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("store", s);
        m.put("unshippedCount", unshipped.getTotalElements());
        m.put("balance", s.getBalance());
        m.put("frozenAmount", s.getFrozenAmount());
        return Result.ok(m);
    }

    /** 资金结算页 */
    @GetMapping("/settlement")
    public Result<Map<String, Object>> settlement(@RequestAttribute("uid") Long uid,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return Result.ok(settlementService.overview(uid, page, size));
    }

    /** 申请提现 */
    @PostMapping("/withdraw")
    public Result<Withdraw> withdraw(@RequestAttribute("uid") Long uid, @Validated @RequestBody WithdrawReq req) {
        return Result.ok(settlementService.applyWithdraw(uid, req.getAmount()));
    }
}
