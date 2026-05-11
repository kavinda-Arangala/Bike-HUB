package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.controller;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.BikeDTO;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.BikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bikes")
@CrossOrigin(origins = "*")
public class BikeController {

    @Autowired
    private BikeService bikeService;

    @PostMapping
    public ResponseEntity<?> addBike(@RequestBody BikeDTO bikeDTO) {
        try {
            BikeDTO saved = bikeService.addBike(bikeDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to add bike: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<BikeDTO>> getAllBikes() {
        return ResponseEntity.ok(bikeService.getAllBikes());
    }

    @GetMapping("/available")
    public ResponseEntity<List<BikeDTO>> getAvailableBikes() {
        return ResponseEntity.ok(bikeService.getAvailableBikes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBikeById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bikeService.getBikeById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBike(@PathVariable Long id,
                                        @RequestBody BikeDTO bikeDTO) {
        try {
            return ResponseEntity.ok(bikeService.updateBike(id, bikeDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBike(@PathVariable Long id) {
        try {
            bikeService.deleteBike(id);
            return ResponseEntity.ok(Map.of("message", "Bike deleted successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<BikeDTO>> getBikesByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(bikeService.getBikesByOwner(ownerId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<BikeDTO>> searchBikes(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String bikeType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        return ResponseEntity.ok(
                bikeService.searchBikes(location, bikeType, status, minPrice, maxPrice));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateBikeStatus(@PathVariable Long id,
                                              @RequestBody Map<String, String> body) {
        try {
            bikeService.updateBikeStatus(id, body.get("status"));
            return ResponseEntity.ok(Map.of("message", "Status updated."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/rating")
    public ResponseEntity<?> updateBikeRating(@PathVariable Long id,
                                              @RequestBody Map<String, Double> body) {
        try {
            bikeService.updateBikeRating(id, body.get("rating"));
            return ResponseEntity.ok(Map.of("message", "Rating updated."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/owner/{ownerId}/stats")
    public ResponseEntity<Map<String, Long>> getOwnerStats(@PathVariable Long ownerId) {
        return ResponseEntity.ok(Map.of(
                "totalBikes",  bikeService.countBikesByOwner(ownerId),
                "rentedBikes", bikeService.countRentedBikesByOwner(ownerId)
        ));
    }
}
