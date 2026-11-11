package com.orchard.service;

import com.orchard.common.BizException;
import com.orchard.entity.Product;
import com.orchard.entity.Store;
import com.orchard.entity.TraceRecord;
import com.orchard.repository.ProductRepository;
import com.orchard.repository.StoreRepository;
import com.orchard.repository.TraceRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 商品溯源（二期）：商家录入全链路记录，消费者/公开渠道可查 */
@Service
public class TraceService {

    private final TraceRecordRepository traceRecordRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    public TraceService(TraceRecordRepository traceRecordRepository, ProductRepository productRepository,
                        StoreRepository storeRepository) {
        this.traceRecordRepository = traceRecordRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
    }

    /** 商家录入溯源记录 */
    @Transactional
    public TraceRecord add(Long merchantUid, TraceRecord r) {
        Store s = storeRepository.findByOwnerId(merchantUid)
                .orElseThrow(() -> new BizException(40004, "尚未开通店铺"));
        Product p = productRepository.findById(r.getProductId())
                .orElseThrow(() -> new BizException(40004, "商品不存在"));
        if (!p.getStoreId().equals(s.getId())) {
            throw new BizException(BizException.FORBIDDEN, "无权为他人店铺商品录入溯源");
        }
        if (r.getStage() == null || r.getStage().isEmpty()) throw new BizException("请选择溯源阶段");
        if (r.getTitle() == null || r.getTitle().isEmpty()) throw new BizException("请填写标题");
        r.setStoreId(s.getId());
        r.setOperatorId(merchantUid);
        return traceRecordRepository.save(r);
    }

    @Transactional
    public void delete(Long merchantUid, Long id) {
        Store s = storeRepository.findByOwnerId(merchantUid)
                .orElseThrow(() -> new BizException(40004, "尚未开通店铺"));
        TraceRecord r = traceRecordRepository.findById(id)
                .orElseThrow(() -> new BizException(40004, "记录不存在"));
        if (!s.getId().equals(r.getStoreId())) {
            throw new BizException(BizException.FORBIDDEN, "无权删除他人记录");
        }
        traceRecordRepository.delete(r);
    }

    /** 商家按商品查询 */
    public List<TraceRecord> byProductForMerchant(Long merchantUid, Long productId) {
        Store s = storeRepository.findByOwnerId(merchantUid)
                .orElseThrow(() -> new BizException(40004, "尚未开通店铺"));
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new BizException(40004, "商品不存在"));
        if (!p.getStoreId().equals(s.getId())) {
            throw new BizException(BizException.FORBIDDEN, "无权查看他人商品溯源");
        }
        return traceRecordRepository.findByProductIdOrderByEventTimeAscIdAsc(productId);
    }

    /** 公开溯源查询：商品信息 + 全链路记录（扫码/链接直达） */
    public Map<String, Object> publicTrace(Long productId) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new BizException(40004, "商品不存在"));
        Store s = storeRepository.findById(p.getStoreId()).orElse(null);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("productId", p.getId());
        m.put("productName", p.getName());
        m.put("cover", p.getCover());
        if (s != null) {
            m.put("storeName", s.getName());
        }
        List<Map<String, Object>> records = traceRecordRepository
                .findByProductIdOrderByEventTimeAscIdAsc(p.getId()).stream()
                .map(r -> {
                    Map<String, Object> rm = new LinkedHashMap<>();
                    rm.put("id", r.getId());
                    rm.put("stage", r.getStage());
                    rm.put("title", r.getTitle());
                    rm.put("content", r.getContent());
                    rm.put("images", r.getImages());
                    rm.put("eventTime", r.getEventTime() != null ? r.getEventTime() : r.getCreatedAt());
                    return rm;
                }).collect(Collectors.toList());
        m.put("records", records);
        return m;
    }
}
