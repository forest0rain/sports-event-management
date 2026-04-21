package com.sports.platform.service;

import com.sports.platform.entity.Role;
import com.sports.platform.entity.User;
import com.sports.platform.repository.RoleRepository;
import com.sports.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 用户服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     */
    @Transactional
    public User register(String username, String password, String email, String realName) {
        // 检查用户名是否存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否存在
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建用户
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .realName(realName)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        // 分配默认角色(观众)，如果不存在则自动创建
        Role spectatorRole = roleRepository.findByCode(Role.SPECTATOR)
                .orElseGet(() -> {
                    log.info("默认角色不存在，自动创建观众角色");
                    Role newRole = Role.builder()
                            .name("观众")
                            .code(Role.SPECTATOR)
                            .description("普通观众")
                            .build();
                    return roleRepository.save(newRole);
                });
        user.addRole(spectatorRole);

        user = userRepository.save(user);
        log.info("用户注册成功: {}", username);
        
        return user;
    }

    /**
     * 创建用户(管理员)
     */
    @Transactional
    public User createUser(User user, List<String> roleCodes) {
        // 检查用户名是否存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否存在
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 分配角色
        Set<Role> roles = new HashSet<>();
        for (String roleCode : roleCodes) {
            Role role = roleRepository.findByCode(roleCode)
                    .orElseThrow(() -> new RuntimeException("角色不存在: " + roleCode));
            roles.add(role);
        }
        user.setRoles(roles);

        user = userRepository.save(user);
        log.info("创建用户成功: {}", user.getUsername());
        
        return user;
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        
        user.setRealName(userDetails.getRealName());
        user.setPhone(userDetails.getPhone());
        user.setAvatar(userDetails.getAvatar());
        
        return userRepository.save(user);
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("用户修改密码: {}", user.getUsername());
    }

    /**
     * 重置密码(管理员)
     */
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = getUserById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("重置用户密码: {}", user.getUsername());
    }

    /**
     * 启用/禁用用户
     */
    @Transactional
    public void toggleUserStatus(Long id) {
        User user = getUserById(id);
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);
        
        log.info("用户状态变更: {} -> {}", user.getUsername(), user.getEnabled() ? "启用" : "禁用");
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
        
        log.info("删除用户: {}", user.getUsername());
    }

    /**
     * 根据ID获取用户
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 根据用户名获取用户
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 获取所有用户
     */
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * 搜索用户
     */
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.search(keyword, pageable);
    }

    /**
     * 根据角色获取用户
     */
    public Page<User> getUsersByRole(String roleCode, Pageable pageable) {
        return userRepository.findByRoleCode(roleCode, pageable);
    }

    /**
     * 更新用户角色
     */
    @Transactional
    public void updateUserRoles(Long userId, List<String> roleCodes) {
        User user = getUserById(userId);
        
        Set<Role> roles = new HashSet<>();
        for (String roleCode : roleCodes) {
            Role role = roleRepository.findByCode(roleCode)
                    .orElseThrow(() -> new RuntimeException("角色不存在: " + roleCode));
            roles.add(role);
        }
        
        user.setRoles(roles);
        userRepository.save(user);
        
        log.info("更新用户角色: {} -> {}", user.getUsername(), roleCodes);
    }
}
