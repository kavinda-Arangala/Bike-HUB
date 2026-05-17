package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentMethod;

@Data
public class PaymentRequest {

    @NotNull(message = "Rental ID is required")
    private Long rentalId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;   // CARD | BKASH | STRIPE

    // ---- CARD-specific (required when paymentMethod = CARD) ----

    /** Full card number (16 digits) — only last 4 are stored after processing */
    @Size(min = 16, max = 19, message = "Card number must be 16-19 digits")
    private String cardNumber;

    @Size(max = 100, message = "Cardholder name too long")
    private String cardHolderName;

    /** MM/YY expiry format */
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "Card expiry must be in MM/YY format")
    private String cardExpiry;

    /** 3 or 4 digit CVV — never stored */
    @Size(min = 3, max = 4, message = "CVV must be 3 or 4 digits")
    private String cvv;

    // ---- BKASH / Mobile Banking (required when paymentMethod = BKASH) ----

    /** Registered mobile number for bKash / mobile banking */
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid mobile number format")
    private String mobileNumber;

    /** OTP or PIN confirmation token sent by the gateway */
    private String mobileToken;

    // ---- STRIPE (required when paymentMethod = STRIPE) ----

    /** Stripe payment method token (e.g., pm_xxxxx from Stripe.js) */
    private String stripeToken;
}
