package com.orchard.service;

import com.orchard.common.BizException;
import com.orchard.entity.Order;
import com.orchard.entity.SettlementFlow;
import com.orchard.entity.Store;
import com.orchard.entity.Withdraw;
import com.orchard.repository.CommissionRuleRepository;
import com.orchard.repository.OrderRepository;
import com.orchard.repository.SettlementFlowRepository;
import com.orchard.repository.StoreRepository;
import com.orchard.repository.WithdrawRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/** 资金结算：抽成扣除、余额/冻结金、提现（PRD 2.2-4 资金流向） */
@Service
public class SettlementService {

    private final StoreRepository storeRepository;
    private final SettlementFlowRepository flowRepository;
    private final WithdrawRepository withdrawRepository;
    private final OrderRepository orderRepository;
    private final CommissionRuleRepository ruleRepository;

    public SettlementService(StoreRepository storeRepository, SettlementFlowRepository flowRepository,
                             WithdrawRepository withdrawRepository, OrderRepository orderRepository,
                             CommissionRuleRepository ruleRepository) {
        this.storeRepository = storeRepository;
        this.flowRepository = flowRepository;
        this.withdrawRepository = withdrawRepository;
        this.orderRepository = orderRepository;
        this.ruleRepository = ruleRepository;
    }

    /** 计算平台抽成：优先店铺专属比例，其次全局生效规则（比例/固定金额） */
    public java.math.BigDecimal computeCommission(Order o) {
        Store s = storeRepository.findById(o.getStoreId()).orElse(null);
        java.math.BigDecimal base = o.getTotalAmount();
        com.orchard.entity.CommissionRule rule = ruleRepository.findByBizTypeAndEffective("NORMAL", 1).orElse(null);
        if (s != null && s.getCommissionRate() != null) {
            return base.multiply(s.getCommissionRate()).divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        }
        if (rule == null) {
            return java.math.BigDecimal.ZERO;
        }
        if ("FIXED".equals(rule.getMode())) {
            return rule.getFixedAmount().min(o.getPayAmount());
        }
        return base.multiply(rule.getRatio()).divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
    }

    /** 订单完成结算：扣抽成 → 货款入可提现余额 */
    @Transactional
    public void settle(Order o) {
        Store s = storeRepository.findById(o.getStoreId())
                .orElseThrow(() -> new BizException(40004, "店铺不存在"));
        // 冻结金释放
        s.setFrozenAmount(s.getFrozenAmount().subtract(o.getPayAmount()).max(java.math.BigDecimal.ZERO));
        // 计算平台抽成
        java.math.BigDecimal commission = computeCommission(o);
        o.setCommissionAmount(commission);
        orderRepository.save(o);
        // 货款入账
        java.math.BigDecimal net = o.getPayAmount().subtract(commission);
        s.setBalance(s.getBalance().add(net));
        storeRepository.save(s);

        SettlementFlow in = new SettlementFlow();
        in.setStoreId(s.getId());
        in.setOrderId(o.getId());
        in.setType("ORDER_IN");
        in.setAmount(net);
        in.setBalanceAfter(s.getBalance());
        in.setRemark("订单货款入账（订单号 " + o.getOrderNo() + "）");
        flowRepository.save(in);

        if (commission.compareTo(java.math.BigDecimal.ZERO) > 0) {
            SettlementFlow c = new SettlementFlow();
            c.setStoreId(s.getId());
            c.setOrderId(o.getId());
            c.setType("COMMISSION");
            c.setAmount(commission);
            c.setBalanceAfter(s.getBalance());
            c.setRemark("平台抽成（订单号 " + o.getOrderNo() + "）");
            flowRepository.save(c);
        }
    }

    /** 商家资金页：卡片 + 流水 */
    public Map<String, Object> overview(Long uid, int page, int size) {
        Store s = storeRepository.findByOwnerId(uid)
                .orElseThrow(() -> new BizException(40004, "尚未开通店铺"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("balance", s.getBalance());
        m.put("frozenAmount", s.getFrozenAmount());
        m.put("deposit", s.getDeposit());
        Page<SettlementFlow> flows = flowRepository.findByStoreIdOrderByCreatedAtDesc(s.getId(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
        m.put("flows", flows);
        return m;
    }

    /** 申请提现：余额充足即可申请，平台线下打款 */
    @Transactional
    public Withdraw applyWithdraw(Long uid, java.math.BigDecimal amount) {
        Store s = storeRepository.findByOwnerId(uid)
                .orElseThrow(() -> new BizException(40004, "尚未开通店铺"));
        if (amount == null || amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BizException("提现金额必须大于 0");
        }
        if (amount.compareTo(s.getBalance()) > 0) {
            throw new BizException("提现金额超过可提现余额");
        }
        s.setBalance(s.getBalance().subtract(amount));
        storeRepository.save(s);
        Withdraw w = new Withdraw();
        w.setStoreId(s.getId());
        w.setAmount(amount);
        withdrawRepository.save(w);

        SettlementFlow f = new SettlementFlow();
        f.setStoreId(s.getId());
        f.setType("WITHDRAW");
        f.setAmount(amount);
        f.setBalanceAfter(s.getBalance());
        f.setRemark("提现申请（待平台打款）");
        flowRepository.save(f);
        return w;
    }
}
