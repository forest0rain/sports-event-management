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

-- 插入赛事数据
INSERT IGNORE INTO event (id, name, description, sport_type_id, venue_id, start_date, end_date, status, max_participants, registration_deadline, created_time) VALUES
(1, '春季田径运动会', '年度春季田径运动会', 6, 1, '2026-04-15', '2026-04-18', 'FINISHED', 200, '2026-04-10', NOW()),
(2, '城市篮球联赛', '城市级别篮球联赛', 1, 2, '2026-05-01', '2026-05-10', 'ONGOING', 120, '2026-04-25', NOW()),
(3, '游泳锦标赛', '市级游泳锦标赛', 5, 3, '2026-05-20', '2026-05-22', 'REGISTRATION', 80, '2026-05-15', NOW()),
(4, '足球邀请赛', '校际足球邀请赛', 2, 4, '2026-06-01', '2026-06-05', 'REGISTRATION', 150, '2026-05-25', NOW()),
(5, '羽毛球公开赛', '市级羽毛球公开赛', 3, 2, '2026-06-10', '2026-06-12', 'REGISTRATION', 64, '2026-06-05', NOW()),
(6, '乒乓球团体赛', '乒乓球团体赛', 4, 2, '2026-06-15', '2026-06-17', 'DRAFT', 48, '2026-06-10', NOW()),
(7, '夏季田径运动会', '年度夏季田径运动会', 6, 1, '2026-07-01', '2026-07-04', 'DRAFT', 200, '2026-06-25', NOW()),
(8, '网球精英赛', '网球精英赛', 3, 5, '2026-07-10', '2026-07-12', 'DRAFT', 32, '2026-07-05', NOW());

-- 插入运动员数据
INSERT IGNORE INTO athlete (id, user_id, name, gender, birth_date, age_group, nationality, organization, phone, email, id_card, specialties, enabled, competition_count, award_count, created_time) VALUES
(1, NULL, '张伟', 'M', '2004-01-01', 'U20', '中国', '体育学院一队', '13900000001', 'zhangwei@example.com', '110101200401010001', '["短跑"]', TRUE, 0, 0, NOW()),
(2, NULL, '李娜', 'F', '2006-01-01', 'U20', '中国', '游泳队', '13900000002', 'lina@example.com', '110101200601010002', '["游泳"]', TRUE, 0, 0, NOW()),
(3, NULL, '王磊', 'M', '2001-01-01', 'OPEN', '中国', '篮球队A', '13900000003', 'wanglei@example.com', '110101200101010003', '["篮球"]', TRUE, 0, 0, NOW()),
(4, NULL, '刘芳', 'F', '2003-01-01', 'OPEN', '中国', '羽毛球队', '13900000004', 'liufang@example.com', '110101200301010004', '["羽毛球"]', TRUE, 0, 0, NOW()),
(5, NULL, '陈强', 'M', '1998-01-01', 'OPEN', '中国', '足球队A', '13900000005', 'chenqiang@example.com', '110101199801010005', '["足球"]', TRUE, 0, 0, NOW()),
(6, NULL, '赵敏', 'F', '2007-01-01', 'U20', '中国', '乒乓球队', '13900000006', 'zhaomin@example.com', '110101200701010006', '["乒乓球"]', TRUE, 0, 0, NOW()),
(7, NULL, '孙浩', 'M', '1996-01-01', 'OPEN', '中国', '田径队二队', '13900000007', 'sunhao@example.com', '110101199601010007', '["长跑"]', TRUE, 0, 0, NOW()),
(8, NULL, '周婷', 'F', '2005-01-01', 'U20', '中国', '游泳队', '13900000008', 'zhouting@example.com', '110101200501010008', '["游泳"]', TRUE, 0, 0, NOW()),
(9, NULL, '吴刚', 'M', '2000-01-01', 'OPEN', '中国', '篮球队B', '13900000009', 'wugang@example.com', '110101200001010009', '["篮球"]', TRUE, 0, 0, NOW()),
(10, NULL, '郑丽', 'F', '2002-01-01', 'OPEN', '中国', '羽毛球队', '13900000010', 'zhengli@example.com', '110101200201010010', '["羽毛球"]', TRUE, 0, 0, NOW()),
(11, NULL, '冯涛', 'M', '2008-01-01', 'U18', '中国', '体育学院二队', '13900000011', 'fengtao@example.com', '110101200801010011', '["短跑"]', TRUE, 0, 0, NOW()),
(12, NULL, '褚瑶', 'F', '2009-01-01', 'U18', '中国', '游泳队', '13900000012', 'chuyao@example.com', '110101200901010012', '["游泳"]', TRUE, 0, 0, NOW()),
(13, NULL, '卫东', 'M', '1994-01-01', 'OPEN', '中国', '足球队B', '13900000013', 'weidong@example.com', '110101199401010013', '["足球"]', TRUE, 0, 0, NOW()),
(14, NULL, '蒋欣', 'F', '2004-01-01', 'U20', '中国', '乒乓球队', '13900000014', 'jiangxin@example.com', '110101200401010014', '["乒乓球"]', TRUE, 0, 0, NOW()),
(15, NULL, '沈鹏', 'M', '1999-01-01', 'OPEN', '中国', '田径队一队', '13900000015', 'shenpeng@example.com', '110101199901010015', '["跳远"]', TRUE, 0, 0, NOW()),
(16, NULL, '韩雪', 'F', '2006-01-01', 'U20', '中国', '羽毛球队', '13900000016', 'hanxue@example.com', '110101200601010016', '["羽毛球"]', TRUE, 0, 0, NOW()),
(17, NULL, '杨帆', 'M', '2003-01-01', 'OPEN', '中国', '篮球队C', '13900000017', 'yangfan@example.com', '110101200301010017', '["篮球"]', TRUE, 0, 0, NOW()),
(18, NULL, '朱琳', 'F', '2001-01-01', 'OPEN', '中国', '游泳队', '13900000018', 'zhulin@example.com', '110101200101010018', '["游泳"]', TRUE, 0, 0, NOW()),
(19, NULL, '秦勇', 'M', '1997-01-01', 'OPEN', '中国', '足球队C', '13900000019', 'qinyong@example.com', '110101199701010019', '["足球"]', TRUE, 0, 0, NOW()),
(20, NULL, '许悦', 'F', '2010-01-01', 'U18', '中国', '乒乓球队', '13900000020', 'xuyue@example.com', '110101201001010020', '["乒乓球"]', TRUE, 0, 0, NOW());

-- 插入赛程数据
INSERT IGNORE INTO schedule (id, event_id, sport_type_id, venue_id, name, round_type, group_number, date, start_time, group_name, status, created_time) VALUES
(1, 1, 6, 1, '男子100米预赛', 'PRELIMINARY', 1, '2026-04-15', '09:00:00', '男子组', 'COMPLETED', NOW()),
(2, 1, 6, 1, '男子200米决赛', 'FINAL', 1, '2026-04-16', '10:00:00', '男子组', 'COMPLETED', NOW()),
(3, 2, 1, 2, '篮球小组赛A组', 'PRELIMINARY', 1, '2026-05-01', '14:00:00', '男子组', 'ONGOING', NOW()),
(4, 3, 5, 3, '女子50米自由泳预赛', 'PRELIMINARY', 1, '2026-05-20', '09:00:00', '女子组', 'SCHEDULED', NOW()),
(5, 4, 2, 4, '足球小组赛B组', 'PRELIMINARY', 1, '2026-06-01', '15:00:00', '男子组', 'SCHEDULED', NOW()),
(6, 5, 3, 2, '羽毛球单打预赛', 'PRELIMINARY', 1, '2026-06-10', '09:00:00', '混合组', 'SCHEDULED', NOW()),
(7, 6, 4, 2, '乒乓球团体赛小组赛', 'PRELIMINARY', 1, '2026-06-15', '10:00:00', '混合组', 'SCHEDULED', NOW());

-- 插入报名数据
INSERT IGNORE INTO registration (id, athlete_id, event_id, sport_type_id, status, created_time) VALUES
(1, 1, 1, 6, 'APPROVED', NOW()),
(2, 2, 3, 5, 'APPROVED', NOW()),
(3, 3, 2, 1, 'APPROVED', NOW()),
(4, 4, 5, 3, 'APPROVED', NOW()),
(5, 5, 4, 2, 'APPROVED', NOW()),
(6, 6, 6, 4, 'PENDING', NOW()),
(7, 7, 1, 6, 'APPROVED', NOW()),
(8, 8, 3, 5, 'APPROVED', NOW()),
(9, 9, 2, 1, 'APPROVED', NOW()),
(10, 10, 5, 3, 'REJECTED', NOW()),
(11, 11, 1, 6, 'APPROVED', NOW()),
(12, 12, 3, 5, 'PENDING', NOW()),
(13, 13, 4, 2, 'APPROVED', NOW()),
(14, 14, 6, 4, 'APPROVED', NOW()),
(15, 15, 1, 6, 'APPROVED', NOW()),
(16, 16, 5, 3, 'APPROVED', NOW()),
(17, 17, 2, 1, 'PENDING', NOW()),
(18, 18, 3, 5, 'APPROVED', NOW()),
(19, 1, 7, 6, 'PENDING', NOW()),
(20, 3, 4, 2, 'APPROVED', NOW());

-- 插入成绩数据
INSERT IGNORE INTO result (id, schedule_id, athlete_id, sport_type_id, score_text, rank, record_type, is_personal_best, is_season_best, status, result_type, created_time) VALUES
(1, 1, 1, 6, '10.85秒', 1, '校纪录', TRUE, TRUE, 'VALID', 'PRELIMINARY', NOW()),
(2, 1, 7, 6, '11.02秒', 2, '无', TRUE, TRUE, 'VALID', 'PRELIMINARY', NOW()),
(3, 1, 11, 6, '11.35秒', 3, '无', TRUE, FALSE, 'VALID', 'PRELIMINARY', NOW()),
(4, 1, 15, 6, '11.48秒', 4, '无', FALSE, FALSE, 'VALID', 'PRELIMINARY', NOW()),
(5, 4, 2, 5, '58.23秒', 1, '市纪录', TRUE, TRUE, 'VALID', 'PRELIMINARY', NOW()),
(6, 4, 8, 5, '59.15秒', 2, '无', TRUE, TRUE, 'VALID', 'PRELIMINARY', NOW()),
(7, 4, 18, 5, '1:00.45', 3, '无', FALSE, FALSE, 'VALID', 'PRELIMINARY', NOW()),
(8, 3, 3, 1, '24分', 1, '无', TRUE, TRUE, 'VALID', 'PRELIMINARY', NOW()),
(9, 3, 9, 1, '22分', 2, '无', TRUE, FALSE, 'VALID', 'PRELIMINARY', NOW()),
(10, 6, 4, 3, '21-18', 1, '无', TRUE, TRUE, 'VALID', 'PRELIMINARY', NOW()),
(11, 6, 10, 3, '18-21', 2, '无', FALSE, FALSE, 'VALID', 'PRELIMINARY', NOW()),
(12, 5, 5, 2, '3-1', 1, '校纪录', TRUE, TRUE, 'VALID', 'PRELIMINARY', NOW()),
(13, 5, 13, 2, '2-3', 2, '无', FALSE, FALSE, 'VALID', 'PRELIMINARY', NOW()),
(14, 7, 6, 4, '3-0', 1, '无', TRUE, TRUE, 'VALID', 'PRELIMINARY', NOW()),
(15, 7, 14, 4, '2-3', 2, '无', TRUE, FALSE, 'VALID', 'PRELIMINARY', NOW()),
(16, 2, 1, 6, '22.15秒', 1, '省纪录', TRUE, TRUE, 'VALID', 'FINAL', NOW()),
(17, 2, 7, 6, '23.01秒', 2, '无', FALSE, FALSE, 'VALID', 'FINAL', NOW()),
(18, 2, 11, 6, '23.45秒', 3, '无', TRUE, FALSE, 'PENDING', 'FINAL', NOW()),
(19, 6, 16, 3, '21-15', 3, '无', TRUE, FALSE, 'VALID', 'PRELIMINARY', NOW());
