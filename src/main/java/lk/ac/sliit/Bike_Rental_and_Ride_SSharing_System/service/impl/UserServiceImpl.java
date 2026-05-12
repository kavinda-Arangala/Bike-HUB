package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.impl;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.ChangePasswordRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.UpdateProfileRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.UserResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.User;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.UserRole;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.exception.ResourceNotFoundException;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.UserRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Profile (logged-in user) ──────────────────────────────────────────────

    @Override
    public UserResponse getMyProfile(String email) {
        return toResponse(findByEmailOrThrow(email));
    }

    @Override
    @Transactional
    public UserResponse updateMyProfile(String email, UpdateProfileRequest request) {
        User user = findByEmailOrThrow(email);

        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());

        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }

        log.info("Profile updated for: {}", email);
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findByEmailOrThrow(email);

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("Current password is incorrect");
        }

        // Confirm new passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalStateException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for: {}", email);
    }

    @Override
    @Transactional
    public void deleteMyAccount(String email) {
        User user = findByEmailOrThrow(email);
        userRepository.delete(user);
        log.info("Account deleted: {}", email);
    }

    // ── Admin operations ──────────────────────────────────────────────────────

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getAllRiders() {
        return userRepository.findByRole(UserRole.RIDER)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(Long id) {
        return toResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional
    public UserResponse activateUser(Long id) {
        User user = findByIdOrThrow(id);
        user.setIsActive(true);
        log.info("User activated: id={}", id);
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse deactivateUser(Long id) {
        User user = findByIdOrThrow(id);
        user.setIsActive(false);
        log.info("User deactivated: id={}", id);
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = findByIdOrThrow(id);
        userRepository.delete(user);
        log.info("User deleted by admin: id={}", id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User findByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private User findByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .profileImage(user.getProfileImage())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}