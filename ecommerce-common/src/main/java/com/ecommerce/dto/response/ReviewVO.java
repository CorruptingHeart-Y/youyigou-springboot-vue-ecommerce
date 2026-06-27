package com.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewVO {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Long productId;
    private String productName;
    private String content;
    private Integer rating;
    private String images;
    private Integer status;
    private LocalDateTime createTime;
}