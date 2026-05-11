package model;
/**
 * OOP: Inheritance — AdminUser extends User
 */
public class AdminUser extends User {
    private String adminCode;

    public AdminUser() { super(); }

    public AdminUser(String id, String name, String username, String password,
                     String email, String phone, String address,
                     String profilePhoto, String adminCode) {
        super(id, name, username, password, email, phone, address, profilePhoto, "ADMIN");
        this.adminCode = adminCode;
    }

    public String getAdminCode()            { return adminCode; }
    public void setAdminCode(String code)   { this.adminCode = code; }

    // OOP: Polymorphism — admin authentication
    @Override
    public String authenticate(String inputPassword) {
        return this.getPassword().equals(inputPassword) ? "SUCCESS" : "FAILED";
    }

    @Override
    public String toFileString() {
        return super.toFileString() + "|" + (adminCode != null ? adminCode : "ADMIN_KEY");
    }
}


