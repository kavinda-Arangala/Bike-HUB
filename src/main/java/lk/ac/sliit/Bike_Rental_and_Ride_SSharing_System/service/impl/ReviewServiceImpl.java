package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.impl;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.ReviewDTO;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Review;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.ReviewRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Override
    public Review addReview(ReviewDTO dto) {

        Review review = new Review();

        review.setBikeId(dto.getBikeId());
        review.setReviewerId(dto.getReviewerId());
        review.setReviewedUserId(dto.getReviewedUserId());
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setCreatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getReviewsByBike(Long bikeId) {
        return reviewRepository.findByBikeId(bikeId);
    }

    @Override
    public Double getAverageRating(Long bikeId) {
        return reviewRepository.getAverageRating(bikeId);
    }

    @Override
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}