package com.liveclass.courseenrollment.enrollment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 수강 신청 요청 DTO
 */
@Schema(description = "수강 신청 요청")
public record EnrollmentCreateRequest(

        @Schema(description = "강의 ID", example = "1")
        @NotNull(message = "강의 ID는 필수입니다.")
        Long courseId
) {}