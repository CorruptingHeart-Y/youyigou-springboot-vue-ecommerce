package com.ecommerce.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequest {

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    private String payMethod;
    private String remark;
}