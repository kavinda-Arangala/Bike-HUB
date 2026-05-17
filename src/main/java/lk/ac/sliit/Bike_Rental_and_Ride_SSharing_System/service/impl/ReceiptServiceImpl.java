package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.ReceiptResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Payment;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Receipt;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Rental;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.PaymentRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.ReceiptRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.RentalRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final PaymentRepository paymentRepository;
    private final RentalRepository  rentalRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    @Transactional
    public ReceiptResponse generateReceipt(Long paymentId) {
        if (receiptRepository.existsByPaymentId(paymentId)) {
            return toResponse(receiptRepository.findByPaymentId(paymentId).orElseThrow());
        }
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        Rental rental = rentalRepository.findById(payment.getRentalId())
                .orElseThrow(() -> new RuntimeException("Rental not found: " + payment.getRentalId()));

        BigDecimal tax = BigDecimal.ZERO;
        Receipt receipt = Receipt.builder()
                .receiptNumber(generateReceiptNumber())
                .paymentId(payment.getId())
                .rentalId(rental.getId())
                .userId(payment.getUserId())
                .totalAmount(payment.getAmount())
                .taxAmount(tax)
                .netAmount(payment.getAmount().add(tax))
                .paymentMethod(payment.getPaymentMethod().name())
                .transactionId(payment.getTransactionId())
                .bikeTitle(rental.getBikeTitle())
                .rentalPlan(rental.getRentalPlan().name())
                .rentalStartDate(rental.getStartDate().toString())
                .rentalEndDate(rental.getEndDate().toString())
                .duration(rental.getDuration())
                .unitPrice(rental.getUnitPrice())
                .receiptData(buildJson(payment, rental))
                .issuedAt(LocalDateTime.now())
                .build();

        Receipt saved = receiptRepository.save(receipt);
        log.info("Receipt generated: number={}, paymentId={}", saved.getReceiptNumber(), paymentId);
        return toResponse(saved);
    }

    @Override
    public ReceiptResponse getReceiptById(Long id) {
        return toResponse(receiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found: " + id)));
    }

    @Override
    public ReceiptResponse getReceiptByNumber(String receiptNumber) {
        return toResponse(receiptRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new RuntimeException("Receipt not found: " + receiptNumber)));
    }

    @Override
    public ReceiptResponse getReceiptByPayment(Long paymentId) {
        return toResponse(receiptRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Receipt not found for payment: " + paymentId)));
    }

    @Override
    public List<ReceiptResponse> getReceiptsByUser(Long userId) {
        return receiptRepository.findByUserIdOrderByIssuedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ReceiptResponse> getReceiptsByRental(Long rentalId) {
        return receiptRepository.findByRentalId(rentalId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteReceipt(Long id) {
        if (!receiptRepository.existsById(id))
            throw new RuntimeException("Receipt not found: " + id);
        receiptRepository.deleteById(id);
    }

    // ---- helpers ----

    private String generateReceiptNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = receiptRepository.count() + 1;
        return String.format("RCP-%s-%06d", date, seq);
    }

    private String buildJson(Payment p, Rental r) {
        Map<String, Object> data = new HashMap<>();
        data.put("paymentId", p.getId());
        data.put("transactionId", p.getTransactionId());
        data.put("paymentMethod", p.getPaymentMethod().name());
        data.put("amount", p.getAmount());
        data.put("paidAt", p.getPaidAt() != null ? p.getPaidAt().toString() : null);
        data.put("rentalId", r.getId());
        data.put("bikeTitle", r.getBikeTitle());
        data.put("rentalPlan", r.getRentalPlan().name());
        data.put("startDate", r.getStartDate().toString());
        data.put("endDate", r.getEndDate().toString());
        data.put("duration", r.getDuration());
        data.put("unitPrice", r.getUnitPrice());
        data.put("totalAmount", r.getTotalAmount());
        try { return MAPPER.writeValueAsString(data); }
        catch (JsonProcessingException e) { return "{}"; }
    }

    private ReceiptResponse toResponse(Receipt r) {
        return ReceiptResponse.builder()
                .id(r.getId()).receiptNumber(r.getReceiptNumber())
                .paymentId(r.getPaymentId()).rentalId(r.getRentalId()).userId(r.getUserId())
                .totalAmount(r.getTotalAmount()).taxAmount(r.getTaxAmount()).netAmount(r.getNetAmount())
                .paymentMethod(r.getPaymentMethod()).transactionId(r.getTransactionId())
                .bikeTitle(r.getBikeTitle()).rentalPlan(r.getRentalPlan())
                .rentalStartDate(r.getRentalStartDate()).rentalEndDate(r.getRentalEndDate())
                .duration(r.getDuration()).unitPrice(r.getUnitPrice())
                .receiptData(r.getReceiptData()).issuedAt(r.getIssuedAt()).createdAt(r.getCreatedAt())
                .build();
    }
}
