package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentMethod;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The rental this payment covers */
    @Column(name = "rental_id", nullable = false)
    private Long rentalId;

    /** The user making the payment */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Total amount charged */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** Payment channel chosen by the rider */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    /** Current lifecycle status of the payment */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    /**
     * Reference returned by the (simulated) gateway.
     * For CARD   → generated UUID
     * For BKASH  → bKash transaction ID format
     * For STRIPE → Stripe charge ID format
     */
    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    // ---- CARD-specific fields ----
    /** Last 4 digits of the card — never store full card numbers */
    @Column(name = "card_last_four", length = 4)
    private String cardLastFour;

    @Column(name = "card_holder_name")
    private String cardHolderName;

    // ---- Mobile Banking fields ----
    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    /** Timestamp when the payment was confirmed by the gateway */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /** Human-readable failure reason if status = FAILED */
    @Column(name = "failure_reason")
    private String failureReason;

    /** If this payment was refunded, link to the refund transaction id */
    @Column(name = "refund_transaction_id")
    private String refundTransactionId;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ---------------------------------------------------------------
    // Lifecycle hooks
    // ---------------------------------------------------------------
    @PrePersist
    protected void onCreate() {
        if (paymentStatus == null) paymentStatus = PaymentStatus.PENDING;
    }
}
