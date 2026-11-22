// 17.11.2026- AKSHITA KAUSHIK
//22.11.2026- ISHITA
package service;
import model.User;
import model.Resource;
import java.util.List;
import java.util.Scanner;
public class MemberDashboard {
    private ResourceService resourceService;
    private BookingService bookingService;

    public MemberDashboard(ResourceService resourceService,BookingService bookingService) {
        this.resourceService = resourceService;
        this.bookingService = bookingService;
    }
    public void showDashboard(User user, Scanner sc) {
        while (true) {
            System.out.println("\n================================");
            System.out.println("             LOOP IT");
            System.out.println("        MEMBER DASHBOARD");
            System.out.println("================================");
            System.out.println("Welcome, " + user.getName() + "!");
            System.out.println("--------------------------------");
            System.out.println("1. Browse Resources");
            System.out.println("2. Search Resources");
            System.out.println("3. Lend a Resource");
            System.out.println("4. Borrow a Resource");
            System.out.println("5. Return a Resource");
            System.out.println("6. My Borrowing History");
            System.out.println("7. Sort Resources");
            System.out.println("8. Logout");
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
                    resourceService.viewResources();
                    break;
                case 2:
                    System.out.print("Enter resource name/category/keyword: ");
                    String keyword = sc.nextLine();

                    List<Resource> results =
                            resourceService.searchResource(keyword);

                    if (results.isEmpty()) {
                        System.out.println("No matching resources found.");
                    } else {
                        for (Resource resource : results) {
                            System.out.println(
                                    resource.getResourceId() + " | "
                                            + resource.getName() + " | "
                                            + resource.getCategory()
                            );
                        }
                    }
                    break;
                case 3:
                    System.out.print("Enter resource name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter category: ");
                    String category = sc.nextLine();
                    System.out.print("Enter description: ");
                    String description = sc.nextLine();
                    int resourceId = resourceService.generateResourceId();
                    Resource resource = new Resource(
                            resourceId,
                            name,
                            category,
                            description,
                            user.getUserId(),
                            true
                    );

                    resourceService.addResource(resource, user);
                    break;
                case 4:
                    System.out.println("\n--- BORROW RESOURCE ---");
                    resourceService.viewResources();
                    System.out.print("Enter Resource ID to borrow: ");
                    try {
                        int borrowId = Integer.parseInt(sc.nextLine());
                        bookingService.borrowResource(user, borrowId);
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid Resource ID!");
                    }
                    break;
                case 5:
                    System.out.println("\n--- RETURN RESOURCE ---");
                    bookingService.viewActiveBookings(user);
                    System.out.print("Enter Booking ID to return: ");
                    try {
                        int bookingId = Integer.parseInt(sc.nextLine());
                        bookingService.returnResource(bookingId, user);
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid Booking ID!");
                    }
                    break;
                case 6:
                    System.out.println("Borrowing History - Coming Soon!");
                    break;
                case 7: {
                    System.out.println("\n--- SORT RESOURCES ---");
                    System.out.println("1. Sort by Name");
                    System.out.println("2. Sort by Category");
                    System.out.println("3. Sort by Name (Descending)");
                    System.out.println("4. Sort by ID");

                    System.out.print("Enter sorting choice: ");

                    int sortChoice;

                    try {
                        sortChoice = Integer.parseInt(sc.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid choice!");
                        break;
                    }

                    List<Resource> sortedResources = null;

                    switch (sortChoice) {
                        case 1:
                            sortedResources = resourceService.sortResourcesByName();
                            break;
                        case 2:
                            sortedResources = resourceService.sortResourcesByCategory();
                            break;
                        case 3:
                            sortedResources = resourceService.sortResourcesByNameDescending();
                            break;
                        case 4:
                            sortedResources = resourceService.sortResourcesById();
                            break;
                        default:
                            System.out.println("Invalid sorting choice!");
                    }

                    if (sortedResources != null) {
                        for (Resource r : sortedResources) {
                            System.out.println(
                                    r.getResourceId() + " | "
                                            + r.getName() + " | "
                                            + r.getCategory()
                            );
                        }
                    }
                    break;
                }
                case 8:
                    System.out.println("Logged out successfully!");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}
