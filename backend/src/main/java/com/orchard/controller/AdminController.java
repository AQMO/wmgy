package com.orchard.controller;

import com.orchard.annotation.RoleRequired;
import com.orchard.common.Result;
import com.orchard.entity.CommissionRule;
import com.orchard.entity.Order;
import com.orchard.entity.Store;
import com.orchard.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/** 平台 Web 管理后台（仅 ADMIN） */
@RestController
@RequestMapping("/api/admin")
@RoleRequired({"ADMIN"})
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /** 商家入驻管理列表 */
    @GetMapping("/stores")
    public Result<Page<Store>> stores(@RequestParam(required = false, defaultValue = "ALL") String status,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminService.stores(status, keyword, page, size));
    }

    /** 入驻审核：PASS/REJECT/FREEZE/UNFREEZE/CLOSE */
    @PutMapping("/stores/{id}/audit")
    public Result<Store> audit(@RequestAttribute("uid") Long uid, @PathVariable Long id,
                               @RequestBody Map<String, String> body) {
        return Result.ok(adminService.audit(uid, id, body.get("action")));
    }

    /** 单店保证金修改 */
    @PutMapping("/stores/{id}/deposit")
    public Result<Store> setDeposit(@RequestAttribute("uid") Long uid, @PathVariable Long id,
                                    @RequestBody Map<String, Object> body) {
        return Result.ok(adminService.setDeposit(uid, id, new BigDecimal(String.valueOf(body.get("deposit")))));
    }

    /** 查询当前生效抽成规则 */
    @GetMapping("/commission-rule")
    public Result<CommissionRule> currentRule() {
        return Result.ok(adminService.currentRule());
    }

    /** 更新抽成规则（生成新版本，仅对新订单生效） */
    @PutMapping("/commission-rule")
    public Result<CommissionRule> saveRule(@RequestAttribute("uid") Long uid, @RequestBody Map<String, Object> body) {
        return Result.ok(adminService.saveRule(uid, body));
    }

    @GetMapping("/config")
    public Result<Map<String, String>> configs() {
        return Result.ok(adminService.configs());
    }

    @PutMapping("/config")
    public Result<Void> setConfig(@RequestAttribute("uid") Long uid, @RequestBody Map<String, String> body) {
        body.forEach((k, v) -> adminService.setConfig(uid, k, v));
        return Result.ok();
    }

    /** 全平台订单监控 */
    @GetMapping("/orders")
    public Result<Page<Order>> orders(@RequestParam(required = false, defaultValue = "ALL") String status,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminService.orders(status, page, size));
    }

    /** 数据大盘 */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.ok(adminService.dashboard());
    }
}
