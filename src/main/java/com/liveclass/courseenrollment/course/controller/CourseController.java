package com.liveclass.courseenrollment.course.controller;

import com.liveclass.courseenrollment.course.dto.CourseCreateRequest;
import com.liveclass.courseenrollment.course.dto.CourseResponse;
import com.liveclass.courseenrollment.course.entity.CourseStatus;
import com.liveclass.courseenrollment.course.service.CourseService;
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
 * 강의 컨트롤러
 * 강의 등록, 조회, 상태 전이 API
 */
@Tag(name = "Course", description = "강의 API")
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "강의 등록 (크리에이터만 가능)")
    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @RequestHeader("X-User-Id") Long creatorId,
            @Valid @RequestBody CourseCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(courseService.createCourse(creatorId, request)));
    }

    @Operation(summary = "강의 목록 조회 (상태 필터)")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> getCourses(
            @RequestParam(required = false) CourseStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getCourses(status, pageable)));
    }

    @Operation(summary = "강의 상세 조회")
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourse(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getCourse(courseId)));
    }

    @Operation(summary = "강의 OPEN 상태 전이 (크리에이터만 가능)")
    @PatchMapping("/{courseId}/open")
    public ResponseEntity<ApiResponse<CourseResponse>> openCourse(
            @RequestHeader("X-User-Id") Long creatorId,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.success(courseService.openCourse(creatorId, courseId)));
    }

    @Operation(summary = "강의 CLOSE 상태 전이 (크리에이터만 가능)")
    @PatchMapping("/{courseId}/close")
    public ResponseEntity<ApiResponse<CourseResponse>> closeCourse(
            @RequestHeader("X-User-Id") Long creatorId,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.success(courseService.closeCourse(creatorId, courseId)));
    }
}