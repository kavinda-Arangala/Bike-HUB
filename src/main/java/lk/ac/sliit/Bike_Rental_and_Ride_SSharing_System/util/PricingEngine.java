package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.util;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Bike;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.Duration;

@Component
public class PricingEngine {

    private static final double BASE_FARE = 20.0;       // Base fare in currency units
    private static final double MIN_FARE = 30.0;         // Minimum charge per booking

    /**
     * Calculate total fare based on time and distance.
     */
    public double calculateFare(Bike bike, LocalDateTime startTime, LocalDateTime endTime, double distanceKm) {
        double hours = getHours(startTime, endTime);
        double timeFare = hours * bike.getPricePerHour();
        double distanceFare = distanceKm * bike.getPricePerKm();
        double total = BASE_FARE + timeFare + distanceFare;
        return Math.max(total, MIN_FARE);
    }

    public double getBaseFare() {
        return BASE_FARE;
    }

    public double calculateTimeFare(Bike bike, LocalDateTime startTime, LocalDateTime endTime) {
        return getHours(startTime, endTime) * bike.getPricePerHour();
    }

    public double calculateDistanceFare(Bike bike, double distanceKm) {
        return distanceKm * bike.getPricePerKm();
    }

    public double getHours(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        return duration.toMinutes() / 60.0;
    }

    /**
     * Estimate fare for advance booking (time only, distance unknown).
     */
    public double estimateFare(Bike bike, double estimatedHours) {
        double timeFare = estimatedHours * bike.getPricePerHour();
        return Math.max(BASE_FARE + timeFare, MIN_FARE);
    }
}