package com.example.variantajersey.repository;

import com.example.variantajersey.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findByCategoryId(Long categoryId, Pageable pageable);

    // ✅ CORRECTION : Utilisez les NOUVELLES méthodes que vous avez créées
    @Query("SELECT i FROM Item i WHERE i.category.id = :categoryId")
    Page<Item> findByCategoryIdWithCategory(@Param("categoryId") Long categoryId, Pageable pageable);

    // ✅ CORRECTION : Méthode simple pour tous les items
    @Query("SELECT i FROM Item i")
    Page<Item> findAllWithCategory(Pageable pageable);

    // ❌ SUPPRIMEZ ces anciennes méthodes si elles existent :
    // Page<Item> findAllWithJoinFetch(Pageable pageable);
    // Page<Item> findByCategoryIdWithJoinFetch(@Param("categoryId") Long categoryId, Pageable pageable);

    boolean existsBySku(String sku);

    long countByCategoryId(Long categoryId);
}