package com.ecommerce.order.controller;

import com.ecommerce.common.Result;
import com.ecommerce.entity.Permission;
import com.ecommerce.order.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "权限管理")
@RestController
@RequestMapping("/api/admin/permission")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('permission:manage')")
public class AdminPermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "获取所有角色权限")
    @GetMapping
    public Result<Map<String, List<Permission>>> getAll() {
        return Result.success(permissionService.getAllRolePermissions());
    }

    @Operation(summary = "获取所有权限列表")
    @GetMapping("/all")
    public Result<List<Permission>> allPermissions() {
        return Result.success(permissionService.getAllPermissions());
    }

    @Operation(summary = "分配权限")
    @PostMapping("/assign")
    public Result<Void> assign(@RequestBody Map<String, Object> params) {
        String role = (String) params.get("role");
        Long permissionId = Long.valueOf(params.get("permissionId").toString());
        boolean grant = Boolean.TRUE.equals(params.get("grant"));
        permissionService.assignPermission(role, permissionId, grant);
        return Result.success();
    }
}