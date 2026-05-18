package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// to transfer data between frontend and backend
@Data
@NoArgsConstructor
@AllArgsConstructor

public class ReviewDTO {

    private Long bikeId;
    private Long reviewerId;
    private Long reviewedUserId;
    private int rating;
    private String comment;
}