package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DisbursementRequest {

    @NotNull(message = "Payment ID is required")
    private Long paymentId;

    @NotNull(message = "Admin ID is required")
    private Long adminId;

    /** Optional admin note (e.g. "Manual bank transfer via NRFC account") */
    private String notes;
}
