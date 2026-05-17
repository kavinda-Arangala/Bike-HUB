package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.controller;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.ApiResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.ReceiptResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for receipts.
 * Base URL: /api/receipts
 */
@RestController
@RequestMapping("/api/receipts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    // ---- GET BY ID ----

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReceiptResponse>> getReceiptById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Receipt found.", receiptService.getReceiptById(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ---- GET BY RECEIPT NUMBER ----

    @GetMapping("/number/{receiptNumber}")
    public ResponseEntity<ApiResponse<ReceiptResponse>> getReceiptByNumber(
            @PathVariable String receiptNumber) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Receipt found.",
                    receiptService.getReceiptByNumber(receiptNumber)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ---- GET BY PAYMENT ----

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<ApiResponse<ReceiptResponse>> getReceiptByPayment(@PathVariable Long paymentId) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Receipt found.",
                    receiptService.getReceiptByPayment(paymentId)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ---- ALL RECEIPTS FOR USER (Payment History) ----

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ReceiptResponse>>> getReceiptsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success("User receipts retrieved.", receiptService.getReceiptsByUser(userId)));
    }

    // ---- ALL RECEIPTS FOR A RENTAL ----

    @GetMapping("/rental/{rentalId}")
    public ResponseEntity<ApiResponse<List<ReceiptResponse>>> getReceiptsByRental(@PathVariable Long rentalId) {
        return ResponseEntity.ok(
                ApiResponse.success("Rental receipts retrieved.", receiptService.getReceiptsByRental(rentalId)));
    }

    // ---- DOWNLOAD (returns full receipt JSON) ----

    @GetMapping("/{id}/download")
    public ResponseEntity<ApiResponse<String>> downloadReceipt(@PathVariable Long id) {
        try {
            ReceiptResponse receipt = receiptService.getReceiptById(id);
            // In production, this could stream a PDF; for now we return the JSON snapshot
            return ResponseEntity.ok(ApiResponse.success("Receipt data.", receipt.getReceiptData()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ---- DELETE (Admin) ----

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReceipt(@PathVariable Long id) {
        try {
            receiptService.deleteReceipt(id);
            return ResponseEntity.ok(ApiResponse.success("Receipt deleted."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }
}
