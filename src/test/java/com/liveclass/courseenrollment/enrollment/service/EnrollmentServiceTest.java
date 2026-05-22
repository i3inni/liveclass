package com.liveclass.courseenrollment.enrollment.service;

import com.liveclass.courseenrollment.course.entity.Course;
import com.liveclass.courseenrollment.course.entity.CourseStatus;
import com.liveclass.courseenrollment.course.repository.CourseRepository;
import com.liveclass.courseenrollment.enrollment.dto.EnrollmentCreateRequest;
import com.liveclass.courseenrollment.enrollment.entity.Enrollment;
import com.liveclass.courseenrollment.enrollment.entity.EnrollmentStatus;
import com.liveclass.courseenrollment.enrollment.repository.EnrollmentRepository;
import com.liveclass.courseenrollment.user.entity.User;
import com.liveclass.courseenrollment.user.entity.UserRole;
import com.liveclass.courseenrollment.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

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

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // 크리에이터 생성
        User creator = userRepository.save(
                User.create("크리에이터", "creator@test.com", UserRole.CREATOR));

        // 정원 1명짜리 강의 생성
        course = Course.create(creator, "테스트 강의", "설명", 10000, 1,
                LocalDate.now(), LocalDate.now().plusMonths(1));
        course.open();
        course = courseRepository.save(course);
    }

    @Test
    @DisplayName("동시에 여러 명이 마지막 자리에 신청하면 1명만 성공하고 나머지는 대기열에 등록된다")
    void concurrentEnrollment() throws InterruptedException {
        // given
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger waitlistCount = new AtomicInteger(0);

        // 수강생 5명 생성
        List<User> users = userRepository.saveAll(List.of(
                User.create("수강생1", "user1@test.com", UserRole.CLASSMATE),
                User.create("수강생2", "user2@test.com", UserRole.CLASSMATE),
                User.create("수강생3", "user3@test.com", UserRole.CLASSMATE),
                User.create("수강생4", "user4@test.com", UserRole.CLASSMATE),
                User.create("수강생5", "user5@test.com", UserRole.CLASSMATE)
        ));

        // when - 5명 동시 신청
        for (User user : users) {
            executorService.submit(() -> {
                try {
                    EnrollmentCreateRequest request = new EnrollmentCreateRequest(course.getId());
                    enrollmentService.enroll(user.getId(), request);
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

        // then
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        long pendingCount = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.PENDING)
                .count();
        long waitlistedCount = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.WAITLISTED)
                .count();

        // 정원 1명이므로 PENDING은 1명, 나머지는 WAITLISTED
        assertThat(pendingCount).isEqualTo(1);
        assertThat(waitlistedCount).isEqualTo(4);
    }

    @Test
    @DisplayName("정원이 초과된 강의에 신청하면 대기열에 등록된다")
    void enrollWhenCourseFull() {
        // given
        User user1 = userRepository.save(
                User.create("수강생1", "user1@test.com", UserRole.CLASSMATE));
        User user2 = userRepository.save(
                User.create("수강생2", "user2@test.com", UserRole.CLASSMATE));

        // user1이 먼저 신청 (정원 채움)
        enrollmentService.enroll(user1.getId(), new EnrollmentCreateRequest(course.getId()));

        // when - user2가 신청하면 대기열 등록
        enrollmentService.enroll(user2.getId(), new EnrollmentCreateRequest(course.getId()));

        // then
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        assertThat(enrollments).hasSize(2);
        assertThat(enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.PENDING).count()).isEqualTo(1);
        assertThat(enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.WAITLISTED).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("수강 취소 시 대기열 첫 번째 사람이 자동 승격된다")
    void cancelAndPromoteWaitlist() {
        // given
        User user1 = userRepository.save(
                User.create("수강생1", "user1@test.com", UserRole.CLASSMATE));
        User user2 = userRepository.save(
                User.create("수강생2", "user2@test.com", UserRole.CLASSMATE));

        // user1 신청 후 결제 확정
        var enrollment1 = enrollmentService.enroll(user1.getId(),
                new EnrollmentCreateRequest(course.getId()));
        enrollmentService.confirm(user1.getId(), enrollment1.id());

        // user2 대기열 등록
        enrollmentService.enroll(user2.getId(), new EnrollmentCreateRequest(course.getId()));

        // when - user1 취소
        enrollmentService.cancel(user1.getId(), enrollment1.id());

        // then - user2가 PENDING으로 승격
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        assertThat(enrollments.stream()
                .filter(e -> e.getUser().getId().equals(user2.getId()))
                .findFirst().get().getStatus()).isEqualTo(EnrollmentStatus.PENDING);
    }

    @Test
    @DisplayName("이미 신청한 강의에 중복 신청하면 예외가 발생한다")
    void duplicateEnrollment() {
        // given
        User user = userRepository.save(
                User.create("수강생", "user@test.com", UserRole.CLASSMATE));
        enrollmentService.enroll(user.getId(), new EnrollmentCreateRequest(course.getId()));

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(
                com.liveclass.courseenrollment.global.exception.BusinessException.class,
                () -> enrollmentService.enroll(user.getId(), new EnrollmentCreateRequest(course.getId()))
        );
    }
}