package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.BikeDTO;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.model.Bike;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.BikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BikeService {

    @Autowired
    private BikeRepository bikeRepository;

    public BikeDTO addBike(BikeDTO dto) {
        validateBikeDTO(dto);

        Bike bike = new Bike();
        mapDtoToEntity(dto, bike);

        Bike saved = bikeRepository.save(bike);
        return BikeDTO.fromEntity(saved);
    }

    public List<BikeDTO> getAllBikes() {
        return bikeRepository.findAll()
                .stream()
                .map(BikeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public BikeDTO getBikeById(Long id) {
        Bike bike = bikeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bike not found with ID: " + id));
        return BikeDTO.fromEntity(bike);
    }

    public BikeDTO updateBike(Long id, BikeDTO dto) {
        Bike bike = bikeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bike not found with ID: " + id));

        mapDtoToEntity(dto, bike);
        Bike updated = bikeRepository.save(bike);
        return BikeDTO.fromEntity(updated);
    }

    public void deleteBike(Long id) {
        if (!bikeRepository.existsById(id)) {
            throw new RuntimeException("Bike not found with ID: " + id);
        }
        bikeRepository.deleteById(id);
    }

    public List<BikeDTO> getBikesByOwner(Long ownerId) {
        return bikeRepository.findByOwnerId(ownerId)
                .stream()
                .map(BikeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<BikeDTO> searchBikes(String location, String bikeType,
                                     String status, BigDecimal minPrice,
                                     BigDecimal maxPrice) {
        Bike.BikeType type = (bikeType != null && !bikeType.isBlank())
                ? Bike.BikeType.valueOf(bikeType.toUpperCase()) : null;

        Bike.BikeStatus bikeStatus = (status != null && !status.isBlank())
                ? Bike.BikeStatus.valueOf(status.toUpperCase()) : null;

        return bikeRepository.searchBikes(location, type, bikeStatus, minPrice, maxPrice)
                .stream()
                .map(BikeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public void updateBikeStatus(Long bikeId, String newStatus) {
        Bike bike = bikeRepository.findById(bikeId)
                .orElseThrow(() -> new RuntimeException("Bike not found with ID: " + bikeId));
        bike.setStatus(Bike.BikeStatus.valueOf(newStatus.toUpperCase()));
        bikeRepository.save(bike);
    }

    public void updateBikeRating(Long bikeId, Double newRating) {
        Bike bike = bikeRepository.findById(bikeId)
                .orElseThrow(() -> new RuntimeException("Bike not found with ID: " + bikeId));
        bike.setAverageRating(newRating);
        bikeRepository.save(bike);
    }

    public List<BikeDTO> getAvailableBikes() {
        return bikeRepository.findByStatus(Bike.BikeStatus.AVAILABLE)
                .stream()
                .map(BikeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public long countBikesByOwner(Long ownerId) {
        return bikeRepository.countByOwnerId(ownerId);
    }

    public long countRentedBikesByOwner(Long ownerId) {
        return bikeRepository.countByOwnerIdAndStatus(ownerId, Bike.BikeStatus.RENTED);
    }


    private void mapDtoToEntity(BikeDTO dto, Bike bike) {
        bike.setTitle(dto.getTitle());
        bike.setBikeType(Bike.BikeType.valueOf(dto.getBikeType().toUpperCase()));
        bike.setLocation(dto.getLocation());
        bike.setDescription(dto.getDescription());
        bike.setDailyPrice(dto.getDailyPrice());
        bike.setWeeklyPrice(dto.getWeeklyPrice());
        bike.setMonthlyPrice(dto.getMonthlyPrice());
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

    private void validateBikeDTO(BikeDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank())
            throw new IllegalArgumentException("Bike title is required.");
        if (dto.getBikeType() == null || dto.getBikeType().isBlank())
            throw new IllegalArgumentException("Bike type is required (CYCLE/SCOOTER/MOTORBIKE).");
        if (dto.getLocation() == null || dto.getLocation().isBlank())
            throw new IllegalArgumentException("Location is required.");
        if (dto.getDailyPrice() == null || dto.getDailyPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Daily price must be greater than 0.");
        if (dto.getOwnerId() == null)
            throw new IllegalArgumentException("Owner ID is required.");
    }
}
