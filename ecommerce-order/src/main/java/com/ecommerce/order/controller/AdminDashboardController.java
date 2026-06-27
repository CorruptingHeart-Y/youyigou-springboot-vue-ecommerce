package com.ecommerce.order.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.dto.response.DashboardVO;
import com.ecommerce.dto.response.UserVO;
import com.ecommerce.entity.User;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.order.service.DashboardService;
import com.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Tag(name = "管理后台-仪表盘")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;
    private final UserMapper userMapper;
    private final OrderService orderService;

    @Operation(summary = "获取仪表盘数据")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('order:manage','user:manage','product:manage','content:manage')")
    public Result<DashboardVO> dashboard() {
        return Result.success(dashboardService.getDashboardData());
    }

    @Operation(summary = "导出仪表盘Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('order:manage','user:manage')")
    public ResponseEntity<ByteArrayResource> exportDashboard() {
        byte[] data = dashboardService.exportDashboardExcel();
        ByteArrayResource resource = new ByteArrayResource(data);
        String filename = "优选购核心数据报表_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @Operation(summary = "用户列表")
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('user:manage')")
    public Result<PageResult<UserVO>> users(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
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
        Page<User> pages = userMapper.selectPage(new Page<>(page, size), wrapper);
        Page<UserVO> voPage = new Page<>(page, size, pages.getTotal());
        voPage.setRecords(pages.getRecords().stream().map(u -> BeanUtil.copyProperties(u, UserVO.class)).toList());
        return Result
                .success(PageResult.of(voPage.getTotal(), voPage.getCurrent(), voPage.getSize(), voPage.getRecords()));
    }

    @Operation(summary = "禁用/启用用户")
    @PutMapping("/users/{userId}/status")
    @PreAuthorize("hasAuthority('user:manage')")
    public Result<Void> toggleUserStatus(@PathVariable Long userId, @RequestParam int status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success();
    }

    @Operation(summary = "修改用户角色")
    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasAuthority('user:manage')")
    public Result<Void> updateUserRole(@PathVariable Long userId, @RequestBody Map<String, String> params) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        String role = params.get("role");
        if (role != null && (role.equals("USER") || role.equals("ADMIN") || role.equals("SUPER_ADMIN"))) {
            user.setRole(role);
            userMapper.updateById(user);
        }
        return Result.success();
    }
}