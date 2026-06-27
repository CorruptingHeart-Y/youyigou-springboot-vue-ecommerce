package com.ecommerce.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.dto.response.CategoryVO;
import com.ecommerce.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {

    List<CategoryVO> getCategoryTree();

    void addCategory(Category category);

    void updateCategory(Category category);

    void deleteCategory(Long id);
}