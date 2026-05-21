package com.liveclass.courseenrollment.enrollment.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 수강 신청 상태
 * PENDING → CONFIRMED → CANCELLED 순으로 전이
 */
@Schema(description = "수강 신청 상태")
public enum EnrollmentStatus {
    @Schema(description = "신청 완료, 결제 대기")
    PENDING,
    @Schema(description = "결제 완료, 수강 확정")
    CONFIRMED,
    @Schema(description = "취소됨")
    CANCELLED
}