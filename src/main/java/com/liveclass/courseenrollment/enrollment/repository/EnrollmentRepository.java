package com.liveclass.courseenrollment.enrollment.repository;

import com.liveclass.courseenrollment.enrollment.entity.Enrollment;
import com.liveclass.courseenrollment.enrollment.entity.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // 내 수강 신청 목록 조회 (페이지네이션)
    Page<Enrollment> findByUserId(Long userId, Pageable pageable);

    // 강의별 수강생 목록 조회 (크리에이터 전용)
    Page<Enrollment> findByCourseId(Long courseId, Pageable pageable);

    // 중복 신청 확인
    boolean existsByCourseIdAndUserIdAndStatusNot(Long courseId, Long userId, EnrollmentStatus status);

    // 수강 신청 상세 조회
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.course JOIN FETCH e.user WHERE e.id = :id")
    Optional<Enrollment> findByIdWithCourseAndUser(Long id);

    // 대기열 첫 번째 조회 (가장 먼저 등록한 사람)
    Optional<Enrollment> findFirstByCourseIdAndStatusOrderByCreatedAtAsc(
            Long courseId, EnrollmentStatus status);

    // 대기열 중복 확인
    boolean existsByCourseIdAndUserIdAndStatus(
            Long courseId, Long userId, EnrollmentStatus status);
}