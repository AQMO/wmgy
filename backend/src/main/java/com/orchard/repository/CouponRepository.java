package com.orchard.repository;

import com.orchard.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findByStoreIdOrderByIdDesc(Long storeId);
}
