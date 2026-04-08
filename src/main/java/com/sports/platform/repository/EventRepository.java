package com.sports.platform.repository;

import com.sports.platform.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 赛事数据访问层
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * 根据状态查询赛事
     */
    Page<Event> findByStatus(String status, Pageable pageable);

    /**
     * 查询公开赛事
     */
    Page<Event> findByIsPublicTrue(Pageable pageable);

    /**
     * 查询正在进行或即将开始的赛事
     */
    @Query("SELECT e FROM Event e WHERE e.status IN ('REGISTRATION', 'ONGOING') AND e.isPublic = true ORDER BY e.startDate ASC")
    List<Event> findActiveEvents();

    /**
     * 根据日期范围查询赛事
     */
    @Query("SELECT e FROM Event e WHERE e.startDate >= :startDate AND e.endDate <= :endDate")
    List<Event> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 搜索赛事
     */
    @Query("SELECT e FROM Event e WHERE e.name LIKE %:keyword% OR e.description LIKE %:keyword%")
    Page<Event> search(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据类型查询
     */
    Page<Event> findByEventType(String eventType, Pageable pageable);

    /**
     * 统计各状态赛事数量
     */
    @Query("SELECT e.status, COUNT(e) FROM Event e GROUP BY e.status")
    List<Object[]> countByStatus();
}
