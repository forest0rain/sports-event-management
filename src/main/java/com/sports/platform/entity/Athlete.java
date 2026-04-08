package com.sports.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运动员实体类
 * 
 * 包含个性化标签体系:
 * - 项目特长(短跑、跳跃、投掷等)
 * - 年龄段(U18、U20、U23等)
 * - 历史成绩亮点
 * - 技术特点
 */
@Entity
@Table(name = "athlete")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Athlete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联系统用户
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    // 性别: M-男, F-女
    @Column(nullable = false, length = 1)
    private String gender;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(length = 20)
    private String idCard;

    @Column(length = 50)
    private String nationality;

    @Column(length = 100)
    private String province;

    @Column(length = 100)
    private String city;

    @Column(length = 200)
    private String organization;  // 所属单位/学校

    @Column(length = 50)
    private String coach;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 50)
    private String emergencyContact;

    @Column(length = 20)
    private String emergencyPhone;

    @Column(length = 200)
    private String avatar;

    @Column(length = 500)
    private String bio;

    // ========== 个性化标签体系 ==========
    
    // 项目特长 (JSON格式: ["短跑", "跳远", "铅球"])
    @Column(columnDefinition = "TEXT")
    private String specialties;

    // 年龄段: U18, U20, U23, OPEN
    @Column(length = 10)
    private String ageGroup;

    // 技术特点 (JSON格式: {"短跑": "起跑快", "跳远": "助跑稳定"})
    @Column(columnDefinition = "TEXT")
    private String technicalFeatures;

    // 历史成绩亮点 (JSON格式)
    @Column(columnDefinition = "TEXT")
    private String highlights;

    // 自定义标签 (JSON格式: ["潜力新星", "种子选手"])
    @Column(columnDefinition = "TEXT")
    private String customTags;

    // 个人最佳成绩 (JSON格式: {"100米": "10.5", "跳远": "7.2"})
    @Column(columnDefinition = "TEXT")
    private String personalBests;

    // 身高(cm)
    private Integer height;

    // 体重(kg)
    private Integer weight;

    // 参赛次数
    @Column(nullable = false)
    @Builder.Default
    private Integer competitionCount = 0;

    // 获奖次数
    @Column(nullable = false)
    @Builder.Default
    private Integer awardCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @LastModifiedDate
    private LocalDateTime updatedTime;

    // 关联报名
    @OneToMany(mappedBy = "athlete", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Registration> registrations = new ArrayList<>();

    // 关联成绩
    @OneToMany(mappedBy = "athlete", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Result> results = new ArrayList<>();

    // 辅助方法
    public Integer getAge() {
        if (birthDate == null) return null;
        return LocalDate.now().getYear() - birthDate.getYear();
    }

    public String determineAgeGroup() {
        Integer age = getAge();
        if (age == null) return "OPEN";
        if (age < 18) return "U18";
        if (age < 20) return "U20";
        if (age < 23) return "U23";
        return "OPEN";
    }
}
