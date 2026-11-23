// RAJNI
package service;
import model.User;
import java.util.*;
public class WaitlistService {
    private final HashMap<Integer, Queue<User>> waitlists =
            new HashMap<>();
    public synchronized void addToWaitlist(int resourceId, User user) {
        if (user == null) {
            System.out.println("Invalid user!");
            return;
        }
        Queue<User> queue = waitlists.computeIfAbsent(
                resourceId,
                id -> new LinkedList<>()
        );
        for (User existingUser : queue) {
            if (existingUser.getUserId() == user.getUserId()) {
                System.out.println("You are already in the waiting list!");
                return;
            }
        }
        queue.offer(user);
        System.out.println(
                "Added to waiting list successfully!"
        );
    }
    public synchronized User getNextUser(int resourceId) {
        Queue<User> queue = waitlists.get(resourceId);
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        User nextUser = queue.poll();
        if (queue.isEmpty()) {
            waitlists.remove(resourceId);
        }
        return nextUser;
    }
    public synchronized void viewWaitlist(int resourceId) {
        Queue<User> queue = waitlists.get(resourceId);
        if (queue == null || queue.isEmpty()) {
            System.out.println(
                    "No users in the waiting list."
            );
            return;
        }
        System.out.println("Waiting List for Resource ID: " + resourceId);
        int position = 1;
        for (User user : queue) {
            System.out.println(position++ + ". " + user.getName());
        }
    }
    public synchronized int getWaitlistSize(int resourceId) {
        Queue<User> queue = waitlists.get(resourceId);
        return (queue == null) ? 0 : queue.size();
    }
}