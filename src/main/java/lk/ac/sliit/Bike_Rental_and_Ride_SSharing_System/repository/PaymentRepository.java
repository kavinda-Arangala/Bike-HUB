package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Payment;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentMethod;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /** Payment history for a specific user (most recent first) */
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Payment(s) associated with a rental */
    List<Payment> findByRentalIdOrderByCreatedAtDesc(Long rentalId);

    /** The latest successful payment for a rental */
    Optional<Payment> findTopByRentalIdAndPaymentStatusOrderByCreatedAtDesc(
            Long rentalId, PaymentStatus status);

    /** Payments by status (admin monitoring) */
    List<Payment> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus status);

    /** Payments by payment method */
    List<Payment> findByPaymentMethodOrderByCreatedAtDesc(PaymentMethod method);

    /** Lookup by gateway transaction ID */
    Optional<Payment> findByTransactionId(String transactionId);

    /** Total revenue collected for a specific period */
    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM Payment p
            WHERE p.paymentStatus = 'SUCCESS'
              AND p.paidAt BETWEEN :from AND :to
            """)
    BigDecimal totalRevenueInPeriod(@Param("from") java.time.LocalDateTime from,
                                    @Param("to") java.time.LocalDateTime to);

    /** Count payments by status for dashboard stats */
    long countByPaymentStatus(PaymentStatus status);

    /** Check if a rental already has a successful payment */
    boolean existsByRentalIdAndPaymentStatus(Long rentalId, PaymentStatus status);
}
