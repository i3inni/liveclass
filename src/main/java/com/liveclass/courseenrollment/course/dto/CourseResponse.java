package com.liveclass.courseenrollment.course.dto;

import com.liveclass.courseenrollment.course.entity.Course;
import com.liveclass.courseenrollment.course.entity.CourseStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 강의 응답 DTO
 */
@Schema(description = "강의 응답")
public record CourseResponse(

        @Schema(description = "강의 ID", example = "1")
        Long id,

        @Schema(description = "크리에이터 ID", example = "1")
        Long creatorId,

        @Schema(description = "크리에이터 이름", example = "김다빈")
        String creatorName,

        @Schema(description = "강의 제목", example = "Spring Boot 입문")
        String title,

        @Schema(description = "강의 설명", example = "Spring Boot 기초부터 실전까지")
        String description,

        @Schema(description = "강의 가격", example = "50000")
        int price,

        @Schema(description = "최대 수강 인원", example = "30")
        int capacity,

        @Schema(description = "현재 수강 신청 인원", example = "10")
        int enrolledCount,

        @Schema(description = "강의 상태", example = "OPEN")
        CourseStatus status,

        @Schema(description = "수강 시작일", example = "2026-07-01")
        LocalDate startDate,

        @Schema(description = "수강 종료일", example = "2026-08-31")
        LocalDate endDate,

        @Schema(description = "생성일시")
        LocalDateTime createdAt
) {
        // 엔티티 → DTO 변환 정적 팩토리 메서드
        public static CourseResponse from(Course course) {
                return new CourseResponse(
                        course.getId(),
                        course.getCreator().getId(),
                        course.getCreator().getName(),
                        course.getTitle(),
                        course.getDescription(),
                        course.getPrice(),
                        course.getCapacity(),
                        course.getEnrolledCount(),
                        course.getStatus(),
                        course.getStartDate(),
                        course.getEndDate(),
                        course.getCreatedAt()
                );
        }
}