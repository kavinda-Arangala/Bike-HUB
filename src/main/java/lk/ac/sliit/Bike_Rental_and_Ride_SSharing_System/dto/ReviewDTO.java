package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto;

import lombok.Data;

// to transfer data between frontend and backend
@Data
public class ReviewDTO {

    private Long bikeId;
    private Long reviewerId;
    private Long reviewedUserId;
    private int rating;
    private String comment;
}