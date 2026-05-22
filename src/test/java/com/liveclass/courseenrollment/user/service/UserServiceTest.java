package com.liveclass.courseenrollment.user.service;

import com.liveclass.courseenrollment.course.repository.CourseRepository;
import com.liveclass.courseenrollment.enrollment.repository.EnrollmentRepository;
import com.liveclass.courseenrollment.global.exception.BusinessException;
import com.liveclass.courseenrollment.user.dto.UserCreateRequest;
import com.liveclass.courseenrollment.user.dto.UserResponse;
import com.liveclass.courseenrollment.user.entity.UserRole;
import com.liveclass.courseenrollment.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("사용자를 등록하면 저장된 사용자 정보가 반환된다")
    void createUser() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "김다빈", "dabin@liveclass.com", UserRole.CREATOR);

        // when
        UserResponse response = userService.createUser(request);

        // then
        assertThat(response.name()).isEqualTo("김다빈");
        assertThat(response.email()).isEqualTo("dabin@liveclass.com");
        assertThat(response.role()).isEqualTo(UserRole.CREATOR);
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 등록하면 예외가 발생한다")
    void createUserDuplicateEmail() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "김다빈", "dabin@liveclass.com", UserRole.CREATOR);
        userService.createUser(request);

        // when & then
        assertThrows(BusinessException.class,
                () -> userService.createUser(new UserCreateRequest(
                        "다른사람", "dabin@liveclass.com", UserRole.CLASSMATE)));
    }

    @Test
    @DisplayName("존재하는 사용자를 조회하면 사용자 정보가 반환된다")
    void getUser() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "김다빈", "dabin@liveclass.com", UserRole.CREATOR);
        UserResponse created = userService.createUser(request);

        // when
        UserResponse response = userService.getUser(created.id());

        // then
        assertThat(response.id()).isEqualTo(created.id());
        assertThat(response.name()).isEqualTo("김다빈");
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 조회하면 예외가 발생한다")
    void getUserNotFound() {
        // when & then
        assertThrows(BusinessException.class,
                () -> userService.getUser(999L));
    }
}