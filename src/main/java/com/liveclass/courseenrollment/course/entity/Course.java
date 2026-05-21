package com.liveclass.courseenrollment.course.entity;

import com.liveclass.courseenrollment.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 강의 엔티티
 * 크리에이터가 개설하는 강의 정보를 관리
 * DRAFT → OPEN → CLOSED 상태 전이
 */
@Schema(description = "강의 엔티티")
@Entity
@Table(name = "courses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Course {

    @Schema(description = "강의 ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 강의를 개설한 크리에이터
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Schema(description = "강의 제목", example = "Spring Boot 입문")
    @Column(nullable = false)
    private String title;

    @Schema(description = "강의 설명", example = "Spring Boot 기초부터 실전까지")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Schema(description = "강의 가격", example = "50000")
    @Column(nullable = false)
    private int price;

    @Schema(description = "최대 수강 인원", example = "30")
    @Column(nullable = false)
    private int capacity;

    @Schema(description = "현재 수강 신청 인원", example = "10")
    @Column(nullable = false)
    private int enrolledCount;

    @Schema(description = "강의 상태", example = "OPEN")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status;

    @Schema(description = "수강 시작일", example = "2026-07-01")
    @Column(nullable = false)
    private LocalDate startDate;

    @Schema(description = "수강 종료일", example = "2026-08-31")
    @Column(nullable = false)
    private LocalDate endDate;

    // 동시성 제어를 위한 낙관적 락
    @Version
    private Long version;

    @Schema(description = "생성일시")
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 정적 팩토리 메서드로 생성 (초기 상태는 DRAFT)
    public static Course create(User creator, String title, String description,
                                int price, int capacity, LocalDate startDate, LocalDate endDate) {
        Course course = new Course();
        course.creator = creator;
        course.title = title;
        course.description = description;
        course.price = price;
        course.capacity = capacity;
        course.enrolledCount = 0;
        course.status = CourseStatus.DRAFT;
        course.startDate = startDate;
        course.endDate = endDate;
        return course;
    }

    // DRAFT → OPEN 상태 전이
    public void open() {
        if (this.status != CourseStatus.DRAFT) {
            throw new IllegalStateException("DRAFT 상태에서만 OPEN으로 변경 가능합니다.");
        }
        this.status = CourseStatus.OPEN;
    }

    // OPEN → CLOSED 상태 전이
    public void close() {
        if (this.status != CourseStatus.OPEN) {
            throw new IllegalStateException("OPEN 상태에서만 CLOSED로 변경 가능합니다.");
        }
        this.status = CourseStatus.CLOSED;
    }

    // 수강 신청 가능 여부 확인
    public boolean isEnrollable() {
        return this.status == CourseStatus.OPEN && this.enrolledCount < this.capacity;
    }

    // 수강 인원 증가
    public void increaseEnrolledCount() {
        if (!isEnrollable()) {
            throw new IllegalStateException("수강 신청이 불가능한 강의입니다.");
        }
        this.enrolledCount++;
    }

    // 수강 인원 감소 (취소 시)
    public void decreaseEnrolledCount() {
        if (this.enrolledCount <= 0) {
            throw new IllegalStateException("수강 인원이 0명 이하입니다.");
        }
        this.enrolledCount--;
    }

    // 크리에이터 본인 확인
    public boolean isOwnedBy(Long userId) {
        return this.creator.getId().equals(userId);
    }
}