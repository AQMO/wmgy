package com.orchard.controller;

import com.orchard.annotation.RoleRequired;
import com.orchard.common.Result;
import com.orchard.entity.TraceRecord;
import com.orchard.service.TraceService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/** 商品溯源（二期）：商家录入维护，公开查询 */
@RestController
public class TraceController {

    private final TraceService traceService;

    public TraceController(TraceService traceService) {
        this.traceService = traceService;
    }

    @RoleRequired("MERCHANT")
    @PostMapping("/api/merchant/traces")
    public Result<TraceRecord> add(@RequestAttribute("uid") Long uid, @RequestBody Map<String, Object> body) {
        TraceRecord r = new TraceRecord();
        r.setProductId(Long.valueOf(String.valueOf(body.get("productId"))));
        r.setStage((String) body.get("stage"));
        r.setTitle((String) body.get("title"));
        r.setContent((String) body.get("content"));
        Object images = body.get("images");
        r.setImages(images instanceof java.util.List
                ? String.join(",", (java.util.List<String>) images)
                : (String) images);
        if (body.get("eventTime") != null && !String.valueOf(body.get("eventTime")).isEmpty()) {
            r.setEventTime(LocalDateTime.parse(String.valueOf(body.get("eventTime"))));
        }
        return Result.ok(traceService.add(uid, r));
    }

    @RoleRequired("MERCHANT")
    @DeleteMapping("/api/merchant/traces/{id}")
    public Result<?> delete(@RequestAttribute("uid") Long uid, @PathVariable Long id) {
        traceService.delete(uid, id);
        return Result.ok(null);
    }

    @RoleRequired("MERCHANT")
    @GetMapping("/api/merchant/traces")
    public Result<?> byProduct(@RequestAttribute("uid") Long uid,
                               @RequestParam Long productId) {
        return Result.ok(traceService.byProductForMerchant(uid, productId));
    }

    /** 公开溯源查询（消费者/扫码），无需登录 */
    @GetMapping("/api/trace/product/{productId}")
    public Result<?> publicTrace(@PathVariable Long productId) {
        return Result.ok(traceService.publicTrace(productId));
    }
}
