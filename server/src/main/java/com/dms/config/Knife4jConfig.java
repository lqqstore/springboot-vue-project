package com.dms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Knife4jConfig implements WebMvcConfigurer {
    // Knife4j 4.x 版本已经内置了配置，只需要在 application.yml 中启用即可
    // 本类仅作为配置标记
}

