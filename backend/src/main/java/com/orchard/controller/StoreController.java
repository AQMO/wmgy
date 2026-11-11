package com.orchard.controller;

import com.orchard.annotation.RoleRequired;
import com.orchard.common.Result;
import com.orchard.dto.StoreApplyReq;
import com.orchard.entity.Category;
import com.orchard.entity.Store;
import com.orchard.service.StoreService;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    // ---------- C端公开接口 ----------

    /** 平台聚合首页：店铺列表（搜索） */
    @GetMapping("/api/stores")
    public Result<Page<Store>> list(@RequestParam(required = false) String keyword,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return Result.ok(storeService.listActive(keyword, page, size));
    }

    /** 果园独立店铺首页详情 */
    @GetMapping("/api/stores/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.ok(storeService.detail(id));
    }

    // ---------- 商家端 ----------

    /** 入驻申请（消费者/商家均可提交） */
    @PostMapping("/api/merchant/store/apply")
    @RoleRequired({"USER", "MERCHANT"})
    public Result<Store> apply(@RequestAttribute("uid") Long uid, @Validated @RequestBody StoreApplyReq req) {
        return Result.ok(storeService.apply(uid, req));
    }

    /** 我的店铺 */
    @GetMapping("/api/merchant/store")
    @RoleRequired({"MERCHANT"})
    public Result<Store> myStore(@RequestAttribute("uid") Long uid) {
        return Result.ok(storeService.myStore(uid));
    }

    /** 店铺装修保存 */
    @PutMapping("/api/merchant/store")
    @RoleRequired({"MERCHANT"})
    public Result<Store> update(@RequestAttribute("uid") Long uid, @RequestBody Map<String, Object> body) {
        return Result.ok(storeService.updateStore(uid, body));
    }

    /** 我的分类列表 */
    @GetMapping("/api/merchant/categories")
    @RoleRequired({"MERCHANT"})
    public Result<List<Category>> myCategories(@RequestAttribute("uid") Long uid) {
        return Result.ok(storeService.myCategories(uid));
    }

    /** 新增分类 */
    @PostMapping("/api/merchant/categories")
    @RoleRequired({"MERCHANT"})
    public Result<Category> addCategory(@RequestAttribute("uid") Long uid, @RequestBody Map<String, Object> body) {
        return Result.ok(storeService.addCategory(uid, body));
    }

    /** 删除分类 */
    @DeleteMapping("/api/merchant/categories/{id}")
    @RoleRequired({"MERCHANT"})
    public Result<Void> deleteCategory(@RequestAttribute("uid") Long uid, @PathVariable Long id) {
        storeService.deleteCategory(uid, id);
        return Result.ok();
    }
}
