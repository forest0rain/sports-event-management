package com.sports.platform.config;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.format.DateTimeFormatter;

/**
 * Web MVC 配置
 */
@Configuration
public class ThymeleafConfig implements WebMvcConfigurer {

    /**
     * Thymeleaf Layout Dialect - 用于模板布局
     */
    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }

    /**
     * 添加 Java 8 Time 格式化支持
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatterForFieldType(java.time.LocalDate.class,
                new org.springframework.format.datetime.joda.LocalDateFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        registry.addFormatterForFieldType(java.time.LocalDateTime.class,
                new org.springframework.format.datetime.joda.LocalDateTimeFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    /**
     * 静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
