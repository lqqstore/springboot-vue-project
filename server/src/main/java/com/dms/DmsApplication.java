package com.dms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan({
        "com.dms.modules.auth.mapper",
        "com.dms.modules.dorm.mapper",
        "com.dms.modules.student.mapper",
        "com.dms.modules.repair.mapper",
        "com.dms.modules.notice.mapper"
})
public class DmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DmsApplication.class, args);
    }
}

