
package service;

import model.User;
import java.util.*;

public class WaitlistService {

    // Each resource has its own waiting queue
    private final HashMap<Integer, Queue<User>> waitlists =
            new HashMap<>();

    // Add a user to the waiting list
    public void addToWaitlist(int resourceId, User user) {

        if (user == null) {
            System.out.println("Invalid user!");
            return;
        }

        // Create a queue if it doesn't already exist
        Queue<User> queue = waitlists.computeIfAbsent(
                resourceId, id -> new LinkedList<>()
        );

        // Prevent duplicate requests from the same user
        for (User existingUser : queue) {
            if (existingUser.getUserId() == user.getUserId()) {
                System.out.println("You are already in the waiting list!");
                return;
            }
        }

        // Add user at the end of the queue
        queue.offer(user);

        System.out.println("Added to waiting list successfully!");
    }

    // Get the next user in the waiting list
    public User getNextUser(int resourceId) {

        Queue<User> queue = waitlists.get(resourceId);

        if (queue == null || queue.isEmpty()) {
            return null;
        }

        // Remove and return the first user
        User nextUser = queue.poll();

        // Remove empty queue from the map
        if (queue.isEmpty()) {
            waitlists.remove(resourceId);
        }

        return nextUser;
    }

    // Display waiting list for a particular resource
    public void viewWaitlist(int resourceId) {

        Queue<User> queue = waitlists.get(resourceId);

        if (queue == null || queue.isEmpty()) {
            System.out.println("No users in the waiting list.");
            return;
        }

        System.out.println("Waiting List for Resource ID: " + resourceId);

        int position = 1;

        for (User user : queue) {
            System.out.println(
                    position++ + ". " + user.getName()
            );
        }
    }

    // Check the number of users waiting
    public int getWaitlistSize(int resourceId) {

        Queue<User> queue = waitlists.get(resourceId);

        return (queue == null) ? 0 : queue.size();
    }
}
