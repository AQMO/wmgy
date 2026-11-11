package com.orchard.controller;

import com.orchard.annotation.RoleRequired;
import com.orchard.common.Result;
import com.orchard.dto.ProductReq;
import com.orchard.entity.Product;
import com.orchard.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ---------- C端公开接口 ----------

    /** 按店铺/分类/关键词浏览上架商品 */
    @GetMapping("/api/products")
    public Result<Page<Product>> list(@RequestParam Long storeId,
                                      @RequestParam(required = false) Long categoryId,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "12") int size) {
        return Result.ok(productService.publicList(storeId, categoryId, keyword, page, size));
    }

    @GetMapping("/api/products/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.ok(productService.publicDetail(id));
    }

    // ---------- 商家端 ----------

    @GetMapping("/api/merchant/products")
    @RoleRequired({"MERCHANT"})
    public Result<Page<Product>> myProducts(@RequestAttribute("uid") Long uid,
                                            @RequestParam(required = false, defaultValue = "ALL") String status,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(productService.myProducts(uid, status, page, size));
    }

    @PostMapping("/api/merchant/products")
    @RoleRequired({"MERCHANT"})
    public Result<Product> create(@RequestAttribute("uid") Long uid, @Validated @RequestBody ProductReq req) {
        return Result.ok(productService.create(uid, req));
    }

    @PutMapping("/api/merchant/products/{id}")
    @RoleRequired({"MERCHANT"})
    public Result<Product> update(@RequestAttribute("uid") Long uid, @PathVariable Long id,
                                  @Validated @RequestBody ProductReq req) {
        return Result.ok(productService.update(uid, id, req));
    }

    @PutMapping("/api/merchant/products/{id}/status")
    @RoleRequired({"MERCHANT"})
    public Result<Product> changeStatus(@RequestAttribute("uid") Long uid, @PathVariable Long id,
                                        @RequestBody Map<String, String> body) {
        return Result.ok(productService.changeStatus(uid, id, body.get("status")));
    }

    @DeleteMapping("/api/merchant/products/{id}")
    @RoleRequired({"MERCHANT"})
    public Result<Void> delete(@RequestAttribute("uid") Long uid, @PathVariable Long id) {
        productService.delete(uid, id);
        return Result.ok();
    }
}
