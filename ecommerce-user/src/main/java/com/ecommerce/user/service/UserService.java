package com.ecommerce.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.dto.request.*;
import com.ecommerce.dto.response.*;
import com.ecommerce.entity.*;

import java.util.List;

public interface UserService extends IService<User> {

    LoginResponse login(LoginRequest request);

    LoginResponse login(LoginRequest request, boolean rememberMe);

    void register(RegisterRequest request);

    UserVO getUserProfile(Long userId);

    void updateProfile(Long userId, UpdateProfileRequest request);

    void updatePassword(Long userId, String oldPassword, String newPassword);

    Page<UserVO> listUsers(int page, int size, String keyword);

    void toggleUserStatus(Long userId, int status);

    void sendVerificationCode(String email, String type);

    void resetPassword(String email, String code, String newPassword);

    void logout(Long userId);
}