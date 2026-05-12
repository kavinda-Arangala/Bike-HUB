package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.utill;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.BikeDTO;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Bike;
import org.springframework.stereotype.Component;

/**
 * BikeMapper
 * Package : utill   (note: matches your project's "utill" spelling)
 *
 * A utility/helper class responsible for converting between:
 *   BikeDTO  ↔  Bike (entity)
 *
 * WHY A SEPARATE MAPPER?
 *   Keeps the Service clean. All conversion logic is in one place.
 *   If you add a new field to Bike, you only update this file.
 *
 * @Component → Spring registers this as a bean so it can be @Autowired
 */
@Component
public class BikeMapper {

    /**
     * Convert BikeDTO → Bike entity
     * Used when ADDING a new bike (POST /api/bikes)
     */
    public Bike toEntity(BikeDTO dto) {
        Bike bike = new Bike();
        applyDto(dto, bike);
        return bike;
    }

    /**
     * Update an existing Bike entity from a BikeDTO
     * Used when EDITING a bike (PUT /api/bikes/{id})
     * The entity already has id + createdAt — we don't overwrite those.
     */
    public void updateEntityFromDto(BikeDTO dto, Bike bike) {
        applyDto(dto, bike);
    }

    // ── Private helper: copy DTO fields → entity ─────────────────
    private void applyDto(BikeDTO dto, Bike bike) {
        bike.setTitle(dto.getTitle());

        // Convert String "CYCLE" → BikeType.CYCLE
        if (dto.getBikeType() != null)
            bike.setBikeType(Bike.BikeType.valueOf(dto.getBikeType().toUpperCase()));

        bike.setLocation(dto.getLocation());
        bike.setDescription(dto.getDescription());
        bike.setDailyPrice(dto.getDailyPrice());
        bike.setWeeklyPrice(dto.getWeeklyPrice());
        bike.setMonthlyPrice(dto.getMonthlyPrice());

        // Convert String "AVAILABLE" → BikeStatus.AVAILABLE
        if (dto.getStatus() != null)
            bike.setStatus(Bike.BikeStatus.valueOf(dto.getStatus().toUpperCase()));

        bike.setPhotoUrl(dto.getPhotoUrl());
        bike.setPhotoUrls(dto.getPhotoUrls());
        bike.setOwnerId(dto.getOwnerId());
        bike.setOwnerName(dto.getOwnerName());
        bike.setOwnerPhone(dto.getOwnerPhone());
        bike.setLatitude(dto.getLatitude());
        bike.setLongitude(dto.getLongitude());
        bike.setYearOfManufacture(dto.getYearOfManufacture());
        bike.setBrand(dto.getBrand());
        bike.setModel(dto.getModel());
        bike.setEngineCC(dto.getEngineCC());
        bike.setColor(dto.getColor());
    }
}
