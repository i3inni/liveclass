package com.liveclass.enrollment.course.repository;

import com.liveclass.enrollment.course.entity.Course;
import com.liveclass.enrollment.course.entity.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // 상태 필터로 강의 목록 조회 (페이지네이션)
    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    // 전체 강의 목록 조회 (페이지네이션)
    Page<Course> findAll(Pageable pageable);

    // 강의 상세 조회 (낙관적 락 적용)
    @Query("SELECT c FROM Course c WHERE c.id = :id")
    Optional<Course> findByIdWithLock(Long id);
}