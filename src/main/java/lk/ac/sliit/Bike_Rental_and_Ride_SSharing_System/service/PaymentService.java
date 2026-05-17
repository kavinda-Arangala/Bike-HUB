package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.PaymentRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.PaymentResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentStatus;

import java.util.List;

public interface PaymentService {

    /** Initiate and process a payment for a rental */
    PaymentResponse initiatePayment(PaymentRequest request);

    /** Get a single payment by ID */
    PaymentResponse getPaymentById(Long id);

    /** Get all payments (admin) */
    List<PaymentResponse> getAllPayments();

    /** Get payment history for a user */
    List<PaymentResponse> getPaymentsByUser(Long userId);

    /** Get payments for a specific rental */
    List<PaymentResponse> getPaymentsByRental(Long rentalId);

    /** Get payments filtered by status */
    List<PaymentResponse> getPaymentsByStatus(PaymentStatus status);

    /** Admin: manually update payment status */
    PaymentResponse updatePaymentStatus(Long id, PaymentStatus status);

    /** Initiate a refund on a successful payment */
    PaymentResponse refundPayment(Long paymentId);

    /** Delete a payment record (admin, usually for test data) */
    void deletePayment(Long id);
}
