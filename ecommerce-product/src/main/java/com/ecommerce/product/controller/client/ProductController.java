package com.ecommerce.product.controller.client;

import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.dto.response.CategoryVO;
import com.ecommerce.dto.response.ProductVO;
import com.ecommerce.dto.response.ReviewVO;
import com.ecommerce.product.service.CategoryService;
import com.ecommerce.product.service.ProductService;
import com.ecommerce.product.service.ReviewService;
import com.ecommerce.product.service.UserFavoriteService;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Client - Product")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserFavoriteService userFavoriteService;
    private final ReviewService reviewService;

    @Operation(summary = "Product list")
    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @CurrentUser LoginUser user) {
        Long userId = user != null ? user.getUserId() : null;
        return Result.success(productService.listProducts(page, size, categoryId, keyword, sort, userId));
    }

    @Operation(summary = "Product detail")
    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id, @CurrentUser LoginUser user) {
        Long userId = user != null ? user.getUserId() : null;
        return Result.success(productService.getProductDetail(id, userId));
    }

    @Operation(summary = "Category tree")
    @GetMapping("/categories")
    public Result<List<CategoryVO>> categories() {
        return Result.success(categoryService.getCategoryTree());
    }

    @Operation(summary = "Hot products")
    @GetMapping("/hot")
    public Result<List<ProductVO>> hot(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(productService.getHotProducts(limit));
    }

    @Operation(summary = "New products")
    @GetMapping("/new")
    public Result<List<ProductVO>> newProducts(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(productService.getNewProducts(limit));
    }

    @Operation(summary = "Toggle favorite")
    @PostMapping("/{id}/favorite")
    public Result<Map<String, Boolean>> favorite(@PathVariable Long id, @CurrentUser LoginUser user) {
        boolean result = userFavoriteService.toggleFavorite(user.getUserId(), id);
        return Result.success(Map.of("favorited", result));
    }

    @Operation(summary = "My favorites")
    @GetMapping("/favorites")
    public Result<List<ProductVO>> favorites(@CurrentUser LoginUser user) {
        return Result.success(userFavoriteService.getFavorites(user.getUserId()));
    }

    @Operation(summary = "Product reviews")
    @GetMapping("/{id}/reviews")
    public Result<PageResult<ReviewVO>> reviews(@PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(reviewService.getProductReviews(id, page, size));
    }

    @Operation(summary = "Hot search keywords")
    @GetMapping("/hot-search")
    public Result<List<String>> hotSearch() {
        return Result.success(productService.getHotSearchKeywords());
    }
}