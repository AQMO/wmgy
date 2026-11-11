package com.orchard.repository;

import com.orchard.entity.VisitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VisitLogRepository extends JpaRepository<VisitLog, Long> {

    long countByStoreIdAndCreatedAtAfter(Long storeId, LocalDateTime since);

    long countByStoreId(Long storeId);

    long countByCreatedAtAfter(LocalDateTime since);

    long count();

    long countByStoreIdAndCreatedAtAfterAndUserIdNotNull(Long storeId, LocalDateTime since);

    /** 7 日 PV 趋势（按天） */
    @Query(value = "SELECT DATE(created_at) AS d, COUNT(*) AS pv FROM t_visit_log " +
            "WHERE store_id = :storeId AND created_at >= :since GROUP BY DATE(created_at) ORDER BY d", nativeQuery = true)
    List<Object[]> trendByStore(@Param("storeId") Long storeId, @Param("since") LocalDateTime since);

    /** 平台店铺 PV 排行 Top10 */
    @Query(value = "SELECT store_id, COUNT(*) AS pv FROM t_visit_log " +
            "GROUP BY store_id ORDER BY pv DESC LIMIT 10", nativeQuery = true)
    List<Object[]> topStores();
}
