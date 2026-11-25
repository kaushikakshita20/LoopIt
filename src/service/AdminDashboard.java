// 20.11.2026 - AKSHITA KAUSHIK
//YASHIKA GUPTA-DATABASE CONNECTIVITY
// 23.11.2026 - AKSHITA KAUSHIK - CONNECTED WITH DATA FROM MEMBERS DASHBOARD
package service;
import model.User;
import model.Resource;
import java.util.Scanner;
import java.util.List;
public class AdminDashboard {
    private ResourceService resourceService;
    private BookingService bookingService;
    private UserService userService;
    public AdminDashboard(ResourceService resourceService, BookingService bookingService, UserService userService) {
        this.resourceService = resourceService;
        this.bookingService = bookingService;
        this.userService = userService;
    }
    public void showDashboard(User user, Scanner sc) {
        while (true) {
            System.out.println("\n================================");
            System.out.println("             LOOP IT");
            System.out.println("         ADMIN DASHBOARD");
            System.out.println("================================");
            System.out.println("Welcome, " + user.getName() + "!");
            System.out.println("--------------------------------");
            System.out.println("1. Add Resource");
            System.out.println("2. View All Resources");
            System.out.println("3. Search Resource");
            System.out.println("4. Update Resource");
            System.out.println("5. Delete Resource");
            System.out.println("6. View All Bookings");
            System.out.println("7. Manage Users");
            System.out.println("8. Generate Reports");
            System.out.println("9. Logout");
            System.out.println("================================");
            System.out.print("Enter your choice: ");
            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
                continue;
            }
            switch (choice) {
                case 1:
                    System.out.println("\n--- ADD NEW RESOURCE ---");
                    System.out.print("Enter resource name: ");
                    String resourceName = sc.nextLine();
                    System.out.print("Enter resource category: ");
                    String resourceCategory = sc.nextLine();
                    System.out.print("Enter resource description: ");
                    String resourceDescription = sc.nextLine();
                    if (resourceName.isBlank()
                            || resourceCategory.isBlank()
                            || resourceDescription.isBlank()) {
                        System.out.println("All fields are required!");
                        break;
                    }
                    Resource resource = new Resource(
                            0,
                            resourceName,
                            resourceCategory,
                            resourceDescription,
                            user.getUserId(),
                            true
                    );
                    resourceService.addResource(resource, user);
                    break;
                case 2:
                    System.out.println("\n--- ALL RESOURCES ---");
                    resourceService.viewResources();
                    break;
                case 3:
                    System.out.print("Enter resource name/category/keyword: ");
                    String keyword = sc.nextLine();
                    List<Resource> results = resourceService.searchResource(keyword);
                    if (results.isEmpty()) {
                        System.out.println("No matching resources found.");
                    } else {
                        System.out.println("\n--- SEARCH RESULTS ---");
                        for (Resource r : results) {
                            System.out.println(
                                    "ID: " + r.getResourceId()
                                            + " | Name: " + r.getName()
                                            + " | Category: " + r.getCategory()
                                            + " | Owner ID: " + r.getOwnerId()
                                            + " | Available: " + r.isAvailable()
                            );
                        }
                    }
                    break;
                case 4:
                    System.out.print("Enter Resource ID to update: ");
                    try {
                        int updateId = Integer.parseInt(sc.nextLine());

                        Resource existing =
                                resourceService.getResourceById(updateId);

                        if (existing == null) {
                            System.out.println("Resource not found!");
                            break;
                        }
                        System.out.print("Enter new name: ");
                        String name = sc.nextLine();

                        System.out.print("Enter new category: ");
                        String category = sc.nextLine();

                        System.out.print("Enter new description: ");
                        String description = sc.nextLine();

                        resourceService.updateResource(
                                updateId, name, category, description
                        );

                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid Resource ID!");
                    }
                    break;

                case 5:
                    System.out.print("Enter Resource ID to delete: ");

                    try {
                        int deleteId = Integer.parseInt(sc.nextLine());

                        resourceService.removeResource(deleteId);

                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid Resource ID!");
                    }
                    break;
                case 6:
                    System.out.println("\n--- ALL BOOKINGS ---");
                    bookingService.viewBookings();
                    break;
                case 7:

    while (true) {

        System.out.println("\n========== MANAGE USERS ==========");
        System.out.println("1. View All Users");
        System.out.println("2. Block User");
        System.out.println("3. Unblock User");
        System.out.println("4. Back");
        System.out.print("Enter your choice: ");

        int userChoice;

        try {
            userChoice = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number!");
            continue;
        }

        switch (userChoice) {

            case 1:
                userService.viewAllUsers();
                break;

            case 2:

                userService.viewAllUsers();

                System.out.print("Enter User ID to block: ");

                try {
                    int blockId = Integer.parseInt(sc.nextLine());
                    userService.blockUser(blockId);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid User ID!");
                }

                break;

            case 3:

                userService.viewAllUsers();

                System.out.print("Enter User ID to unblock: ");

                try {
                    int unblockId = Integer.parseInt(sc.nextLine());
                    userService.unblockUser(unblockId);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid User ID!");
                }

                break;

            case 4:
                break;

            default:
                System.out.println("Invalid choice!");
        }

        if (userChoice == 4) {
            break;
        }
    }

    break;

                case 8:
                    System.out.println("\n========== LOOP IT REPORT ==========");
                    int totalResources = 0;
                    int availableResources = 0;
                    int borrowedResources = 0;
                    for (Resource r : resourceService.getAllResources()) {
                        totalResources++;
                        if (r.isAvailable()) {
                            availableResources++;
                        } else {
                            borrowedResources++;
                        }
                    }
                    System.out.println("\n--- RESOURCE STATISTICS ---");
                    System.out.println("Total Resources: " + totalResources);
                    System.out.println("Available Resources: " + availableResources);
                    System.out.println("Currently Borrowed Resources: " + borrowedResources);
                    System.out.println("\n--- BOOKING STATISTICS ---");
                    System.out.println("Total Bookings: " + bookingService.getTotalBookingCount());
                    System.out.println("Active Bookings: " + bookingService.getActiveBookingCount());
                    System.out.println("Returned Bookings: " + bookingService.getReturnedBookingCount());
                    System.out.println("Overdue Bookings: " + bookingService.getOverdueBookingCount());
                    System.out.println("====================================");
                    break;
                case 9:
                    System.out.println("Admin logged out successfully!");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}
