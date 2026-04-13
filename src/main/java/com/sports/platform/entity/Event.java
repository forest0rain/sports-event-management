package com.sports.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 赛事实体类
 */
@Entity
@Table(name = "event")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String shortName;

    @Column(length = 500)
    private String description;

    @Column(length = 200)
    private String coverImage;

    // 赛事类型: 校园、企业、社区、市级、省级
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String eventType = "校园";

    // 赛事状态: DRAFT(草稿), REGISTRATION(报名中), 
    // ONGOING(进行中), FINISHED(已结束), CANCELLED(已取消)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "DRAFT";

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private LocalDate registrationDeadline;

    @Column(length = 200)
    private String organizer;

    @Column(length = 200)
    private String sponsor;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxParticipants = 100;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentParticipants = 0;

    @Column(length = 500)
    private String rules;

    @Column(length = 500)
    private String awards;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPublic = true;

    // 报名限制: ALL(所有人), ATHLETE(仅运动员), STUDENT(学生), STAFF(教职工)
    @Column(length = 20)
    @Builder.Default
    private String registrationScope = "ALL";

    // 报名是否需要审核
    @Column(nullable = false)
    @Builder.Default
    private Boolean requireApproval = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @LastModifiedDate
    private LocalDateTime updatedTime;

    // 关联运动项目
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SportType> sportTypes = new ArrayList<>();

    // 关联赛程
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Schedule> schedules = new ArrayList<>();

    // 关联报名
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Registration> registrations = new ArrayList<>();
}
