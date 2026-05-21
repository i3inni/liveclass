package com.liveclass.courseenrollment.user.dto;

import com.liveclass.courseenrollment.user.entity.User;
import com.liveclass.courseenrollment.user.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 사용자 응답 DTO
 */
@Schema(description = "사용자 응답")
public record UserResponse(

        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "사용자 이름", example = "김다빈")
        String name,

        @Schema(description = "이메일", example = "dabin@liveclass.com")
        String email,

        @Schema(description = "사용자 역할", example = "CREATOR")
        UserRole role,

        @Schema(description = "생성일시")
        LocalDateTime createdAt
) {
        // 엔티티 → DTO 변환 정적 팩토리 메서드
        public static UserResponse from(User user) {
                return new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getCreatedAt()
                );
        }
}