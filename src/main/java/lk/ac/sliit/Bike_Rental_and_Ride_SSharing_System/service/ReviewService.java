package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Review;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    //for add reviews
    public Review addReview(Review review) {
        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByBike(Long bikeId) {
        return reviewRepository.findByBikeId(bikeId);
    }

    //For delete reviews
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    //For get average rating
    public double getAverageRating(Long bikeId) {

        List<Review> reviews = reviewRepository.findByBikeId(bikeId);

        if (reviews.isEmpty()) {
            return 0;
        }

        double total = 0;

        for (Review r : reviews) {
            total += r.getRating();
        }

        return total / reviews.size();
    }
}