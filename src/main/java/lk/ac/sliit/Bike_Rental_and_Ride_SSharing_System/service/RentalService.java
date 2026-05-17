package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RentalRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.RentalResponse;

import java.math.BigDecimal;
import java.util.List;

public interface RentalService {

    /** Create a new rental booking (status = PENDING, awaiting payment) */
    RentalResponse createRental(RentalRequest request);

    /** Get a single rental by ID */
    RentalResponse getRentalById(Long id);

    /** Get all rentals (admin) */
    List<RentalResponse> getAllRentals();

    /** Get rentals for a specific user */
    List<RentalResponse> getRentalsByUser(Long userId);

    /** Get rentals for a specific bike */
    List<RentalResponse> getRentalsByBike(Long bikeId);

    /** Get rentals for a specific owner */
    List<RentalResponse> getRentalsByOwner(Long ownerId);

    /** Update rental status (e.g. ACTIVE, COMPLETED, CANCELLED) */
    RentalResponse updateRentalStatus(Long id, String status);

    /** Full rental update (extend dates, change plan) */
    RentalResponse updateRental(Long id, RentalRequest request);

    /** Delete a rental record (admin only) */
    void deleteRental(Long id);

    /** Estimate cost before booking without saving to DB */
    BigDecimal estimatePrice(Long bikeId, String rentalPlan, Integer duration);
}
