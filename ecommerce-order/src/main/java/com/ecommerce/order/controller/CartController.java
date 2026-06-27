package com.ecommerce.order.controller;

import com.ecommerce.common.Result;
import com.ecommerce.dto.request.CartAddRequest;
import com.ecommerce.dto.response.CartItemVO;
import com.ecommerce.order.service.CartService;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "客户端-购物车")
@RestController
@RequestMapping("/api/client/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "加入购物车")
    @PostMapping("/add")
    public Result<Void> add(@CurrentUser LoginUser user, @Valid @RequestBody CartAddRequest request) {
        cartService.addToCart(user.getUserId(), request);
        return Result.success();
    }

    @Operation(summary = "购物车列表")
    @GetMapping("/list")
    public Result<List<CartItemVO>> list(@CurrentUser LoginUser user) {
        return Result.success(cartService.listCart(user.getUserId()));
    }

    @Operation(summary = "修改数量")
    @PutMapping("/{cartItemId}/quantity")
    public Result<Void> updateQuantity(@CurrentUser LoginUser user,
                                        @PathVariable Long cartItemId,
                                        @RequestParam int quantity) {
        cartService.updateQuantity(user.getUserId(), cartItemId, quantity);
        return Result.success();
    }

    @Operation(summary = "删除购物车项")
    @DeleteMapping("/{cartItemId}")
    public Result<Void> delete(@CurrentUser LoginUser user, @PathVariable Long cartItemId) {
        cartService.deleteCartItem(user.getUserId(), cartItemId);
        return Result.success();
    }

    @Operation(summary = "勾选/取消勾选")
    @PutMapping("/{cartItemId}/check")
    public Result<Void> check(@CurrentUser LoginUser user,
                               @PathVariable Long cartItemId,
                               @RequestParam boolean checked) {
        cartService.checkItem(user.getUserId(), cartItemId, checked);
        return Result.success();
    }

    @Operation(summary = "全选/取消全选")
    @PutMapping("/check-all")
    public Result<Void> checkAll(@CurrentUser LoginUser user, @RequestParam boolean checked) {
        cartService.checkAll(user.getUserId(), checked);
        return Result.success();
    }
}