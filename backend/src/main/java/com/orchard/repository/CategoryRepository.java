package com.orchard.repository;

import com.orchard.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByStoreIdOrderBySortAsc(Long storeId);

    boolean existsByStoreIdAndName(Long storeId, String name);
}
