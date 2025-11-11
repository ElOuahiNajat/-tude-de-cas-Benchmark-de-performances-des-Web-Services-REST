package com.example.variantajersey.resource;

import com.example.variantajersey.entity.Item;
import com.example.variantajersey.repository.ItemRepository;
import com.example.variantajersey.repository.CategoryRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ItemResource {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // GET /items?page=&size=
    @GET
    public Response getItems(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        // ✅ CORRECTION : Utilisez la NOUVELLE méthode
        Page<Item> items = itemRepository.findAllWithCategory(PageRequest.of(page, size));

        return Response.ok(items.getContent())
                .header("X-Total-Count", items.getTotalElements())
                .header("X-Total-Pages", items.getTotalPages())
                .build();
    }

    // GET /items?categoryId=...
    @GET
    @Path("/by-category")
    public Response getItemsByCategory(
            @QueryParam("categoryId") Long categoryId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        if (categoryId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("categoryId parameter is required")
                    .build();
        }

        if (!categoryRepository.existsById(categoryId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Category not found")
                    .build();
        }

        // ✅ CORRECTION : Utilisez la NOUVELLE méthode
        Page<Item> items = itemRepository.findByCategoryIdWithCategory(categoryId, PageRequest.of(page, size));

        return Response.ok(items.getContent())
                .header("X-Total-Count", items.getTotalElements())
                .header("X-Total-Pages", items.getTotalPages())
                .build();
    }

    // ... le reste de votre code reste identique
    // GET /items/{id}
    @GET
    @Path("/{id}")
    public Response getItem(@PathParam("id") Long id) {
        return itemRepository.findById(id)
                .map(item -> Response.ok(item).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("Item not found")
                        .build());
    }

    // POST /items
    @POST
    public Response createItem(Item item) {
        if (item.getSku() == null || item.getSku().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Item SKU is required")
                    .build();
        }

        if (itemRepository.existsBySku(item.getSku())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Item SKU already exists")
                    .build();
        }

        if (item.getCategory() == null || item.getCategory().getId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Category is required")
                    .build();
        }

        if (!categoryRepository.existsById(item.getCategory().getId())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Category not found")
                    .build();
        }

        item.setUpdatedAt(java.time.LocalDateTime.now());
        Item saved = itemRepository.save(item);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }

    // PUT /items/{id}
    @PUT
    @Path("/{id}")
    public Response updateItem(@PathParam("id") Long id, Item item) {
        return itemRepository.findById(id)
                .map(existing -> {
                    existing.setName(item.getName());
                    existing.setPrice(item.getPrice());
                    existing.setStock(item.getStock());
                    existing.setDescription(item.getDescription());
                    existing.setUpdatedAt(java.time.LocalDateTime.now());

                    Item updated = itemRepository.save(existing);
                    return Response.ok(updated).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("Item not found")
                        .build());
    }

    // DELETE /items/{id}
    @DELETE
    @Path("/{id}")
    public Response deleteItem(@PathParam("id") Long id) {
        return itemRepository.findById(id)
                .map(item -> {
                    itemRepository.delete(item);
                    return Response.noContent().build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("Item not found")
                        .build());
    }
}