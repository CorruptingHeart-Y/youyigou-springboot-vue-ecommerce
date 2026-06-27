package com.ecommerce.user.controller;

import com.ecommerce.common.Result;
import com.ecommerce.dto.request.*;
import com.ecommerce.dto.response.LoginResponse;
import com.ecommerce.dto.response.UserVO;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.LoginUser;
import com.ecommerce.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "用户认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request, request.isRememberMe()));
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    @Operation(summary = "发送验证码")
    @PostMapping("/send-code")
    public Result<String> sendCode(@RequestBody Map<String, String> params) {
        userService.sendVerificationCode(params.get("email"), params.get("type"));
        return Result.success("验证码已发送");
    }

    @Operation(summary = "找回密码")
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody Map<String, String> params) {
        userService.resetPassword(
                params.get("email"),
                params.get("code"),
                params.get("newPassword")
        );
        return Result.success("密码重置成功");
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/profile")
    public Result<UserVO> profile(@CurrentUser LoginUser user) {
        return Result.success(userService.getUserProfile(user.getUserId()));
    }

    @Operation(summary = "修改个人信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@CurrentUser LoginUser user,
                                       @RequestBody UpdateProfileRequest request) {
        userService.updateProfile(user.getUserId(), request);
        return Result.success();
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(@CurrentUser LoginUser user) {
        userService.logout(user.getUserId());
        return Result.success();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(@CurrentUser LoginUser user,
                                        @RequestBody Map<String, String> params) {
        userService.updatePassword(user.getUserId(),
                params.get("oldPassword"), params.get("newPassword"));
        return Result.success();
    }
}