package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.ReviewDTO;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Review;

import java.util.List;

public interface ReviewService {

    Review addReview(ReviewDTO dto);

    List<Review> getReviewsByBike(Long bikeId);

    Double getAverageRating(Long bikeId);

    void deleteReview(Long reviewId);
}