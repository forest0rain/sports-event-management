-- ============================================
-- 体育赛事管理平台数据库表结构
-- ============================================

-- 0. 禁用外键检查（用于初始化）
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. 用户表
-- ============================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT 'Username',
    password VARCHAR(200) NOT NULL COMMENT 'Password',
    email VARCHAR(100) UNIQUE COMMENT 'Email',
    phone VARCHAR(20) COMMENT 'Phone',
    real_name VARCHAR(50) COMMENT 'Real Name',
    avatar VARCHAR(200) COMMENT 'Avatar',
    gender VARCHAR(10) COMMENT 'Gender',
    id_card VARCHAR(20) COMMENT 'ID Card',
    organization VARCHAR(100) COMMENT 'Organization',
    department VARCHAR(100) COMMENT 'Department',
    student_id VARCHAR(20) COMMENT 'Student ID',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Enabled',
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Credentials Non Expired',
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Account Non Locked',
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Account Non Expired',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_organization (organization(50))
) ENGINE=InnoDB COMMENT='User Table';

-- ============================================
-- 2. 角色表
-- ============================================
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT 'Role Name',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT 'Role Code',
    description VARCHAR(200) COMMENT 'Description',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Role Table';

-- ============================================
-- 3. 权限表
-- ============================================
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT 'Permission Name',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT 'Permission Code',
    description VARCHAR(200) COMMENT 'Description',
    resource VARCHAR(200) COMMENT 'Resource URL',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Permission Table';

-- ============================================
-- 4. 用户角色关联表
-- ============================================
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='User-Role Mapping';

-- ============================================
-- 5. 角色权限关联表
-- ============================================
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Role-Permission Mapping';

-- ============================================
-- 6. 场地表
-- ============================================
DROP TABLE IF EXISTS venue;
CREATE TABLE venue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Venue Name',
    location VARCHAR(200) COMMENT 'Location',
    description VARCHAR(500) COMMENT 'Description',
    type VARCHAR(20) NOT NULL COMMENT 'Type',
    capacity INT NOT NULL DEFAULT 100 COMMENT 'Capacity',
    max_concurrent_events INT NOT NULL DEFAULT 1 COMMENT 'Max Concurrent Events',
    facilities VARCHAR(500) COMMENT 'Facilities',
    contact_person VARCHAR(50) COMMENT 'Contact Person',
    contact_phone VARCHAR(20) COMMENT 'Contact Phone',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Enabled',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Venue Table';

-- ============================================
-- 7. 赛事表
-- ============================================
DROP TABLE IF EXISTS event;
CREATE TABLE event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Event Name',
    short_name VARCHAR(20) COMMENT 'Short Name',
    description VARCHAR(500) COMMENT 'Description',
    cover_image VARCHAR(200) COMMENT 'Cover Image',
    event_type VARCHAR(20) NOT NULL DEFAULT '校园' COMMENT 'Event Type',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'Status',
    start_date DATE NOT NULL COMMENT 'Start Date',
    end_date DATE NOT NULL COMMENT 'End Date',
    registration_deadline DATE COMMENT 'Registration Deadline',
    organizer VARCHAR(200) COMMENT 'Organizer',
    sponsor VARCHAR(200) COMMENT 'Sponsor',
    max_participants INT NOT NULL DEFAULT 100 COMMENT 'Max Participants',
    current_participants INT NOT NULL DEFAULT 0 COMMENT 'Current Participants',
    rules VARCHAR(500) COMMENT 'Rules',
    awards VARCHAR(500) COMMENT 'Awards',
    is_public BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Is Public',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_dates (start_date, end_date)
) ENGINE=InnoDB COMMENT='Event Table';

-- ============================================
-- 8. 运动项目表
-- ============================================
DROP TABLE IF EXISTS sport_type;
CREATE TABLE sport_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT 'Sport Name',
    code VARCHAR(20) COMMENT 'Sport Code',
    description VARCHAR(200) COMMENT 'Description',
    category VARCHAR(20) NOT NULL COMMENT 'Category',
    is_individual BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Is Individual',
    is_timed BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Is Timed',
    is_scored BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Is Scored',
    unit VARCHAR(10) COMMENT 'Unit',
    group_size INT NOT NULL DEFAULT 8 COMMENT 'Group Size',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Sort Order',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Enabled',
    event_id BIGINT COMMENT 'Event ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    INDEX idx_event (event_id)
) ENGINE=InnoDB COMMENT='Sport Type Table';

-- ============================================
-- 9. 运动员表
-- ============================================
DROP TABLE IF EXISTS athlete;
CREATE TABLE athlete (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT 'User ID',
    gender VARCHAR(10) COMMENT 'Gender',
    birth_date DATE COMMENT 'Birth Date',
    organization VARCHAR(100) COMMENT 'Organization',
    avatar VARCHAR(200) COMMENT 'Avatar',
    bio VARCHAR(500) COMMENT 'Bio',
    specialties TEXT COMMENT 'Specialties',
    age_group VARCHAR(10) COMMENT 'Age Group',
    technical_features TEXT COMMENT 'Technical Features',
    highlights TEXT COMMENT 'Highlights',
    custom_tags TEXT COMMENT 'Custom Tags',
    personal_bests TEXT COMMENT 'Personal Bests',
    height INT COMMENT 'Height(cm)',
    weight INT COMMENT 'Weight(kg)',
    competition_count INT NOT NULL DEFAULT 0 COMMENT 'Competition Count',
    award_count INT NOT NULL DEFAULT 0 COMMENT 'Award Count',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Enabled',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE SET NULL,
    INDEX idx_gender (gender),
    INDEX idx_age_group (age_group),
    INDEX idx_organization (organization(50))
) ENGINE=InnoDB COMMENT='Athlete Table';

-- ============================================
-- 10. 参赛报名表
-- ============================================
DROP TABLE IF EXISTS registration;
CREATE TABLE registration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL COMMENT 'Event ID',
    athlete_id BIGINT NOT NULL COMMENT 'Athlete ID',
    sport_type_id BIGINT NOT NULL COMMENT 'Sport Type ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Status',
    bib_number VARCHAR(20) COMMENT 'Bib Number',
    `group` VARCHAR(20) COMMENT 'Group',
    lane INT COMMENT 'Lane',
    seed_score VARCHAR(50) COMMENT 'Seed Score',
    remark VARCHAR(500) COMMENT 'Remark',
    reviewer_id BIGINT COMMENT 'Reviewer ID',
    review_time DATETIME COMMENT 'Review Time',
    review_comment VARCHAR(500) COMMENT 'Review Comment',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    FOREIGN KEY (athlete_id) REFERENCES athlete(id) ON DELETE CASCADE,
    FOREIGN KEY (sport_type_id) REFERENCES sport_type(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES sys_user(id) ON DELETE SET NULL,
    UNIQUE KEY uk_event_athlete_sport (event_id, athlete_id, sport_type_id),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='Registration Table';

-- ============================================
-- 11. 赛程安排表
-- ============================================
DROP TABLE IF EXISTS schedule;
CREATE TABLE schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL COMMENT 'Event ID',
    sport_type_id BIGINT NOT NULL COMMENT 'Sport Type ID',
    venue_id BIGINT NOT NULL COMMENT 'Venue ID',
    name VARCHAR(100) NOT NULL COMMENT 'Schedule Name',
    round_type VARCHAR(20) NOT NULL COMMENT 'Round Type',
    group_number INT NOT NULL COMMENT 'Group Number',
    date DATE NOT NULL COMMENT 'Date',
    start_time TIME NOT NULL COMMENT 'Start Time',
    end_time TIME COMMENT 'End Time',
    group_name VARCHAR(20) COMMENT 'Group Name',
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' COMMENT 'Status',
    participant_count INT NOT NULL DEFAULT 0 COMMENT 'Participant Count',
    remark VARCHAR(500) COMMENT 'Remark',
    chief_referee_id BIGINT COMMENT 'Chief Referee ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    FOREIGN KEY (sport_type_id) REFERENCES sport_type(id) ON DELETE CASCADE,
    FOREIGN KEY (venue_id) REFERENCES venue(id) ON DELETE CASCADE,
    FOREIGN KEY (chief_referee_id) REFERENCES sys_user(id) ON DELETE SET NULL,
    INDEX idx_date (date),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='Schedule Table';

-- ============================================
-- 12. 成绩表
-- ============================================
DROP TABLE IF EXISTS result;
CREATE TABLE result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT COMMENT 'Schedule ID',
    athlete_id BIGINT NOT NULL COMMENT 'Athlete ID',
    sport_type_id BIGINT NOT NULL COMMENT 'Sport Type ID',
    registration_id BIGINT COMMENT 'Registration ID',
    performance VARCHAR(50) COMMENT 'Performance',
    rank INT COMMENT 'Rank',
    points INT COMMENT 'Points',
    status VARCHAR(20) NOT NULL DEFAULT 'VALID' COMMENT 'Status',
    remark VARCHAR(500) COMMENT 'Remark',
    referee_id BIGINT COMMENT 'Referee ID',
    record_time DATETIME COMMENT 'Record Time',
    result_type VARCHAR(20) COMMENT 'Result Type',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (schedule_id) REFERENCES schedule(id) ON DELETE CASCADE,
    FOREIGN KEY (athlete_id) REFERENCES athlete(id) ON DELETE CASCADE,
    FOREIGN KEY (sport_type_id) REFERENCES sport_type(id) ON DELETE CASCADE,
    FOREIGN KEY (registration_id) REFERENCES registration(id) ON DELETE SET NULL,
    FOREIGN KEY (referee_id) REFERENCES sys_user(id) ON DELETE SET NULL,
    INDEX idx_rank (rank),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='Result Table';

-- ============================================
-- 初始化数据
-- ============================================

-- Insert roles
INSERT INTO sys_role (name, code, description) VALUES
('Admin', 'ROLE_ADMIN', 'System Administrator'),
('Referee', 'ROLE_REFEREE', 'Referee'),
('Athlete', 'ROLE_ATHLETE', 'Athlete'),
('Spectator', 'ROLE_SPECTATOR', 'Spectator');

-- Insert permissions
INSERT INTO sys_permission (name, code, description, resource) VALUES
('User Management', 'user:manage', 'Manage Users', '/admin/users'),
('Event Management', 'event:manage', 'Manage Events', '/events'),
('Athlete Management', 'athlete:manage', 'Manage Athletes', '/athletes'),
('Schedule Management', 'schedule:manage', 'Manage Schedules', '/schedules'),
('Result Management', 'result:manage', 'Manage Results', '/results'),
('Result Recording', 'result:record', 'Record Results', '/results/record'),
('Registration Review', 'registration:review', 'Review Registrations', '/registrations');

-- Assign all permissions to admin role
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

-- Assign result recording permissions to referee role
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission WHERE code IN ('result:record', 'result:manage');

-- Insert default admin user (password: admin123, BCrypt encrypted)
INSERT INTO sys_user (username, password, email, real_name, enabled) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@sports.com', 'System Admin', TRUE);

-- Assign admin role to admin user
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- Insert sample venues
INSERT INTO venue (name, location, type, capacity, description) VALUES
('Main Stadium', 'East Campus', 'Track Field', 5000, 'Standard 400m Plastic Track'),
('Indoor Gym', 'West Campus', 'Gymnasium', 2000, 'Multi-purpose Indoor Venue'),
('Swimming Pool', 'North Campus', 'Swimming Pool', 800, 'Standard 50m Lane');

-- Insert sample event
INSERT INTO event (name, short_name, description, event_type, status, start_date, end_date, registration_deadline, organizer, max_participants, is_public) VALUES
('2024 Campus Sports Meeting', 'Sports Meeting', 'Annual Campus Sports Meeting', '校园', 'PUBLISHED', '2024-05-01', '2024-05-03', '2024-04-15', 'Sports Department', 500, TRUE);

-- Insert sport types
INSERT INTO sport_type (name, code, description, category, is_individual, is_timed, is_scored, unit, group_size, event_id) VALUES
('100m Dash', '100M', '100 meters dash', '田径', TRUE, TRUE, TRUE, '秒', 8, 1),
('Long Jump', 'LJ', 'Long jump', '田径', TRUE, TRUE, TRUE, '米', 12, 1),
('Shot Put', 'SP', 'Shot put', '田径', TRUE, TRUE, TRUE, '米', 12, 1),
('4x100m Relay', '4X100M', '4x100 meters relay', '田径', FALSE, TRUE, TRUE, '秒', 8, 1);

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;
