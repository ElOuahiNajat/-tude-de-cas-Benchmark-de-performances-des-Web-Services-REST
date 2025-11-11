package com.example.variantcspringmvc.controller;

import com.example.variantcspringmvc.entity.Category;
import com.example.variantcspringmvc.entity.Item;
import com.example.variantcspringmvc.repository.CategoryRepository;
import com.example.variantcspringmvc.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;

    public CategoryController(CategoryRepository categoryRepository, ItemRepository itemRepository) {
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Category>> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Category> categories = categoryRepository.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(categories.getTotalElements()))
                .header("X-Total-Pages", String.valueOf(categories.getTotalPages()))
                .body(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategory(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        if (category.getCode() == null || category.getCode().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (categoryRepository.existsByCode(category.getCode())) {
            return ResponseEntity.status(409).build();
        }

        category.setUpdatedAt(java.time.LocalDateTime.now());
        Category saved = categoryRepository.save(category);
        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return categoryRepository.findById(id)
                .map(existing -> {
                    existing.setName(category.getName());
                    existing.setUpdatedAt(java.time.LocalDateTime.now());
                    return ResponseEntity.ok(categoryRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        long itemCount = itemRepository.countByCategoryId(id);
        if (itemCount > 0) {
            return ResponseEntity.status(409).build();
        }

        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<Page<Item>> getCategoryItems(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        Page<Item> items = itemRepository.findByCategoryIdWithCategory(id, PageRequest.of(page, size));
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(items.getTotalElements()))
                .header("X-Total-Pages", String.valueOf(items.getTotalPages()))
                .body(items);
    }
}