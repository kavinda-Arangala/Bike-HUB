package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.RentalPlan;

import java.time.LocalDate;

@Data
public class RentalRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Bike ID is required")
    private Long bikeId;

    @NotNull(message = "Rental plan is required")
    private RentalPlan rentalPlan;   // DAILY | WEEKLY | MONTHLY

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1")
    private Integer duration;        // Number of days / weeks / months

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDate startDate;
}
