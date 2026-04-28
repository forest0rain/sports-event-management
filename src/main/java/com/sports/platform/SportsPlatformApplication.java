package com.sports.platform;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 体育赛事管理平台主启动类
 * 
 * @author Sports Platform Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class SportsPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportsPlatformApplication.class, args);
    }

    @Bean
    public CommandLineRunner testPassword() {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String rawPassword = "password123";
            String hash = encoder.encode(rawPassword);
            System.out.println("\n========== 密码测试 ==========");
            System.out.println("原始密码: " + rawPassword);
            System.out.println("BCrypt哈希: " + hash);
            System.out.println("哈希长度: " + hash.length());
            System.out.println("验证结果: " + encoder.matches(rawPassword, hash));
            System.out.println("==============================\n");
        };
    }
}
