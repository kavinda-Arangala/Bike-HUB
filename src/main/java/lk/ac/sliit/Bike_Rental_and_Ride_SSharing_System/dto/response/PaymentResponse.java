package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {

    private Long id;

    // Rental info
    private Long rentalId;
    private String bikeTitle;

    // User info
    private Long userId;
    private String username;

    // Payment details
    private BigDecimal amount;
    private String status;
    private String paymentMethod;
    private String transactionId;
    private String paymentNote;

    // Refund details
    private BigDecimal refundAmount;
    private String refundReason;
    private LocalDateTime refundedAt;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}