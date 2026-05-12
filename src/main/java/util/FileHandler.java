package util;
import model.AdminUser;
import model.RegularUser;
import model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * File-based data storage for users.txt
 * Format: id|name|username|password|email|phone|address|profilePhoto|role|extraField
 */
public class FileHandler {

    private static final String FILE_PATH =
            System.getProperty("user.home") + File.separator
                    + "bikerental" + File.separator + "users.txt";

    static {
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
                // Seed a default admin account
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    bw.write("ADMIN001|Admin User|admin|admin123|admin@bikerental.com|0771000000|Colombo|default.png|ADMIN|ADMINKEY");
                    bw.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** READ — Load all users */
    public static List<User> readAllUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    User u = parseLine(line);
                    if (u != null) users.add(u);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    /** Parse a pipe-separated line into a User object */
    private static User parseLine(String line) {
        String[] p = line.split("\\|");
        if (p.length < 10) return null;
        // id|name|username|password|email|phone|address|profilePhoto|role|extraField
        String role = p[8];
        if ("ADMIN".equals(role)) {
            return new AdminUser(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[9]);
        } else {
            return new RegularUser(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], role, p[9]);
        }
    }

    /** WRITE — Overwrite file with all users */
    public static void writeAllUsers(List<User> users) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (User u : users) {
                bw.write(u.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** CREATE — Add a new user. Returns false if username already exists */
    public static boolean addUser(User user) {
        List<User> users = readAllUsers();
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(user.getUsername())) return false;
        }
        users.add(user);
        writeAllUsers(users);
        return true;
    }

    /** READ — Find by ID */
    public static User findById(String id) {
        for (User u : readAllUsers()) {
            if (u.getId().equals(id)) return u;
        }
        return null;
    }

    /** READ — Find by username */
    public static User findByUsername(String username) {
        for (User u : readAllUsers()) {
            if (u.getUsername().equalsIgnoreCase(username)) return u;
        }
        return null;
    }

    /** READ — Search by keyword (name, username, or email) */
    public static List<User> searchUsers(String keyword) {
        List<User> result = new ArrayList<>();
        String kw = keyword.toLowerCase();
        for (User u : readAllUsers()) {
            if (u.getName().toLowerCase().contains(kw)
                    || u.getUsername().toLowerCase().contains(kw)
                    || u.getEmail().toLowerCase().contains(kw)) {
                result.add(u);
            }
        }
        return result;
    }

    /** UPDATE — Replace user by ID */
    public static boolean updateUser(User updated) {
        List<User> users = readAllUsers();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(updated.getId())) {
                users.set(i, updated);
                writeAllUsers(users);
                return true;
            }
        }
        return false;
    }

    /** DELETE — Remove user by ID */
    public static boolean deleteUser(String id) {
        List<User> users = readAllUsers();
        boolean removed = users.removeIf(u -> u.getId().equals(id));
        if (removed) writeAllUsers(users);
        return removed;
    }

    /** Generate unique user ID */
    public static String generateId(String role) {
        String prefix = switch (role) {
            case "ADMIN"      -> "ADM";
            case "BIKE_OWNER" -> "OWN";
            default           -> "RNT";
        };
        return prefix + System.currentTimeMillis();
    }
}