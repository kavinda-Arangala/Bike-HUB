package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.RentalPlan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rentals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who is renting the bike */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** The bike being rented */
    @Column(name = "bike_id", nullable = false)
    private Long bikeId;

    /** The ID of the bike owner (denormalised for quick disbursement lookup) */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    /** Chosen pricing plan */
    @Enumerated(EnumType.STRING)
    @Column(name = "rental_plan", nullable = false)
    private RentalPlan rentalPlan;

    /** Number of days / weeks / months depending on the plan */
    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Total rental cost computed at booking time.
     * Stored so price changes on the bike do not affect existing rentals.
     */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Rental lifecycle status.
     * PENDING   → created, waiting for payment
     * ACTIVE    → payment confirmed
     * COMPLETED → return confirmed
     * CANCELLED → cancelled before or after payment
     */
    @Column(name = "status", nullable = false)
    private String status;  // PENDING | ACTIVE | COMPLETED | CANCELLED

    /** Snapshot of the bike title for display/receipts (avoids JOIN) */
    @Column(name = "bike_title")
    private String bikeTitle;

    /** Snapshot of the unit price used at booking time */
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

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
        if (status == null) status = "PENDING";
    }
}
