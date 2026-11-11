package com.orchard.repository;

import com.orchard.entity.PointsLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointsLogRepository extends JpaRepository<PointsLog, Long> {
    Page<PointsLog> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);
}
