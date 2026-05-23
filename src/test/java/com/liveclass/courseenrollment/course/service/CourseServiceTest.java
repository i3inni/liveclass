package com.liveclass.courseenrollment.course.service;

import com.liveclass.courseenrollment.course.dto.CourseCreateRequest;
import com.liveclass.courseenrollment.course.dto.CourseResponse;
import com.liveclass.courseenrollment.course.entity.CourseStatus;
import com.liveclass.courseenrollment.course.repository.CourseRepository;
import com.liveclass.courseenrollment.enrollment.repository.EnrollmentRepository;
import com.liveclass.courseenrollment.global.exception.BusinessException;
import com.liveclass.courseenrollment.user.entity.User;
import com.liveclass.courseenrollment.user.entity.UserRole;
import com.liveclass.courseenrollment.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class CourseServiceTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    private User creator;
    private User classmate;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        creator = userRepository.save(
                User.create("크리에이터", "creator@test.com", UserRole.CREATOR));
        classmate = userRepository.save(
                User.create("수강생", "classmate@test.com", UserRole.CLASSMATE));
    }

    @Test
    @DisplayName("크리에이터가 강의를 등록하면 DRAFT 상태로 생성된다")
    void createCourse() {
        CourseCreateRequest request = new CourseCreateRequest(
                "Spring Boot 입문", "설명", 50000, 30,
                LocalDate.now(), LocalDate.now().plusMonths(1));

        CourseResponse response = courseService.createCourse(creator.getId(), request);

        assertThat(response.status()).isEqualTo(CourseStatus.DRAFT);
        assertThat(response.title()).isEqualTo("Spring Boot 입문");
        assertThat(response.enrolledCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("크리에이터가 아닌 사용자가 강의를 등록하면 예외가 발생한다")
    void createCourseByNonCreator() {
        CourseCreateRequest request = new CourseCreateRequest(
                "Spring Boot 입문", "설명", 50000, 30,
                LocalDate.now(), LocalDate.now().plusMonths(1));

        assertThrows(BusinessException.class,
                () -> courseService.createCourse(classmate.getId(), request));
    }

    @Test
    @DisplayName("DRAFT 상태의 강의를 OPEN으로 전이할 수 있다")
    void openCourse() {
        CourseCreateRequest request = new CourseCreateRequest(
                "Spring Boot 입문", "설명", 50000, 30,
                LocalDate.now(), LocalDate.now().plusMonths(1));
        CourseResponse created = courseService.createCourse(creator.getId(), request);

        CourseResponse response = courseService.openCourse(creator.getId(), created.id());

        assertThat(response.status()).isEqualTo(CourseStatus.OPEN);
    }

    @Test
    @DisplayName("OPEN 상태의 강의를 다시 OPEN으로 전이하면 예외가 발생한다")
    void openCourseAlreadyOpen() {
        CourseCreateRequest request = new CourseCreateRequest(
                "Spring Boot 입문", "설명", 50000, 30,
                LocalDate.now(), LocalDate.now().plusMonths(1));
        CourseResponse created = courseService.createCourse(creator.getId(), request);
        courseService.openCourse(creator.getId(), created.id());

        assertThrows(BusinessException.class,
                () -> courseService.openCourse(creator.getId(), created.id()));
    }

    @Test
    @DisplayName("CLOSED 상태의 강의를 OPEN으로 전이하면 예외가 발생한다")
    void openCourseWhenClosed() {
        CourseCreateRequest request = new CourseCreateRequest(
                "Spring Boot 입문", "설명", 50000, 30,
                LocalDate.now(), LocalDate.now().plusMonths(1));
        CourseResponse created = courseService.createCourse(creator.getId(), request);
        courseService.openCourse(creator.getId(), created.id());
        courseService.closeCourse(creator.getId(), created.id());

        assertThrows(BusinessException.class,
                () -> courseService.openCourse(creator.getId(), created.id()));
    }

    @Test
    @DisplayName("OPEN 상태의 강의를 CLOSED로 전이할 수 있다")
    void closeCourse() {
        CourseCreateRequest request = new CourseCreateRequest(
                "Spring Boot 입문", "설명", 50000, 30,
                LocalDate.now(), LocalDate.now().plusMonths(1));
        CourseResponse created = courseService.createCourse(creator.getId(), request);
        courseService.openCourse(creator.getId(), created.id());

        CourseResponse response = courseService.closeCourse(creator.getId(), created.id());

        assertThat(response.status()).isEqualTo(CourseStatus.CLOSED);
    }

    @Test
    @DisplayName("DRAFT 상태에서 바로 CLOSED로 전이하면 예외가 발생한다")
    void closeCourseFromDraft() {
        CourseCreateRequest request = new CourseCreateRequest(
                "Spring Boot 입문", "설명", 50000, 30,
                LocalDate.now(), LocalDate.now().plusMonths(1));
        CourseResponse created = courseService.createCourse(creator.getId(), request);

        assertThrows(BusinessException.class,
                () -> courseService.closeCourse(creator.getId(), created.id()));
    }

    @Test
    @DisplayName("강의 상세 조회가 정상적으로 동작한다")
    void getCourseDetail() {
        CourseCreateRequest request = new CourseCreateRequest(
                "Spring Boot 입문", "설명", 50000, 30,
                LocalDate.now(), LocalDate.now().plusMonths(1));
        CourseResponse created = courseService.createCourse(creator.getId(), request);

        CourseResponse response = courseService.getCourse(created.id());

        assertThat(response.id()).isEqualTo(created.id());
        assertThat(response.title()).isEqualTo("Spring Boot 입문");
        assertThat(response.price()).isEqualTo(50000);
    }

    @Test
    @DisplayName("존재하지 않는 강의를 조회하면 예외가 발생한다")
    void getCourseNotFound() {
        assertThrows(BusinessException.class,
                () -> courseService.getCourse(999L));
    }

    @Test
    @DisplayName("강의 목록을 상태 필터로 조회할 수 있다")
    void getCoursesByStatus() {
        CourseCreateRequest request = new CourseCreateRequest(
                "Spring Boot 입문", "설명", 50000, 30,
                LocalDate.now(), LocalDate.now().plusMonths(1));
        CourseResponse created = courseService.createCourse(creator.getId(), request);
        courseService.openCourse(creator.getId(), created.id());

        Page<CourseResponse> openCourses = courseService.getCourses(CourseStatus.OPEN, PageRequest.of(0, 10));
        Page<CourseResponse> draftCourses = courseService.getCourses(CourseStatus.DRAFT, PageRequest.of(0, 10));

        assertThat(openCourses.getTotalElements()).isEqualTo(1);
        assertThat(draftCourses.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("상태 필터 없이 전체 강의 목록을 조회할 수 있다")
    void getAllCourses() {
        courseService.createCourse(creator.getId(), new CourseCreateRequest(
                "강의1", "설명", 50000, 30, LocalDate.now(), LocalDate.now().plusMonths(1)));
        courseService.createCourse(creator.getId(), new CourseCreateRequest(
                "강의2", "설명", 30000, 20, LocalDate.now(), LocalDate.now().plusMonths(1)));

        Page<CourseResponse> all = courseService.getCourses(null, PageRequest.of(0, 10));

        assertThat(all.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("본인 강의가 아닌 경우 상태 전이 시 예외가 발생한다")
    void openCourseByNonOwner() {
        User anotherCreator = userRepository.save(
                User.create("다른 크리에이터", "another@test.com", UserRole.CREATOR));
        CourseCreateRequest request = new CourseCreateRequest(
                "Spring Boot 입문", "설명", 50000, 30,
                LocalDate.now(), LocalDate.now().plusMonths(1));
        CourseResponse created = courseService.createCourse(creator.getId(), request);

        assertThrows(BusinessException.class,
                () -> courseService.openCourse(anotherCreator.getId(), created.id()));
    }
}
