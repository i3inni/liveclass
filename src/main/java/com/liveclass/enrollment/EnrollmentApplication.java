package com.liveclass.enrollment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class EnrollmentApplication {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EnrollmentApplication.class, args);
        log.info("Swagger UI: http://localhost:8080/swagger-ui.html");
        log.info("API Docs: http://localhost:8080/api-docs");
    }
}
