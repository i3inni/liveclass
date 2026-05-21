package com.liveclass.courseenrollment.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * 강의 등록 요청 DTO
 */
@Schema(description = "강의 등록 요청")
public record CourseCreateRequest(

        @Schema(description = "강의 제목", example = "Spring Boot 입문")
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @Schema(description = "강의 설명", example = "Spring Boot 기초부터 실전까지")
        String description,

        @Schema(description = "강의 가격", example = "50000")
        @NotNull(message = "가격은 필수입니다.")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Integer price,

        @Schema(description = "최대 수강 인원", example = "30")
        @NotNull(message = "정원은 필수입니다.")
        @Min(value = 1, message = "정원은 1명 이상이어야 합니다.")
        Integer capacity,

        @Schema(description = "수강 시작일", example = "2026-07-01")
        @NotNull(message = "시작일은 필수입니다.")
        @FutureOrPresent(message = "시작일은 오늘 이후여야 합니다.")
        LocalDate startDate,

        @Schema(description = "수강 종료일", example = "2026-08-31")
        @NotNull(message = "종료일은 필수입니다.")
        @Future(message = "종료일은 오늘 이후여야 합니다.")
        LocalDate endDate
) {}