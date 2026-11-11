package com.orchard.repository;

import com.orchard.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByOwnerId(Long ownerId);

    Page<Store> findByStatus(String status, Pageable pageable);

    Page<Store> findByNameContainingAndStatus(String name, String status, Pageable pageable);
}
