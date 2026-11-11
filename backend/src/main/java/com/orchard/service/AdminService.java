package com.orchard.service;

import com.orchard.common.BizException;
import com.orchard.entity.*;
import com.orchard.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/** 平台管理后台服务 */
@Service
public class AdminService {

    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final CommissionRuleRepository ruleRepository;
    private final ConfigRepository configRepository;
    private final OperationLogRepository opLogRepository;
    private final WithdrawRepository withdrawRepository;
    private final StoreService storeService;
    private final NotificationService notificationService;

    public AdminService(StoreRepository storeRepository, OrderRepository orderRepository,
                        CommissionRuleRepository ruleRepository, ConfigRepository configRepository,
                        OperationLogRepository opLogRepository, WithdrawRepository withdrawRepository,
                        StoreService storeService, NotificationService notificationService) {
        this.storeRepository = storeRepository;
        this.orderRepository = orderRepository;
        this.ruleRepository = ruleRepository;
        this.configRepository = configRepository;
        this.opLogRepository = opLogRepository;
        this.withdrawRepository = withdrawRepository;
        this.storeService = storeService;
        this.notificationService = notificationService;
    }

    /** 商家列表（按状态筛选） */
    public Page<Store> stores(String status, String keyword, int page, int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
            if (keyword != null && !keyword.isEmpty()) {
                return storeRepository.findByNameContainingAndStatus(keyword, status, pr);
            }
            return storeRepository.findByStatus(status, pr);
        }
        return storeRepository.findAll(pr);
    }

    /** 入驻审核：PASS 开通并缴保证金 / REJECT 驳回 / FREEZE 冻结 / CLOSE 关停 */
    @Transactional
    public Store audit(Long adminId, Long storeId, String action) {
        Store s = storeRepository.findById(storeId)
                .orElseThrow(() -> new BizException(40004, "店铺不存在"));
        switch (action) {
            case "PASS":
                if (!"PENDING".equals(s.getStatus())) {
                    throw new BizException("仅待审核店铺可通过");
                }
                storeService.activateWithDeposit(s);
                break;
            case "REJECT":
                s.setStatus("CLOSED");
                storeRepository.save(s);
                break;
            case "FREEZE":
                s.setStatus("FROZEN");
                storeRepository.save(s);
                break;
            case "UNFREEZE":
            case "OPEN":
                s.setStatus("ACTIVE");
                storeRepository.save(s);
                break;
            case "CLOSE":
                s.setStatus("CLOSED");
                storeRepository.save(s);
                break;
            default:
                throw new BizException("无效的审核操作");
        }
        log(adminId, "STORE_AUDIT", "店铺[" + s.getName() + "]审核操作：" + action);
        String title = "PASS".equals(action) ? "入驻审核通过" : ("REJECT".equals(action) ? "入驻申请被驳回" : ("FREEZE".equals(action) ? "店铺已被冻结" : "店铺状态更新"));
        notificationService.push(s.getOwnerId(), title, "您的果园店铺「" + s.getName() + "」状态变更为 " + s.getStatus(), "AUDIT", s.getId());
        return s;
    }

    /** 单店保证金修改（PRD 2.1-2 差异化管控） */
    @Transactional
    public Store setDeposit(Long adminId, Long storeId, java.math.BigDecimal deposit) {
        Store s = storeRepository.findById(storeId)
                .orElseThrow(() -> new BizException(40004, "店铺不存在"));
        s.setDeposit(deposit);
        storeRepository.save(s);
        log(adminId, "DEPOSIT_CHANGE", "店铺[" + s.getName() + "]保证金调整为 " + deposit);
        return s;
    }

    /** 当前生效抽成规则 */
    public CommissionRule currentRule() {
        return ruleRepository.findByBizTypeAndEffective("NORMAL", 1).orElse(null);
    }

    /** 更新抽成规则：旧版本失效，生成新版本（仅对新订单生效，PRD 5.2） */
    @Transactional
    public CommissionRule saveRule(Long adminId, Map<String, Object> body) {
        String mode = (String) body.getOrDefault("mode", "RATIO");
        if (!"RATIO".equals(mode) && !"FIXED".equals(mode)) {
            throw new BizException("抽成模式无效");
        }
        // 旧版本失效
        ruleRepository.findByBizTypeAndEffective("NORMAL", 1).ifPresent(old -> {
            old.setEffective(0);
            ruleRepository.save(old);
        });
        CommissionRule r = new CommissionRule();
        r.setMode(mode);
        r.setBizType("NORMAL");
        r.setIncludeFreight(Boolean.TRUE.equals(body.get("includeFreight")));
        if ("RATIO".equals(mode)) {
            java.math.BigDecimal ratio = new java.math.BigDecimal(String.valueOf(body.get("ratio")));
            if (ratio.compareTo(java.math.BigDecimal.ZERO) < 0 || ratio.compareTo(java.math.BigDecimal.valueOf(100)) > 0) {
                throw new BizException("抽成比例需在 0-100 之间");
            }
            r.setRatio(ratio);
        } else {
            r.setFixedAmount(new java.math.BigDecimal(String.valueOf(body.get("fixedAmount"))));
        }
        r = ruleRepository.save(r);
        log(adminId, "RULE_CHANGE", "抽成规则更新：" + mode + " " + body);
        return r;
    }

    /** 全局配置读写（默认保证金等） */
    public Map<String, String> configs() {
        Map<String, String> m = new LinkedHashMap<>();
        configRepository.findAll().forEach(c -> m.put(c.getConfigKey(), c.getConfigValue()));
        return m;
    }

    @Transactional
    public void setConfig(Long adminId, String key, String value) {
        Config c = configRepository.findById(key).orElseGet(Config::new);
        c.setConfigKey(key);
        c.setConfigValue(value);
        configRepository.save(c);
        log(adminId, "CONFIG_CHANGE", "配置 " + key + " = " + value);
    }

    /** 全平台订单监控 */
    public Page<Order> orders(String status, int page, int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
            return orderRepository.findByStatus(status, pr);
        }
        return orderRepository.findAll(pr);
    }

    /** 数据大盘（PRD 5.5） */
    public Map<String, Object> dashboard() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("storeCount", storeRepository.count());
        m.put("activeStoreCount", storeRepository.findByStatus("ACTIVE", PageRequest.of(0, 1)).getTotalElements());
        m.put("orderCount", orderRepository.count());
        m.put("unshippedCount", orderRepository.countByStatus("UNSHIPPED"));
        m.put("gmv", orderRepository.sumGmv());
        m.put("commissionIncome", orderRepository.sumCommission());
        m.put("pendingWithdraw", withdrawRepository.count());
        return m;
    }

    private void log(Long operator, String action, String detail) {
        OperationLog l = new OperationLog();
        l.setOperatorId(operator);
        l.setModule("ADMIN");
        l.setAction(action);
        l.setDetail(detail);
        opLogRepository.save(l);
    }
}
