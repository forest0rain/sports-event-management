package com.sports.platform.config;

import com.sports.platform.entity.Role;
import com.sports.platform.entity.SportType;
import com.sports.platform.repository.RoleRepository;
import com.sports.platform.repository.SportTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final SportTypeRepository sportTypeRepository;

    @Override
    public void run(String... args) {
        initRole("ROLE_ADMIN", "管理员", "系统管理员");
        initRole("ROLE_SPECTATOR", "观众", "普通观众");
        initRole("ROLE_ATHLETE", "运动员", "运动员角色");
        initRole("ROLE_REFEREE", "裁判", "裁判角色");
        
        // 初始化运动项目
        initSportTypes();
    }

    private void initRole(String code, String name, String description) {
        boolean exists = roleRepository.findByCode(code).isPresent();
        if (exists) {
            System.out.println("角色已存在，跳过初始化：" + code);
            return;
        }

        Role role = new Role();
        role.setCode(code);
        role.setName(name);
        role.setDescription(description);
        role.setCreatedTime(LocalDateTime.now());

        roleRepository.save(role);
        System.out.println("初始化角色成功：" + code + " - " + name);
    }
    
    private void initSportTypes() {
        // 检查是否已有运动项目
        if (sportTypeRepository.count() > 0) {
            System.out.println("运动项目已存在，跳过初始化");
            return;
        }
        
        // 田径项目
        createSportType("100米", "100米短跑", "田径", true, true, "秒", 8, 1);
        createSportType("200米", "200米短跑", "田径", true, true, "秒", 8, 2);
        createSportType("400米", "400米中跑", "田径", true, true, "秒", 8, 3);
        createSportType("800米", "800米中跑", "田径", true, true, "秒", 8, 4);
        createSportType("1500米", "1500米长跑", "田径", true, true, "秒", 8, 5);
        createSportType("5000米", "5000米长跑", "田径", true, true, "秒", 12, 6);
        createSportType("跳远", "跳远", "田径", true, false, "米", 8, 7);
        createSportType("跳高", "跳高", "田径", true, false, "米", 8, 8);
        createSportType("铅球", "铅球", "田径", true, false, "米", 8, 9);
        createSportType("标枪", "标枪", "田径", true, false, "米", 8, 10);
        createSportType("铁饼", "铁饼", "田径", true, false, "米", 8, 11);
        createSportType("4×100米接力", "4×100米接力", "田径", false, true, "秒", 4, 12);
        createSportType("4×400米接力", "4×400米接力", "田径", false, true, "秒", 4, 13);
        
        // 球类项目
        createSportType("篮球", "篮球比赛", "球类", false, false, "分", 10, 20);
        createSportType("足球", "足球比赛", "球类", false, false, "分", 22, 21);
        createSportType("排球", "排球比赛", "球类", false, false, "分", 12, 22);
        createSportType("乒乓球", "乒乓球单打", "球类", true, false, "分", 2, 23);
        createSportType("羽毛球", "羽毛球单打", "球类", true, false, "分", 2, 24);
        createSportType("网球", "网球单打", "球类", true, false, "分", 2, 25);
        
        // 游泳项目
        createSportType("50米自由泳", "50米自由泳", "游泳", true, true, "秒", 8, 30);
        createSportType("100米自由泳", "100米自由泳", "游泳", true, true, "秒", 8, 31);
        createSportType("100米蛙泳", "100米蛙泳", "游泳", true, true, "秒", 8, 32);
        createSportType("100米仰泳", "100米仰泳", "游泳", true, true, "秒", 8, 33);
        
        System.out.println("运动项目初始化完成");
    }
    
    private void createSportType(String name, String description, String category, 
                                  boolean isIndividual, boolean isTimed, String unit, 
                                  int groupSize, int sortOrder) {
        SportType sportType = SportType.builder()
                .name(name)
                .description(description)
                .category(category)
                .isIndividual(isIndividual)
                .isTimed(isTimed)
                .isScored(!isTimed)
                .unit(unit)
                .groupSize(groupSize)
                .sortOrder(sortOrder)
                .enabled(true)
                .createdTime(LocalDateTime.now())
                .build();
        sportTypeRepository.save(sportType);
    }
}