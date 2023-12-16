package org.java2.backend.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author: TaoYiCheng
 * @createDate: 2023/10/19
 * @description: 跨域配置
 * 跨域资源共享(CORS)是一种机制
 * 它使用额外的HTTP头来告诉浏览器
 * 让运行在一个源上的web应用程序从不同源访问所选资源。
 */
@Configuration
public class CorsConfig {
// 当前跨域请求最大有效时长。这里默认1天

    private static final long MAX_AGE = 24 * 60 * 60;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*"); // 1 设置访问源地址
        corsConfiguration.addAllowedHeader("*"); // 2 设置访问源请求头
        corsConfiguration.addAllowedMethod("*"); // 3 设置访问源请求方法
        corsConfiguration.setMaxAge(MAX_AGE);
        source.registerCorsConfiguration("/**", corsConfiguration);// 4 对接口配置跨域设置
        return new CorsFilter(source);
    }
}
