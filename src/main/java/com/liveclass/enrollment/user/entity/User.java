package com.liveclass.enrollment.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 엔티티
 * 크리에이터(강사)와 클래스메이트(수강생)를 구분
 */
@Schema(description = "사용자 엔티티")
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Schema(description = "사용자 ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "사용자 이름", example = "김다빈")
    @Column(nullable = false)
    private String name;

    @Schema(description = "이메일 (로그인 식별자, 중복 불가)", example = "dabin@liveclass.com")
    @Column(nullable = false, unique = true)
    private String email;

    @Schema(description = "사용자 역할 (CREATOR: 강사, CLASSMATE: 수강생)", example = "CREATOR")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Schema(description = "생성일시")
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 정적 팩토리 메서드로 생성
    public static User create(String name, String email, UserRole role) {
        User user = new User();
        user.name = name;
        user.email = email;
        user.role = role;
        return user;
    }

    // 크리에이터 여부 확인
    public boolean isCreator() {
        return this.role == UserRole.CREATOR;
    }
}