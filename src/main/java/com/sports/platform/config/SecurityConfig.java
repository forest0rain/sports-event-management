package com.sports.platform.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/error"
                        ).permitAll()

                        // 只有管理员能看上传文件
                        .requestMatchers("/files/**").hasRole("ADMIN")

                        // 赛事管理 - 管理员和裁判
                        .requestMatchers("/events/create", "/events/*/edit", "/events/*/publish", "/events/*/cancel").hasAnyRole("ADMIN", "REFEREE")
                        // 赛程管理 - 管理员和裁判
                        .requestMatchers("/schedules/create", "/schedules/*/edit", "/schedules/generate").hasAnyRole("ADMIN", "REFEREE")
                        // 成绩管理 - 管理员和裁判可录入
                        .requestMatchers("/results/record").hasAnyRole("ADMIN", "REFEREE")
                        // 报名相关 - 所有登录用户
                        .requestMatchers("/registrations/**").authenticated()
                        // 统计页面
                        .requestMatchers("/results/statistics").hasAnyRole("ADMIN", "REFEREE")
                        // 管理员审核报名
                        .requestMatchers("/admin/registrations/**").hasAnyRole("ADMIN", "REFEREE")

                        // 其余请求都要登录
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}