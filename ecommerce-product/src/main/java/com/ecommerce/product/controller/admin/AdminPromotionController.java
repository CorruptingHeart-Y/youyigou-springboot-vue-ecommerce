package com.ecommerce.product.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.Result;
import com.ecommerce.entity.Promotion;
import com.ecommerce.mapper.PromotionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - Promotion Management")
@RestController
@RequestMapping("/api/admin/promotions")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('promotion:manage')")
public class AdminPromotionController {

    private final PromotionMapper promotionMapper;

    @Operation(summary = "Promotion list")
    @GetMapping
    public Result<?> list() {
        return Result.success(promotionMapper.selectList(
                new LambdaQueryWrapper<Promotion>().orderByDesc(Promotion::getCreateTime)));
    }

    @Operation(summary = "Add promotion")
    @PostMapping
    public Result<?> add(@RequestBody Promotion promotion) {
        promotionMapper.insert(promotion);
        return Result.success();
    }

    @Operation(summary = "Update promotion")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Promotion promotion) {
        promotion.setId(id);
        promotionMapper.updateById(promotion);
        return Result.success();
    }

    @Operation(summary = "Delete promotion")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        promotionMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "Toggle promotion status")
    @PutMapping("/{id}/status")
    public Result<?> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        Promotion promotion = promotionMapper.selectById(id);
        if (promotion == null) {
            return Result.error("promotion not found");
        }
        promotion.setStatus(status);
        promotionMapper.updateById(promotion);
        return Result.success();
    }
}