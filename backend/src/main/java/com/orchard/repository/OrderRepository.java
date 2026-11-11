package com.orchard.repository;

import com.orchard.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNo(String orderNo);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    Page<Order> findByStoreId(Long storeId, Pageable pageable);

    Page<Order> findByStoreIdAndStatus(Long storeId, String status, Pageable pageable);

    Page<Order> findByStatus(String status, Pageable pageable);

    /** 同商家重复快递单号校验（PRD 2.3-3） */
    boolean existsByStoreIdAndExpressNo(Long storeId, String expressNo);

    /** 数据大盘统计 */
    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(o.payAmount), 0) FROM Order o WHERE o.status IN ('UNSHIPPED','SHIPPED','COMPLETED')")
    java.math.BigDecimal sumGmv();

    @Query("SELECT COALESCE(SUM(o.commissionAmount), 0) FROM Order o WHERE o.status = 'COMPLETED'")
    java.math.BigDecimal sumCommission();

    @Query("SELECT o FROM Order o WHERE o.storeId = :storeId AND o.status = :status")
    List<Order> findByStoreIdAndStatusList(@Param("storeId") Long storeId, @Param("status") String status);

    /** 物流预警：已支付超时未发货订单 */
    List<Order> findByStatusAndPaidAtBefore(String status, java.time.LocalDateTime time);
}
