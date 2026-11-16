// 16.11.2026 - AKSHITA KAUSHIK
package service;
import model.User;
import java.util.*;
public class UserService {
    private HashMap<String, User> users = new HashMap<>();
    private int nextUserId = 1;
    public boolean register(String name, String email, String password, String confirmPassword) {
        email = email.trim().toLowerCase();
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            System.out.println("All fields are required!");
            return false;
        }
        if (!email.contains("@") || !email.contains(".")) {
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
        User user = new User(nextUserId++,name, email, password, "MEMBER");
        users.put(email, user);
        System.out.println("Account created successfully!");
        return true;
    }
    // Login
    public User login(String email, String password) {
        email = email.trim().toLowerCase();
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
