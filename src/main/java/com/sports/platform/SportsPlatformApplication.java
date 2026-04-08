package com.sports.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

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
        System.out.println("""

            ========================================
              体育赛事管理平台启动成功!
              访问地址: http://localhost:8080
              默认管理员: admin / admin123
            ========================================
            """);
    }
}
