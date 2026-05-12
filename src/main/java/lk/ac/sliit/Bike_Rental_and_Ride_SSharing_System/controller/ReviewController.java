package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.controller;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Review;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/add")
    public Review addReview(@RequestBody Review review) {
        return reviewService.addReview(review);
    }

    @GetMapping("/bike/{bikeId}")
    public List<Review> getBikeReviews(@PathVariable Long bikeId) {
        return reviewService.getReviewsByBike(bikeId);
    }

    @DeleteMapping("/{reviewId}")
    public String deleteReview(@PathVariable Long reviewId) {

        reviewService.deleteReview(reviewId);

        return "Review deleted successfully";
    }

    @GetMapping("/average/{bikeId}")
    public double getAverageRating(@PathVariable Long bikeId) {
        return reviewService.getAverageRating(bikeId);
    }
}