package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.impl;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.DisbursementRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.DisbursementResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Disbursement;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Payment;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Rental;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.DisbursementStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.DisbursementRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.PaymentRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.RentalRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.DisbursementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisbursementServiceImpl implements DisbursementService {

    private static final BigDecimal PLATFORM_FEE_PERCENT = new BigDecimal("10.00");

    private final DisbursementRepository disbursementRepository;
    private final PaymentRepository      paymentRepository;
    private final RentalRepository       rentalRepository;

    @Override
    @Transactional
    public DisbursementResponse createDisbursement(DisbursementRequest req) {
        Payment payment = paymentRepository.findById(req.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Payment not found: " + req.getPaymentId()));

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Disbursement only allowed for successful payments.");
        }
        if (disbursementRepository.existsByPaymentId(req.getPaymentId())) {
            throw new IllegalStateException("Disbursement already exists for payment: " + req.getPaymentId());
        }

        Rental rental = rentalRepository.findById(payment.getRentalId())
                .orElseThrow(() -> new RuntimeException("Rental not found: " + payment.getRentalId()));

        BigDecimal gross      = payment.getAmount();
        BigDecimal fee        = gross.multiply(PLATFORM_FEE_PERCENT)
                                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal ownerAmt   = gross.subtract(fee);

        Disbursement d = Disbursement.builder()
                .paymentId(payment.getId())
                .rentalId(rental.getId())
                .ownerId(rental.getOwnerId())
                .adminId(req.getAdminId())
                .grossAmount(gross)
                .platformFee(fee)
                .ownerAmount(ownerAmt)
                .feePercentage(PLATFORM_FEE_PERCENT)
                .disbursementStatus(DisbursementStatus.PENDING)
                .notes(req.getNotes())
                .build();

        Disbursement saved = disbursementRepository.save(d);
        log.info("Disbursement created: id={}, owner={}, ownerAmt={}", saved.getId(), saved.getOwnerId(), ownerAmt);
        return toResponse(saved);
    }

    @Override
    public DisbursementResponse getDisbursementById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<DisbursementResponse> getAllDisbursements() {
        return disbursementRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<DisbursementResponse> getDisbursementsByOwner(Long ownerId) {
        return disbursementRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<DisbursementResponse> getDisbursementsByStatus(DisbursementStatus status) {
        return disbursementRepository.findByDisbursementStatusOrderByCreatedAtDesc(status)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DisbursementResponse updateDisbursementStatus(Long id, DisbursementStatus status, String transferRef) {
        Disbursement d = findOrThrow(id);
        d.setDisbursementStatus(status);
        if (status == DisbursementStatus.TRANSFERRED) {
            d.setTransferReference(transferRef != null ? transferRef
                    : "TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            d.setDisbursedAt(LocalDateTime.now());
        }
        log.info("Disbursement status updated: id={}, status={}", id, status);
        return toResponse(disbursementRepository.save(d));
    }

    @Override
    @Transactional
    public void deleteDisbursement(Long id) {
        if (!disbursementRepository.existsById(id))
            throw new RuntimeException("Disbursement not found: " + id);
        disbursementRepository.deleteById(id);
    }

    @Override
    public BigDecimal getTotalEarningsByOwner(Long ownerId) {
        BigDecimal result = disbursementRepository.totalTransferredToOwner(ownerId);
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalPlatformFees() {
        BigDecimal result = disbursementRepository.totalPlatformFeesCollected();
        return result != null ? result : BigDecimal.ZERO;
    }

    private Disbursement findOrThrow(Long id) {
        return disbursementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Disbursement not found: " + id));
    }

    private DisbursementResponse toResponse(Disbursement d) {
        return DisbursementResponse.builder()
                .id(d.getId()).paymentId(d.getPaymentId()).rentalId(d.getRentalId())
                .ownerId(d.getOwnerId()).adminId(d.getAdminId())
                .grossAmount(d.getGrossAmount()).platformFee(d.getPlatformFee())
                .ownerAmount(d.getOwnerAmount()).feePercentage(d.getFeePercentage())
                .disbursementStatus(d.getDisbursementStatus())
                .transferReference(d.getTransferReference()).notes(d.getNotes())
                .disbursedAt(d.getDisbursedAt()).createdAt(d.getCreatedAt())
                .build();
    }
}
