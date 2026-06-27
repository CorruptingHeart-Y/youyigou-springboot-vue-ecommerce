package com.ecommerce.user.controller;

import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.dto.response.UserVO;
import com.ecommerce.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理后台-用户管理")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "用户列表")
    @GetMapping
    public Result<PageResult<UserVO>> users(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        var pages = userService.listUsers(page, size, keyword);
        return Result.success(PageResult.of(pages.getTotal(), pages.getCurrent(), pages.getSize(), pages.getRecords()));
    }

    @Operation(summary = "禁用/启用用户")
    @PutMapping("/{userId}/status")
    public Result<Void> toggleUserStatus(@PathVariable Long userId, @RequestParam int status) {
        userService.toggleUserStatus(userId, status);
        return Result.success();
    }
}