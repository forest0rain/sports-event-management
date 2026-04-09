package com.sports.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security 安全配置
 * 
 * 功能:
 * 1. 配置认证和授权规则
 * 2. 密码BCrypt加密
 * 3. 登录/登出配置
 * 4. 角色权限控制
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * 密码加密器 - BCrypt算法
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 授权配置
            .authorizeHttpRequests(auth -> auth
                // 静态资源放行
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                // 注册页面放行
                .requestMatchers("/register", "/api/auth/register").permitAll()
                // 公开页面放行
                .requestMatchers("/", "/index", "/events/public", "/athletes/public").permitAll()
                // 错误页面放行
                .requestMatchers("/error").permitAll()
                // H2控制台(开发环境)
                .requestMatchers("/h2-console/**").permitAll()
                // 登录页面放行
                .requestMatchers("/login").permitAll()
                // 文件访问 - 管理员可查看资质文件
                .requestMatchers("/files/**").hasRole("ADMIN")
                // 仪表盘 - 所有认证用户可访问
                .requestMatchers("/dashboard").authenticated()
                // 个人中心 - 所有认证用户可访问
                .requestMatchers("/profile/**").authenticated()
                // 统计API - 所有认证用户可访问
                .requestMatchers("/api/statistics/**").authenticated()
                // 赛事管理 - 管理员和裁判可管理
                .requestMatchers("/events/create", "/events/*/edit", "/events/*/delete").hasAnyRole("ADMIN", "REFEREE")
                .requestMatchers("/events/**").hasAnyRole("ADMIN", "REFEREE", "ATHLETE", "SPECTATOR")
                // 运动员管理 - 管理员可增删改，裁判和运动员可查看
                .requestMatchers("/athletes/create", "/athletes/*/edit", "/athletes/*/delete").hasRole("ADMIN")
                .requestMatchers("/athletes/**").hasAnyRole("ADMIN", "REFEREE", "ATHLETE", "SPECTATOR")
                // 赛程管理 - 管理员和裁判可管理
                .requestMatchers("/schedules/create", "/schedules/*/edit", "/schedules/*/delete").hasAnyRole("ADMIN", "REFEREE")
                .requestMatchers("/schedules/**").hasAnyRole("ADMIN", "REFEREE", "ATHLETE", "SPECTATOR")
                // 成绩管理 - 管理员和裁判可管理
                .requestMatchers("/results/record", "/results/*/edit", "/results/*/delete").hasAnyRole("ADMIN", "REFEREE")
                .requestMatchers("/results/**").hasAnyRole("ADMIN", "REFEREE", "ATHLETE", "SPECTATOR")
                // 管理员专用接口
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // 裁判专用接口
                .requestMatchers("/referee/**").hasAnyRole("ADMIN", "REFEREE")
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            // 表单登录配置
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            // 登出配置
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // 记住我功能
            .rememberMe(remember -> remember
                .key("sports-platform-secret-key")
                .tokenValiditySeconds(7 * 24 * 60 * 60) // 7天
                .rememberMeParameter("remember-me")
            )
            // CSRF保护
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/h2-console/**")
            )
            // 允许H2控制台iframe
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }
}
