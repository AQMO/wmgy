package com.orchard.controller;

import com.orchard.annotation.RoleRequired;
import com.orchard.common.Result;
import com.orchard.entity.CartItem;
import com.orchard.service.CartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RoleRequired({"USER"})
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestAttribute("uid") Long uid) {
        return Result.ok(cartService.list(uid));
    }

    @PostMapping
    public Result<CartItem> add(@RequestAttribute("uid") Long uid, @RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(String.valueOf(body.get("productId")));
        Integer quantity = Integer.valueOf(String.valueOf(body.getOrDefault("quantity", 1)));
        return Result.ok(cartService.add(uid, productId, quantity));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@RequestAttribute("uid") Long uid, @PathVariable Long id,
                               @RequestBody Map<String, Object> body) {
        Integer quantity = Integer.valueOf(String.valueOf(body.getOrDefault("quantity", 1)));
        cartService.updateQuantity(uid, id, quantity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@RequestAttribute("uid") Long uid, @PathVariable Long id) {
        cartService.remove(uid, id);
        return Result.ok();
    }
}
