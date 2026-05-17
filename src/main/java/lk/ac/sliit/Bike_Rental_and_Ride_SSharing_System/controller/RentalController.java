package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.controller;

import jakarta.validation.Valid;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RentalRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.ApiResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.RentalResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for rental bookings.
 * Base URL: /api/rentals
 */
@RestController
@RequestMapping("/api/rentals")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    // ---- CREATE ----

    @PostMapping
    public ResponseEntity<ApiResponse<RentalResponse>> createRental(
            @Valid @RequestBody RentalRequest request) {
        try {
            RentalResponse rental = rentalService.createRental(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Rental booked successfully. Proceed to payment.", rental));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create rental: " + e.getMessage()));
        }
    }

    // ---- READ ALL (Admin) ----

    @GetMapping
    public ResponseEntity<ApiResponse<List<RentalResponse>>> getAllRentals() {
        return ResponseEntity.ok(ApiResponse.success("Rentals retrieved.", rentalService.getAllRentals()));
    }

    // ---- READ ONE ----

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RentalResponse>> getRentalById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Rental found.", rentalService.getRentalById(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ---- READ BY USER ----

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<RentalResponse>>> getRentalsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success("User rentals retrieved.", rentalService.getRentalsByUser(userId)));
    }

    // ---- READ BY BIKE ----

    @GetMapping("/bike/{bikeId}")
    public ResponseEntity<ApiResponse<List<RentalResponse>>> getRentalsByBike(@PathVariable Long bikeId) {
        return ResponseEntity.ok(
                ApiResponse.success("Bike rentals retrieved.", rentalService.getRentalsByBike(bikeId)));
    }

    // ---- READ BY OWNER ----

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<RentalResponse>>> getRentalsByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(
                ApiResponse.success("Owner rentals retrieved.", rentalService.getRentalsByOwner(ownerId)));
    }

    // ---- PRICE ESTIMATE ----

    @GetMapping("/estimate")
    public ResponseEntity<ApiResponse<BigDecimal>> estimatePrice(
            @RequestParam Long bikeId,
            @RequestParam String rentalPlan,
            @RequestParam Integer duration) {
        try {
            BigDecimal price = rentalService.estimatePrice(bikeId, rentalPlan, duration);
            return ResponseEntity.ok(ApiResponse.success("Price estimated.", price));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ---- UPDATE STATUS ----

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RentalResponse>> updateRentalStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            RentalResponse updated = rentalService.updateRentalStatus(id, status.toUpperCase());
            return ResponseEntity.ok(ApiResponse.success("Rental status updated to " + status + ".", updated));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ---- UPDATE (full) ----

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RentalResponse>> updateRental(
            @PathVariable Long id,
            @Valid @RequestBody RentalRequest request) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.success("Rental updated.", rentalService.updateRental(id, request)));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ---- DELETE ----

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRental(@PathVariable Long id) {
        try {
            rentalService.deleteRental(id);
            return ResponseEntity.ok(ApiResponse.success("Rental deleted successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }
}
