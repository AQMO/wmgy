package com.orchard.repository;

import com.orchard.entity.CommissionRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommissionRuleRepository extends JpaRepository<CommissionRule, Long> {
    Optional<CommissionRule> findByBizTypeAndEffective(String bizType, Integer effective);
}
