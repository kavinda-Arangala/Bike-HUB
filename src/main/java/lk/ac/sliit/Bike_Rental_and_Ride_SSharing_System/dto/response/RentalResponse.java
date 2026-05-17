package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response;

import lombok.*;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.RentalPlan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalResponse {

    private Long id;
    private Long userId;
    private Long bikeId;
    private Long ownerId;
    private String bikeTitle;

    private RentalPlan rentalPlan;
    private Integer duration;
    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal unitPrice;    // Rate used (daily/weekly/monthly)
    private BigDecimal totalAmount;  // Computed total
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Human-friendly label: e.g. "3 Days", "2 Weeks", "1 Month" */
    public String getDurationLabel() {
        if (rentalPlan == null || duration == null) return "";
        return switch (rentalPlan) {
            case DAILY   -> duration + (duration == 1 ? " Day"   : " Days");
            case WEEKLY  -> duration + (duration == 1 ? " Week"  : " Weeks");
            case MONTHLY -> duration + (duration == 1 ? " Month" : " Months");
        };
    }
}
