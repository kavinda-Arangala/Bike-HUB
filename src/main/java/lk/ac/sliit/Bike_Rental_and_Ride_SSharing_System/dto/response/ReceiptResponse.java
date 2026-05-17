package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptResponse {

    private Long id;
    private String receiptNumber;

    private Long paymentId;
    private Long rentalId;
    private Long userId;

    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;

    private String paymentMethod;
    private String transactionId;

    private String bikeTitle;
    private String rentalPlan;
    private String rentalStartDate;
    private String rentalEndDate;
    private Integer duration;
    private BigDecimal unitPrice;

    private LocalDateTime issuedAt;
    private LocalDateTime createdAt;

    /** Full JSON data snapshot (for download/print use cases) */
    private String receiptData;
}
