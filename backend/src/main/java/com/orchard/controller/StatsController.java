package com.orchard.controller;

import com.orchard.common.JwtUtil;
import com.orchard.common.Result;
import com.orchard.entity.VisitLog;
import com.orchard.repository.StoreRepository;
import com.orchard.repository.VisitLogRepository;
import com.orchard.service.StoreService;
import com.orchard.entity.Store;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/** 小店流量统计：访问埋点 + 商家店铺流量 + 平台流量监控 */
@RestController
public class StatsController {

    private final VisitLogRepository visitLogRepository;
    private final StoreRepository storeRepository;
    private final StoreService storeService;
    private final JwtUtil jwtUtil;

    public StatsController(VisitLogRepository visitLogRepository, StoreRepository storeRepository,
                           StoreService storeService, JwtUtil jwtUtil) {
        this.visitLogRepository = visitLogRepository;
        this.storeRepository = storeRepository;
        this.storeService = storeService;
        this.jwtUtil = jwtUtil;
    }

    /** 访问埋点（公开，登录用户自动识别身份） */
    @PostMapping("/api/stats/visit")
    public Result<Void> visit(@RequestBody Map<String, Object> body,
                              @RequestHeader(value = "Authorization", required = false) String auth) {
        Long storeId = Long.valueOf(String.valueOf(body.get("storeId")));
        String page = String.valueOf(body.getOrDefault("page", "home"));
        VisitLog v = new VisitLog();
        v.setStoreId(storeId);
        v.setPage(page);
        // 尽力解析登录身份，未登录按匿名访客记录
        if (auth != null && auth.startsWith("Bearer ")) {
            try {
                v.setUserId(Long.valueOf(jwtUtil.parse(auth.substring(7)).getSubject()));
            } catch (Exception ignored) { }
        }
        visitLogRepository.save(v);
        return Result.ok();
    }

    /** 商家：我的店铺流量（今日/累计 PV、今日 UV、7 日趋势） */
    @GetMapping("/api/merchant/traffic")
    public Result<Map<String, Object>> merchantTraffic(@RequestAttribute("uid") Long uid) {
        Store s = storeService.myStore(uid);
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekAgo = LocalDate.now().minusDays(6).atStartOfDay();

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("todayPv", visitLogRepository.countByStoreIdAndCreatedAtAfter(s.getId(), todayStart));
        m.put("todayUv", visitLogRepository.countByStoreIdAndCreatedAtAfterAndUserIdNotNull(s.getId(), todayStart));
        m.put("totalPv", visitLogRepository.countByStoreId(s.getId()));

        List<Map<String, Object>> trend = new ArrayList<>();
        for (Object[] r : visitLogRepository.trendByStore(s.getId(), weekAgo)) {
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("date", String.valueOf(r[0]));
            t.put("pv", ((Number) r[1]).longValue());
            trend.add(t);
        }
        m.put("trend", trend);
        return Result.ok(m);
    }

    /** 平台：全站流量与店铺 PV 排行 */
    @GetMapping("/api/admin/traffic")
    public Result<Map<String, Object>> adminTraffic() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("todayPv", visitLogRepository.countByCreatedAtAfter(todayStart));
        m.put("totalPv", visitLogRepository.count());

        List<Map<String, Object>> top = new ArrayList<>();
        for (Object[] r : visitLogRepository.topStores()) {
            Long sid = ((Number) r[0]).longValue();
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("storeId", sid);
            t.put("storeName", storeRepository.findById(sid).map(Store::getName).orElse("店铺#" + sid));
            t.put("pv", ((Number) r[1]).longValue());
            top.add(t);
        }
        m.put("topStores", top);
        return Result.ok(m);
    }
}
