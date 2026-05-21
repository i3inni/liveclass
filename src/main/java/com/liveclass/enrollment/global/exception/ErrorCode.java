package com.liveclass.enrollment.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 * 비즈니스 예외 상황별 코드, 메시지, HTTP 상태 관리
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    NOT_CREATOR(HttpStatus.FORBIDDEN, "크리에이터만 접근 가능합니다."),

    // Course
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "강의를 찾을 수 없습니다."),
    COURSE_NOT_OPEN(HttpStatus.BAD_REQUEST, "모집 중인 강의가 아닙니다."),
    COURSE_FULL(HttpStatus.CONFLICT, "수강 정원이 초과되었습니다."),
    INVALID_COURSE_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 강의 상태 전이입니다."),
    NOT_COURSE_OWNER(HttpStatus.FORBIDDEN, "강의 개설자만 접근 가능합니다."),

    // Enrollment
    ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "수강 신청을 찾을 수 없습니다."),
    ALREADY_ENROLLED(HttpStatus.CONFLICT, "이미 수강 신청한 강의입니다."),
    CANCEL_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "취소 가능 기간이 지났습니다."),
    INVALID_ENROLLMENT_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 수강 신청 상태입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}