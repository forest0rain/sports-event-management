package com.sports.platform.repository;

import com.sports.platform.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 权限数据访问层
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * 根据权限代码查询
     */
    Optional<Permission> findByCode(String code);

    /**
     * 检查权限代码是否存在
     */
    boolean existsByCode(String code);
}
