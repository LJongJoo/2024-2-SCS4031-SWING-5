package com.example.swingback.User.Service;


import com.example.swingback.User.dto.UserData;
import com.example.swingback.User.dto.UserExtraInfoDTO;
import com.example.swingback.User.entity.UserEntity;
import com.example.swingback.User.repository.UserRepository;
import com.example.swingback.oauth.dto.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserEntity findByProviderId(String providerId) {
        return userRepository.findByProviderId(providerId);
    }

    public UserData findByProvider() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("authentication : " + authentication);
        if (authentication != null && authentication.isAuthenticated()) {
            // 현재 인증된 사용자의 providerId를 가져옴
            CustomOAuth2User userDetails = (CustomOAuth2User) authentication.getPrincipal();
            String providerId = userDetails.getProviderId(); // CustomOAuth2User에서 providerId를 가져온다고 가정
            UserEntity user = userRepository.findByProviderId(providerId);
            log.info("user.getFamilyRole : " + user.getFamilyRole());
            return new UserData(user.getName(), user.getFamilyRole());

        }
        return null;
    }

    public Optional<UserEntity> getExtraInfo(UserExtraInfoDTO userExtraInfoDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("authentication : " + authentication);
        if (authentication != null && authentication.isAuthenticated()) {
            // 현재 인증된 사용자의 providerId를 가져옴
            CustomOAuth2User userDetails = (CustomOAuth2User) authentication.getPrincipal();
            String providerId = userDetails.getProviderId(); // CustomOAuth2User에서 providerId를 가져온다고 가정
            UserEntity existingUser = userRepository.findByProviderId(providerId);

            if (existingUser != null) {
                // 기존 값을 유지하면서 업데이트할 값만 변경하여 새로운 객체를 빌드
                UserEntity updatedUser = UserEntity.builder()
                        .userId(existingUser.getUserId())  // 기존 userId 유지
                        .name(existingUser.getName())      // 기존 name 유지
                        .email(existingUser.getEmail())    // 기존 email 유지
                        .provider(existingUser.getProvider())  // 기존 provider 유지
                        .providerId(existingUser.getProviderId())  // 기존 providerId 유지
                        .role(existingUser.getRole())  // 기존 role 값
                        .phoneNumber(userExtraInfoDTO.getPhoneNumber())  // 업데이트할 phoneNumber 값
                        .familyRole(userExtraInfoDTO.getFamilyRole())  // 업데이트할 familyRole 값
                        .gender(userExtraInfoDTO.getGender())  // 업데이트할 gender 값
                        .age(userExtraInfoDTO.getAge())  // 업데이트할 age 값
                        .familyId(existingUser.getFamilyId())  // 기존 familyId 유지 (필요 시 null)
                        .build();

                // 엔티티 저장 (기존 행이 업데이트됨)
                userRepository.save(updatedUser);
                return Optional.of(updatedUser);  // 업데이트된 사용자 반환
            }
        }
        return Optional.empty();  // 인증 실패 또는 사용자를 찾을 수 없을 때
    }

}
