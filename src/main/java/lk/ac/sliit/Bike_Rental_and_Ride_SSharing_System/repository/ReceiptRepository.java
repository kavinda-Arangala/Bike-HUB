package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    /** Receipt by unique receipt number */
    Optional<Receipt> findByReceiptNumber(String receiptNumber);

    /** Receipt for a specific payment */
    Optional<Receipt> findByPaymentId(Long paymentId);

    /** All receipts for a user (payment history) */
    List<Receipt> findByUserIdOrderByIssuedAtDesc(Long userId);

    /** All receipts for a specific rental */
    List<Receipt> findByRentalId(Long rentalId);

    /** Check if a receipt already exists for a payment */
    boolean existsByPaymentId(Long paymentId);
}
