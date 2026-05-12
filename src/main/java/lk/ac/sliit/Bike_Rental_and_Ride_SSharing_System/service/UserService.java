package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.ChangePasswordRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.UpdateProfileRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    // Profile — for the logged-in user
    UserResponse getMyProfile(String email);
    UserResponse updateMyProfile(String email, UpdateProfileRequest request);
    void changePassword(String email, ChangePasswordRequest request);
    void deleteMyAccount(String email);

    // Admin operations
    List<UserResponse> getAllUsers();
    List<UserResponse> getAllRiders();
    UserResponse getUserById(Long id);
    UserResponse activateUser(Long id);
    UserResponse deactivateUser(Long id);
    void deleteUser(Long id);
}