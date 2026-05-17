package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response;

import lombok.*;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentMethod;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long rentalId;
    private Long userId;

    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    private String transactionId;

    // Masked card info (only last 4 digits shown)
    private String cardLastFour;
    private String cardHolderName;
    private String mobileNumber;

    private LocalDateTime paidAt;
    private String failureReason;
    private String refundTransactionId;
    private LocalDateTime refundedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Masked card display: e.g. "**** **** **** 4242" */
    public String getMaskedCard() {
        if (cardLastFour == null) return null;
        return "**** **** **** " + cardLastFour;
    }
}
