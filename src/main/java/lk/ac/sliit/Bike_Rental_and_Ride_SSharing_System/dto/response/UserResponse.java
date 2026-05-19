package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String profileImage;
    private UserRole role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
