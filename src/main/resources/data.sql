-- ============================================
-- 体育赛事管理平台 - 初始化数据
-- ============================================

-- 插入权限
INSERT IGNORE INTO sys_permission (id, name, code, description, resource, method) VALUES
(1, '用户管理', 'user:manage', '用户管理权限', '/api/users/**', 'GET'),
(2, '角色管理', 'role:manage', '角色管理权限', '/api/roles/**', 'GET'),
(3, '赛事管理', 'event:manage', '赛事管理权限', '/api/events/**', 'POST'),
(4, '报名管理', 'registration:manage', '报名管理权限', '/api/registrations/**', 'POST'),
(5, '场地管理', 'venue:manage', '场地管理权限', '/api/venues/**', 'POST'),
(6, '赛事查看', 'event:view', '赛事查看权限', '/api/events/**', 'GET'),
(7, '报名查看', 'registration:view', '报名查看权限', '/api/registrations/**', 'GET'),
(8, '角色申请', 'role:apply', '角色申请权限', '/api/role-applications/**', 'POST');

-- 插入角色
INSERT IGNORE INTO sys_role (id, name, code, description, enabled) VALUES
(1, '管理员', 'ADMIN', '系统管理员', TRUE),
(2, '运动员', 'ATHLETE', '运动员', TRUE),
(3, '裁判', 'REFEREE', '裁判', TRUE),
(4, '观众', 'SPECTATOR', '观众', TRUE);

-- 插入角色-权限关联
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
(2, 6), (2, 7), (2, 8),
(3, 6), (3, 7), (3, 8),
(4, 6), (4, 7), (4, 8);

-- 插入管理员用户 (密码: admin123)
INSERT IGNORE INTO sys_user (id, username, password, email, real_name, enabled, account_non_expired, account_non_locked, credentials_non_expired) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@example.com', '系统管理员', TRUE, TRUE, TRUE, TRUE);

-- 插入运动员用户 (密码: athlete123)
INSERT IGNORE INTO sys_user (id, username, password, email, real_name, enabled, account_non_expired, account_non_locked, credentials_non_expired) VALUES
(2, 'athlete', '$2a$10$X5wFutsrWd86F2qU8HJqVeOZqLyXQhQVAF6zA1j9A1qU1qHQwZGy2', 'athlete@example.com', '测试运动员', TRUE, TRUE, TRUE, TRUE);

-- 插入裁判用户 (密码: referee123)
INSERT IGNORE INTO sys_user (id, username, password, email, real_name, enabled, account_non_expired, account_non_locked, credentials_non_expired) VALUES
(3, 'referee', '$2a$10$Yq7.8xqPLl5kqDZ5uIqP4eVjD8vZvMqUqUqUqUqUqUqUqUqUqUqU', 'referee@example.com', '测试裁判', TRUE, TRUE, TRUE, TRUE);

-- 插入观众用户 (密码: spectator123)
INSERT IGNORE INTO sys_user (id, username, password, email, real_name, enabled, account_non_expired, account_non_locked, credentials_non_expired) VALUES
(4, 'spectator', '$2a$10$Zw8.rTusrdHqDZ5uIqP4eVjD8vZvMqUqUqUqUqUqUqUqUqUqUqU', 'spectator@example.com', '测试观众', TRUE, TRUE, TRUE, TRUE);

-- 插入用户-角色关联
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4);

-- 插入运动类型
INSERT IGNORE INTO sport_type (id, name, description, category, equipment_required, created_time) VALUES
(1, '篮球', '篮球比赛', '球类', '篮球、球衣', NOW()),
(2, '足球', '足球比赛', '球类', '足球、球衣', NOW()),
(3, '羽毛球', '羽毛球比赛', '球类', '羽毛球拍、羽毛球', NOW()),
(4, '乒乓球', '乒乓球比赛', '球类', '乒乓球拍、乒乓球', NOW()),
(5, '游泳', '游泳比赛', '水上', '泳衣、泳镜', NOW()),
(6, '田径', '田径比赛', '田径', '运动服、运动鞋', NOW());
