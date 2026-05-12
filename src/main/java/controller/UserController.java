package controller;
import jakarta.servlet.http.HttpSession;
import model.AdminUser;
import model.RegularUser;
import model.User;
import util.FileHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class UserController {

    // Upload directory for profile photos
    private static final String UPLOAD_DIR =
            System.getProperty("user.home") + File.separator + "bikerental" + File.separator + "uploads" + File.separator;

    // ─────────────────────────────────────────────
    //  HOME
    // ─────────────────────────────────────────────
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // ─────────────────────────────────────────────
    //  REGISTER
    // ─────────────────────────────────────────────
    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String name,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam String role,
            @RequestParam(required = false) String licenseNumber,
            @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto,
            RedirectAttributes redirectAttributes) {

        // Handle profile photo upload
        String photoFilename = "default.png";
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            try {
                new File(UPLOAD_DIR).mkdirs();
                photoFilename = System.currentTimeMillis() + "_" + profilePhoto.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + photoFilename);
                Files.write(path, profilePhoto.getBytes());
            } catch (IOException e) {
                photoFilename = "default.png";
            }
        }

        User newUser;
        String id = FileHandler.generateId(role);

        if ("ADMIN".equals(role)) {
            newUser = new AdminUser(id, name, username, password, email, phone, address, photoFilename, "ADMINKEY");
        } else {
            newUser = new RegularUser(id, name, username, password, email, phone, address, photoFilename, role,
                    (licenseNumber != null ? licenseNumber : "N/A"));
        }

        boolean success = FileHandler.addUser(newUser);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "Username '" + username + "' already exists.");
            return "redirect:/register";
        }
    }

    // ─────────────────────────────────────────────
    //  LOGIN
    // ─────────────────────────────────────────────
    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User user = FileHandler.findByUsername(username);

        // OOP: Polymorphism — authenticate() differs per subclass
        if (user != null && "SUCCESS".equals(user.authenticate(password))) {
            session.setAttribute("loggedUser", user);
            session.setAttribute("userRole", user.getRole());

            return switch (user.getRole()) {
                case "ADMIN"      -> "redirect:/admin/users";
                case "BIKE_OWNER" -> "redirect:/dashboard";
                default           -> "redirect:/dashboard";
            };
        }

        redirectAttributes.addFlashAttribute("error", "Invalid username or password.");
        return "redirect:/login";
    }

    // ─────────────────────────────────────────────
    //  LOGOUT
    // ─────────────────────────────────────────────
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "You have been logged out.");
        return "redirect:/login";
    }

    // ─────────────────────────────────────────────
    //  DASHBOARD (Renter / Bike Owner)
    // ─────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        return "dashboard";
    }

    // ─────────────────────────────────────────────
    //  ADMIN — User List & Search
    // ─────────────────────────────────────────────
    @GetMapping("/admin/users")
    public String userList(@RequestParam(required = false) String search,
                           HttpSession session, Model model) {
        User admin = (User) session.getAttribute("loggedUser");
        if (admin == null || !"ADMIN".equals(admin.getRole())) return "redirect:/login";

        List<User> users = (search != null && !search.isBlank())
                ? FileHandler.searchUsers(search)
                : FileHandler.readAllUsers();

        model.addAttribute("users", users);
        model.addAttribute("search", search);
        model.addAttribute("admin", admin);
        return "userList";
    }

    // ─────────────────────────────────────────────
    //  EDIT PROFILE (GET)
    // ─────────────────────────────────────────────
    @GetMapping("/edit")
    public String showEdit(@RequestParam(required = false) String id,
                           HttpSession session, Model model) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) return "redirect:/login";

        User userToEdit = (id != null && "ADMIN".equals(loggedUser.getRole()))
                ? FileHandler.findById(id)
                : loggedUser;

        if (userToEdit == null) return "redirect:/dashboard";
        model.addAttribute("editUser", userToEdit);
        return "editUser";
    }

    // ─────────────────────────────────────────────
    //  UPDATE PROFILE (POST)
    // ─────────────────────────────────────────────
    @PostMapping("/update")
    public String updateUser(
            @RequestParam String id,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String licenseNumber,
            @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User existing = FileHandler.findById(id);
        if (existing == null) return "redirect:/dashboard";

        existing.setName(name);
        existing.setEmail(email);
        existing.setPhone(phone);
        existing.setAddress(address);
        if (password != null && !password.isBlank()) {
            existing.setPassword(password);
        }

        // Handle profile photo update
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            try {
                new File(UPLOAD_DIR).mkdirs();
                String photoFilename = System.currentTimeMillis() + "_" + profilePhoto.getOriginalFilename();
                Files.write(Paths.get(UPLOAD_DIR + photoFilename), profilePhoto.getBytes());
                existing.setProfilePhoto(photoFilename);
            } catch (IOException ignored) {}
        }

        if (existing instanceof RegularUser ru && licenseNumber != null) {
            ru.setLicenseNumber(licenseNumber);
        }

        FileHandler.updateUser(existing);

        // Refresh session if updating own profile
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser.getId().equals(id)) {
            session.setAttribute("loggedUser", existing);
        }

        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "ADMIN".equals(loggedUser.getRole())
                ? "redirect:/admin/users"
                : "redirect:/dashboard";
    }

    // ─────────────────────────────────────────────
    //  DELETE USER (Admin only)
    // ─────────────────────────────────────────────
    @PostMapping("/admin/delete")
    public String deleteUser(@RequestParam String id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("loggedUser");
        if (admin == null || !"ADMIN".equals(admin.getRole())) return "redirect:/login";

        if (admin.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "You cannot delete your own account!");
            return "redirect:/admin/users";
        }

        FileHandler.deleteUser(id);
        redirectAttributes.addFlashAttribute("success", "User deleted successfully.");
        return "redirect:/admin/users";
    }
}
