package com.sports.platform.repository;

import com.sports.platform.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 场地数据访问层
 */
@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    /**
     * 根据类型查询
     */
    List<Venue> findByType(String type);

    /**
     * 查询启用的场地
     */
    List<Venue> findByEnabledTrue();

    /**
     * 根据名称模糊查询
     */
    List<Venue> findByNameContaining(String name);
}
