package com.ecommerce.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.dto.response.CategoryVO;
import com.ecommerce.entity.Category;
import com.ecommerce.mapper.CategoryMapper;
import com.ecommerce.product.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<CategoryVO> getCategoryTree() {
        List<Category> all = this.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder));
        Map<Long, List<CategoryVO>> parentMap = all.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() > 0)
                .map(this::toVO)
                .collect(Collectors.groupingBy(vo -> {
                    Category cat = all.stream().filter(c -> c.getId().equals(vo.getId())).findFirst().orElse(null);
                    return cat != null ? cat.getParentId() : 0L;
                }));
        List<CategoryVO> roots = all.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .map(this::toVO)
                .collect(Collectors.toList());
        for (CategoryVO root : roots) {
            buildTree(root, parentMap);
        }
        return roots;
    }

    private void buildTree(CategoryVO parent, Map<Long, List<CategoryVO>> parentMap) {
        List<CategoryVO> children = parentMap.get(parent.getId());
        if (children != null) {
            parent.setChildren(children);
            for (CategoryVO child : children) {
                buildTree(child, parentMap);
            }
        }
    }

    @Override
    public void addCategory(Category category) {
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        this.save(category);
    }

    @Override
    public void updateCategory(Category category) {
        Category exist = this.getById(category.getId());
        if (exist == null) {
            throw new BusinessException("category not found");
        }
        this.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        long count = this.count(new LambdaQueryWrapper<Category>().eq(Category::getParentId, id));
        if (count > 0) {
            throw new BusinessException("cannot delete category with subcategories");
        }
        this.removeById(id);
    }

    private CategoryVO toVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setSortOrder(category.getSortOrder());
        vo.setIcon(category.getIcon());
        vo.setChildren(new ArrayList<>());
        return vo;
    }
}