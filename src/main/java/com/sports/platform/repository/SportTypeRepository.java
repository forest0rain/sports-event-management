package com.sports.platform.repository;

import com.sports.platform.entity.SportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 运动项目数据访问层
 */
@Repository
public interface SportTypeRepository extends JpaRepository<SportType, Long> {

    /**
     * 根据类别查询
     */
    List<SportType> findByCategory(String category);

    /**
     * 查询启用的项目
     */
    List<SportType> findByEnabledTrueOrderBySortOrderAsc();

    /**
     * 根据赛事查询项目
     */
    @Query("SELECT s FROM SportType s WHERE s.event.id = :eventId")
    List<SportType> findByEventId(@Param("eventId") Long eventId);

    /**
     * 查询个人项目
     */
    List<SportType> findByIsIndividualTrue();

    /**
     * 查询计时项目
     */
    List<SportType> findByIsTimedTrue();

    /**
     * 根据名称模糊搜索
     */
    @Query("SELECT s FROM SportType s WHERE s.name LIKE %:keyword%")
    List<SportType> search(@Param("keyword") String keyword);
}
