package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.controller;

import jakarta.validation.Valid;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.PaymentRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.ApiResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.PaymentResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for payments.
 * Base URL: /api/payments
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ---- INITIATE PAYMENT ----

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @Valid @RequestBody PaymentRequest request) {
        try {
            PaymentResponse payment = paymentService.initiatePayment(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Payment processed successfully.", payment));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ---- READ ALL (Admin) ----

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments() {
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved.", paymentService.getAllPayments()));
    }

    // ---- READ ONE ----

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Payment found.", paymentService.getPaymentById(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ---- PAYMENT HISTORY BY USER ----

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success("User payment history retrieved.", paymentService.getPaymentsByUser(userId)));
    }

    // ---- PAYMENTS BY RENTAL ----

    @GetMapping("/rental/{rentalId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByRental(@PathVariable Long rentalId) {
        return ResponseEntity.ok(
                ApiResponse.success("Rental payments retrieved.", paymentService.getPaymentsByRental(rentalId)));
    }

    // ---- PAYMENTS BY STATUS ----

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByStatus(@PathVariable String status) {
        try {
            PaymentStatus ps = PaymentStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(
                    ApiResponse.success("Payments by status retrieved.", paymentService.getPaymentsByStatus(ps)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid payment status: " + status));
        }
    }

    // ---- UPDATE STATUS (Admin manual override) ----

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            PaymentStatus ps = PaymentStatus.valueOf(status.toUpperCase());
            PaymentResponse updated = paymentService.updatePaymentStatus(id, ps);
            return ResponseEntity.ok(ApiResponse.success("Payment status updated.", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid status: " + status));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ---- REFUND ----

    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(@PathVariable Long id) {
        try {
            PaymentResponse refunded = paymentService.refundPayment(id);
            return ResponseEntity.ok(ApiResponse.success("Payment refunded successfully.", refunded));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ---- DELETE ----

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(@PathVariable Long id) {
        try {
            paymentService.deletePayment(id);
            return ResponseEntity.ok(ApiResponse.success("Payment record deleted."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }
}
