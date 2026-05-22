package com.liveclass.courseenrollment.course.service;

import com.liveclass.courseenrollment.course.dto.CourseCreateRequest;
import com.liveclass.courseenrollment.course.dto.CourseResponse;
import com.liveclass.courseenrollment.course.entity.Course;
import com.liveclass.courseenrollment.course.entity.CourseStatus;
import com.liveclass.courseenrollment.course.repository.CourseRepository;
import com.liveclass.courseenrollment.global.exception.BusinessException;
import com.liveclass.courseenrollment.global.exception.ErrorCode;
import com.liveclass.courseenrollment.user.entity.User;
import com.liveclass.courseenrollment.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 강의 서비스
 * 강의 등록, 조회, 상태 전이 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    // 강의 등록 (크리에이터만 가능)
    @Transactional
    public CourseResponse createCourse(Long creatorId, CourseCreateRequest request) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 크리에이터 권한 확인
        if (!creator.isCreator()) {
            throw new BusinessException(ErrorCode.NOT_CREATOR);
        }

        Course course = Course.create(
                creator,
                request.title(),
                request.description(),
                request.price(),
                request.capacity(),
                request.startDate(),
                request.endDate()
        );

        return CourseResponse.from(courseRepository.save(course));
    }

    // 강의 목록 조회 (상태 필터)
    public Page<CourseResponse> getCourses(CourseStatus status, Pageable pageable) {
        if (status != null) {
            return courseRepository.findByStatus(status, pageable)
                    .map(CourseResponse::from);
        }
        return courseRepository.findAll(pageable)
                .map(CourseResponse::from);
    }

    // 강의 상세 조회
    public CourseResponse getCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));
        return CourseResponse.from(course);
    }

    // 강의 OPEN 상태 전이 (크리에이터만 가능)
    @Transactional
    public CourseResponse openCourse(Long creatorId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        // 크리에이터 본인 확인
        if (!course.isOwnedBy(creatorId)) {
            throw new BusinessException(ErrorCode.NOT_COURSE_OWNER);
        }

        course.open();
        return CourseResponse.from(course);
    }

    // 강의 CLOSE 상태 전이 (크리에이터만 가능)
    @Transactional
    public CourseResponse closeCourse(Long creatorId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        // 크리에이터 본인 확인
        if (!course.isOwnedBy(creatorId)) {
            throw new BusinessException(ErrorCode.NOT_COURSE_OWNER);
        }

        course.close();
        return CourseResponse.from(course);
    }
}