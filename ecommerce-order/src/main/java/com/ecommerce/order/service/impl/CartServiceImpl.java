package com.ecommerce.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.dto.request.CartAddRequest;
import com.ecommerce.dto.response.CartItemVO;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.mapper.CartItemMapper;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.order.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartService {

    private final ProductMapper productMapper;

    @Override
    @Transactional
    public void addToCart(Long userId, CartAddRequest request) {
        Product product = productMapper.selectById(request.getProductId());
        if (product == null || product.getStatus() == 0) {
            throw new BusinessException("商品不存在或已下架");
        }
        if (product.getStock() < request.getQuantity()) {
            throw new BusinessException("库存不足");
        }

        CartItem existItem = this.getOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, request.getProductId())
                .eq(CartItem::getSpec, request.getSpec()));

        if (existItem != null) {
            existItem.setQuantity(existItem.getQuantity() + request.getQuantity());
            this.updateById(existItem);
        } else {
            CartItem cartItem = CartItem.builder()
                    .userId(userId)
                    .productId(product.getId())
                    .productName(product.getName())
                    .productImage(product.getCoverImage())
                    .price(product.getPrice())
                    .spec(StringUtils.hasText(request.getSpec()) ? request.getSpec() : "默认")
                    .quantity(request.getQuantity())
                    .checked(true)
                    .build();
            this.save(cartItem);
        }
    }

    @Override
    public List<CartItemVO> listCart(Long userId) {
        List<CartItem> list = this.list(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getCreateTime));
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateQuantity(Long userId, Long cartItemId, int quantity) {
        CartItem item = getOwnedCartItem(userId, cartItemId);
        item.setQuantity(quantity);
        this.updateById(item);
    }

    @Override
    @Transactional
    public void deleteCartItem(Long userId, Long cartItemId) {
        CartItem item = getOwnedCartItem(userId, cartItemId);
        this.removeById(item.getId());
    }

    @Override
    @Transactional
    public void checkItem(Long userId, Long cartItemId, boolean checked) {
        CartItem item = getOwnedCartItem(userId, cartItemId);
        item.setChecked(checked);
        this.updateById(item);
    }

    @Override
    @Transactional
    public void checkAll(Long userId, boolean checked) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId);
        CartItem update = new CartItem();
        update.setChecked(checked);
        this.update(update, wrapper);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        this.remove(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
    }

    private CartItem getOwnedCartItem(Long userId, Long cartItemId) {
        CartItem item = this.getById(cartItemId);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        return item;
    }

    private CartItemVO toVO(CartItem item) {
        return CartItemVO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productImage(item.getProductImage())
                .price(item.getPrice())
                .spec(item.getSpec())
                .quantity(item.getQuantity())
                .checked(item.getChecked())
                .subtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }
}