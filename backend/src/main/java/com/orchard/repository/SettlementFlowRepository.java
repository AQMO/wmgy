package com.orchard.repository;

import com.orchard.entity.SettlementFlow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementFlowRepository extends JpaRepository<SettlementFlow, Long> {
    Page<SettlementFlow> findByStoreIdOrderByCreatedAtDesc(Long storeId, Pageable pageable);
}
