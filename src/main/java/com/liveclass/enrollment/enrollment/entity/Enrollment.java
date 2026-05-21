package com.liveclass.enrollment.enrollment.entity;

import com.liveclass.enrollment.course.entity.Course;
import com.liveclass.enrollment.user.entity.User;
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
 * 수강 신청 엔티티
 * 수강생이 강의에 신청한 정보를 관리
 * PENDING → CONFIRMED → CANCELLED 상태 전이
 */
@Schema(description = "수강 신청 엔티티")
@Entity
@Table(
        name = "enrollments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "user_id"}),
        indexes = {
                @Index(name = "idx_enrollment_course_id", columnList = "course_id"),
                @Index(name = "idx_enrollment_user_id", columnList = "user_id"),
                @Index(name = "idx_enrollment_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Enrollment {

    @Schema(description = "수강 신청 ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 수강 신청한 강의
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // 수강 신청한 수강생
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Schema(description = "수강 신청 상태", example = "PENDING")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    // 결제 확정 일시
    @Schema(description = "결제 확정 일시")
    private LocalDateTime confirmedAt;

    // 취소 일시
    @Schema(description = "취소 일시")
    private LocalDateTime cancelledAt;

    @Schema(description = "생성일시")
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 정적 팩토리 메서드로 생성 (초기 상태는 PENDING)
    public static Enrollment create(Course course, User user) {
        Enrollment enrollment = new Enrollment();
        enrollment.course = course;
        enrollment.user = user;
        enrollment.status = EnrollmentStatus.PENDING;
        return enrollment;
    }

    // PENDING → CONFIRMED 상태 전이 (결제 확정)
    public void confirm() {
        if (this.status != EnrollmentStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 CONFIRMED로 변경 가능합니다.");
        }
        this.status = EnrollmentStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    // CONFIRMED → CANCELLED 상태 전이 (수강 취소)
    public void cancel() {
        if (this.status == EnrollmentStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 수강 신청입니다.");
        }
        this.status = EnrollmentStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    // 취소 가능 여부 확인 (결제 후 7일 이내)
    public boolean isCancellable() {
        if (this.status != EnrollmentStatus.CONFIRMED) {
            return false;
        }
        return this.confirmedAt.plusDays(7).isAfter(LocalDateTime.now());
    }
}