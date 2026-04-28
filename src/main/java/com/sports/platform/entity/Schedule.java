package com.sports.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 赛程安排实体类
 */
@Entity
@Table(name = "schedule")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联赛事
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // 关联运动项目
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_type_id", nullable = false)
    private SportType sportType;

    // 关联场地 (允许为null，支持重置)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    // 赛程名称
    @Column(nullable = false, length = 100)
    private String name;

    // 轮次类型: PRELIMINARY(预赛), SEMI_FINAL(半决赛), FINAL(决赛)
    @Column(nullable = false, length = 20)
    private String roundType;

    // 组别(第几组)
    @Column(nullable = false)
    private Integer groupNumber;

    // 比赛日期 (允许为null，支持重置)
    @Column
    private LocalDate date;

    // 开始时间 (允许为null，支持重置)
    @Column
    private LocalTime startTime;

    // 结束时间
    private LocalTime endTime;

    // 组别名称(如: 男子组、女子组)
    @Column(length = 20)
    private String groupName;

    // 赛程状态: SCHEDULED(已安排), ONGOING(进行中), 
    // COMPLETED(已完成), CANCELLED(已取消), POSTPONED(延期)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "SCHEDULED";

    // 参赛人数
    @Column(nullable = false)
    @Builder.Default
    private Integer participantCount = 0;

    // 备注
    @Column(length = 500)
    private String remark;

    // 主裁判
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chief_referee_id")
    private User chiefReferee;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @LastModifiedDate
    private LocalDateTime updatedTime;

    // 关联成绩
    @OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Result> results = new ArrayList<>();
}
