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
 * 场地实体类
 */
@Entity
@Table(name = "venue")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 200)
    private String location;

    @Column(length = 500)
    private String description;

    // 场地类型: 田径场、体育馆、游泳池等
    @Column(nullable = false, length = 20)
    private String type;

    // 容量
    @Column(nullable = false)
    @Builder.Default
    private Integer capacity = 100;

    // 可容纳的项目数
    @Column(nullable = false)
    @Builder.Default
    private Integer maxConcurrentEvents = 1;

    // 设施描述
    @Column(length = 500)
    private String facilities;

    // 联系人
    @Column(length = 50)
    private String contactPerson;

    @Column(length = 20)
    private String contactPhone;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @LastModifiedDate
    private LocalDateTime updatedTime;

    // 关联赛程
    @OneToMany(mappedBy = "venue", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Schedule> schedules = new ArrayList<>();
}
