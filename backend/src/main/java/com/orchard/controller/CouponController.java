package com.orchard.controller;

import com.orchard.annotation.RoleRequired;
import com.orchard.common.Result;
import com.orchard.entity.Coupon;
import com.orchard.entity.UserCoupon;
import com.orchard.service.CouponService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/** 优惠券营销（二期）：商家发券、消费者领券用券 */
@RestController
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @RoleRequired("MERCHANT")
    @PostMapping("/api/merchant/coupons")
    public Result<Coupon> create(@RequestAttribute("uid") Long uid, @RequestBody Map<String, Object> body) {
        Coupon c = new Coupon();
        c.setName((String) body.get("name"));
        c.setAmount(new java.math.BigDecimal(String.valueOf(body.get("amount"))));
        if (body.get("threshold") != null && !String.valueOf(body.get("threshold")).isEmpty()) {
            c.setThreshold(new java.math.BigDecimal(String.valueOf(body.get("threshold"))));
        }
        if (body.get("total") != null && !String.valueOf(body.get("total")).isEmpty()) {
            c.setTotal(Integer.parseInt(String.valueOf(body.get("total"))));
        }
        if (body.get("startTime") != null && !String.valueOf(body.get("startTime")).isEmpty()) {
            c.setStartTime(LocalDateTime.parse(String.valueOf(body.get("startTime"))));
        }
        if (body.get("endTime") != null && !String.valueOf(body.get("endTime")).isEmpty()) {
            c.setEndTime(LocalDateTime.parse(String.valueOf(body.get("endTime"))));
        }
        return Result.ok(couponService.create(uid, c));
    }

    @RoleRequired("MERCHANT")
    @GetMapping("/api/merchant/coupons")
    public Result<?> merchantList(@RequestAttribute("uid") Long uid) {
        return Result.ok(couponService.merchantList(uid));
    }

    @RoleRequired("MERCHANT")
    @PutMapping("/api/merchant/coupons/{id}/status")
    public Result<Coupon> toggle(@RequestAttribute("uid") Long uid, @PathVariable Long id,
                                 @RequestBody Map<String, Integer> body) {
        return Result.ok(couponService.toggle(uid, id, body.get("status")));
    }

    /** 店铺可领券列表（公开给登录用户） */
    @RoleRequired({"USER", "MERCHANT", "ADMIN"})
    @GetMapping("/api/coupons/store/{storeId}")
    public Result<?> storeCoupons(@PathVariable Long storeId) {
        return Result.ok(couponService.storeCoupons(storeId));
    }

    @RoleRequired({"USER", "MERCHANT", "ADMIN"})
    @PostMapping("/api/coupons/{id}/claim")
    public Result<UserCoupon> claim(@RequestAttribute("uid") Long uid, @PathVariable Long id) {
        return Result.ok(couponService.claim(uid, id));
    }

    @RoleRequired({"USER", "MERCHANT", "ADMIN"})
    @GetMapping("/api/coupons/mine")
    public Result<?> mine(@RequestAttribute("uid") Long uid,
                          @RequestParam(required = false) String status) {
        return Result.ok(couponService.mine(uid, status));
    }
}
