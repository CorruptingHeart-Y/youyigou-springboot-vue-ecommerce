package com.ecommerce.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.dto.request.*;
import com.ecommerce.dto.response.LoginResponse;
import com.ecommerce.dto.response.UserVO;
import com.ecommerce.entity.User;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.security.JwtUtil;
import com.ecommerce.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    public UserServiceImpl(PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                           RedisTemplate<String, Object> redisTemplate,
                           JavaMailSender mailSender) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        return login(request, false);
    }

    @Override
    public LoginResponse login(LoginRequest request, boolean rememberMe) {
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token;
        if (rememberMe) {
            token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), 7 * 24 * 60 * 60 * 1000L);
        } else {
            token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        }
        String cacheKey = "user:token:" + user.getId();
        try {
            redisTemplate.opsForValue().set(cacheKey, token, rememberMe ? 7 : 1, TimeUnit.DAYS);
        } catch (Exception ignored) {
            log.warn("Redis不可用，跳过token缓存");
        }

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (!StringUtils.hasText(request.getCode())) {
            throw new BusinessException("请输入验证码");
        }
        String cacheKey = "verify:code:" + request.getEmail();
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached == null || !cached.toString().equals(request.getCode())) {
            throw new BusinessException("验证码错误或已过期");
        }
        redisTemplate.delete(cacheKey);
        long count = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        count = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail()));
        if (count > 0) {
            throw new BusinessException("邮箱已被注册");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .nickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername())
                .phone(request.getPhone())
                .role("USER")
                .status(1)
                .build();
        this.save(user);
    }

    @Override
    public UserVO getUserProfile(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        this.updateById(user);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }

    @Override
    public Page<UserVO> listUsers(int page, int size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getPhone, keyword)
                    .or()
                    .like(User::getNickname, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> pages = this.page(new Page<>(page, size), wrapper);
        return (Page<UserVO>) pages.convert(u -> BeanUtil.copyProperties(u, UserVO.class));
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId, int status) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        this.updateById(user);
    }

    @Override
    public void sendVerificationCode(String email, String type) {
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email));
        if ("register".equals(type)) {
            if (user != null) {
                throw new BusinessException("该邮箱已被注册");
            }
        } else {
            if (user == null) {
                throw new BusinessException("该邮箱未注册");
            }
        }
        String code = RandomUtil.randomNumbers(6);
        try {
            redisTemplate.opsForValue().set("verify:code:" + email, code, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Redis不可用，无法缓存验证码", e);
            throw new BusinessException("服务暂不可用，请稍后重试");
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(email);
            message.setSubject("电商平台 - 验证码");
            message.setText("您的验证码是：" + code + "，有效期5分钟，请勿泄露。");
            mailSender.send(message);
        } catch (Exception e) {
            throw new BusinessException("邮件发送失败，请稍后再试");
        }
    }

    @Override
    public void logout(Long userId) {
        try {
            redisTemplate.delete("user:token:" + userId);
        } catch (Exception ignored) {
        }
    }

    @Override
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        String cacheKey = "verify:code:" + email;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached == null || !cached.toString().equals(code)) {
            throw new BusinessException("验证码错误或已过期");
        }
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
        redisTemplate.delete(cacheKey);
    }
}