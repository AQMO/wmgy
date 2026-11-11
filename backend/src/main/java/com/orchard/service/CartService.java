package com.orchard.service;

import com.orchard.common.BizException;
import com.orchard.entity.CartItem;
import com.orchard.entity.Product;
import com.orchard.repository.CartItemRepository;
import com.orchard.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    /** 购物车列表（带商品/店铺信息） */
    public List<Map<String, Object>> list(Long uid) {
        return cartItemRepository.findByUserIdOrderByCreatedAtDesc(uid).stream()
                .map(ci -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", ci.getId());
                    m.put("productId", ci.getProductId());
                    m.put("quantity", ci.getQuantity());
                    productRepository.findById(ci.getProductId()).ifPresent(p -> {
                        m.put("name", p.getName());
                        m.put("cover", p.getCover());
                        m.put("price", p.getPrice());
                        m.put("stock", p.getStock());
                        m.put("status", p.getStatus());
                        m.put("storeId", p.getStoreId());
                    });
                    return m;
                }).collect(Collectors.toList());
    }

    @Transactional
    public CartItem add(Long uid, Long productId, Integer quantity) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new BizException(40004, "商品不存在"));
        if (!"ON_SHELF".equals(p.getStatus())) {
            throw new BizException("商品已下架");
        }
        if (p.getStock() < quantity) {
            throw new BizException("库存不足");
        }
        CartItem ci = cartItemRepository.findByUserIdAndProductId(uid, productId).orElse(null);
        if (ci == null) {
            ci = new CartItem();
            ci.setUserId(uid);
            ci.setProductId(productId);
            ci.setQuantity(quantity);
        } else {
            ci.setQuantity(ci.getQuantity() + quantity);
            if (ci.getQuantity() > p.getStock()) {
                throw new BizException("超过库存数量");
            }
        }
        return cartItemRepository.save(ci);
    }

    @Transactional
    public void updateQuantity(Long uid, Long id, Integer quantity) {
        CartItem ci = cartItemRepository.findById(id)
                .orElseThrow(() -> new BizException(40004, "购物车项不存在"));
        if (!ci.getUserId().equals(uid)) {
            throw new BizException(BizException.FORBIDDEN, "无权操作");
        }
        if (quantity <= 0) {
            cartItemRepository.delete(ci);
            return;
        }
        ci.setQuantity(quantity);
        cartItemRepository.save(ci);
    }

    @Transactional
    public void remove(Long uid, Long id) {
        cartItemRepository.deleteByUserIdAndId(uid, id);
    }
}
