-- ============================================
-- 体育赛事管理平台 - 初始化数据
-- ============================================

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

-- 插入管理员用户 (密码: admin123)
INSERT IGNORE INTO sys_user (id, username, password, email, real_name, enabled, account_non_expired, account_non_locked, credentials_non_expired) VALUES
(1, 'admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'admin@example.com', 'Admin', TRUE, TRUE, TRUE, TRUE);

-- 插入运动员用户 (密码: athlete123)
INSERT IGNORE INTO sys_user (id, username, password, email, real_name, enabled, account_non_expired, account_non_locked, credentials_non_expired) VALUES
(2, 'athlete', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'athlete@example.com', 'Athlete', TRUE, TRUE, TRUE, TRUE);

-- 插入裁判用户 (密码: referee123)
INSERT IGNORE INTO sys_user (id, username, password, email, real_name, enabled, account_non_expired, account_non_locked, credentials_non_expired) VALUES
(3, 'referee', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'referee@example.com', 'Referee', TRUE, TRUE, TRUE, TRUE);

-- 插入观众用户 (密码: spectator123)
INSERT IGNORE INTO sys_user (id, username, password, email, real_name, enabled, account_non_expired, account_non_locked, credentials_non_expired) VALUES
(4, 'spectator', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'spectator@example.com', 'Spectator', TRUE, TRUE, TRUE, TRUE);

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
