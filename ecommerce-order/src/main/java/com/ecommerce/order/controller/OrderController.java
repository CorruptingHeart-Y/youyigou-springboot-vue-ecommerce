package com.ecommerce.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.dto.request.AddressRequest;
import com.ecommerce.dto.response.OrderVO;
import com.ecommerce.entity.Address;
import com.ecommerce.mapper.AddressMapper;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "客户端-订单")
@RestController
@RequestMapping("/api/client/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final AddressMapper addressMapper;

    @Operation(summary = "提交订单")
    @PostMapping("/create")
    public Result<OrderVO> create(@CurrentUser LoginUser user, @RequestBody Map<String, Object> params) {
        Long addressId = Long.valueOf(params.get("addressId").toString());
        String payMethod = (String) params.getOrDefault("payMethod", "ALIPAY");
        String remark = (String) params.getOrDefault("remark", "");
        Long couponId = params.containsKey("couponId") && params.get("couponId") != null
                ? Long.valueOf(params.get("couponId").toString())
                : null;
        java.math.BigDecimal couponDiscount = params.containsKey("couponDiscount")
                && params.get("couponDiscount") != null
                        ? new java.math.BigDecimal(params.get("couponDiscount").toString())
                        : java.math.BigDecimal.ZERO;
        return Result.success(
                orderService.createOrder(user.getUserId(), addressId, payMethod, remark, couponId, couponDiscount));
    }

    @Operation(summary = "支付订单")
    @PostMapping("/pay")
    public Result<Void> pay(@CurrentUser LoginUser user, @RequestBody Map<String, String> params) {
        orderService.payOrder(params.get("orderNo"), params.getOrDefault("payMethod", "ALIPAY"));
        return Result.success();
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@CurrentUser LoginUser user, @PathVariable String orderNo) {
        return Result.success(orderService.getOrderDetail(orderNo));
    }

    @Operation(summary = "我的订单列表")
    @GetMapping("/list")
    public Result<PageResult<OrderVO>> list(@CurrentUser LoginUser user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        var pages = orderService.listUserOrders(user.getUserId(), page, size, status);
        return Result.success(PageResult.of(pages.getTotal(), pages.getCurrent(), pages.getSize(), pages.getRecords()));
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@CurrentUser LoginUser user, @PathVariable String orderNo) {
        orderService.cancelOrder(user.getUserId(), orderNo);
        return Result.success();
    }

    @Operation(summary = "确认收货")
    @PutMapping("/{orderNo}/confirm")
    public Result<Void> confirm(@CurrentUser LoginUser user, @PathVariable String orderNo) {
        orderService.confirmReceive(user.getUserId(), orderNo);
        return Result.success();
    }

    @Operation(summary = "申请退款")
    @PutMapping("/{orderNo}/refund")
    public Result<Void> refund(@CurrentUser LoginUser user, @PathVariable String orderNo) {
        orderService.refundOrder(user.getUserId(), orderNo);
        return Result.success();
    }

    @Operation(summary = "收货地址列表")
    @GetMapping("/addresses")
    public Result<List<Address>> addresses(@CurrentUser LoginUser user) {
        List<Address> list = addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, user.getUserId())
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getUpdateTime));
        return Result.success(list);
    }

    @Operation(summary = "新增收货地址")
    @PostMapping("/address")
    public Result<Void> addAddress(@CurrentUser LoginUser user,
            @Valid @RequestBody AddressRequest request) {
        Address address = new Address();
        address.setUserId(user.getUserId());
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetail(request.getDetail());
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefault(user.getUserId());
            address.setIsDefault(true);
        } else {
            long count = addressMapper
                    .selectCount(new LambdaQueryWrapper<Address>().eq(Address::getUserId, user.getUserId()));
            address.setIsDefault(count == 0);
        }
        addressMapper.insert(address);
        return Result.success();
    }

    @Operation(summary = "修改收货地址")
    @PutMapping("/address/{addressId}")
    public Result<Void> updateAddress(@CurrentUser LoginUser user,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(user.getUserId())) {
            throw new BusinessException("地址不存在");
        }
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetail(request.getDetail());
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefault(user.getUserId());
            address.setIsDefault(true);
        }
        addressMapper.updateById(address);
        return Result.success();
    }

    @Operation(summary = "删除收货地址")
    @DeleteMapping("/address/{addressId}")
    public Result<Void> deleteAddress(@CurrentUser LoginUser user,
            @PathVariable Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(user.getUserId())) {
            throw new BusinessException("地址不存在");
        }
        addressMapper.deleteById(addressId);
        return Result.success();
    }

    @Operation(summary = "设为默认地址")
    @PutMapping("/address/{addressId}/default")
    public Result<Void> setDefaultAddress(@CurrentUser LoginUser user,
            @PathVariable Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(user.getUserId())) {
            throw new BusinessException("地址不存在");
        }
        clearDefault(user.getUserId());
        address.setIsDefault(true);
        addressMapper.updateById(address);
        return Result.success();
    }

    private void clearDefault(Long userId) {
        Address update = new Address();
        update.setIsDefault(false);
        addressMapper.update(update, new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId));
    }
}