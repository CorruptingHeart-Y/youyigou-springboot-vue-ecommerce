package com.ecommerce.product.controller.admin;

import com.ecommerce.common.Result;
import com.ecommerce.product.service.CategoryService;
import com.ecommerce.entity.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - Category Management")
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('product:manage')")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Category tree")
    @GetMapping
    public Result<?> tree() {
        return Result.success(categoryService.getCategoryTree());
    }

    @Operation(summary = "Add category")
    @PostMapping
    public Result<?> add(@RequestBody Category category) {
        categoryService.addCategory(category);
        return Result.success();
    }

    @Operation(summary = "Update category")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        categoryService.updateCategory(category);
        return Result.success();
    }

    @Operation(summary = "Delete category")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}