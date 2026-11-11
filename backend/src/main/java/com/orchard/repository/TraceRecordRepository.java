package com.orchard.repository;

import com.orchard.entity.TraceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TraceRecordRepository extends JpaRepository<TraceRecord, Long> {
    List<TraceRecord> findByProductIdOrderByEventTimeAscIdAsc(Long productId);
}
