package com.ecommerce.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.entity.Permission;
import com.ecommerce.entity.RolePermission;
import com.ecommerce.mapper.PermissionMapper;
import com.ecommerce.mapper.RolePermissionMapper;
import com.ecommerce.order.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public List<Permission> getAllPermissions() {
        return permissionMapper.selectList(null);
    }

    @Override
    public List<Permission> getPermissionsByRole(String role) {
        List<RolePermission> rps = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRole, role));
        if (rps.isEmpty()) return Collections.emptyList();
        List<Long> ids = rps.stream().map(RolePermission::getPermissionId).collect(Collectors.toList());
        return permissionMapper.selectBatchIds(ids);
    }

    @Override
    public Map<String, List<Permission>> getAllRolePermissions() {
        List<RolePermission> all = rolePermissionMapper.selectList(null);
        List<Permission> allPerms = permissionMapper.selectList(null);
        Map<Long, Permission> permMap = allPerms.stream()
                .collect(Collectors.toMap(Permission::getId, p -> p));

        return all.stream().collect(Collectors.groupingBy(
                RolePermission::getRole,
                LinkedHashMap::new,
                Collectors.mapping(rp -> permMap.get(rp.getPermissionId()),
                        Collectors.collectingAndThen(Collectors.toList(),
                                list -> list.stream().filter(Objects::nonNull).collect(Collectors.toList())))));
    }

    @Override
    @Transactional
    public void assignPermission(String role, Long permissionId, boolean grant) {
        if (grant) {
            long count = rolePermissionMapper.selectCount(new LambdaQueryWrapper<RolePermission>()
                    .eq(RolePermission::getRole, role)
                    .eq(RolePermission::getPermissionId, permissionId));
            if (count == 0) {
                RolePermission rp = RolePermission.builder()
                        .role(role).permissionId(permissionId).build();
                rolePermissionMapper.insert(rp);
            }
        } else {
            rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                    .eq(RolePermission::getRole, role)
                    .eq(RolePermission::getPermissionId, permissionId));
        }
    }
}