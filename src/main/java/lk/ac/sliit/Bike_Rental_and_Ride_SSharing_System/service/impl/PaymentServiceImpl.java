package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.impl;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.PaymentRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RefundRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.PaymentResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Payment;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Rental;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.User;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentMethod;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.RentalStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.exception.ResourceNotFoundException;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.PaymentRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.RentalRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.UserRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.PaymentService;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RentalRepository  rentalRepository;
    private final UserRepository    userRepository;
    private final SecurityUtil      securityUtil;

    // ── Make Payment ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PaymentResponse makePayment(String username, PaymentRequest request) {
        User user = findUserOrThrow(username);
        Rental rental = findRentalOrThrow(request.getRentalId());

        // Only the rental owner can pay
        if (!rental.getUser().getUsername().equals(username)) {
            throw new IllegalStateException(
                    "You are not authorized to pay for this rental");
        }

        // Rental must be COMPLETED to make payment
        if (rental.getStatus() != RentalStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Payment can only be made for COMPLETED rentals. " +
                            "Current status: " + rental.getStatus());
        }

        // Check if already paid
        boolean alreadyPaid = paymentRepository
                .findByRentalIdAndStatus(rental.getId(), PaymentStatus.SUCCESS)
                .isPresent();
        if (alreadyPaid) {
            throw new IllegalStateException(
                    "Payment already made for this rental");
        }

        // Validate payment method
        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid payment method. Must be: CASH, CARD, BANK_TRANSFER, or WALLET");
        }

        // Amount = finalFare from rental
        BigDecimal amount = rental.getFinalFare() != null
                ? rental.getFinalFare()
                : rental.getEstimatedFare();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Invalid payment amount for this rental");
        }

        // Generate unique transaction ID
        String transactionId = "TXN-" + UUID.randomUUID().toString().toUpperCase();

        Payment payment = Payment.builder()
                .rental(rental)
                .user(user)
                .amount(amount)
                .status(PaymentStatus.SUCCESS)
                .paymentMethod(method)
                .transactionId(transactionId)
                .paymentNote(request.getPaymentNote())
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment successful: id={}, rentalId={}, amount={}, txn={}",
                saved.getId(), rental.getId(), amount, transactionId);

        return toResponse(saved);
    }

    // ── Get My Payments ───────────────────────────────────────────────────────

    @Override
    public List<PaymentResponse> getMyPayments(String username) {
        User user = findUserOrThrow(username);
        return paymentRepository.findByUserId(user.getId())
                .stream().map(this::toResponse).toList();
    }

    // ── Get Payment By ID ─────────────────────────────────────────────────────

    @Override
    public PaymentResponse getPaymentById(String username, Long paymentId) {
        Payment payment = findPaymentOrThrow(paymentId);
        validatePaymentAccess(payment, username);
        return toResponse(payment);
    }

    // ── Get Payments By Rental ────────────────────────────────────────────────

    @Override
    public List<PaymentResponse> getPaymentsByRental(String username, Long rentalId) {
        Rental rental = findRentalOrThrow(rentalId);
        if (!securityUtil.isAdmin() &&
                !rental.getUser().getUsername().equals(username)) {
            throw new IllegalStateException(
                    "You are not authorized to view payments for this rental");
        }
        return paymentRepository.findByRentalId(rentalId)
                .stream().map(this::toResponse).toList();
    }

    // ── Admin: Get All Payments ───────────────────────────────────────────────

    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    // ── Admin: Get Payments By Status ─────────────────────────────────────────

    @Override
    public List<PaymentResponse> getPaymentsByStatus(String status) {
        PaymentStatus paymentStatus;
        try {
            paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status. Must be: PENDING, SUCCESS, FAILED, or REFUNDED");
        }
        return paymentRepository.findByStatus(paymentStatus)
                .stream().map(this::toResponse).toList();
    }

    // ── Admin: Get Payments By User ───────────────────────────────────────────

    @Override
    public List<PaymentResponse> getPaymentsByUser(Long userId) {
        return paymentRepository.findByUserId(userId)
                .stream().map(this::toResponse).toList();
    }

    // ── Admin: Process Refund ─────────────────────────────────────────────────

    @Override
    @Transactional
    public PaymentResponse processRefund(String username, RefundRequest request) {
        Payment payment = findPaymentOrThrow(request.getPaymentId());

        // Only SUCCESS payments can be refunded
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException(
                    "Only successful payments can be refunded. " +
                            "Current status: " + payment.getStatus());
        }

        // Use refund amount from rental cancellation if available
        BigDecimal refundAmount = payment.getRental().getRefundAmount() != null
                ? payment.getRental().getRefundAmount()
                : payment.getAmount();

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundAmount(refundAmount);
        payment.setRefundReason(request.getRefundReason());
        payment.setRefundedAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);
        log.info("Refund processed: paymentId={}, refundAmount={}", saved.getId(), refundAmount);

        return toResponse(saved);
    }

    // ── Admin: Total Revenue ──────────────────────────────────────────────────

    @Override
    public BigDecimal getTotalRevenue() {
        return paymentRepository.sumAmountByStatus(PaymentStatus.SUCCESS);
    }

    // ── Admin: Revenue Between Dates ──────────────────────────────────────────

    @Override
    public BigDecimal getRevenueBetween(LocalDateTime start, LocalDateTime end) {
        return paymentRepository.sumAmountByStatusBetween(PaymentStatus.SUCCESS, start, end);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private void validatePaymentAccess(Payment payment, String username) {
        if (!securityUtil.isAdmin() &&
                !payment.getUser().getUsername().equals(username)) {
            throw new IllegalStateException(
                    "You are not authorized to access this payment");
        }
    }

    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));
    }

    private Rental findRentalOrThrow(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rental not found: id=" + id));
    }

    private Payment findPaymentOrThrow(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found: id=" + id));
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .rentalId(payment.getRental().getId())
                .bikeTitle(payment.getRental().getBike().getTitle())
                .userId(payment.getUser().getId())
                .username(payment.getUser().getUsername())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .paymentMethod(payment.getPaymentMethod().name())
                .transactionId(payment.getTransactionId())
                .paymentNote(payment.getPaymentNote())
                .refundAmount(payment.getRefundAmount())
                .refundReason(payment.getRefundReason())
                .refundedAt(payment.getRefundedAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}