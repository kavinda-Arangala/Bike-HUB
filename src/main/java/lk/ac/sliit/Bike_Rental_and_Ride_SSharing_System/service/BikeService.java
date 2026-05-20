package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.BikeDTO;

import java.math.BigDecimal;
import java.util.List;

public interface BikeService {

    // ── CRUD ──────────────────────────────────────────────────────────────────
    BikeDTO addBike(BikeDTO bikeDTO);
    List<BikeDTO> getAllBikes();
    BikeDTO getBikeById(Long id);
    BikeDTO updateBike(Long id, BikeDTO bikeDTO);
    void deleteBike(Long id);

    // ── Search & Filter ───────────────────────────────────────────────────────
    List<BikeDTO> getAvailableBikes();
    List<BikeDTO> searchBikes(String location, String bikeType,
                              String status, BigDecimal minPrice, BigDecimal maxPrice);

    // ── Owner ─────────────────────────────────────────────────────────────────
    List<BikeDTO> getBikesByOwner(Long ownerId);
    long countBikesByOwner(Long ownerId);
    long countRentedBikesByOwner(Long ownerId);

    // ── Status & Rating ───────────────────────────────────────────────────────
    void updateBikeStatus(Long bikeId, String newStatus);
    void updateBikeRating(Long bikeId, Double newRating);
}