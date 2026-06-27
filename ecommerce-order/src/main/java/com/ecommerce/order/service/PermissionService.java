package com.ecommerce.order.service;

import com.ecommerce.entity.Permission;

import java.util.List;
import java.util.Map;

public interface PermissionService {

    List<Permission> getAllPermissions();

    List<Permission> getPermissionsByRole(String role);

    Map<String, List<Permission>> getAllRolePermissions();

    void assignPermission(String role, Long permissionId, boolean grant);
}