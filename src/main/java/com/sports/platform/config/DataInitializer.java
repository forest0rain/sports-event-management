package com.sports.platform.config;

import com.sports.platform.entity.Role;
import com.sports.platform.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        initRole("ROLE_ADMIN", "管理员", "系统管理员", true);
        initRole("ROLE_SPECTATOR", "观众", "普通观众", true);
        initRole("ROLE_ATHLETE", "运动员", "运动员角色", true);
        initRole("ROLE_REFEREE", "裁判", "裁判角色", true);
    }

    private void initRole(String code, String name, String description, boolean enabled) {
        boolean exists = roleRepository.findByCode(code).isPresent();
        if (exists) {
            System.out.println("角色已存在，跳过初始化：" + code);
            return;
        }

        Role role = new Role();
        role.setCode(code);
        role.setName(name);
        role.setDescription(description);
        role.setEnabled(enabled);
        role.setCreatedTime(LocalDateTime.now());

        roleRepository.save(role);
        System.out.println("初始化角色成功：" + code + " - " + name);
    }
}