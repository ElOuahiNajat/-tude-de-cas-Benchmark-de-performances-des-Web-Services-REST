package com.example.variantdspringdatarest.repository;

import com.example.variantdspringdatarest.entity.Category;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "categories")
public interface CategoryRepository extends PagingAndSortingRepository<Category, Long> {
    // ✅ Spring Data REST expose automatiquement tous les endpoints
}
