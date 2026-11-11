package com.orchard.service;

import com.orchard.entity.PointsLog;
import com.orchard.entity.User;
import com.orchard.repository.PointsLogRepository;
import com.orchard.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** 会员积分（二期）：订单完成按实付金额 1 元 = 1 分自动累计 */
@Slf4j
@Service
public class PointsService {

    private final PointsLogRepository pointsLogRepository;
    private final UserRepository userRepository;

    public PointsService(PointsLogRepository pointsLogRepository, UserRepository userRepository) {
        this.pointsLogRepository = pointsLogRepository;
        this.userRepository = userRepository;
    }

    /** 订单完成发放积分：实付金额整数部分（多单拆分时每单独立累计） */
    @Transactional
    public void earn(Long userId, Long orderId, BigDecimal payAmount) {
        if (userId == null || payAmount == null) return;
        int delta = payAmount.intValue();
        if (delta <= 0) return;
        User u = userRepository.findById(userId).orElse(null);
        if (u == null) return;
        int before = u.getPoints() == null ? 0 : u.getPoints();
        u.setPoints(before + delta);
        userRepository.save(u);

        PointsLog pl = new PointsLog();
        pl.setUserId(userId);
        pl.setOrderId(orderId);
        pl.setDelta(delta);
        pl.setBalance(u.getPoints());
        pl.setType("ORDER_EARN");
        pl.setRemark("订单完成奖励（实付 " + payAmount + " 元）");
        pointsLogRepository.save(pl);
    }

    public Map<String, Object> overview(Long uid, int page, int size) {
        User u = userRepository.findById(uid).orElse(null);
        Page<PointsLog> logs = pointsLogRepository.findByUserIdOrderByIdDesc(uid, PageRequest.of(page, size));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("points", u != null && u.getPoints() != null ? u.getPoints() : 0);
        m.put("logs", logs);
        return m;
    }
}
