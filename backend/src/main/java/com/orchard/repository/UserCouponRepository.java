package com.orchard.repository;

import com.orchard.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    List<UserCoupon> findByUserIdAndStatusOrderByIdDesc(Long userId, String status);

    boolean existsByCouponIdAndUserId(Long couponId, Long userId);

    @Modifying
    @Query("update UserCoupon uc set uc.status = 'USED', uc.orderId = :orderId, uc.usedAt = CURRENT_TIMESTAMP "
            + "where uc.id = :id and uc.userId = :userId and uc.status = 'UNUSED'")
    int use(@Param("id") Long id, @Param("userId") Long userId, @Param("orderId") Long orderId);
}
