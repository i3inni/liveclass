package com.liveclass.courseenrollment.user.dto;

import com.liveclass.courseenrollment.user.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 사용자 등록 요청 DTO
 */
@Schema(description = "사용자 등록 요청")
public record UserCreateRequest(

        @Schema(description = "사용자 이름", example = "김다빈")
        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @Schema(description = "이메일", example = "dabin@liveclass.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @Schema(description = "사용자 역할 (CREATOR / CLASSMATE)", example = "CREATOR")
        @NotNull(message = "역할은 필수입니다.")
        UserRole role
) {}