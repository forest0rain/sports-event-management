# 体育赛事赛程与运动员信息管理平台

## 项目简介

本系统是一个基于 Spring Boot 的体育赛事管理平台，旨在解决中小型体育赛事管理痛点，提供赛程安排、运动员管理、成绩统计、信息共享等一体化数字解决方案。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.0 | 核心框架 |
| Spring Security | 6.x | 安全框架 |
| Spring Data JPA | 3.x | ORM框架 |
| MySQL | 8.0 | 数据库 |
| Thymeleaf | 3.x | 模板引擎 |
| Bootstrap | 5.3 | 前端框架 |
| ECharts | 5.x | 数据可视化 |

## 功能模块

### 1. 用户与权限管理
- **RBAC角色模型**: 管理员、裁判、运动员、观众四种角色
- **权限控制**: 基于Spring Security的细粒度权限控制
- **密码安全**: BCrypt加密存储

### 2. 赛事基础信息管理
- 赛事创建、编辑、删除、发布
- 运动项目管理
- 场地资源管理
- 赛事状态流转: 草稿 → 报名中 → 进行中 → 已结束

### 3. 运动员信息管理
- 运动员档案CRUD
- **个性化标签体系**:
  - 项目特长（短跑、跳跃、投掷等）
  - 年龄段（U18、U20、U23、OPEN）
  - 历史成绩亮点
  - 技术特点
  - 自定义标签

### 4. 赛程管理
- 在线报名与审核
- **智能赛程编排算法**（分阶段贪婪策略）:
  - 第一阶段: 按项目和性别分组
  - 第二阶段: 根据场地时间贪婪填充
  - 优先安排决赛
- 赛程表自动生成与人工调整

### 5. 成绩管理
- 裁判录入成绩
- 系统自动计算排名
- 破纪录检测
- ECharts数据可视化展示

## 项目结构

```
sports-event-management/
├── pom.xml                          # Maven配置
├── src/
│   └── main/
│       ├── java/com/sports/platform/
│       │   ├── SportsPlatformApplication.java    # 启动类
│       │   ├── config/              # 配置类
│       │   │   ├── SecurityConfig.java
│       │   │   └── ThymeleafConfig.java
│       │   ├── entity/              # 实体类
│       │   │   ├── User.java
│       │   │   ├── Role.java
│       │   │   ├── Permission.java
│       │   │   ├── Event.java
│       │   │   ├── SportType.java
│       │   │   ├── Venue.java
│       │   │   ├── Athlete.java
│       │   │   ├── Registration.java
│       │   │   ├── Schedule.java
│       │   │   └── Result.java
│       │   ├── repository/          # 数据访问层
│       │   ├── service/             # 业务逻辑层
│       │   │   ├── UserService.java
│       │   │   ├── EventService.java
│       │   │   ├── AthleteService.java
│       │   │   ├── ScheduleService.java
│       │   │   ├── RegistrationService.java
│       │   │   └── ResultService.java
│       │   └── controller/          # 控制器层
│       │       ├── AuthController.java
│       │       ├── EventController.java
│       │       ├── AthleteController.java
│       │       ├── ScheduleController.java
│       │       └── ResultController.java
│       └── resources/
│           ├── application.yml      # 主配置
│           ├── application-dev.yml  # 开发配置
│           ├── db/
│           │   └── schema.sql      # 数据库初始化
│           ├── templates/           # Thymeleaf模板
│           │   ├── layout/
│           │   ├── auth/
│           │   ├── dashboard/
│           │   ├── event/
│           │   ├── athlete/
│           │   ├── schedule/
│           │   └── result/
│           └── static/              # 静态资源
│               ├── css/
│               └── js/
└── README.md
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/your-repo/sports-event-management.git
cd sports-event-management
```

2. **创建数据库**
```sql
CREATE DATABASE sports_platform 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

3. **修改配置**
编辑 `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sports_platform
    username: root
    password: your_password
```

4. **运行初始化脚本**
```bash
mysql -u root -p sports_platform < src/main/resources/db/schema.sql
```

5. **启动项目**
```bash
# 使用Maven
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/sports-event-management-1.0.0.jar
```

6. **访问系统**
- 地址: http://localhost:8080
- 默认管理员: `admin` / `admin123`

### 使用H2数据库（开发环境）

1. 激活开发配置:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

2. 访问H2控制台: http://localhost:8080/h2-console
   - JDBC URL: `jdbc:h2:mem:sports_platform`
   - 用户名: `sa`
   - 密码: (空)

## 核心功能说明

### 智能赛程编排算法

系统采用**分阶段贪婪策略**实现智能赛程编排:

```
算法流程:
1. 第一阶段: 分组
   - 按运动项目分组
   - 按性别进一步细分
   
2. 第二阶段: 时间槽分配
   - 遍历每个分组
   - 贪婪选择最早可用的时间槽
   - 检查场地冲突
   
3. 优化策略:
   - 决赛优先安排
   - 预估比赛时长
   - 自动避让休息时间
```

### 运动员标签体系

支持JSON格式存储灵活的标签数据:

```json
{
  "specialties": ["短跑", "跳远"],
  "ageGroup": "U23",
  "technicalFeatures": {
    "短跑": "起跑快",
    "跳远": "助跑稳定"
  },
  "highlights": ["2023年校运会100米冠军"],
  "customTags": ["潜力新星", "种子选手"],
  "personalBests": {
    "100米": "10.58",
    "跳远": "7.25"
  }
}
```

## 安全设计

- **密码加密**: BCrypt算法
- **CSRF防护**: Spring Security默认开启
- **SQL注入防护**: JPA参数化查询
- **XSS防护**: Thymeleaf自动转义
- **权限控制**: 方法级@PreAuthorize

## 扩展开发

### 添加新的运动项目类型

1. 在 `SportType` 实体中添加新字段
2. 更新 `ScheduleService.estimateDuration()` 方法
3. 在前端添加对应的表单选项

### 集成其他数据库

修改 `pom.xml` 和 `application.yml`:
```xml
<!-- PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sports_platform
    driver-class-name: org.postgresql.Driver
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

## 常见问题

**Q: 启动时报数据库连接错误?**
A: 检查MySQL服务是否启动，数据库是否已创建，用户名密码是否正确。

**Q: 登录时提示用户不存在?**
A: 确保已运行 `schema.sql` 初始化脚本，默认管理员账户会自动创建。

**Q: 如何修改默认端口?**
A: 在 `application.yml` 中修改 `server.port` 配置。

## 许可证

MIT License

## 联系方式

如有问题或建议，请提交 Issue 或 Pull Request。

---

**注意**: 本系统适合中小型体育赛事（校园、企业、社区赛事），聚焦核心功能，避免过度设计，易于部署和维护。
