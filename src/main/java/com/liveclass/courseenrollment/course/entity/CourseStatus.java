package com.liveclass.courseenrollment.course.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 강의 상태
 * DRAFT → OPEN → CLOSED 순으로 전이
 */
@Schema(description = "강의 상태")
public enum CourseStatus {
    @Schema(description = "초안 (신청 불가)")
    DRAFT,
    @Schema(description = "모집 중 (신청 가능)")
    OPEN,
    @Schema(description = "모집 마감 (신청 불가)")
    CLOSED
}