package com.rainlu.oj.codesandbox.security;

import java.security.Permission;

/**
 * @description 默认的安全管理器实现
 * @author Jun Lu
 */
public class DefaultSecurityManager extends SecurityManager {

    // 检查所有的权限
    @Override
    public void checkPermission(Permission perm) {
        System.out.println("默认不做任何限制");
        System.out.println(perm);
    }
}
