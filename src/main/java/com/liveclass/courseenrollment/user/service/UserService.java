package com.liveclass.courseenrollment.user.service;

import com.liveclass.courseenrollment.global.exception.BusinessException;
import com.liveclass.courseenrollment.global.exception.ErrorCode;
import com.liveclass.courseenrollment.user.dto.UserCreateRequest;
import com.liveclass.courseenrollment.user.dto.UserResponse;
import com.liveclass.courseenrollment.user.entity.User;
import com.liveclass.courseenrollment.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 서비스
 * 사용자 등록 및 조회 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // 사용자 등록
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.create(request.name(), request.email(), request.role());
        return UserResponse.from(userRepository.save(user));
    }

    // 사용자 조회
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }
}