package com.orchard.config;

import com.orchard.entity.Order;
import com.orchard.entity.Store;
import com.orchard.repository.StoreRepository;
import com.orchard.service.NotificationService;
import com.orchard.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * 物流预警定时任务（二期）：每小时扫描已支付超时未发货订单，
 * 对商家推送预警通知（PRD 二期物流预警）。
 */
@Slf4j
@Configuration
@EnableScheduling
public class OrderAlertJob {

    private final OrderService orderService;
    private final StoreRepository storeRepository;
    private final NotificationService notificationService;

    public OrderAlertJob(OrderService orderService, StoreRepository storeRepository,
                         NotificationService notificationService) {
        this.orderService = orderService;
        this.storeRepository = storeRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelay = 3600_000L, initialDelay = 60_000L)
    public void scan() {
        try {
            List<Order> overdue = orderService.overdueAll(24);
            if (overdue.isEmpty()) {
                return;
            }
            overdue.forEach(o -> storeRepository.findById(o.getStoreId()).ifPresent(s -> warn(s, o)));
            log.info("物流预警扫描完成，超时未发货订单 {} 笔", overdue.size());
        } catch (Exception e) {
            log.warn("物流预警扫描失败", e);
        }
    }

    private void warn(Store s, Order o) {
        notificationService.push(s.getOwnerId(), "发货超时预警",
                "订单 " + o.getOrderNo() + " 已支付超过 24 小时未发货，请尽快处理", "ALERT", o.getId());
    }
}
