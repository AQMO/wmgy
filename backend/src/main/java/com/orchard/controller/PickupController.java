package com.orchard.controller;

import com.orchard.annotation.RoleRequired;
import com.orchard.common.Result;
import com.orchard.entity.PickupAppointment;
import com.orchard.service.PickupService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/** 采摘/自提预约（二期） */
@RestController
public class PickupController {

    private final PickupService pickupService;

    public PickupController(PickupService pickupService) {
        this.pickupService = pickupService;
    }

    /** 消费者提交预约 */
    @PostMapping("/api/pickup")
    public Result<PickupAppointment> create(@RequestAttribute("uid") Long uid,
                                            @RequestBody Map<String, Object> body) {
        LocalDate date = body.get("pickDate") == null ? null
                : LocalDate.parse(String.valueOf(body.get("pickDate")));
        Integer qty = body.get("quantity") == null ? 1
                : Integer.parseInt(String.valueOf(body.get("quantity")));
        return Result.ok(pickupService.create(uid,
                Long.valueOf(String.valueOf(body.get("productId"))),
                date,
                (String) body.get("timeSlot"),
                qty,
                (String) body.get("phone"),
                (String) body.get("remark")));
    }

    @RoleRequired({"USER", "MERCHANT", "ADMIN"})
    @GetMapping("/api/pickup/mine")
    public Result<?> mine(@RequestAttribute("uid") Long uid,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size) {
        return Result.ok(pickupService.mine(uid, page, size));
    }

    @RoleRequired("MERCHANT")
    @GetMapping("/api/pickup/store")
    public Result<?> byStore(@RequestAttribute("uid") Long uid,
                             @RequestParam(required = false) String status,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size) {
        return Result.ok(pickupService.byStore(uid, status, page, size));
    }

    @RoleRequired("MERCHANT")
    @GetMapping("/api/pickup/stats")
    public Result<?> stats(@RequestAttribute("uid") Long uid) {
        return Result.ok(pickupService.stats(uid));
    }

    /** 商家处理：CONFIRM / REJECT / DONE */
    @RoleRequired("MERCHANT")
    @PutMapping("/api/pickup/{id}/handle")
    public Result<PickupAppointment> handle(@RequestAttribute("uid") Long uid, @PathVariable Long id,
                                            @RequestBody Map<String, String> body) {
        return Result.ok(pickupService.handle(uid, id, body.get("action"), body.get("storeReply")));
    }

    @RoleRequired({"USER", "MERCHANT", "ADMIN"})
    @PutMapping("/api/pickup/{id}/cancel")
    public Result<PickupAppointment> cancel(@RequestAttribute("uid") Long uid, @PathVariable Long id) {
        return Result.ok(pickupService.cancel(uid, id));
    }
}
