package com.example.variantcspringmvc.repository;

import com.example.variantcspringmvc.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findByCategoryId(Long categoryId, Pageable pageable);
    boolean existsBySku(String sku);
    long countByCategoryId(Long categoryId);

    @Query("SELECT i FROM Item i JOIN FETCH i.category")
    Page<Item> findAllWithCategory(Pageable pageable);

    @Query("SELECT i FROM Item i JOIN FETCH i.category WHERE i.category.id = :categoryId")
    Page<Item> findByCategoryIdWithCategory(@Param("categoryId") Long categoryId, Pageable pageable);
}