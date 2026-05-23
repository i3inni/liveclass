package com.liveclass.courseenrollment.enrollment.service;

import com.liveclass.courseenrollment.course.entity.Course;
import com.liveclass.courseenrollment.course.repository.CourseRepository;
import com.liveclass.courseenrollment.enrollment.dto.EnrollmentCreateRequest;
import com.liveclass.courseenrollment.enrollment.entity.Enrollment;
import com.liveclass.courseenrollment.enrollment.entity.EnrollmentStatus;
import com.liveclass.courseenrollment.enrollment.repository.EnrollmentRepository;
import com.liveclass.courseenrollment.global.exception.BusinessException;
import com.liveclass.courseenrollment.global.exception.OptimisticLockConflictException;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class EnrollmentServiceTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private Course course;
    private User creator;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        creator = userRepository.save(
                User.create("크리에이터", "creator@test.com", UserRole.CREATOR));

        course = Course.create(creator, "테스트 강의", "설명", 10000, 1,
                LocalDate.now(), LocalDate.now().plusMonths(1));
        course.open();
        course = courseRepository.save(course);
    }

    // ───────────── 수강 신청 ─────────────

    @Test
    @DisplayName("동시에 여러 명이 마지막 자리에 신청하면 1명만 성공하고 나머지는 대기열에 등록된다")
    void concurrentEnrollment() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger waitlistCount = new AtomicInteger(0);

        List<User> users = userRepository.saveAll(List.of(
                User.create("수강생1", "user1@test.com", UserRole.CLASSMATE),
                User.create("수강생2", "user2@test.com", UserRole.CLASSMATE),
                User.create("수강생3", "user3@test.com", UserRole.CLASSMATE),
                User.create("수강생4", "user4@test.com", UserRole.CLASSMATE),
                User.create("수강생5", "user5@test.com", UserRole.CLASSMATE)
        ));

        for (User user : users) {
            executorService.submit(() -> {
                try {
                    EnrollmentCreateRequest request = new EnrollmentCreateRequest(course.getId());
                    try {
                        enrollmentService.enroll(user.getId(), request);
                    } catch (OptimisticLockConflictException e) {
                        enrollmentService.enrollWaitlist(e.getUserId(), e.getCourseId());
                    }
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    waitlistCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        List<Enrollment> enrollments = enrollmentRepository.findAll();
        long pendingCount = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.PENDING).count();
        long waitlistedCount = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.WAITLISTED).count();

        assertThat(pendingCount).isEqualTo(1);
        assertThat(waitlistedCount).isEqualTo(4);
    }

    @Test
    @DisplayName("정원이 초과된 강의에 신청하면 대기열에 등록된다")
    void enrollWhenCourseFull() {
        User user1 = userRepository.save(User.create("수강생1", "user1@test.com", UserRole.CLASSMATE));
        User user2 = userRepository.save(User.create("수강생2", "user2@test.com", UserRole.CLASSMATE));

        enrollmentService.enroll(user1.getId(), new EnrollmentCreateRequest(course.getId()));
        enrollmentService.enroll(user2.getId(), new EnrollmentCreateRequest(course.getId()));

        List<Enrollment> enrollments = enrollmentRepository.findAll();
        assertThat(enrollments).hasSize(2);
        assertThat(enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.PENDING).count()).isEqualTo(1);
        assertThat(enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.WAITLISTED).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("DRAFT 상태의 강의에 신청하면 예외가 발생한다")
    void enrollDraftCourse() {
        Course draftCourse = courseRepository.save(
                Course.create(creator, "드래프트 강의", "설명", 10000, 30,
                        LocalDate.now(), LocalDate.now().plusMonths(1)));
        User user = userRepository.save(User.create("수강생", "user@test.com", UserRole.CLASSMATE));

        assertThrows(BusinessException.class,
                () -> enrollmentService.enroll(user.getId(), new EnrollmentCreateRequest(draftCourse.getId())));
    }

    @Test
    @DisplayName("CLOSED 상태의 강의에 신청하면 예외가 발생한다")
    void enrollClosedCourse() {
        Course closedCourse = Course.create(creator, "종료 강의", "설명", 10000, 30,
                LocalDate.now(), LocalDate.now().plusMonths(1));
        closedCourse.open();
        closedCourse.close();
        closedCourse = courseRepository.save(closedCourse);
        User user = userRepository.save(User.create("수강생", "user@test.com", UserRole.CLASSMATE));

        Long courseId = closedCourse.getId();
        Long userId = user.getId();
        assertThrows(BusinessException.class,
                () -> enrollmentService.enroll(userId, new EnrollmentCreateRequest(courseId)));
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 신청하면 예외가 발생한다")
    void enrollUserNotFound() {
        assertThrows(BusinessException.class,
                () -> enrollmentService.enroll(999L, new EnrollmentCreateRequest(course.getId())));
    }

    @Test
    @DisplayName("존재하지 않는 강의에 신청하면 예외가 발생한다")
    void enrollCourseNotFound() {
        User user = userRepository.save(User.create("수강생", "user@test.com", UserRole.CLASSMATE));

        assertThrows(BusinessException.class,
                () -> enrollmentService.enroll(user.getId(), new EnrollmentCreateRequest(999L)));
    }

    @Test
    @DisplayName("이미 신청한 강의에 중복 신청하면 예외가 발생한다")
    void duplicateEnrollment() {
        User user = userRepository.save(User.create("수강생", "user@test.com", UserRole.CLASSMATE));
        enrollmentService.enroll(user.getId(), new EnrollmentCreateRequest(course.getId()));

        assertThrows(BusinessException.class,
                () -> enrollmentService.enroll(user.getId(), new EnrollmentCreateRequest(course.getId())));
    }

    // ───────────── 결제 확정 ─────────────

    @Test
    @DisplayName("PENDING 상태의 수강 신청을 결제 확정할 수 있다")
    void confirmEnrollment() {
        User user = userRepository.save(User.create("수강생", "user@test.com", UserRole.CLASSMATE));
        var enrollment = enrollmentService.enroll(user.getId(), new EnrollmentCreateRequest(course.getId()));

        var result = enrollmentService.confirm(user.getId(), enrollment.id());

        assertThat(result.status()).isEqualTo(EnrollmentStatus.CONFIRMED);
    }

    @Test
    @DisplayName("타인의 수강 신청을 결제 확정하면 예외가 발생한다")
    void confirmByOtherUser() {
        User user1 = userRepository.save(User.create("수강생1", "user1@test.com", UserRole.CLASSMATE));
        User user2 = userRepository.save(User.create("수강생2", "user2@test.com", UserRole.CLASSMATE));

        var enrollment = enrollmentService.enroll(user1.getId(), new EnrollmentCreateRequest(course.getId()));

        assertThrows(BusinessException.class,
                () -> enrollmentService.confirm(user2.getId(), enrollment.id()));
    }

    @Test
    @DisplayName("PENDING이 아닌 상태(WAITLISTED)의 수강 신청을 결제 확정하면 예외가 발생한다")
    void confirmWaitlistedStatus() {
        User user1 = userRepository.save(User.create("수강생1", "user1@test.com", UserRole.CLASSMATE));
        User user2 = userRepository.save(User.create("수강생2", "user2@test.com", UserRole.CLASSMATE));

        enrollmentService.enroll(user1.getId(), new EnrollmentCreateRequest(course.getId()));
        var waitlisted = enrollmentService.enroll(user2.getId(), new EnrollmentCreateRequest(course.getId()));

        assertThrows(BusinessException.class,
                () -> enrollmentService.confirm(user2.getId(), waitlisted.id()));
    }

    // ───────────── 수강 취소 ─────────────

    @Test
    @DisplayName("수강 취소 시 대기열 첫 번째 사람이 자동 승격된다")
    void cancelAndPromoteWaitlist() {
        User user1 = userRepository.save(User.create("수강생1", "user1@test.com", UserRole.CLASSMATE));
        User user2 = userRepository.save(User.create("수강생2", "user2@test.com", UserRole.CLASSMATE));

        var enrollment1 = enrollmentService.enroll(user1.getId(), new EnrollmentCreateRequest(course.getId()));
        enrollmentService.confirm(user1.getId(), enrollment1.id());
        enrollmentService.enroll(user2.getId(), new EnrollmentCreateRequest(course.getId()));

        enrollmentService.cancel(user1.getId(), enrollment1.id());

        Enrollment user2Enrollment = enrollmentRepository.findAll().stream()
                .filter(e -> e.getUser().getId().equals(user2.getId()))
                .findFirst().get();
        assertThat(user2Enrollment.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
    }

    @Test
    @DisplayName("타인의 수강 신청을 취소하면 예외가 발생한다")
    void cancelByOtherUser() {
        User user1 = userRepository.save(User.create("수강생1", "user1@test.com", UserRole.CLASSMATE));
        User user2 = userRepository.save(User.create("수강생2", "user2@test.com", UserRole.CLASSMATE));

        var enrollment = enrollmentService.enroll(user1.getId(), new EnrollmentCreateRequest(course.getId()));
        enrollmentService.confirm(user1.getId(), enrollment.id());

        assertThrows(BusinessException.class,
                () -> enrollmentService.cancel(user2.getId(), enrollment.id()));
    }

    @Test
    @DisplayName("결제 확정 후 7일이 지난 수강 신청을 취소하면 예외가 발생한다")
    void cancelExpiredPeriod() {
        User user = userRepository.save(User.create("수강생", "user@test.com", UserRole.CLASSMATE));
        var enrollmentResponse = enrollmentService.enroll(user.getId(), new EnrollmentCreateRequest(course.getId()));
        enrollmentService.confirm(user.getId(), enrollmentResponse.id());

        Enrollment enrollment = enrollmentRepository.findById(enrollmentResponse.id()).get();
        ReflectionTestUtils.setField(enrollment, "confirmedAt", LocalDateTime.now().minusDays(8));
        enrollmentRepository.save(enrollment);

        assertThrows(BusinessException.class,
                () -> enrollmentService.cancel(user.getId(), enrollmentResponse.id()));
    }

    @Test
    @DisplayName("PENDING 상태(결제 전)의 수강 신청은 취소할 수 없다")
    void cancelPendingEnrollment() {
        User user = userRepository.save(User.create("수강생", "user@test.com", UserRole.CLASSMATE));
        var enrollment = enrollmentService.enroll(user.getId(), new EnrollmentCreateRequest(course.getId()));

        assertThrows(BusinessException.class,
                () -> enrollmentService.cancel(user.getId(), enrollment.id()));
    }

    // ───────────── 목록 조회 ─────────────

    @Test
    @DisplayName("내 수강 신청 목록을 조회할 수 있다")
    void getMyEnrollments() {
        User user = userRepository.save(User.create("수강생", "user@test.com", UserRole.CLASSMATE));
        enrollmentService.enroll(user.getId(), new EnrollmentCreateRequest(course.getId()));

        Page<com.liveclass.courseenrollment.enrollment.dto.EnrollmentResponse> result =
                enrollmentService.getMyEnrollments(user.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).status()).isEqualTo(EnrollmentStatus.PENDING);
    }

    @Test
    @DisplayName("크리에이터는 강의별 수강생 목록을 조회할 수 있다")
    void getCourseEnrollments() {
        User user = userRepository.save(User.create("수강생", "user@test.com", UserRole.CLASSMATE));
        enrollmentService.enroll(user.getId(), new EnrollmentCreateRequest(course.getId()));

        Page<com.liveclass.courseenrollment.enrollment.dto.EnrollmentResponse> result =
                enrollmentService.getCourseEnrollments(creator.getId(), course.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("강의 소유자가 아닌 크리에이터가 수강생 목록을 조회하면 예외가 발생한다")
    void getCourseEnrollmentsByNonOwner() {
        User anotherCreator = userRepository.save(
                User.create("다른 크리에이터", "another@test.com", UserRole.CREATOR));

        assertThrows(BusinessException.class,
                () -> enrollmentService.getCourseEnrollments(
                        anotherCreator.getId(), course.getId(), PageRequest.of(0, 10)));
    }
}
