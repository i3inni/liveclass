package com.liveclass.enrollment.global.exception;

import lombok.Getter;

/**
 * 비즈니스 예외 클래스
 * ErrorCode를 기반으로 예외를 발생시킴
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}