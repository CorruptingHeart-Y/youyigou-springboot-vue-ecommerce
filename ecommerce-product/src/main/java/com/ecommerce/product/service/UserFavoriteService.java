package com.ecommerce.product.service;

import com.ecommerce.dto.response.ProductVO;
import com.ecommerce.entity.UserFavorite;

import java.util.List;

public interface UserFavoriteService {

    boolean toggleFavorite(Long userId, Long productId);

    boolean isFavorited(Long userId, Long productId);

    List<ProductVO> getFavorites(Long userId);
}