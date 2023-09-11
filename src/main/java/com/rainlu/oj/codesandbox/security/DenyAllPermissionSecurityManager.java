package com.rainlu.oj.codesandbox.security;

import java.security.Permission;

/**
 * @description 禁用所有权限的安全管理器
 */
public class DenyAllPermissionSecurityManager extends SecurityManager {

    // 检查所有的权限
    @Override
    public void checkPermission(Permission perm) {
        throw new SecurityException("权限异常：" + perm.toString());
    }

}
