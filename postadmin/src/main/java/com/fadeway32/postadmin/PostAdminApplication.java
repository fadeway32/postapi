package com.fadeway32.postadmin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {
        "com.fadeway32.postadmin",
        "com.fadeway32.postapi",
        "com.fadeway32.crypto"
})
@EnableAspectJAutoProxy(exposeProxy = true,proxyTargetClass = true)
@EnableAsync
@MapperScan("com.fadeway32.postadmin.mapper")
public class PostAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(PostAdminApplication.class, args);
    }
}
