package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RentalCancelRequest {

    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}