package com.liveclass.courseenrollment.enrollment.dto;

import com.liveclass.courseenrollment.enrollment.entity.Enrollment;
import com.liveclass.courseenrollment.enrollment.entity.EnrollmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 수강 신청 응답 DTO
 */
@Schema(description = "수강 신청 응답")
public record EnrollmentResponse(

        @Schema(description = "수강 신청 ID", example = "1")
        Long id,

        @Schema(description = "강의 ID", example = "1")
        Long courseId,

        @Schema(description = "강의 제목", example = "Spring Boot 입문")
        String courseTitle,

        @Schema(description = "수강생 ID", example = "1")
        Long userId,

        @Schema(description = "수강생 이름", example = "김다빈")
        String userName,

        @Schema(description = "수강 신청 상태", example = "PENDING")
        EnrollmentStatus status,

        @Schema(description = "결제 확정 일시")
        LocalDateTime confirmedAt,

        @Schema(description = "취소 일시")
        LocalDateTime cancelledAt,

        @Schema(description = "생성일시")
        LocalDateTime createdAt
) {
        // 엔티티 → DTO 변환 정적 팩토리 메서드
        public static EnrollmentResponse from(Enrollment enrollment) {
                return new EnrollmentResponse(
                        enrollment.getId(),
                        enrollment.getCourse().getId(),
                        enrollment.getCourse().getTitle(),
                        enrollment.getUser().getId(),
                        enrollment.getUser().getName(),
                        enrollment.getStatus(),
                        enrollment.getConfirmedAt(),
                        enrollment.getCancelledAt(),
                        enrollment.getCreatedAt()
                );
        }
}