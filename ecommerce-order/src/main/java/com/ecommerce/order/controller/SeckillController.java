package com.ecommerce.order.controller;

import com.ecommerce.common.Result;
import com.ecommerce.dto.response.OrderVO;
import com.ecommerce.order.service.SeckillService;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "秒杀")
@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    @Operation(summary = "秒杀下单")
    @PostMapping("/seckill/{promotionId}")
    public Result<OrderVO> seckill(@PathVariable Long promotionId,
                                   @CurrentUser LoginUser user,
                                   @RequestBody Map<String, Object> params) {
        Long productId = Long.valueOf(params.get("productId").toString());
        int quantity = params.containsKey("quantity") ? Integer.parseInt(params.get("quantity").toString()) : 1;
        String spec = (String) params.getOrDefault("spec", "");
        Long addressId = params.containsKey("addressId") && params.get("addressId") != null
                ? Long.valueOf(params.get("addressId").toString()) : null;
        String payMethod = (String) params.getOrDefault("payMethod", "ALIPAY");
        return Result.success(seckillService.executeSeckill(user.getUserId(), promotionId, productId, quantity, spec, addressId, payMethod));
    }
}