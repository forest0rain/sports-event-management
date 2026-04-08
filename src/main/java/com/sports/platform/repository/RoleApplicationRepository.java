package com.sports.platform.repository;

import com.sports.platform.entity.RoleApplication;
import com.sports.platform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色申请 Repository
 */
@Repository
public interface RoleApplicationRepository extends JpaRepository<RoleApplication, Long> {

    /**
     * 查找用户的申请记录
     */
    List<RoleApplication> findByUserOrderByCreatedTimeDesc(User user);

    /**
     * 查找用户待审核的申请
     */
    Optional<RoleApplication> findByUserAndStatus(User user, String status);

    /**
     * 查找所有待审核的申请
     */
    Page<RoleApplication> findByStatusOrderByCreatedTimeDesc(String status, Pageable pageable);

    /**
     * 查找所有申请（管理员用）
     */
    Page<RoleApplication> findAllByOrderByCreatedTimeDesc(Pageable pageable);

    /**
     * 检查用户是否有待审核的申请
     */
    @Query("SELECT COUNT(a) > 0 FROM RoleApplication a WHERE a.user = :user AND a.status = 'PENDING'")
    boolean hasPendingApplication(@Param("user") User user);

    /**
     * 统计待审核申请数量
     */
    long countByStatus(String status);

    /**
     * 根据ID查询申请记录，同时加载关联的User对象（使用 LEFT JOIN FETCH）
     * 使用 LEFT JOIN 确保即使 user_id 为 null 也能返回申请记录
     */
    @Query("SELECT ra FROM RoleApplication ra LEFT JOIN FETCH ra.user LEFT JOIN FETCH ra.reviewer WHERE ra.id = :id")
    Optional<RoleApplication> findApplicationWithUser(@Param("id") Long id);
}
