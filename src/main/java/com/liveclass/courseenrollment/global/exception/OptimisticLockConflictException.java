package com.liveclass.courseenrollment.global.exception;

import lombok.Getter;

@Getter
public class OptimisticLockConflictException extends RuntimeException {

    private final Long userId;
    private final Long courseId;

    public OptimisticLockConflictException(Long userId, Long courseId) {
        super("낙관적 락 충돌 발생");
        this.userId = userId;
        this.courseId = courseId;
    }
}
