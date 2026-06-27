package com.ecommerce.user.controller;

import com.ecommerce.common.Result;
import com.ecommerce.dto.request.AddressRequest;
import com.ecommerce.entity.Address;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.LoginUser;
import com.ecommerce.user.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户地址管理")
@RestController
@RequestMapping("/api/user/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "收货地址列表")
    @GetMapping
    public Result<List<Address>> addresses(@CurrentUser LoginUser user) {
        return Result.success(addressService.listAddresses(user.getUserId()));
    }

    @Operation(summary = "新增收货地址")
    @PostMapping
    public Result<Void> addAddress(@CurrentUser LoginUser user,
                                    @Valid @RequestBody AddressRequest request) {
        addressService.addAddress(user.getUserId(), request);
        return Result.success();
    }

    @Operation(summary = "修改收货地址")
    @PutMapping("/{addressId}")
    public Result<Void> updateAddress(@CurrentUser LoginUser user,
                                       @PathVariable Long addressId,
                                       @Valid @RequestBody AddressRequest request) {
        addressService.updateAddress(user.getUserId(), addressId, request);
        return Result.success();
    }

    @Operation(summary = "删除收货地址")
    @DeleteMapping("/{addressId}")
    public Result<Void> deleteAddress(@CurrentUser LoginUser user,
                                       @PathVariable Long addressId) {
        addressService.deleteAddress(user.getUserId(), addressId);
        return Result.success();
    }

    @Operation(summary = "设为默认地址")
    @PutMapping("/{addressId}/default")
    public Result<Void> setDefaultAddress(@CurrentUser LoginUser user,
                                           @PathVariable Long addressId) {
        addressService.setDefault(user.getUserId(), addressId);
        return Result.success();
    }
}