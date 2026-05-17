package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Disbursement;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.DisbursementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DisbursementRepository extends JpaRepository<Disbursement, Long> {

    /** All disbursements for a bike owner */
    List<Disbursement> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    /** All disbursements triggered by an admin */
    List<Disbursement> findByAdminIdOrderByCreatedAtDesc(Long adminId);

    /** Disbursements by status */
    List<Disbursement> findByDisbursementStatusOrderByCreatedAtDesc(DisbursementStatus status);

    /** Disbursement linked to a specific payment */
    Optional<Disbursement> findByPaymentId(Long paymentId);

    /** Check if payment already has a disbursement (prevent duplicates) */
    boolean existsByPaymentId(Long paymentId);

    /** Total amount disbursed to an owner (completed only) */
    @Query("""
            SELECT COALESCE(SUM(d.ownerAmount), 0)
            FROM Disbursement d
            WHERE d.ownerId = :ownerId
              AND d.disbursementStatus = 'TRANSFERRED'
            """)
    BigDecimal totalTransferredToOwner(@Param("ownerId") Long ownerId);

    /** Total platform fees collected */
    @Query("SELECT COALESCE(SUM(d.platformFee), 0) FROM Disbursement d WHERE d.disbursementStatus = 'TRANSFERRED'")
    BigDecimal totalPlatformFeesCollected();

    /** Pending disbursements count for admin dashboard */
    long countByDisbursementStatus(DisbursementStatus status);
}
