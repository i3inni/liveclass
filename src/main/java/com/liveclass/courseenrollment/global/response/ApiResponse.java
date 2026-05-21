package com.liveclass.courseenrollment.global.response;

import com.liveclass.courseenrollment.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 공통 API 응답 형식
 * 모든 API 응답을 일관된 형식으로 반환
 */
@Schema(description = "공통 API 응답")
@Getter
@RequiredArgsConstructor
public class ApiResponse<T> {

    @Schema(description = "성공 여부", example = "true")
    private final boolean success;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private final String message;

    @Schema(description = "응답 데이터")
    private final T data;

    // 성공 응답 (데이터 있음)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "요청이 성공적으로 처리되었습니다.", data);
    }

    // 성공 응답 (데이터 없음)
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, "요청이 성공적으로 처리되었습니다.", null);
    }

    // 비즈니스 예외 응답
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, errorCode.getMessage(), null);
    }

    // 유효성 검증 예외 응답
    public static <T> ApiResponse<T> validationError(String message) {
        return new ApiResponse<>(false, message, null);
    }

    // 서버 에러 응답
    public static <T> ApiResponse<T> serverError() {
        return new ApiResponse<>(false, "서버 내부 오류가 발생했습니다.", null);
    }
}