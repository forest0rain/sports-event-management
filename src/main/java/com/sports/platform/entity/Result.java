package com.sports.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩实体类
 */
@Entity
@Table(name = "result")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联赛程
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    // 关联运动员
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id", nullable = false)
    private Athlete athlete;

    // 关联运动项目
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_type_id", nullable = false)
    private SportType sportType;

    // 关联报名
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id")
    private Registration registration;

    // 道次/赛道
    private Integer lane;

    // 参赛号码
    @Column(length = 20)
    private String bibNumber;

    // 成绩值 (秒/米/分等，根据项目类型)
    @Column(precision = 10, scale = 3)
    private BigDecimal score;

    // 成绩文本表示 (如: 10.58秒, 7.25米)
    @Column(length = 50)
    private String scoreText;

    // 排名
    private Integer rank;

    // 是否破纪录: 无、校纪录、市纪录、省纪录、国家纪录
    @Column(length = 20)
    @Builder.Default
    private String recordType = "无";

    // 是否PB(个人最佳)
    @Column(nullable = false)
    @Builder.Default
    private Boolean isPersonalBest = false;

    // 是否SB(赛季最佳)
    @Column(nullable = false)
    @Builder.Default
    private Boolean isSeasonBest = false;

    // 风速(田径项目)
    private BigDecimal windSpeed;

    // 得分(计分项目)
    private Integer points;

    // 成绩状态: VALID(有效), INVALID(无效), DNS(未起跑), 
    // DNF(未完赛), DQ(取消资格)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "VALID";

    // 备注
    @Column(length = 500)
    private String remark;

    // 录入裁判
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referee_id")
    private User referee;

    // 录入时间
    private LocalDateTime recordTime;

    // 成绩类型: 预赛成绩、决赛成绩等
    @Column(length = 20)
    private String resultType;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @LastModifiedDate
    private LocalDateTime updatedTime;
}
