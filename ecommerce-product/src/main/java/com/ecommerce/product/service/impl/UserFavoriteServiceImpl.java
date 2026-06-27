package com.ecommerce.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.dto.response.ProductVO;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.UserFavorite;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.mapper.UserFavoriteMapper;
import com.ecommerce.product.service.UserFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserFavoriteServiceImpl implements UserFavoriteService {

    private final UserFavoriteMapper userFavoriteMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public boolean toggleFavorite(Long userId, Long productId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId).eq(UserFavorite::getProductId, productId);
        UserFavorite exist = userFavoriteMapper.selectOne(wrapper);
        if (exist != null) {
            userFavoriteMapper.deleteById(exist.getId());
            return false;
        }
        UserFavorite fav = new UserFavorite();
        fav.setUserId(userId);
        fav.setProductId(productId);
        userFavoriteMapper.insert(fav);
        return true;
    }

    @Override
    public boolean isFavorited(Long userId, Long productId) {
        if (userId == null)
            return false;
        return userFavoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getProductId, productId)) > 0;
    }

    @Override
    public List<ProductVO> getFavorites(Long userId) {
        List<UserFavorite> favs = userFavoriteMapper.selectList(
                new LambdaQueryWrapper<UserFavorite>().eq(UserFavorite::getUserId, userId));
        return favs.stream().map(fav -> {
            Product product = productMapper.selectById(fav.getProductId());
            if (product == null)
                return null;
            ProductVO vo = new ProductVO();
            vo.setId(product.getId());
            vo.setName(product.getName());
            vo.setPrice(product.getPrice());
            vo.setStock(product.getStock());
            vo.setSales(product.getSales());
            vo.setCoverImage(product.getCoverImage());
            vo.setStatus(product.getStatus());
            return vo;
        }).filter(v -> v != null).collect(Collectors.toList());
    }
}