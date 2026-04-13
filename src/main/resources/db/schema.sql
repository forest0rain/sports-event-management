-- ============================================
-- 体育赛事管理平台 - 数据库初始化脚本
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS sports_platform 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE sports_platform;

-- ============================================
-- 1. 权限表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '权限名称',
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限代码',
    description VARCHAR(200) COMMENT '描述',
    resource VARCHAR(200) COMMENT '资源路径',
    method VARCHAR(20) COMMENT '请求方法',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_code (code)
) ENGINE=InnoDB COMMENT='权限表';

-- ============================================
-- 2. 角色表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色代码',
    description VARCHAR(200) COMMENT '描述',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_code (code)
) ENGINE=InnoDB COMMENT='角色表';

-- ============================================
-- 3. 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    real_name VARCHAR(50) COMMENT '真实姓名',
    avatar VARCHAR(255) COMMENT '头像URL',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账户未过期',
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账户未锁定',
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE COMMENT '凭证未过期',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB COMMENT='用户表';

-- ============================================
-- 4. 角色-权限关联表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='角色权限关联表';

-- ============================================
-- 5. 用户-角色关联表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='用户角色关联表';

-- ============================================
-- 6. 场地表
-- ============================================
CREATE TABLE IF NOT EXISTS venue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '场地名称',
    location VARCHAR(200) COMMENT '位置',
    description VARCHAR(500) COMMENT '描述',
    type VARCHAR(20) NOT NULL COMMENT '类型',
    capacity INT NOT NULL DEFAULT 100 COMMENT '容量',
    max_concurrent_events INT NOT NULL DEFAULT 1 COMMENT '最大并发赛事',
    facilities VARCHAR(500) COMMENT '设施',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='场地表';

-- ============================================
-- 7. 赛事表
-- ============================================
CREATE TABLE IF NOT EXISTS event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '赛事名称',
    short_name VARCHAR(20) COMMENT '简称',
    description VARCHAR(500) COMMENT '描述',
    cover_image VARCHAR(200) COMMENT '封面图',
    event_type VARCHAR(20) NOT NULL DEFAULT '校园' COMMENT '赛事类型',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    registration_deadline DATE COMMENT '报名截止日期',
    organizer VARCHAR(200) COMMENT '主办方',
    sponsor VARCHAR(200) COMMENT '赞助商',
    max_participants INT NOT NULL DEFAULT 100 COMMENT '最大参赛人数',
    current_participants INT NOT NULL DEFAULT 0 COMMENT '当前参赛人数',
    rules VARCHAR(500) COMMENT '规则',
    awards VARCHAR(500) COMMENT '奖励',
    is_public BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否公开',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_dates (start_date, end_date)
) ENGINE=InnoDB COMMENT='赛事表';

-- ============================================
-- 8. 运动项目表
-- ============================================
CREATE TABLE IF NOT EXISTS sport_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '项目名称',
    code VARCHAR(20) COMMENT '项目代码',
    description VARCHAR(200) COMMENT '描述',
    category VARCHAR(20) NOT NULL COMMENT '项目类别',
    is_individual BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否个人项目',
    is_timed BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否计时项目',
    is_scored BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否计分项目',
    unit VARCHAR(10) COMMENT '单位',
    group_size INT NOT NULL DEFAULT 8 COMMENT '每组人数',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    event_id BIGINT COMMENT '所属赛事',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE SET NULL,
    INDEX idx_category (category)
) ENGINE=InnoDB COMMENT='运动项目表';

-- ============================================
-- 9. 运动员表
-- ============================================
CREATE TABLE IF NOT EXISTS athlete (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE COMMENT '关联用户ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender CHAR(1) NOT NULL COMMENT '性别',
    birth_date DATE NOT NULL COMMENT '出生日期',
    id_card VARCHAR(20) UNIQUE COMMENT '身份证号',
    nationality VARCHAR(50) COMMENT '国籍',
    province VARCHAR(100) COMMENT '省份',
    city VARCHAR(100) COMMENT '城市',
    organization VARCHAR(200) COMMENT '所属单位',
    coach VARCHAR(50) COMMENT '教练',
    phone VARCHAR(20) COMMENT '电话',
    email VARCHAR(100) COMMENT '邮箱',
    emergency_contact VARCHAR(50) COMMENT '紧急联系人',
    emergency_phone VARCHAR(20) COMMENT '紧急联系电话',
    avatar VARCHAR(200) COMMENT '头像',
    bio VARCHAR(500) COMMENT '简介',
    specialties TEXT COMMENT '项目特长(JSON)',
    age_group VARCHAR(10) COMMENT '年龄段',
    technical_features TEXT COMMENT '技术特点(JSON)',
    highlights TEXT COMMENT '历史亮点(JSON)',
    custom_tags TEXT COMMENT '自定义标签(JSON)',
    personal_bests TEXT COMMENT '个人最佳(JSON)',
    height INT COMMENT '身高(cm)',
    weight INT COMMENT '体重(kg)',
    competition_count INT NOT NULL DEFAULT 0 COMMENT '参赛次数',
    award_count INT NOT NULL DEFAULT 0 COMMENT '获奖次数',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE SET NULL,
    INDEX idx_gender (gender),
    INDEX idx_age_group (age_group),
    INDEX idx_organization (organization(50))
) ENGINE=InnoDB COMMENT='运动员表';

-- ============================================
-- 10. 参赛报名表
-- ============================================
CREATE TABLE IF NOT EXISTS registration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL COMMENT '赛事ID',
    athlete_id BIGINT NOT NULL COMMENT '运动员ID',
    sport_type_id BIGINT NOT NULL COMMENT '项目ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    bib_number VARCHAR(20) COMMENT '参赛号码',
    `group` VARCHAR(20) COMMENT '组别',
    lane INT COMMENT '道次',
    seed_score VARCHAR(50) COMMENT '种子成绩',
    remark VARCHAR(500) COMMENT '备注',
    reviewer_id BIGINT COMMENT '审核人ID',
    review_time DATETIME COMMENT '审核时间',
    review_comment VARCHAR(500) COMMENT '审核意见',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    FOREIGN KEY (athlete_id) REFERENCES athlete(id) ON DELETE CASCADE,
    FOREIGN KEY (sport_type_id) REFERENCES sport_type(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES sys_user(id) ON DELETE SET NULL,
    UNIQUE KEY uk_event_athlete_sport (event_id, athlete_id, sport_type_id),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='参赛报名表';

-- ============================================
-- 11. 赛程安排表
-- ============================================
CREATE TABLE IF NOT EXISTS schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL COMMENT '赛事ID',
    sport_type_id BIGINT NOT NULL COMMENT '项目ID',
    venue_id BIGINT NOT NULL COMMENT '场地ID',
    name VARCHAR(100) NOT NULL COMMENT '赛程名称',
    round_type VARCHAR(20) NOT NULL COMMENT '轮次类型',
    group_number INT NOT NULL COMMENT '组号',
    date DATE NOT NULL COMMENT '比赛日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME COMMENT '结束时间',
    group_name VARCHAR(20) COMMENT '组别名称',
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' COMMENT '状态',
    participant_count INT NOT NULL DEFAULT 0 COMMENT '参赛人数',
    remark VARCHAR(500) COMMENT '备注',
    chief_referee_id BIGINT COMMENT '主裁判ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    FOREIGN KEY (sport_type_id) REFERENCES sport_type(id) ON DELETE CASCADE,
    FOREIGN KEY (venue_id) REFERENCES venue(id) ON DELETE CASCADE,
    FOREIGN KEY (chief_referee_id) REFERENCES sys_user(id) ON DELETE SET NULL,
    INDEX idx_date (date),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='赛程安排表';

-- ============================================
-- 12. 成绩表
-- ============================================
CREATE TABLE IF NOT EXISTS result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL COMMENT '赛程ID',
    athlete_id BIGINT NOT NULL COMMENT '运动员ID',
    sport_type_id BIGINT NOT NULL COMMENT '项目ID',
    registration_id BIGINT COMMENT '报名ID',
    lane INT COMMENT '道次',
    bib_number VARCHAR(20) COMMENT '参赛号码',
    score DECIMAL(10,3) COMMENT '成绩值',
    score_text VARCHAR(50) COMMENT '成绩文本',
    rank INT COMMENT '排名',
    record_type VARCHAR(20) DEFAULT '无' COMMENT '破纪录类型',
    is_personal_best BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否PB',
    is_season_best BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否SB',
    wind_speed DECIMAL(5,2) COMMENT '风速',
    points INT COMMENT '得分',
    status VARCHAR(20) NOT NULL DEFAULT 'VALID' COMMENT '状态',
    remark VARCHAR(500) COMMENT '备注',
    referee_id BIGINT COMMENT '录入裁判ID',
    record_time DATETIME COMMENT '录入时间',
    result_type VARCHAR(20) COMMENT '成绩类型',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (schedule_id) REFERENCES schedule(id) ON DELETE CASCADE,
    FOREIGN KEY (athlete_id) REFERENCES athlete(id) ON DELETE CASCADE,
    FOREIGN KEY (sport_type_id) REFERENCES sport_type(id) ON DELETE CASCADE,
    FOREIGN KEY (registration_id) REFERENCES registration(id) ON DELETE SET NULL,
    FOREIGN KEY (referee_id) REFERENCES sys_user(id) ON DELETE SET NULL,
    INDEX idx_rank (rank),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='成绩表';

-- ============================================
-- 初始化数据
-- ============================================

-- 插入角色
INSERT INTO sys_role (name, code, description) VALUES
('管理员', 'ROLE_ADMIN', '系统管理员，拥有所有权限'),
('裁判', 'ROLE_REFEREE', '裁判员，可录入成绩'),
('运动员', 'ROLE_ATHLETE', '运动员，可查看个人信息和报名'),
('观众', 'ROLE_SPECTATOR', '观众，仅可查看公开信息');

-- 插入权限
INSERT INTO sys_permission (name, code, description, resource) VALUES
('用户管理', 'user:manage', '管理用户', '/admin/users'),
('赛事管理', 'event:manage', '管理赛事', '/events'),
('运动员管理', 'athlete:manage', '管理运动员', '/athletes'),
('赛程管理', 'schedule:manage', '管理赛程', '/schedules'),
('成绩管理', 'result:manage', '管理成绩', '/results'),
('成绩录入', 'result:record', '录入成绩', '/results/record'),
('报名审核', 'registration:review', '审核报名', '/registrations');

-- 为管理员分配所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

-- 为裁判分配成绩录入权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission WHERE code IN ('result:record', 'result:manage');

-- 插入默认管理员账户 (密码: admin123，BCrypt加密)
INSERT INTO sys_user (username, password, email, real_name, enabled) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@sports.com', '系统管理员', TRUE);

-- 为管理员分配角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 插入示例场地
INSERT INTO venue (name, location, type, capacity, description) VALUES
('中心体育场', '校园东区', '田径场', 5000, '标准400米塑胶跑道'),
('室内体育馆', '校园西区', '体育馆', 2000, '多功能室内场馆'),
('游泳馆', '校园北区', '游泳馆', 800, '标准50米泳道');

-- 插入示例赛事
INSERT INTO event (name, short_name, event_type, status, start_date, end_date, registration_deadline, organizer, max_participants) VALUES
('2024年春季运动会', '春季运动会', '校园', 'REGISTRATION', '2024-04-15', '2024-04-17', '2024-04-10', '体育部', 500);

-- 插入运动项目
INSERT INTO sport_type (name, code, category, is_individual, is_timed, unit, group_size) VALUES
('男子100米', 'M100', '田径', TRUE, TRUE, '秒', 8),
('男子200米', 'M200', '田径', TRUE, TRUE, '秒', 8),
('女子100米', 'W100', '田径', TRUE, TRUE, '秒', 8),
('男子跳远', 'MLJ', '田径', TRUE, FALSE, '米', 12),
('男子铅球', 'MSP', '田径', TRUE, FALSE, '米', 10),
('男子100米自由泳', 'M100F', '游泳', TRUE, TRUE, '秒', 8);

COMMIT;
