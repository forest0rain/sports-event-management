-- ============================================
-- 体育赛事管理平台 - 初始化数据
-- ============================================

-- 创建角色申请表（如果不存在）
CREATE TABLE IF NOT EXISTS sys_role_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    requested_role VARCHAR(50) NOT NULL,
    reason VARCHAR(500),
    qualification VARCHAR(500),
    qualification_file VARCHAR(255),
    qualification_file_name VARCHAR(100),
    contact_phone VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewer_id BIGINT,
    review_comment VARCHAR(500),
    review_time DATETIME,
    created_time DATETIME NOT NULL,
    updated_time DATETIME,
    CONSTRAINT fk_role_application_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_role_application_reviewer FOREIGN KEY (reviewer_id) REFERENCES sys_user(id)
);

-- 插入权限
INSERT IGNORE INTO sys_permission (id, name, code, description, resource) VALUES
(1, 'User Management', 'user:manage', 'User Management Permission', '/api/users/**'),
(2, 'Role Management', 'role:manage', 'Role Management Permission', '/api/roles/**'),
(3, 'Event Management', 'event:manage', 'Event Management Permission', '/api/events/**'),
(4, 'Registration Management', 'registration:manage', 'Registration Management Permission', '/api/registrations/**'),
(5, 'Venue Management', 'venue:manage', 'Venue Management Permission', '/api/venues/**'),
(6, 'Event View', 'event:view', 'Event View Permission', '/api/events/**'),
(7, 'Registration View', 'registration:view', 'Registration View Permission', '/api/registrations/**'),
(8, 'Role Apply', 'role:apply', 'Role Apply Permission', '/api/role-applications/**');

-- 插入角色
INSERT IGNORE INTO sys_role (id, name, code, description) VALUES
(1, 'Admin', 'ADMIN', 'System Administrator'),
(2, 'Athlete', 'ATHLETE', 'Athlete'),
(3, 'Referee', 'REFEREE', 'Referee'),
(4, 'Spectator', 'SPECTATOR', 'Spectator');

-- 插入角色-权限关联
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
(2, 6), (2, 7), (2, 8),
(3, 6), (3, 7), (3, 8),
(4, 6), (4, 7), (4, 8);

-- 插入管理员用户 (密码: password123)
INSERT INTO sys_user (id, username, password, email, real_name, enabled, account_non_expired, account_non_locked, credentials_non_expired) VALUES
(1, 'admin', '$2a$10$tH9H90JJQJvNDI91YWPdXOymoW6i0dSiap4oLw4jqYd18XjviRGjq', 'admin@example.com', 'Admin', TRUE, TRUE, TRUE, TRUE)
ON DUPLICATE KEY UPDATE password = VALUES(password);

-- 插入运动员用户 (密码: password123)
INSERT INTO sys_user (id, username, password, email, real_name, enabled, account_non_expired, account_non_locked, credentials_non_expired) VALUES
(2, 'athlete', '$2a$10$tH9H90JJQJvNDI91YWPdXOymoW6i0dSiap4oLw4jqYd18XjviRGjq', 'athlete@example.com', 'Athlete', TRUE, TRUE, TRUE, TRUE)
ON DUPLICATE KEY UPDATE password = VALUES(password);

-- 插入裁判用户 (密码: password123)
INSERT INTO sys_user (id, username, password, email, real_name, enabled, account_non_expired, account_non_locked, credentials_non_expired) VALUES
(3, 'referee', '$2a$10$tH9H90JJQJvNDI91YWPdXOymoW6i0dSiap4oLw4jqYd18XjviRGjq', 'referee@example.com', 'Referee', TRUE, TRUE, TRUE, TRUE)
ON DUPLICATE KEY UPDATE password = VALUES(password);

-- 插入观众用户 (密码: password123)
INSERT INTO sys_user (id, username, password, email, real_name, enabled, account_non_expired, account_non_locked, credentials_non_expired) VALUES
(4, 'spectator', '$2a$10$tH9H90JJQJvNDI91YWPdXOymoW6i0dSiap4oLw4jqYd18XjviRGjq', 'spectator@example.com', 'Spectator', TRUE, TRUE, TRUE, TRUE)
ON DUPLICATE KEY UPDATE password = VALUES(password);

-- 插入用户-角色关联
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4);

-- 插入运动类型
INSERT IGNORE INTO sport_type (id, name, code, description, category, is_individual, is_scored, created_time) VALUES
(1, 'Basketball', 'basketball', 'Basketball game', 'Ball Sports', TRUE, TRUE, NOW()),
(2, 'Football', 'football', 'Football game', 'Ball Sports', TRUE, TRUE, NOW()),
(3, 'Badminton', 'badminton', 'Badminton game', 'Ball Sports', TRUE, TRUE, NOW()),
(4, 'Table Tennis', 'table_tennis', 'Table tennis game', 'Ball Sports', TRUE, TRUE, NOW()),
(5, 'Swimming', 'swimming', 'Swimming competition', 'Water Sports', TRUE, TRUE, NOW()),
(6, 'Track and Field', 'track_field', 'Track and field competition', 'Athletics', TRUE, TRUE, NOW());

-- 插入场地数据
INSERT IGNORE INTO venue (id, name, location, description, type, capacity, max_concurrent_events, facilities, contact_person, contact_phone, enabled, created_time) VALUES
(1, '主体育场', 'A区1号', '标准400米田径场，可举办田径赛事', 'TRACK', 5000, 3, '跑道、跳高区、跳远区、投掷区', '张主任', '13800000001', TRUE, NOW()),
(2, '综合体育馆', 'B区2号', '室内综合体育馆，可举办篮球、羽毛球等', 'GYM', 3000, 2, '篮球场、羽毛球场、乒乓球区', '李主任', '13800000002', TRUE, NOW()),
(3, '游泳馆', 'C区3号', '标准50米泳池', 'POOL', 1500, 2, '50米标准泳池、热身池、观众席', '王主任', '13800000003', TRUE, NOW()),
(4, '足球场', 'D区4号', '标准11人制足球场', 'FIELD', 4000, 1, '标准草坪、灯光系统、观众席', '赵主任', '13800000004', TRUE, NOW()),
(5, '网球中心', 'E区5号', '室外网球中心', 'COURT', 800, 4, '4块标准场地、灯光系统', '陈主任', '13800000005', TRUE, NOW());
