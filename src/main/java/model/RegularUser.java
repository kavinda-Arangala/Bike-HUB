package model;
/**
 * OOP: Inheritance — RegularUser extends User
 * Covers both RENTER and BIKE_OWNER roles
 */
public class RegularUser extends User {
    private String licenseNumber;

    public RegularUser() { super(); }

    public RegularUser(String id, String name, String username, String password,
                       String email, String phone, String address,
                       String profilePhoto, String role, String licenseNumber) {
        super(id, name, username, password, email, phone, address, profilePhoto, role);
        this.licenseNumber = licenseNumber;
    }

    public String getLicenseNumber()             { return licenseNumber; }
    public void setLicenseNumber(String license) { this.licenseNumber = license; }

    // OOP: Polymorphism — standard password check
    @Override
    public String authenticate(String inputPassword) {
        return this.getPassword().equals(inputPassword) ? "SUCCESS" : "FAILED";
    }

    @Override
    public String toFileString() {
        return super.toFileString() + "|" + (licenseNumber != null ? licenseNumber : "N/A");
    }
}

