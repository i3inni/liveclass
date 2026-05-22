package com.liveclass.courseenrollment.enrollment.controller;

import com.liveclass.courseenrollment.enrollment.dto.EnrollmentCreateRequest;
import com.liveclass.courseenrollment.enrollment.dto.EnrollmentResponse;
import com.liveclass.courseenrollment.enrollment.service.EnrollmentService;
import com.liveclass.courseenrollment.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 수강 신청 컨트롤러
 * 수강 신청, 결제 확정, 취소, 목록 조회 API
 */
@Tag(name = "Enrollment", description = "수강 신청 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Operation(summary = "수강 신청")
    @PostMapping("/enrollments")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody EnrollmentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(enrollmentService.enroll(userId, request)));
    }

    @Operation(summary = "결제 확정")
    @PatchMapping("/enrollments/{enrollmentId}/confirm")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> confirm(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long enrollmentId) {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.confirm(userId, enrollmentId)));
    }

    @Operation(summary = "수강 취소")
    @PatchMapping("/enrollments/{enrollmentId}/cancel")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> cancel(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long enrollmentId) {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.cancel(userId, enrollmentId)));
    }

    @Operation(summary = "내 수강 신청 목록 조회")
    @GetMapping("/enrollments/me")
    public ResponseEntity<ApiResponse<Page<EnrollmentResponse>>> getMyEnrollments(
            @RequestHeader("X-User-Id") Long userId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.getMyEnrollments(userId, pageable)));
    }

    @Operation(summary = "강의별 수강생 목록 조회 (크리에이터 전용)")
    @GetMapping("/courses/{courseId}/enrollments")
    public ResponseEntity<ApiResponse<Page<EnrollmentResponse>>> getCourseEnrollments(
            @RequestHeader("X-User-Id") Long creatorId,
            @PathVariable Long courseId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                enrollmentService.getCourseEnrollments(creatorId, courseId, pageable)));
    }
}