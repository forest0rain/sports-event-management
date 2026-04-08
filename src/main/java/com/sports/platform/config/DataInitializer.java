package com.sports.platform.config;

import com.sports.platform.entity.*;
import com.sports.platform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 数据初始化器
 * 在项目启动时自动创建默认管理员账号和示例数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EventRepository eventRepository;
    private final AthleteRepository athleteRepository;
    private final SportTypeRepository sportTypeRepository;
    private final VenueRepository venueRepository;
    private final ScheduleRepository scheduleRepository;
    private final ResultRepository resultRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已有管理员
        if (userRepository.findByUsername("admin").isEmpty()) {
            log.info("开始初始化默认数据...");
            
            // 创建角色
            Role adminRole = createRoleIfNotExists("管理员", "ROLE_ADMIN", "系统管理员");
            createRoleIfNotExists("裁判", "ROLE_REFEREE", "裁判员");
            createRoleIfNotExists("运动员", "ROLE_ATHLETE", "运动员");
            createRoleIfNotExists("观众", "ROLE_SPECTATOR", "观众");
            
            // 创建管理员账号
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@sports.com")
                    .realName("系统管理员")
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);
            
            userRepository.save(admin);
            log.info("默认管理员账号创建成功: admin / admin123");
            
            // 创建运动项目类型
            SportType running = createSportType("100米短跑", "RUNNING", "田径");
            SportType longJump = createSportType("跳远", "JUMPING", "田径");
            SportType shotPut = createSportType("铅球", "THROWING", "田径");
            SportType basketball = createSportType("篮球", "TEAM", "球类");
            
            // 创建场地
            Venue stadium = createVenue("主体育场", "田径场", "A区");
            Venue gym = createVenue("综合体育馆", "室内馆", "B区");
            
            // 创建示例赛事
            Event event1 = createEvent(
                "2024年春季田径运动会",
                "春季运动会",
                "校园",
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(9),
                LocalDate.now().plusDays(5),
                "XX学校",
                "100"
            );
            
            Event event2 = createEvent(
                "市级青少年田径锦标赛",
                "市级锦标赛",
                "市级",
                LocalDate.now().plusDays(14),
                LocalDate.now().plusDays(16),
                LocalDate.now().plusDays(10),
                "市体育局",
                "200"
            );
            
            Event event3 = createEvent(
                "企业运动会",
                "企业运动",
                "企业",
                LocalDate.now().plusDays(21),
                LocalDate.now().plusDays(22),
                LocalDate.now().plusDays(18),
                "XX公司",
                "50"
            );
            
            // 创建示例运动员
            Athlete athlete1 = createAthlete("张三", "M", LocalDate.of(2005, 3, 15), "XX学校", "U20");
            Athlete athlete2 = createAthlete("李四", "M", LocalDate.of(2006, 5, 20), "YY学校", "U18");
            Athlete athlete3 = createAthlete("王五", "M", LocalDate.of(2004, 8, 10), "XX学校", "U20");
            Athlete athlete4 = createAthlete("赵六", "F", LocalDate.of(2005, 11, 25), "ZZ学校", "U20");
            Athlete athlete5 = createAthlete("钱七", "F", LocalDate.of(2007, 2, 8), "AA学校", "U18");
            
            // 创建示例赛程
            Schedule schedule1 = createSchedule(
                event1, running, stadium,
                "男子100米预赛",
                "PRELIMINARY", 1, LocalDate.now().plusDays(7),
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                "男子组"
            );
            
            Schedule schedule2 = createSchedule(
                event1, longJump, stadium,
                "男子跳远决赛",
                "FINAL", 1, LocalDate.now().plusDays(7),
                LocalTime.of(14, 0), LocalTime.of(16, 0),
                "男子组"
            );
            
            // 创建示例成绩
            createResult(schedule1, athlete1, running, new BigDecimal("10.58"), "10.58秒", 1, "VALID");
            createResult(schedule1, athlete2, running, new BigDecimal("10.85"), "10.85秒", 2, "VALID");
            createResult(schedule1, athlete3, running, new BigDecimal("11.02"), "11.02秒", 3, "VALID");
            createResult(schedule2, athlete1, longJump, new BigDecimal("7.25"), "7.25米", 1, "VALID");
            createResult(schedule2, athlete3, longJump, new BigDecimal("6.98"), "6.98米", 2, "VALID");
            
            log.info("示例数据创建完成");
        } else {
            log.info("管理员账号已存在，跳过初始化");
        }
    }
    
    private Role createRoleIfNotExists(String name, String code, String description) {
        return roleRepository.findByCode(code)
                .orElseGet(() -> {
                    Role role = Role.builder()
                            .name(name)
                            .code(code)
                            .description(description)
                            .enabled(true)
                            .build();
                    return roleRepository.save(role);
                });
    }
    
    private SportType createSportType(String name, String category, String discipline) {
        SportType sportType = SportType.builder()
                .name(name)
                .category(category)
                .enabled(true)
                .build();
        return sportTypeRepository.save(sportType);
    }
    
    private Venue createVenue(String name, String type, String location) {
        Venue venue = Venue.builder()
                .name(name)
                .type(type)
                .location(location)
                .enabled(true)
                .build();
        return venueRepository.save(venue);
    }
    
    private Event createEvent(String name, String shortName, String eventType, 
                              LocalDate startDate, LocalDate endDate, 
                              LocalDate registrationDeadline, String organizer, String maxParticipants) {
        Event event = Event.builder()
                .name(name)
                .shortName(shortName)
                .eventType(eventType)
                .startDate(startDate)
                .endDate(endDate)
                .registrationDeadline(registrationDeadline)
                .organizer(organizer)
                .maxParticipants(Integer.parseInt(maxParticipants))
                .status("REGISTRATION")
                .isPublic(true)
                .currentParticipants(3)
                .build();
        return eventRepository.save(event);
    }
    
    private Athlete createAthlete(String name, String gender, LocalDate birthDate, 
                                   String organization, String ageGroup) {
        Athlete athlete = Athlete.builder()
                .name(name)
                .gender(gender)
                .birthDate(birthDate)
                .organization(organization)
                .ageGroup(ageGroup)
                .enabled(true)
                .competitionCount(2)
                .awardCount(1)
                .build();
        return athleteRepository.save(athlete);
    }
    
    private Schedule createSchedule(Event event, SportType sportType, Venue venue,
                                     String name, String roundType, Integer groupNumber,
                                     LocalDate date, LocalTime startTime, LocalTime endTime,
                                     String groupName) {
        Schedule schedule = Schedule.builder()
                .event(event)
                .sportType(sportType)
                .venue(venue)
                .name(name)
                .roundType(roundType)
                .groupNumber(groupNumber)
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .groupName(groupName)
                .status("SCHEDULED")
                .participantCount(3)
                .build();
        return scheduleRepository.save(schedule);
    }
    
    private Result createResult(Schedule schedule, Athlete athlete, SportType sportType,
                                 BigDecimal score, String scoreText, Integer rank, String status) {
        Result result = Result.builder()
                .schedule(schedule)
                .athlete(athlete)
                .sportType(sportType)
                .score(score)
                .scoreText(scoreText)
                .rank(rank)
                .status(status)
                .isPersonalBest(true)
                .build();
        return resultRepository.save(result);
    }
}
