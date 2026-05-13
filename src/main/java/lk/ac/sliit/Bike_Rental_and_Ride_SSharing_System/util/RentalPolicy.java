package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.util;

public class RentalPolicy {

    private RentalPolicy() {}

    // ── Time Validation ───────────────────────────────────────────────────────
    public static final int    MIN_RENTAL_HOURS         = 1;
    public static final int    MAX_RENTAL_DAYS          = 30;
    public static final int    MAX_ADVANCE_BOOKING_DAYS = 90;
    public static final int    MIN_BOOKING_LEAD_MINUTES = 30;

    // ── Cancellation Policy ───────────────────────────────────────────────────
    public static final int    FREE_CANCEL_HOURS        = 24;
    public static final double LATE_CANCEL_FEE_PCT      = 0.20;
    public static final int    NO_CANCEL_HOURS          = 1;
}