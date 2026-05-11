package model;
/**
 * OOP: Abstraction + Encapsulation
 * Abstract base class for all user types
 */
public abstract class User {
    private String id;
    private String name;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String address;
    private String profilePhoto; // filename stored in /uploads/
    private String role; // RENTER, BIKE_OWNER, ADMIN

    public User() {}

    public User(String id, String name, String username, String password,
                String email, String phone, String address,
                String profilePhoto, String role) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.profilePhoto = profilePhoto;
        this.role = role;
    }

    // --- Getters & Setters (Encapsulation) ---
    public String getId()                      { return id; }
    public void setId(String id)               { this.id = id; }
    public String getName()                    { return name; }
    public void setName(String name)           { this.name = name; }
    public String getUsername()                { return username; }
    public void setUsername(String username)   { this.username = username; }
    public String getPassword()                { return password; }
    public void setPassword(String password)   { this.password = password; }
    public String getEmail()                   { return email; }
    public void setEmail(String email)         { this.email = email; }
    public String getPhone()                   { return phone; }
    public void setPhone(String phone)         { this.phone = phone; }
    public String getAddress()                 { return address; }
    public void setAddress(String address)     { this.address = address; }
    public String getProfilePhoto()            { return profilePhoto; }
    public void setProfilePhoto(String p)      { this.profilePhoto = p; }
    public String getRole()                    { return role; }
    public void setRole(String role)           { this.role = role; }

    // OOP: Polymorphism — each subclass defines its own authentication
    public abstract String authenticate(String inputPassword);

    // Serialize to CSV line for file storage
    public String toFileString() {
        return String.join("|",
                id, name, username, password, email,
                phone, address,
                (profilePhoto != null ? profilePhoto : "default.png"),
                role
        );
    }
}

