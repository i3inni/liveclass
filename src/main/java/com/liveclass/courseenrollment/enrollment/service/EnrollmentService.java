package com.liveclass.courseenrollment.enrollment.service;

import com.liveclass.courseenrollment.course.entity.Course;
import com.liveclass.courseenrollment.course.entity.CourseStatus;
import com.liveclass.courseenrollment.course.repository.CourseRepository;
import com.liveclass.courseenrollment.enrollment.dto.EnrollmentCreateRequest;
import com.liveclass.courseenrollment.enrollment.dto.EnrollmentResponse;
import com.liveclass.courseenrollment.enrollment.entity.Enrollment;
import com.liveclass.courseenrollment.enrollment.entity.EnrollmentStatus;
import com.liveclass.courseenrollment.enrollment.repository.EnrollmentRepository;
import com.liveclass.courseenrollment.global.exception.BusinessException;
import com.liveclass.courseenrollment.global.exception.ErrorCode;
import com.liveclass.courseenrollment.global.exception.OptimisticLockConflictException;
import com.liveclass.courseenrollment.user.entity.User;
import com.liveclass.courseenrollment.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 수강 신청 서비스
 * 수강 신청, 결제 확정, 취소 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    // 수강 신청 메서드(정원 초과 시 대기열 등록)
    @Transactional
    public EnrollmentResponse enroll(Long userId, EnrollmentCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (course.getStatus() != CourseStatus.OPEN) {
            throw new BusinessException(ErrorCode.COURSE_NOT_OPEN);
        }

        if (enrollmentRepository.existsByCourseIdAndUserIdAndStatusNot(
                course.getId(), userId, EnrollmentStatus.CANCELLED)) {
            throw new BusinessException(ErrorCode.ALREADY_ENROLLED);
        }

        if (!course.isEnrollable()) {
            Enrollment waitlist = Enrollment.createWaitlist(course, user);
            return EnrollmentResponse.from(enrollmentRepository.save(waitlist));
        }

        try {
            course.increaseEnrolledCount();
            courseRepository.saveAndFlush(course);
            Enrollment enrollment = Enrollment.create(course, user);
            return EnrollmentResponse.from(enrollmentRepository.save(enrollment));
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new OptimisticLockConflictException(userId, course.getId());
        }
    }

    @Transactional
    public EnrollmentResponse enrollWaitlist(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));
        Enrollment waitlist = Enrollment.createWaitlist(course, user);
        return EnrollmentResponse.from(enrollmentRepository.save(waitlist));
    }

    // 결제 확정
    @Transactional
    public EnrollmentResponse confirm(Long userId, Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findByIdWithCourseAndUser(enrollmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND));

        // 본인 확인
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND);
        }

        try {
            enrollment.confirm();
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.INVALID_ENROLLMENT_STATUS);
        }
        return EnrollmentResponse.from(enrollment);
    }

    // 취소 시 대기열 첫 번째 사람 자동 승격
    @Transactional
    public EnrollmentResponse cancel(Long userId, Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findByIdWithCourseAndUser(enrollmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND));

        if (!enrollment.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND);
        }

        if (!enrollment.isCancellable()) {
            throw new BusinessException(ErrorCode.CANCEL_PERIOD_EXPIRED);
        }

        enrollment.cancel();
        enrollment.getCourse().decreaseEnrolledCount();

        // 대기열 첫 번째 사람 승격
        enrollmentRepository.findFirstByCourseIdAndStatusOrderByCreatedAtAsc(
                        enrollment.getCourse().getId(), EnrollmentStatus.WAITLISTED)
                .ifPresent(waitlisted -> {
                    waitlisted.promote();
                    enrollment.getCourse().increaseEnrolledCount();
                });

        return EnrollmentResponse.from(enrollment);
    }

    // 내 수강 신청 목록 조회
    public Page<EnrollmentResponse> getMyEnrollments(Long userId, Pageable pageable) {
        return enrollmentRepository.findByUserId(userId, pageable)
                .map(EnrollmentResponse::from);
    }

    // 강의별 수강생 목록 조회 (크리에이터 전용)
    public Page<EnrollmentResponse> getCourseEnrollments(Long creatorId, Long courseId, Pageable pageable) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        // 크리에이터 본인 확인
        if (!course.isOwnedBy(creatorId)) {
            throw new BusinessException(ErrorCode.NOT_COURSE_OWNER);
        }

        return enrollmentRepository.findByCourseId(courseId, pageable)
                .map(EnrollmentResponse::from);
    }


}