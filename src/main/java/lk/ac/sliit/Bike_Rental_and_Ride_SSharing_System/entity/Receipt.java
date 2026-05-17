package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "receipts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique human-readable receipt number e.g. RCP-20260516-000042 */
    @Column(name = "receipt_number", unique = true, nullable = false, length = 50)
    private String receiptNumber;

    /** The payment this receipt was issued for */
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    /** The rental this receipt covers */
    @Column(name = "rental_id", nullable = false)
    private Long rentalId;

    /** The user (rider) who made the payment */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Gross amount charged */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /** Tax amount (if applicable — 0 by default, extendable) */
    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;

    /** Net amount = totalAmount + taxAmount */
    @Column(name = "net_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal netAmount;

    /** Payment method used (string snapshot for display) */
    @Column(name = "payment_method")
    private String paymentMethod;

    /** Transaction ID from the gateway (for reference on the receipt) */
    @Column(name = "transaction_id")
    private String transactionId;

    /** Bike title snapshot */
    @Column(name = "bike_title")
    private String bikeTitle;

    /** Rental plan snapshot (DAILY / WEEKLY / MONTHLY) */
    @Column(name = "rental_plan")
    private String rentalPlan;

    /** Rental start date (string snapshot) */
    @Column(name = "rental_start_date")
    private String rentalStartDate;

    /** Rental end date (string snapshot) */
    @Column(name = "rental_end_date")
    private String rentalEndDate;

    /** Duration (days/weeks/months) */
    @Column(name = "duration")
    private Integer duration;

    /** Unit price used (daily/weekly/monthly rate) */
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Full JSON snapshot of payment + rental data at the time of receipt creation.
     * Useful for reconstructing receipts even if original records are modified.
     */
    @Column(name = "receipt_data", columnDefinition = "TEXT")
    private String receiptData;

    /** Timestamp the receipt was officially issued */
    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ---------------------------------------------------------------
    // Lifecycle hooks
    // ---------------------------------------------------------------
    @PrePersist
    protected void onCreate() {
        if (issuedAt == null) issuedAt = LocalDateTime.now();
        if (taxAmount == null) taxAmount = BigDecimal.ZERO;
    }
}
