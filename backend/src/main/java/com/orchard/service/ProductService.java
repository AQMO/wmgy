package com.orchard.service;

import com.orchard.common.BizException;
import com.orchard.dto.ProductReq;
import com.orchard.entity.Product;
import com.orchard.entity.Store;
import com.orchard.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final StoreService storeService;

    public ProductService(ProductRepository productRepository, StoreService storeService) {
        this.productRepository = productRepository;
        this.storeService = storeService;
    }

    /** 商家商品列表（仅自有店铺数据，PRD 数据隔离） */
    public Page<Product> myProducts(Long uid, String status, int page, int size) {
        Store s = storeService.myStore(uid);
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
            return productRepository.findByStoreIdAndStatus(s.getId(), status, pr);
        }
        return productRepository.findByStoreId(s.getId(), pr);
    }

    @Transactional
    public Product create(Long uid, ProductReq req) {
        Store s = storeService.myStore(uid);
        Product p = new Product();
        p.setStoreId(s.getId());
        applyReq(p, req);
        return productRepository.save(p);
    }

    @Transactional
    public Product update(Long uid, Long id, ProductReq req) {
        Product p = getOwned(uid, id);
        applyReq(p, req);
        return productRepository.save(p);
    }

    /** 上下架 */
    @Transactional
    public Product changeStatus(Long uid, Long id, String status) {
        if (!"ON_SHELF".equals(status) && !"OFF_SHELF".equals(status)) {
            throw new BizException("无效的商品状态");
        }
        Product p = getOwned(uid, id);
        p.setStatus(status);
        return productRepository.save(p);
    }

    @Transactional
    public void delete(Long uid, Long id) {
        productRepository.delete(getOwned(uid, id));
    }

    // ---------- C端公开查询 ----------

    /** 按店铺/分类/关键词浏览上架商品 */
    public Page<Product> publicList(Long storeId, Long categoryId, String keyword, int page, int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sales"));
        if (categoryId != null) {
            return productRepository.findByStoreIdAndCategoryIdAndStatus(storeId, categoryId, "ON_SHELF", pr);
        }
        if (keyword != null && !keyword.isEmpty()) {
            return productRepository.findByNameContainingAndStoreIdAndStatus(keyword, storeId, "ON_SHELF", pr);
        }
        return productRepository.findByStoreIdAndStatus(storeId, "ON_SHELF", pr);
    }

    public Product publicDetail(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new BizException(40004, "商品不存在"));
        if (!"ON_SHELF".equals(p.getStatus())) {
            throw new BizException(40004, "商品已下架");
        }
        return p;
    }

    // ---------- 权限校验：只能操作自有店铺商品 ----------
    private Product getOwned(Long uid, Long id) {
        Store s = storeService.myStore(uid);
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new BizException(40004, "商品不存在"));
        if (!p.getStoreId().equals(s.getId())) {
            throw new BizException(BizException.FORBIDDEN, "无权操作他人商品");
        }
        return p;
    }

    private void applyReq(Product p, ProductReq req) {
        p.setName(req.getName());
        p.setCategoryId(req.getCategoryId());
        p.setCover(req.getCover());
        p.setPrice(req.getPrice());
        p.setStock(req.getStock());
        p.setDetail(req.getDetail());
        String st = req.getStatus();
        p.setStatus("ON_SHELF".equals(st) ? "ON_SHELF" : ("OFF_SHELF".equals(st) ? "OFF_SHELF" : "DRAFT"));
    }
}
