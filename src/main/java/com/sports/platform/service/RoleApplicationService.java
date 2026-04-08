package com.sports.platform.service;

import com.sports.platform.entity.Role;
import com.sports.platform.entity.RoleApplication;
import com.sports.platform.entity.User;
import com.sports.platform.repository.RoleApplicationRepository;
import com.sports.platform.repository.RoleRepository;
import com.sports.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色申请服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleApplicationService {

    private final RoleApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * 提交角色申请
     */
    @Transactional
    public RoleApplication submitApplication(User user, String requestedRole, String reason, 
                                              String qualification, String contactPhone,
                                              String qualificationFilePath, String qualificationFileName) {
        // 检查是否已有待审核的申请
        if (applicationRepository.hasPendingApplication(user)) {
            throw new RuntimeException("您已有待审核的申请，请等待审核结果");
        }

        // 检查是否已拥有该角色
        boolean hasRole = user.getRoles().stream()
                .anyMatch(role -> role.getCode().equals(requestedRole));
        if (hasRole) {
            throw new RuntimeException("您已拥有该角色，无需申请");
        }

        RoleApplication application = RoleApplication.builder()
                .user(user)
                .requestedRole(requestedRole)
                .reason(reason)
                .qualification(qualification)
                .contactPhone(contactPhone)
                .qualificationFile(qualificationFilePath)
                .qualificationFileName(qualificationFileName)
                .status(RoleApplication.STATUS_PENDING)
                .build();

        application = applicationRepository.save(application);
        log.info("用户 {} 提交角色申请: {}", user.getUsername(), requestedRole);
        
        return application;
    }

    /**
     * 审核通过
     */
    @Transactional
    public void approveApplication(Long applicationId, Long reviewerId, String comment) {
        RoleApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("申请不存在"));

        if (!RoleApplication.STATUS_PENDING.equals(application.getStatus())) {
            throw new RuntimeException("该申请已被处理");
        }

        // 获取审核人
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("审核人不存在"));

        // 获取申请的角色
        Role role = roleRepository.findByCode(application.getRequestedRole())
                .orElseThrow(() -> new RuntimeException("角色不存在: " + application.getRequestedRole()));

        // 更新用户角色
        User user = application.getUser();
        user.addRole(role);
        userRepository.save(user);

        // 更新申请状态
        application.setStatus(RoleApplication.STATUS_APPROVED);
        application.setReviewer(reviewer);
        application.setReviewComment(comment);
        application.setReviewTime(LocalDateTime.now());
        applicationRepository.save(application);

        log.info("角色申请 {} 已通过，审核人: {}", applicationId, reviewer.getUsername());
    }

    /**
     * 审核拒绝
     */
    @Transactional
    public void rejectApplication(Long applicationId, Long reviewerId, String comment) {
        RoleApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("申请不存在"));

        if (!RoleApplication.STATUS_PENDING.equals(application.getStatus())) {
            throw new RuntimeException("该申请已被处理");
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("审核人不存在"));

        application.setStatus(RoleApplication.STATUS_REJECTED);
        application.setReviewer(reviewer);
        application.setReviewComment(comment);
        application.setReviewTime(LocalDateTime.now());
        applicationRepository.save(application);

        log.info("角色申请 {} 已拒绝，审核人: {}", applicationId, reviewer.getUsername());
    }

    /**
     * 获取用户的申请记录
     */
    public List<RoleApplication> getUserApplications(User user) {
        return applicationRepository.findByUserOrderByCreatedTimeDesc(user);
    }

    /**
     * 获取待审核的申请列表
     */
    public Page<RoleApplication> getPendingApplications(Pageable pageable) {
        return applicationRepository.findByStatusOrderByCreatedTimeDesc(
                RoleApplication.STATUS_PENDING, pageable);
    }

    /**
     * 获取所有申请列表
     */
    public Page<RoleApplication> getAllApplications(Pageable pageable) {
        return applicationRepository.findAllByOrderByCreatedTimeDesc(pageable);
    }

    /**
     * 检查用户是否有待审核的申请
     */
    public boolean hasPendingApplication(User user) {
        return applicationRepository.hasPendingApplication(user);
    }

    /**
     * 获取待审核申请数量
     */
    public long getPendingCount() {
        return applicationRepository.countByStatus(RoleApplication.STATUS_PENDING);
    }

    /**
     * 获取申请详情
     */
    public RoleApplication getApplicationById(Long id) {
        RoleApplication application = applicationRepository.findByIdWithUser(id)
                .orElseThrow(() -> new RuntimeException("申请不存在"));

        // 调试日志
        log.info("获取申请详情 - Application ID: {}, User: {}, User is null: {}",
                application.getId(),
                application.getUser() != null ? application.getUser().getUsername() : "NULL",
                application.getUser() == null);

        // 如果 User 为 null，尝试使用 JOIN FETCH 方法
        if (application.getUser() == null) {
            log.warn("User 为 null，尝试使用 JOIN FETCH 方法");
            application = applicationRepository.findByIdWithUserFetch(id)
                    .orElseThrow(() -> new RuntimeException("申请不存在"));
            log.info("JOIN FETCH 方法结果 - User is null: {}", application.getUser() == null);
        }

        return application;
    }
}
