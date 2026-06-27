package com.ecommerce.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class FeedbackRequest {

    @NotBlank(message = "反馈内容不能为空")
    private String content;

    private String images;
}