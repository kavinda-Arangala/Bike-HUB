package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.util;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Simulates external payment gateway calls.
 *
 * <p>Each method validates input, "charges" the payment, and returns a
 * transaction ID.  Replace the body of each method with a real SDK call
 * (Stripe Java SDK, bKash REST API, etc.) without touching the service layer.</p>
 */
@Component
public class PaymentGatewaySimulator {

    private static final String CARD_TX_PREFIX   = "CARD-";
    private static final String BKASH_TX_PREFIX  = "BK-";
    private static final String STRIPE_TX_PREFIX = "ch_";   // Stripe charge ID format

    // ----------------------------------------------------------------
    // Public gateway methods
    // ----------------------------------------------------------------

    /**
     * Process a card payment.
     *
     * @param cardNumber     Full 16-digit card number (never stored after this call)
     * @param cardHolderName Cardholder name
     * @param cardExpiry     MM/YY expiry
     * @param cvv            3 or 4 digit CVV (never stored)
     * @param amountInCents  Amount in smallest currency unit (e.g. LKR paisa)
     * @return               Gateway transaction ID on success
     * @throws PaymentGatewayException on card validation failure or decline
     */
    public String processCardPayment(String cardNumber,
                                     String cardHolderName,
                                     String cardExpiry,
                                     String cvv,
                                     long amountInCents) {
        validateCardNumber(cardNumber);
        validateCardExpiry(cardExpiry);
        validateCvv(cvv);

        // Simulate a test-card decline (card ending in 0000 → declined)
        if (cardNumber.endsWith("0000")) {
            throw new PaymentGatewayException("Card declined by issuer.", PaymentMethod.CARD);
        }

        return CARD_TX_PREFIX + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    /**
     * Process a bKash mobile banking payment.
     *
     * @param mobileNumber Mobile number registered with bKash
     * @param token        OTP / PIN confirmation token from bKash
     * @param amount       Amount in LKR (or BDT for bKash)
     * @return             bKash transaction ID
     */
    public String processBkashPayment(String mobileNumber,
                                      String token,
                                      java.math.BigDecimal amount) {
        validateMobileNumber(mobileNumber);
        if (token == null || token.isBlank()) {
            throw new PaymentGatewayException("bKash confirmation token is required.", PaymentMethod.BKASH);
        }

        // Simulate failed token: "000000" → fail
        if ("000000".equals(token)) {
            throw new PaymentGatewayException("Invalid bKash token. Transaction declined.", PaymentMethod.BKASH);
        }

        return BK_TX_PREFIX + System.currentTimeMillis() + "-" + mobileNumber.replaceAll("\\D", "").substring(
                Math.max(0, mobileNumber.replaceAll("\\D", "").length() - 4));
    }

    /**
     * Process a Stripe payment using a Stripe.js payment method token.
     *
     * @param stripeToken Stripe payment method token (pm_xxxx or tok_xxxx)
     * @param amount      Amount
     * @return            Stripe charge ID
     */
    public String processStripePayment(String stripeToken,
                                       java.math.BigDecimal amount) {
        if (stripeToken == null || stripeToken.isBlank()) {
            throw new PaymentGatewayException("Stripe token is required.", PaymentMethod.STRIPE);
        }
        if (!stripeToken.startsWith("pm_") && !stripeToken.startsWith("tok_")) {
            throw new PaymentGatewayException("Invalid Stripe token format.", PaymentMethod.STRIPE);
        }

        // Simulate test token: "pm_card_declined" → fail
        if ("pm_card_declined".equals(stripeToken)) {
            throw new PaymentGatewayException("Stripe card was declined.", PaymentMethod.STRIPE);
        }

        return STRIPE_TX_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Initiate a refund for a previously successful transaction.
     *
     * @param originalTransactionId The transaction ID to refund
     * @param method                Payment method of the original transaction
     * @return                      Refund reference ID
     */
    public String processRefund(String originalTransactionId, PaymentMethod method) {
        if (originalTransactionId == null || originalTransactionId.isBlank()) {
            throw new PaymentGatewayException("Transaction ID required for refund.", method);
        }
        return "REF-" + originalTransactionId;
    }

    // ----------------------------------------------------------------
    // Private validation helpers
    // ----------------------------------------------------------------

    private void validateCardNumber(String cardNumber) {
        if (cardNumber == null || !cardNumber.replaceAll("\\s|-", "").matches("\\d{16}")) {
            throw new PaymentGatewayException("Invalid card number. Must be 16 digits.", PaymentMethod.CARD);
        }
    }

    private void validateCardExpiry(String expiry) {
        if (expiry == null || !expiry.matches("^(0[1-9]|1[0-2])/[0-9]{2}$")) {
            throw new PaymentGatewayException("Invalid card expiry. Use MM/YY format.", PaymentMethod.CARD);
        }
        // Basic expiry check
        String[] parts = expiry.split("/");
        int month = Integer.parseInt(parts[0]);
        int year  = 2000 + Integer.parseInt(parts[1]);
        java.time.YearMonth expYM = java.time.YearMonth.of(year, month);
        if (expYM.isBefore(java.time.YearMonth.now())) {
            throw new PaymentGatewayException("Card has expired.", PaymentMethod.CARD);
        }
    }

    private void validateCvv(String cvv) {
        if (cvv == null || !cvv.matches("\\d{3,4}")) {
            throw new PaymentGatewayException("Invalid CVV.", PaymentMethod.CARD);
        }
    }

    private void validateMobileNumber(String mobile) {
        if (mobile == null || !mobile.replaceAll("\\s", "").matches("^\\+?[0-9]{10,15}$")) {
            throw new PaymentGatewayException("Invalid mobile number.", PaymentMethod.BKASH);
        }
    }

    // ----------------------------------------------------------------
    // Inner exception class
    // ----------------------------------------------------------------

    /** Thrown when the (simulated) payment gateway rejects a transaction. */
    public static class PaymentGatewayException extends RuntimeException {
        private final PaymentMethod method;

        public PaymentGatewayException(String message, PaymentMethod method) {
            super(message);
            this.method = method;
        }

        public PaymentMethod getMethod() { return method; }
    }

    // Fix: define constant properly used in processBkashPayment
    private static final String BK_TX_PREFIX = BKASH_TX_PREFIX;
}
