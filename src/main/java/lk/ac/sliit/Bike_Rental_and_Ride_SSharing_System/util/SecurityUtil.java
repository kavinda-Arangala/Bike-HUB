package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public String getCurrentUserEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication().getName();
    }

    public boolean isOwnerOrAdmin(String ownerEmail) {
        return isAdmin() || getCurrentUserEmail().equals(ownerEmail);
    }
}