package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Bike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


@Repository
public interface BikeRepository extends JpaRepository<Bike, Long> {

    List<Bike> findByOwnerId(Long ownerId);

    List<Bike> findByStatusAndBikeType(Bike.BikeStatus status, Bike.BikeType bikeType);

    List<Bike> findByStatus(Bike.BikeStatus status);

    List<Bike> findByLocationContainingIgnoreCase(String location);

    List<Bike> findByBikeTypeAndLocationContainingIgnoreCase(
            Bike.BikeType bikeType, String location);

    List<Bike> findByDailyPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    @Query("SELECT b FROM Bike b WHERE " +
            "(:location IS NULL OR LOWER(b.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:bikeType IS NULL OR b.bikeType = :bikeType) AND " +
            "(:status IS NULL OR b.status = :status) AND " +
            "(:minPrice IS NULL OR b.dailyPrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR b.dailyPrice <= :maxPrice)")
    List<Bike> searchBikes(
            @Param("location") String location,
            @Param("bikeType") Bike.BikeType bikeType,
            @Param("status") Bike.BikeStatus status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);

    long countByOwnerId(Long ownerId);

    long countByOwnerIdAndStatus(Long ownerId, Bike.BikeStatus status);
}
