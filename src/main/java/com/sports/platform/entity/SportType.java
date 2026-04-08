package com.sports.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 运动项目实体类
 */
@Entity
@Table(name = "sport_type")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SportType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String code;

    @Column(length = 200)
    private String description;

    // 项目类型: 田径、球类、游泳、体操等
    @Column(nullable = false, length = 20)
    private String category;

    // 是否个人项目
    @Column(nullable = false)
    @Builder.Default
    private Boolean isIndividual = true;

    // 是否计时项目
    @Column(nullable = false)
    @Builder.Default
    private Boolean isTimed = false;

    // 是否计分项目
    @Column(nullable = false)
    @Builder.Default
    private Boolean isScored = true;

    // 单位: 秒、米、分等
    @Column(length = 10)
    private String unit;

    // 每组人数
    @Column(nullable = false)
    @Builder.Default
    private Integer groupSize = 8;

    // 排序号
    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @LastModifiedDate
    private LocalDateTime updatedTime;

    // 关联赛程
    @OneToMany(mappedBy = "sportType", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Schedule> schedules = new ArrayList<>();

    // 关联成绩
    @OneToMany(mappedBy = "sportType", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Result> results = new ArrayList<>();
}
