package com.ecommerce.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductVO;
import com.ecommerce.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService extends IService<Product> {

    Page<ProductVO> listProducts(int page, int size, Long categoryId, String keyword, String sort, Long userId);

    ProductVO getProductDetail(Long productId, Long userId);

    void addProduct(ProductRequest request);

    void updateProduct(Long productId, ProductRequest request);

    void deleteProduct(Long productId);

    void toggleProductStatus(Long productId, int status);

    List<ProductVO> getHotProducts(int limit);

    List<ProductVO> getNewProducts(int limit);

    void exportProductsToExcel(String filePath);

    void importProductsFromExcel(MultipartFile file);

    List<String> getHotSearchKeywords();
}