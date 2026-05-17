package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.DisbursementRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.DisbursementResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.DisbursementStatus;

import java.math.BigDecimal;
import java.util.List;

public interface DisbursementService {

    /** Admin triggers a disbursement (payout) for a completed payment */
    DisbursementResponse createDisbursement(DisbursementRequest request);

    /** Get a disbursement by ID */
    DisbursementResponse getDisbursementById(Long id);

    /** Get all disbursements (admin) */
    List<DisbursementResponse> getAllDisbursements();

    /** Get payout history for a specific owner */
    List<DisbursementResponse> getDisbursementsByOwner(Long ownerId);

    /** Get disbursements filtered by status */
    List<DisbursementResponse> getDisbursementsByStatus(DisbursementStatus status);

    /** Update disbursement status (e.g. mark as TRANSFERRED or FAILED) */
    DisbursementResponse updateDisbursementStatus(Long id, DisbursementStatus status, String transferReference);

    /** Delete a disbursement record (admin) */
    void deleteDisbursement(Long id);

    /** Get total earnings for an owner (sum of TRANSFERRED ownerAmount) */
    BigDecimal getTotalEarningsByOwner(Long ownerId);

    /** Get total platform fees collected (admin stat) */
    BigDecimal getTotalPlatformFees();
}
