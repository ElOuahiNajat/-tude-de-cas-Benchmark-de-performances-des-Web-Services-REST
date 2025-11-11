package com.example.variantcspringmvc.controller;

import com.example.variantcspringmvc.entity.Item;
import com.example.variantcspringmvc.repository.CategoryRepository;
import com.example.variantcspringmvc.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    public ItemController(ItemRepository itemRepository, CategoryRepository categoryRepository) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Item>> getItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Item> items = itemRepository.findAllWithCategory(PageRequest.of(page, size));
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(items.getTotalElements()))
                .header("X-Total-Pages", String.valueOf(items.getTotalPages()))
                .body(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItem(@PathVariable Long id) {
        return itemRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-category")
    public ResponseEntity<Page<Item>> getItemsByCategory(
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (!categoryRepository.existsById(categoryId)) {
            return ResponseEntity.notFound().build();
        }

        Page<Item> items = itemRepository.findByCategoryIdWithCategory(categoryId, PageRequest.of(page, size));
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(items.getTotalElements()))
                .header("X-Total-Pages", String.valueOf(items.getTotalPages()))
                .body(items);
    }

    @PostMapping
    public ResponseEntity<Item> createItem(@RequestBody Item item) {
        if (item.getSku() == null || item.getSku().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (itemRepository.existsBySku(item.getSku())) {
            return ResponseEntity.status(409).build();
        }
        if (item.getCategory() == null || item.getCategory().getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!categoryRepository.existsById(item.getCategory().getId())) {
            return ResponseEntity.badRequest().build();
        }

        item.setUpdatedAt(java.time.LocalDateTime.now());
        Item saved = itemRepository.save(item);
        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        return itemRepository.findById(id)
                .map(existing -> {
                    existing.setName(item.getName());
                    existing.setPrice(item.getPrice());
                    existing.setStock(item.getStock());
                    existing.setDescription(item.getDescription());
                    existing.setUpdatedAt(java.time.LocalDateTime.now());
                    return ResponseEntity.ok(itemRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (itemRepository.existsById(id)) {
            itemRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}