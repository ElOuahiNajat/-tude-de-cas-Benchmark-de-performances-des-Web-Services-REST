package com.example.variantdspringdatarest.repository;

import com.example.variantdspringdatarest.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(path = "items")
public interface ItemRepository extends PagingAndSortingRepository<Item, Long> {

    // ✅ Exposition automatique des endpoints de recherche
    @RestResource(path = "by-category", rel = "by-category")
    Page<Item> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
}