package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.impl;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RentalRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.RentalResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Bike;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Rental;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.RentalPlan;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.BikeRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.RentalRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.RentalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;
    private final BikeRepository   bikeRepository;

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public RentalResponse createRental(RentalRequest request) {
        // 1. Load and validate the bike
        Bike bike = bikeRepository.findById(request.getBikeId())
                .orElseThrow(() -> new RuntimeException("Bike not found with id: " + request.getBikeId()));

        if (bike.getStatus() == Bike.BikeStatus.RENTED) {
            throw new IllegalStateException("Bike is currently rented and not available.");
        }
        if (bike.getStatus() == Bike.BikeStatus.MAINTENANCE) {
            throw new IllegalStateException("Bike is under maintenance and cannot be rented.");
        }

        // 2. Calculate dates
        LocalDate startDate = request.getStartDate();
        LocalDate endDate   = computeEndDate(startDate, request.getRentalPlan(), request.getDuration());

        // 3. Check for overlapping rentals
        List<Rental> overlaps = rentalRepository.findOverlappingRentals(
                request.getBikeId(), startDate, endDate);
        if (!overlaps.isEmpty()) {
            throw new IllegalStateException(
                    "Bike is already booked between " + startDate + " and " + endDate);
        }

        // 4. Compute price
        BigDecimal unitPrice   = resolveUnitPrice(bike, request.getRentalPlan());
        BigDecimal totalAmount = computeTotal(unitPrice, request.getRentalPlan(), request.getDuration());

        // 5. Build and save the rental
        Rental rental = Rental.builder()
                .userId(request.getUserId())
                .bikeId(request.getBikeId())
                .ownerId(bike.getOwnerId())
                .rentalPlan(request.getRentalPlan())
                .duration(request.getDuration())
                .startDate(startDate)
                .endDate(endDate)
                .unitPrice(unitPrice)
                .totalAmount(totalAmount)
                .bikeTitle(bike.getTitle())
                .status("PENDING")
                .build();

        Rental saved = rentalRepository.save(rental);
        log.info("Rental created: id={}, bike={}, user={}, amount={}",
                saved.getId(), saved.getBikeId(), saved.getUserId(), saved.getTotalAmount());

        return toResponse(saved);
    }

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    @Override
    public RentalResponse getRentalById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<RentalResponse> getAllRentals() {
        return rentalRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<RentalResponse> getRentalsByUser(Long userId) {
        return rentalRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<RentalResponse> getRentalsByBike(Long bikeId) {
        return rentalRepository.findByBikeIdOrderByCreatedAtDesc(bikeId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<RentalResponse> getRentalsByOwner(Long ownerId) {
        return rentalRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public RentalResponse updateRentalStatus(Long id, String status) {
        Rental rental = findOrThrow(id);
        validateStatusTransition(rental.getStatus(), status);
        rental.setStatus(status);
        return toResponse(rentalRepository.save(rental));
    }

    @Override
    @Transactional
    public RentalResponse updateRental(Long id, RentalRequest request) {
        Rental rental = findOrThrow(id);

        if (!"PENDING".equals(rental.getStatus())) {
            throw new IllegalStateException("Only PENDING rentals can be modified.");
        }

        Bike bike = bikeRepository.findById(request.getBikeId())
                .orElseThrow(() -> new RuntimeException("Bike not found: " + request.getBikeId()));

        LocalDate startDate = request.getStartDate();
        LocalDate endDate   = computeEndDate(startDate, request.getRentalPlan(), request.getDuration());

        BigDecimal unitPrice   = resolveUnitPrice(bike, request.getRentalPlan());
        BigDecimal totalAmount = computeTotal(unitPrice, request.getRentalPlan(), request.getDuration());

        rental.setRentalPlan(request.getRentalPlan());
        rental.setDuration(request.getDuration());
        rental.setStartDate(startDate);
        rental.setEndDate(endDate);
        rental.setUnitPrice(unitPrice);
        rental.setTotalAmount(totalAmount);

        return toResponse(rentalRepository.save(rental));
    }

    // ----------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public void deleteRental(Long id) {
        if (!rentalRepository.existsById(id)) {
            throw new RuntimeException("Rental not found with id: " + id);
        }
        rentalRepository.deleteById(id);
        log.info("Rental deleted: id={}", id);
    }

    // ----------------------------------------------------------------
    // PRICE ESTIMATE (no DB write)
    // ----------------------------------------------------------------

    @Override
    public BigDecimal estimatePrice(Long bikeId, String rentalPlan, Integer duration) {
        Bike bike = bikeRepository.findById(bikeId)
                .orElseThrow(() -> new RuntimeException("Bike not found: " + bikeId));
        RentalPlan plan = RentalPlan.valueOf(rentalPlan.toUpperCase());
        BigDecimal unit = resolveUnitPrice(bike, plan);
        return computeTotal(unit, plan, duration);
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private Rental findOrThrow(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rental not found with id: " + id));
    }

    /**
     * Resolve the per-unit price for the chosen plan.
     * Falls back to derived prices if the bike owner hasn't set them.
     */
    private BigDecimal resolveUnitPrice(Bike bike, RentalPlan plan) {
        return switch (plan) {
            case DAILY   -> bike.getDailyPrice();
            case WEEKLY  -> bike.getWeeklyPrice() != null
                    ? bike.getWeeklyPrice()
                    : bike.getDailyPrice().multiply(BigDecimal.valueOf(7));
            case MONTHLY -> bike.getMonthlyPrice() != null
                    ? bike.getMonthlyPrice()
                    : bike.getDailyPrice().multiply(BigDecimal.valueOf(30));
        };
    }

    /**
     * Compute total = unitPrice × duration.
     */
    private BigDecimal computeTotal(BigDecimal unitPrice, RentalPlan plan, int duration) {
        return unitPrice.multiply(BigDecimal.valueOf(duration));
    }

    /**
     * Compute end date based on plan and duration.
     */
    private LocalDate computeEndDate(LocalDate start, RentalPlan plan, int duration) {
        return switch (plan) {
            case DAILY   -> start.plusDays(duration);
            case WEEKLY  -> start.plusWeeks(duration);
            case MONTHLY -> start.plusMonths(duration);
        };
    }

    /**
     * Validate allowed status transitions.
     */
    private void validateStatusTransition(String current, String next) {
        boolean valid = switch (current) {
            case "PENDING"   -> List.of("ACTIVE", "CANCELLED").contains(next);
            case "ACTIVE"    -> List.of("COMPLETED", "CANCELLED").contains(next);
            case "COMPLETED" -> false;
            case "CANCELLED" -> false;
            default          -> false;
        };
        if (!valid) {
            throw new IllegalStateException(
                    "Cannot transition rental from " + current + " to " + next);
        }
    }

    // ----------------------------------------------------------------
    // Mapper
    // ----------------------------------------------------------------

    private RentalResponse toResponse(Rental r) {
        return RentalResponse.builder()
                .id(r.getId())
                .userId(r.getUserId())
                .bikeId(r.getBikeId())
                .ownerId(r.getOwnerId())
                .bikeTitle(r.getBikeTitle())
                .rentalPlan(r.getRentalPlan())
                .duration(r.getDuration())
                .startDate(r.getStartDate())
                .endDate(r.getEndDate())
                .unitPrice(r.getUnitPrice())
                .totalAmount(r.getTotalAmount())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
