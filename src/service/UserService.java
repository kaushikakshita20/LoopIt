package service;

// 20.11.2026 - AKSHITA KAUSHIK
import model.User;
import model.Role;
import java.util.HashMap;
import java.util.Locale;

public class UserService {
    private final HashMap<String, User> users = new HashMap<>();
    private int nextUserId = 1;
    public UserService() {
        User admin = new User(
                nextUserId++,
                "Loop It Admin",
                "admin@loopit.com",
                "Admin@123",
                Role.ADMIN);
        users.put(admin.getEmail(), admin);
    }
    public boolean register(String name, String email, String password, String confirmPassword) {
        if (name == null || email == null || password == null || confirmPassword == null) {
            System.out.println("All fields are required!");
            return false;
        }
        name = name.trim();
        email = email.trim().toLowerCase(Locale.ROOT);
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            System.out.println("All fields are required!");
            return false;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            System.out.println("Invalid email!");
            return false;
        }
        if (users.containsKey(email)) {
            System.out.println("Email already registered!");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            System.out.println("Passwords do not match!");
            return false;
        }
        User user = new User(
                nextUserId++,
                name,
                email,
                password,
                Role.MEMBER);
        users.put(email, user);
        System.out.println("Account created successfully!");
        System.out.println("Your User ID is: " + user.getUserId());
        return true;
    }
    public User login(String email, String password) {
        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            System.out.println("Email and password are required!");
            return null;
        }
        email = email.trim().toLowerCase(Locale.ROOT);
        User user = users.get(email);
        if (user == null) {
            System.out.println("Account not found!");
            return null;
        }
        if (!user.getPassword().equals(password)) {
            System.out.println("Incorrect password!");
            return null;
        }
        System.out.println("Login successful!");
        return user;
    }
}
