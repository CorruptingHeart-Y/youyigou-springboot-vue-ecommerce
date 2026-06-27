package com.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("e_feedback")
public class Feedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String content;
    private String images;          // JSON数组
    private String reply;           // 管理员回复
    private Long replyBy;
    private LocalDateTime replyTime;
    private Integer status;         // 0-待处理 1-已处理

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}