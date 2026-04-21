package com.sports.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "registration")
public class Registration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, insertable = false, updatable = false)
    private Event event;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id", nullable = false, insertable = false, updatable = false)
    private Athlete athlete;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_type_id", nullable = false, insertable = false, updatable = false)
    private SportType sportType;
    
    @Column(name = "event_id", nullable = false)
    private Long eventId;
    
    @Column(name = "athlete_id", nullable = false)
    private Long athleteId;
    
    @Column(name = "sport_type_id", nullable = false)
    private Long sportTypeId;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "bib_number", length = 20)
    private String bibNumber;
    
    @Column(name = "`group`", length = 20)
    private String group;
    
    @Column(name = "lane")
    private Integer lane;
    
    @Column(name = "seed_score", length = 50)
    private String seedScore;
    
    @Column(name = "remark", length = 500)
    private String remark;
    
    @Column(name = "reviewer_id")
    private Long reviewerId;
    
    @Column(name = "review_time")
    private LocalDateTime reviewTime;
    
    @Column(name = "review_comment", length = 500)
    private String reviewComment;
    
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
