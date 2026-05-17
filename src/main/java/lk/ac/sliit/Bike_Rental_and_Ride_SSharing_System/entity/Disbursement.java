package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.DisbursementStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "disbursements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Disbursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The payment this disbursement originates from */
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    /** The bike rental this is for (for convenience) */
    @Column(name = "rental_id", nullable = false)
    private Long rentalId;

    /** The bike owner receiving the payout */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    /** The admin who triggered / approved the disbursement */
    @Column(name = "admin_id")
    private Long adminId;

    /** Full payment amount before the platform fee */
    @Column(name = "gross_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal grossAmount;

    /** Platform commission deducted (10% by default) */
    @Column(name = "platform_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal platformFee;

    /** Net amount actually transferred to the owner */
    @Column(name = "owner_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal ownerAmount;

    /** Platform fee percentage used (stored for audit trail) */
    @Column(name = "fee_percentage", precision = 5, scale = 2)
    private BigDecimal feePercentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "disbursement_status", nullable = false)
    private DisbursementStatus disbursementStatus;

    /** Reference ID returned by the payout gateway / bank */
    @Column(name = "transfer_reference")
    private String transferReference;

    /** Optional admin note (e.g. "Manual bank transfer") */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** Timestamp when the transfer was confirmed */
    @Column(name = "disbursed_at")
    private LocalDateTime disbursedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ---------------------------------------------------------------
    // Lifecycle hooks
    // ---------------------------------------------------------------
    @PrePersist
    protected void onCreate() {
        if (disbursementStatus == null) disbursementStatus = DisbursementStatus.PENDING;
        if (feePercentage == null) feePercentage = new BigDecimal("10.00");
    }
}
