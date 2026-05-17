package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response;

import lombok.*;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.DisbursementStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementResponse {

    private Long id;
    private Long paymentId;
    private Long rentalId;
    private Long ownerId;
    private Long adminId;

    private BigDecimal grossAmount;
    private BigDecimal platformFee;
    private BigDecimal ownerAmount;
    private BigDecimal feePercentage;

    private DisbursementStatus disbursementStatus;
    private String transferReference;
    private String notes;

    private LocalDateTime disbursedAt;
    private LocalDateTime createdAt;
}
