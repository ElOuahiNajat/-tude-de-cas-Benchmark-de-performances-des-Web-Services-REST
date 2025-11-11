package com.example.variantajersey.resource;

import com.example.variantajersey.entity.Category;
import com.example.variantajersey.entity.Item;
import com.example.variantajersey.repository.CategoryRepository;
import com.example.variantajersey.repository.ItemRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ItemRepository itemRepository;

    // GET /categories?page=&size=
    @GET
    public Response getCategories(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        Page<Category> categories = categoryRepository.findAll(PageRequest.of(page, size));
        return Response.ok(categories.getContent())
                .header("X-Total-Count", categories.getTotalElements())
                .header("X-Total-Pages", categories.getTotalPages())
                .build();
    }

    // GET /categories/{id}
    @GET
    @Path("/{id}")
    public Response getCategory(@PathParam("id") Long id) {
        return categoryRepository.findById(id)
                .map(category -> Response.ok(category).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("Category not found")
                        .build());
    }

    // POST /categories
    @POST
    public Response createCategory(Category category) {
        if (category.getCode() == null || category.getCode().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Category code is required")
                    .build();
        }

        if (categoryRepository.existsByCode(category.getCode())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Category code already exists")
                    .build();
        }

        category.setUpdatedAt(java.time.LocalDateTime.now());
        Category saved = categoryRepository.save(category);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }

    // PUT /categories/{id}
    @PUT
    @Path("/{id}")
    public Response updateCategory(@PathParam("id") Long id, Category category) {
        return categoryRepository.findById(id)
                .map(existing -> {
                    existing.setName(category.getName());
                    existing.setUpdatedAt(java.time.LocalDateTime.now());
                    Category updated = categoryRepository.save(existing);
                    return Response.ok(updated).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("Category not found")
                        .build());
    }

    // DELETE /categories/{id}
    @DELETE
    @Path("/{id}")
    public Response deleteCategory(@PathParam("id") Long id) {
        return categoryRepository.findById(id)
                .map(category -> {
                    // Vérifier s'il y a des items associés
                    long itemCount = itemRepository.countByCategoryId(id);
                    if (itemCount > 0) {
                        return Response.status(Response.Status.CONFLICT)
                                .entity("Cannot delete category with associated items")
                                .build();
                    }

                    categoryRepository.delete(category);
                    return Response.noContent().build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("Category not found")
                        .build());
    }

    // GET /categories/{id}/items (association inverse) - ✅ CORRIGÉ
    @GET
    @Path("/{id}/items")
    public Response getCategoryItems(
            @PathParam("id") Long categoryId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        // ✅ SUPPRIMÉ: @QueryParam("joinFetch") @DefaultValue("false") boolean joinFetch

        // Vérifier que la catégorie existe
        if (!categoryRepository.existsById(categoryId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Category not found")
                    .build();
        }

        // ✅ CORRECTION : Utilisez la NOUVELLE méthode du repository
        Page<Item> items = itemRepository.findByCategoryIdWithCategory(categoryId, PageRequest.of(page, size));

        return Response.ok(items.getContent())
                .header("X-Total-Count", items.getTotalElements())
                .header("X-Total-Pages", items.getTotalPages())
                .build();
    }
}