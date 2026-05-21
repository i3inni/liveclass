package com.liveclass.enrollment.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 스웨거 설정 파일
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LiveClass 수강 신청 API")
                        .description("크리에이터와 클래스메이트를 위한 수강 신청 시스템")
                        .version("v1.0.0"));
    }
}