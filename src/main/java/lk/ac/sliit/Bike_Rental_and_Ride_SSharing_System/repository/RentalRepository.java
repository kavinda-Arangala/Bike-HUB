package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    /** All rentals for a specific rider */
    List<Rental> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** All rentals for a specific bike */
    List<Rental> findByBikeIdOrderByCreatedAtDesc(Long bikeId);

    /** All rentals belonging to a bike owner */
    List<Rental> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    /** Active rentals for a specific user */
    List<Rental> findByUserIdAndStatus(Long userId, String status);

    /** Active rentals for a specific bike */
    List<Rental> findByBikeIdAndStatus(Long bikeId, String status);

    /** Check if a bike is already rented in a date range (overlap detection) */
    @Query("""
            SELECT r FROM Rental r
            WHERE r.bikeId = :bikeId
              AND r.status IN ('PENDING', 'ACTIVE')
              AND r.startDate <= :endDate
              AND r.endDate   >= :startDate
            """)
    List<Rental> findOverlappingRentals(@Param("bikeId") Long bikeId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    /** Latest active rental for a bike (for status sync) */
    Optional<Rental> findTopByBikeIdAndStatusOrderByStartDateDesc(Long bikeId, String status);

    /** Count rentals by status for admin dashboard */
    long countByStatus(String status);

    /** Revenue summary per owner */
    @Query("SELECT SUM(r.totalAmount) FROM Rental r WHERE r.ownerId = :ownerId AND r.status = 'COMPLETED'")
    java.math.BigDecimal sumCompletedRevenueByOwner(@Param("ownerId") Long ownerId);
}
