package com.liveclass.enrollment.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 사용자 역할
 * CREATOR: 강의를 개설하는 강사
 * CLASSMATE: 강의를 수강하는 수강생
 */
@Schema(description = "사용자 역할")
public enum UserRole {
    @Schema(description = "강의를 개설하는 강사")
    CREATOR,
    @Schema(description = "강의를 수강하는 수강생")
    CLASSMATE
}