package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.ReceiptResponse;

import java.util.List;

public interface ReceiptService {

    /** Generate a receipt for a successful payment (called internally by PaymentService) */
    ReceiptResponse generateReceipt(Long paymentId);

    /** Get a receipt by its DB ID */
    ReceiptResponse getReceiptById(Long id);

    /** Get a receipt by its human-readable receipt number */
    ReceiptResponse getReceiptByNumber(String receiptNumber);

    /** Get the receipt for a specific payment */
    ReceiptResponse getReceiptByPayment(Long paymentId);

    /** Get all receipts for a user (payment history view) */
    List<ReceiptResponse> getReceiptsByUser(Long userId);

    /** Get receipts for a specific rental */
    List<ReceiptResponse> getReceiptsByRental(Long rentalId);

    /** Delete a receipt (admin only) */
    void deleteReceipt(Long id);
}
