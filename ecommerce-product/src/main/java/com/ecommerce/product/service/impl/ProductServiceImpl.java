package com.ecommerce.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductVO;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.mapper.CategoryMapper;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final CategoryMapper categoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Page<ProductVO> listProducts(int page, int size, Long categoryId, String keyword, String sort, Long userId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        if (categoryId != null && categoryId > 0) {
            // Collect categoryId + all its children so parent-category filter works
            List<Long> categoryIds = new ArrayList<>();
            categoryIds.add(categoryId);
            List<Category> children = categoryMapper.selectList(
                    new LambdaQueryWrapper<Category>().eq(Category::getParentId, categoryId));
            for (Category child : children) {
                categoryIds.add(child.getId());
            }
            wrapper.in(Product::getCategoryId, categoryIds);
        }
        if (StringUtils.hasText(keyword)) {
            // Find categories whose name matches keyword (for searches like "电脑", "手机")
            Set<Long> matchedCategoryIds = new HashSet<>();
            List<Category> matchedCategories = categoryMapper.selectList(
                    new LambdaQueryWrapper<Category>().like(Category::getName, keyword));
            for (Category cat : matchedCategories) {
                matchedCategoryIds.add(cat.getId());
                List<Category> children = categoryMapper.selectList(
                        new LambdaQueryWrapper<Category>().eq(Category::getParentId, cat.getId()));
                for (Category child : children) {
                    matchedCategoryIds.add(child.getId());
                }
            }

            wrapper.and(w -> {
                w.like(Product::getName, keyword).or().like(Product::getKeywords, keyword);
                if (!matchedCategoryIds.isEmpty()) {
                    w.or().in(Product::getCategoryId, matchedCategoryIds);
                }
            });
        }
        if ("price_asc".equals(sort)) {
            wrapper.orderByAsc(Product::getPrice);
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc(Product::getPrice);
        } else if ("sales".equals(sort)) {
            wrapper.orderByDesc(Product::getSales);
        } else {
            wrapper.orderByDesc(Product::getCreateTime);
        }
        Page<Product> pages = this.page(new Page<>(page, size), wrapper);
        return (Page<ProductVO>) pages.convert(p -> toVO(p, userId));
    }

    @Override
    public ProductVO getProductDetail(Long productId, Long userId) {
        String cacheKey = "product:detail:" + productId;
        Object cached = null;
        try {
            cached = redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception ignored) {
        }
        if (cached instanceof ProductVO) {
            return (ProductVO) cached;
        }
        Product product = this.getById(productId);
        if (product == null || product.getStatus() == 0) {
            throw new BusinessException("product not found or offline");
        }
        ProductVO vo = toVO(product, userId);
        try {
            redisTemplate.opsForValue().set(cacheKey, vo, 30, TimeUnit.MINUTES);
        } catch (Exception ignored) {
        }
        return vo;
    }

    @Override
    @Transactional
    public void addProduct(ProductRequest request) {
        Product product = BeanUtil.copyProperties(request, Product.class);
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        product.setSales(0);
        this.save(product);
    }

    @Override
    @Transactional
    public void updateProduct(Long productId, ProductRequest request) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("product not found");
        }
        BeanUtil.copyProperties(request, product, "id", "sales", "createTime");
        this.updateById(product);
        try {
            redisTemplate.delete("product:detail:" + productId);
        } catch (Exception ignored) {
        }
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("product not found");
        }
        this.removeById(productId);
        try {
            redisTemplate.delete("product:detail:" + productId);
        } catch (Exception ignored) {
        }
    }

    @Override
    @Transactional
    public void toggleProductStatus(Long productId, int status) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("product not found");
        }
        product.setStatus(status);
        this.updateById(product);
        try {
            redisTemplate.delete("product:detail:" + productId);
        } catch (Exception ignored) {
        }
    }

    @Override
    public List<ProductVO> getHotProducts(int limit) {
        List<Product> list = this.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getSales)
                .last("LIMIT " + limit));
        return list.stream().map(p -> toVO(p, null)).collect(Collectors.toList());
    }

    @Override
    public List<ProductVO> getNewProducts(int limit) {
        List<Product> list = this.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getCreateTime)
                .last("LIMIT " + limit));
        return list.stream().map(p -> toVO(p, null)).collect(Collectors.toList());
    }

    @Override
    public void exportProductsToExcel(String filePath) {
        List<Product> list = this.list(new LambdaQueryWrapper<Product>().orderByDesc(Product::getCreateTime));
        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Product List");
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Price");
            header.createCell(3).setCellValue("Stock");
            header.createCell(4).setCellValue("Sales");
            header.createCell(5).setCellValue("Status");
            for (int i = 0; i < list.size(); i++) {
                Product p = list.get(i);
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getName());
                row.createCell(2).setCellValue(p.getPrice().doubleValue());
                row.createCell(3).setCellValue(p.getStock());
                row.createCell(4).setCellValue(p.getSales());
                row.createCell(5).setCellValue(p.getStatus() == 1 ? "online" : "offline");
            }
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        } catch (Exception e) {
            throw new BusinessException("export failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void importProductsFromExcel(MultipartFile file) {
        try {
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(file.getInputStream());
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;
                Product product = new Product();
                String name = row.getCell(0) != null ? row.getCell(0).getStringCellValue() : null;
                if (name == null || name.isBlank()) continue;
                product.setName(name);
                product.setCategoryId((long) row.getCell(1).getNumericCellValue());
                product.setPrice(BigDecimal.valueOf(row.getCell(2).getNumericCellValue()));
                product.setStock((int) row.getCell(3).getNumericCellValue());
                product.setStatus(1);
                product.setSales(0);
                if (row.getCell(4) != null) {
                    product.setDescription(row.getCell(4).getStringCellValue());
                }
                this.save(product);
            }
            workbook.close();
        } catch (Exception e) {
            throw new BusinessException("import failed: " + e.getMessage());
        }
    }

    @Override
    public List<String> getHotSearchKeywords() {
        try {
            Set<Object> cached = redisTemplate.opsForZSet().reverseRange("hot:keywords", 0, 9);
            if (cached != null && !cached.isEmpty()) {
                return cached.stream().map(Object::toString).collect(Collectors.toList());
            }
        } catch (Exception ignored) {
        }
        return List.of("手机", "电脑", "咖啡豆", "坚果", "JK裙", "洛丽塔", "T恤", "A字短裙");
    }

    private ProductVO toVO(Product product, Long userId) {
        ProductVO vo = BeanUtil.copyProperties(product, ProductVO.class);
        Category category = categoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        if (StringUtils.hasText(product.getImages())) {
            try {
                vo.setImages(new com.fasterxml.jackson.databind.ObjectMapper().readValue(product.getImages(),
                        String[].class));
            } catch (Exception ignored) {
            }
        }
        return vo;
    }
}