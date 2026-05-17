package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.impl;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.PaymentRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.PaymentResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Payment;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Rental;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentMethod;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.PaymentRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.RentalRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.PaymentService;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.ReceiptService;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.util.PaymentGatewaySimulator;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.util.PaymentGatewaySimulator.PaymentGatewayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository      paymentRepository;
    private final RentalRepository       rentalRepository;
    private final ReceiptService         receiptService;
    private final PaymentGatewaySimulator gateway;

    // ----------------------------------------------------------------
    // INITIATE PAYMENT
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest req) {
        // 1. Load the rental
        Rental rental = rentalRepository.findById(req.getRentalId())
                .orElseThrow(() -> new RuntimeException("Rental not found: " + req.getRentalId()));

        if (!"PENDING".equals(rental.getStatus())) {
            throw new IllegalStateException(
                    "Payment can only be made for PENDING rentals. Current status: " + rental.getStatus());
        }

        // 2. Guard against duplicate payments
        if (paymentRepository.existsByRentalIdAndPaymentStatus(req.getRentalId(), PaymentStatus.SUCCESS)) {
            throw new IllegalStateException("This rental has already been paid successfully.");
        }

        // 3. Build the payment record (PENDING initially)
        Payment payment = Payment.builder()
                .rentalId(req.getRentalId())
                .userId(req.getUserId())
                .amount(rental.getTotalAmount())
                .paymentMethod(req.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        // 4. Set method-specific fields
        if (req.getPaymentMethod() == PaymentMethod.CARD) {
            payment.setCardHolderName(req.getCardHolderName());
            // Store only last 4 digits
            if (req.getCardNumber() != null && req.getCardNumber().length() >= 4) {
                payment.setCardLastFour(
                        req.getCardNumber().replaceAll("\\s|-", "")
                           .substring(req.getCardNumber().replaceAll("\\s|-", "").length() - 4));
            }
        } else if (req.getPaymentMethod() == PaymentMethod.BKASH) {
            payment.setMobileNumber(req.getMobileNumber());
        }

        // 5. Save as PENDING first (so we have an ID even if gateway fails)
        payment = paymentRepository.save(payment);

        // 6. Call (simulated) gateway
        try {
            String transactionId = chargeGateway(req, rental.getTotalAmount());
            payment.setTransactionId(transactionId);
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            payment = paymentRepository.save(payment);

            // 7. Activate the rental
            rental.setStatus("ACTIVE");
            rentalRepository.save(rental);

            // 8. Auto-generate receipt
            try {
                receiptService.generateReceipt(payment.getId());
            } catch (Exception e) {
                // Receipt generation failure must NOT roll back the payment
                log.error("Receipt generation failed for paymentId={}: {}", payment.getId(), e.getMessage());
            }

            log.info("Payment SUCCESS: id={}, rental={}, method={}, txId={}",
                    payment.getId(), rental.getId(), req.getPaymentMethod(), transactionId);

        } catch (PaymentGatewayException e) {
            // Mark the payment as FAILED but do NOT roll back the DB record
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            payment = paymentRepository.save(payment);

            log.warn("Payment FAILED: rental={}, method={}, reason={}",
                    req.getRentalId(), req.getPaymentMethod(), e.getMessage());

            throw new RuntimeException("Payment failed: " + e.getMessage());
        }

        return toResponse(payment);
    }

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    @Override
    public PaymentResponse getPaymentById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsByUser(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsByRental(Long rentalId) {
        return paymentRepository.findByRentalIdOrderByCreatedAtDesc(rentalId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByPaymentStatusOrderByCreatedAtDesc(status)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // UPDATE STATUS (admin manual override)
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(Long id, PaymentStatus status) {
        Payment payment = findOrThrow(id);
        payment.setPaymentStatus(status);
        if (status == PaymentStatus.SUCCESS && payment.getPaidAt() == null) {
            payment.setPaidAt(LocalDateTime.now());
        }
        return toResponse(paymentRepository.save(payment));
    }

    // ----------------------------------------------------------------
    // REFUND
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = findOrThrow(paymentId);

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Only successful payments can be refunded.");
        }
        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Payment has already been refunded.");
        }

        // Process refund via gateway
        String refundRef = gateway.processRefund(payment.getTransactionId(), payment.getPaymentMethod());

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        payment.setRefundTransactionId(refundRef);
        payment.setRefundedAt(LocalDateTime.now());

        // Cancel the associated rental
        rentalRepository.findById(payment.getRentalId()).ifPresent(rental -> {
            rental.setStatus("CANCELLED");
            rentalRepository.save(rental);
        });

        log.info("Payment REFUNDED: id={}, refundRef={}", paymentId, refundRef);
        return toResponse(paymentRepository.save(payment));
    }

    // ----------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new RuntimeException("Payment not found with id: " + id);
        }
        paymentRepository.deleteById(id);
        log.info("Payment deleted: id={}", id);
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private Payment findOrThrow(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    /**
     * Route to the correct gateway method based on the payment method.
     */
    private String chargeGateway(PaymentRequest req, java.math.BigDecimal amount) {
        return switch (req.getPaymentMethod()) {
            case CARD   -> gateway.processCardPayment(
                    req.getCardNumber(),
                    req.getCardHolderName(),
                    req.getCardExpiry(),
                    req.getCvv(),
                    amount.movePointRight(2).longValue());
            case BKASH  -> gateway.processBkashPayment(
                    req.getMobileNumber(),
                    req.getMobileToken(),
                    amount);
            case STRIPE -> gateway.processStripePayment(
                    req.getStripeToken(),
                    amount);
        };
    }

    // ----------------------------------------------------------------
    // Mapper
    // ----------------------------------------------------------------

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .rentalId(p.getRentalId())
                .userId(p.getUserId())
                .amount(p.getAmount())
                .paymentMethod(p.getPaymentMethod())
                .paymentStatus(p.getPaymentStatus())
                .transactionId(p.getTransactionId())
                .cardLastFour(p.getCardLastFour())
                .cardHolderName(p.getCardHolderName())
                .mobileNumber(p.getMobileNumber())
                .paidAt(p.getPaidAt())
                .failureReason(p.getFailureReason())
                .refundTransactionId(p.getRefundTransactionId())
                .refundedAt(p.getRefundedAt())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
