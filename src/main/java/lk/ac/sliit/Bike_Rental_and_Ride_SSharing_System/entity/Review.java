package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//Marks this class as a database table
@Entity

//Table name in MySQL
@Table(name = "reviews")

//generates getters, setters
@Data
//Empty constructor
@NoArgsConstructor
//Full constructor
@AllArgsConstructor

public class Review {

    //Primary key
    @Id
    //Auto increment ID
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    //Bike that is being reviewed
    private Long bikeId;
    //Who wrote the review
    private Long reviewerId;
    //owner or renter being reviewed
    private Long reviewedUserId;
    //for rating 1 to 5 star
    private int rating;
    //for comment
    private String comment;
    //Time when review is created
    private LocalDateTime createdAt;
}