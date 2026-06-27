package com.ecommerce.order.controller;

import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.dto.response.OrderVO;
import com.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "管理后台-订单管理")
@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('order:manage')")
public class AdminOrderController {

    private final OrderService orderService;

    @Operation(summary = "订单列表")
    @GetMapping("/list")
    public Result<PageResult<OrderVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String status) {
        var pages = orderService.listAllOrders(page, size, orderNo, status);
        return Result.success(PageResult.of(pages.getTotal(), pages.getCurrent(), pages.getSize(), pages.getRecords()));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@PathVariable String orderNo) {
        return Result.success(orderService.getOrderDetail(orderNo));
    }

    @Operation(summary = "发货")
    @PutMapping("/{orderNo}/deliver")
    public Result<Void> deliver(@PathVariable String orderNo, @RequestBody Map<String, String> params) {
        orderService.adminDeliverOrder(orderNo,
                params.get("logisticsNo"), params.get("logisticsCompany"));
        return Result.success();
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo) {
        orderService.adminCancelOrder(orderNo);
        return Result.success();
    }

    @Operation(summary = "处理退款")
    @PutMapping("/{orderNo}/refund")
    public Result<Void> processRefund(@PathVariable String orderNo, @RequestParam boolean approve) {
        orderService.adminProcessRefund(orderNo, approve);
        return Result.success();
    }

    @Operation(summary = "导出订单Excel")
    @GetMapping("/export")
    public Result<String> exportOrders() {
        orderService.exportOrdersToExcel("uploads/exports/orders.xlsx");
        return Result.success("导出成功");
    }
}