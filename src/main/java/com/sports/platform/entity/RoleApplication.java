package com.sports.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 角色申请实体
 * 用户申请升级为运动员或裁判
 */
@Entity
@Table(name = "sys_role_application")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RoleApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 申请人
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 申请的角色编码
     */
    @Column(nullable = false, length = 50)
    private String requestedRole;

    /**
     * 申请理由
     */
    @Column(length = 500)
    private String reason;

    /**
     * 相关资质/证书信息
     */
    @Column(length = 500)
    private String qualification;

    /**
     * 资质文件路径（上传的证书图片或文件）
     */
    @Column(length = 255)
    private String qualificationFile;

    /**
     * 资质文件原始名称
     */
    @Column(length = 100)
    private String qualificationFileName;

    /**
     * 联系电话
     */
    @Column(length = 20)
    private String contactPhone;

    /**
     * 状态: PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    /**
     * 审核人
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    /**
     * 审核意见
     */
    @Column(length = 500)
    private String reviewComment;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @LastModifiedDate
    private LocalDateTime updatedTime;

    // 状态常量
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    // 可申请的角色
    public static final String ROLE_ATHLETE = "ROLE_ATHLETE";
    public static final String ROLE_REFEREE = "ROLE_REFEREE";
}
