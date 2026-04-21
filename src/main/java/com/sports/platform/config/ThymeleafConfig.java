package com.sports.platform.config;

import com.sports.platform.util.DateTimeFormatUtil;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
     * 日期时间格式化工具类 - 用于 Thymeleaf 模板
     * 在模板中使用: ${@dateTimeFormatUtil.formatDate(event.startDate)}
     */
    @Bean
    public DateTimeFormatUtil dateTimeFormatUtil() {
        return new DateTimeFormatUtil();
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
