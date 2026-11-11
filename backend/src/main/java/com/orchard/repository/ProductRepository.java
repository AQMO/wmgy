package com.orchard.repository;

import com.orchard.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByStoreId(Long storeId, Pageable pageable);

    Page<Product> findByStoreIdAndStatus(Long storeId, String status, Pageable pageable);

    Page<Product> findByStoreIdAndStatusIn(Long storeId, List<String> statuses, Pageable pageable);

    Page<Product> findByStoreIdAndCategoryIdAndStatus(Long storeId, Long categoryId, String status, Pageable pageable);

    Page<Product> findByNameContainingAndStoreIdAndStatus(String name, Long storeId, String status, Pageable pageable);

    Page<Product> findByStatus(String status, Pageable pageable);

    Page<Product> findByStatusIn(List<String> statuses, Pageable pageable);

    /** 条件扣库存，防超卖 */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :qty, p.sales = p.sales + :qty " +
            "WHERE p.id = :id AND p.stock >= :qty AND p.status = 'ON_SHELF'")
    int deductStock(@Param("id") Long id, @Param("qty") Integer qty);

    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock + :qty WHERE p.id = :id")
    void restoreStock(@Param("id") Long id, @Param("qty") Integer qty);
}
