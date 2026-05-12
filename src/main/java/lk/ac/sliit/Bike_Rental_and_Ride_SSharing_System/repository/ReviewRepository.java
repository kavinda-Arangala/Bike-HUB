package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository;


import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//Handles database
public interface ReviewRepository extends JpaRepository<Review, Long> {

    //Get all reviews for a bike
    List<Review> findByBikeId(Long bikeId);

}