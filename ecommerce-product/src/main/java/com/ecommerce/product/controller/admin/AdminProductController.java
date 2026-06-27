package com.ecommerce.product.controller.admin;

import com.ecommerce.common.Result;
import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductVO;
import com.ecommerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "Admin - Product Management")
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('product:manage')")
public class AdminProductController {

    private final ProductService productService;

    @Operation(summary = "Product list")
    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int size,
                          @RequestParam(required = false) Long categoryId,
                          @RequestParam(required = false) String keyword) {
        return Result.success(productService.listProducts(page, size, categoryId, keyword, null, null));
    }

    @Operation(summary = "Get product detail")
    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id, null));
    }

    @Operation(summary = "Add product")
    @PostMapping
    public Result<?> add(@RequestBody ProductRequest request) {
        productService.addProduct(request);
        return Result.success();
    }

    @Operation(summary = "Update product")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        productService.updateProduct(id, request);
        return Result.success();
    }

    @Operation(summary = "Delete product")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }

    @Operation(summary = "Toggle product status")
    @PutMapping("/{id}/status")
    public Result<?> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        productService.toggleProductStatus(id, params.get("status"));
        return Result.success();
    }

    @Operation(summary = "Export products to Excel")
    @GetMapping("/export")
    public Result<String> export() {
        String filePath = System.getProperty("java.io.tmpdir") + "/products.xlsx";
        productService.exportProductsToExcel(filePath);
        return Result.success(filePath);
    }

    @Operation(summary = "Import products from Excel")
    @PostMapping("/import")
    public Result<?> importProducts(@RequestParam("file") MultipartFile file) {
        productService.importProductsFromExcel(file);
        return Result.success();
    }
}