package com.sports.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 参赛报名实体类
 */
@Entity
@Table(name = "registration")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联赛事
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // 关联运动员（可选，非运动员报名时为空）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id")
    private Athlete athlete;

    // 关联报名用户（非运动员报名时使用）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 报名人姓名
    @Column(length = 50)
    private String registrantName;

    // 报名人联系方式
    @Column(length = 20)
    private String registrantPhone;

    // 报名人单位/班级
    @Column(length = 100)
    private String registrantOrg;

    // 关联运动项目（可选，自定义项目时使用）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_type_id")
    private SportType sportType;

    // 报名状态: PENDING(待审核), APPROVED(已通过), 
    // REJECTED(已拒绝), CANCELLED(已取消)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    // 报名号码(参赛号)
    @Column(length = 20)
    private String bibNumber;

    // 组别(如: 男子组、女子组、青年组)
    @Column(length = 20)
    private String group;

    // 道次/赛道
    private Integer lane;

    // 种子成绩(用于分组)
    private String seedScore;

    // 报名备注
    @Column(length = 500)
    private String remark;

    // 审核人
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    // 审核时间
    private LocalDateTime reviewTime;

    // 审核意见
    @Column(length = 500)
    private String reviewComment;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @LastModifiedDate
    private LocalDateTime updatedTime;
}
