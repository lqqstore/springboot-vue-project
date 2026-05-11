package com.dms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置占位。
 *
 * 说明：当前依赖版本下不存在 `PaginationInnerInterceptor` 类。
 * 为避免覆盖掉 Spring Boot MyBatis-Plus 自动配置（分页拦截器等），
 * 此处不手动声明 `MybatisPlusInterceptor` Bean。
 */
@Configuration
public class MybatisPlusConfig {

    // 预留：后续可在不覆盖默认分页拦截器的前提下扩展配置
}

