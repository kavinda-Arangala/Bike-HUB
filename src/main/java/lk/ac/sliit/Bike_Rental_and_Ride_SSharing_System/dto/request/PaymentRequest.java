package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "Rental ID is required")
    private Long rentalId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;  // CASH, CARD, BANK_TRANSFER, WALLET

    private String paymentNote;    // optional note from user
}