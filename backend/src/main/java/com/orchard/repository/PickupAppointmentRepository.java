package com.orchard.repository;

import com.orchard.entity.PickupAppointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PickupAppointmentRepository extends JpaRepository<PickupAppointment, Long> {
    Page<PickupAppointment> findByStoreIdOrderByIdDesc(Long storeId, Pageable pageable);

    Page<PickupAppointment> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    List<PickupAppointment> findByStatus(String status);

    long countByStoreIdAndStatus(Long storeId, String status);

    long countByStoreId(Long storeId);

    Page<PickupAppointment> findByStoreIdAndStatusOrderByIdDesc(Long storeId, String status, Pageable pageable);
}
