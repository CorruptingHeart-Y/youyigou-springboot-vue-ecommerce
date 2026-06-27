package com.ecommerce.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.dto.request.CartAddRequest;
import com.ecommerce.dto.response.CartItemVO;
import com.ecommerce.entity.CartItem;

import java.util.List;

public interface CartService extends IService<CartItem> {

    void addToCart(Long userId, CartAddRequest request);

    List<CartItemVO> listCart(Long userId);

    void updateQuantity(Long userId, Long cartItemId, int quantity);

    void deleteCartItem(Long userId, Long cartItemId);

    void checkItem(Long userId, Long cartItemId, boolean checked);

    void checkAll(Long userId, boolean checked);

    void clearCart(Long userId);
}