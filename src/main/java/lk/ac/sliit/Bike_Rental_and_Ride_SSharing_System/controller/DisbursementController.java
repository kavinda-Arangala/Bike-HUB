package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.controller;

import jakarta.validation.Valid;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.DisbursementRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.ApiResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.DisbursementResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.DisbursementStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.DisbursementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST controller for owner/admin disbursements (payouts).
 * Base URL: /api/disbursements
 */
@RestController
@RequestMapping("/api/disbursements")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DisbursementController {

    private final DisbursementService disbursementService;

    // ---- CREATE DISBURSEMENT (Admin triggers payout) ----

    @PostMapping
    public ResponseEntity<ApiResponse<DisbursementResponse>> createDisbursement(
            @Valid @RequestBody DisbursementRequest request) {
        try {
            DisbursementResponse d = disbursementService.createDisbursement(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Disbursement created successfully.", d));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ---- READ ALL (Admin) ----

    @GetMapping
    public ResponseEntity<ApiResponse<List<DisbursementResponse>>> getAllDisbursements() {
        return ResponseEntity.ok(
                ApiResponse.success("Disbursements retrieved.", disbursementService.getAllDisbursements()));
    }

    // ---- READ ONE ----

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DisbursementResponse>> getDisbursementById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.success("Disbursement found.", disbursementService.getDisbursementById(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ---- READ BY OWNER ----

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<DisbursementResponse>>> getDisbursementsByOwner(
            @PathVariable Long ownerId) {
        return ResponseEntity.ok(ApiResponse.success("Owner disbursements retrieved.",
                disbursementService.getDisbursementsByOwner(ownerId)));
    }

    // ---- READ BY STATUS ----

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<DisbursementResponse>>> getDisbursementsByStatus(
            @PathVariable String status) {
        try {
            DisbursementStatus ds = DisbursementStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(ApiResponse.success("Disbursements by status.",
                    disbursementService.getDisbursementsByStatus(ds)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid status: " + status));
        }
    }

    // ---- UPDATE STATUS (mark as TRANSFERRED or FAILED) ----

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DisbursementResponse>> updateDisbursementStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            DisbursementStatus ds = DisbursementStatus.valueOf(
                    body.getOrDefault("status", "").toUpperCase());
            String transferRef = body.get("transferReference");
            DisbursementResponse updated = disbursementService.updateDisbursementStatus(id, ds, transferRef);
            return ResponseEntity.ok(ApiResponse.success("Disbursement status updated.", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid status value."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ---- OWNER TOTAL EARNINGS ----

    @GetMapping("/owner/{ownerId}/earnings")
    public ResponseEntity<ApiResponse<BigDecimal>> getOwnerEarnings(@PathVariable Long ownerId) {
        BigDecimal earnings = disbursementService.getTotalEarningsByOwner(ownerId);
        return ResponseEntity.ok(ApiResponse.success("Total owner earnings retrieved.", earnings));
    }

    // ---- PLATFORM FEES (Admin stat) ----

    @GetMapping("/platform-fees")
    public ResponseEntity<ApiResponse<BigDecimal>> getPlatformFees() {
        BigDecimal fees = disbursementService.getTotalPlatformFees();
        return ResponseEntity.ok(ApiResponse.success("Total platform fees collected.", fees));
    }

    // ---- DELETE ----

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDisbursement(@PathVariable Long id) {
        try {
            disbursementService.deleteDisbursement(id);
            return ResponseEntity.ok(ApiResponse.success("Disbursement deleted."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }
}
