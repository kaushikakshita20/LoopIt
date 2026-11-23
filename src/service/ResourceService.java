
//ISHITA

package service;

import model.Resource;
import model.User;

import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class ResourceService {

    private LinkedHashMap<Integer, Resource> resources =
            new LinkedHashMap<>();

    private HashSet<String> categories = new HashSet<>();
    private int nextResourceId = 1;

    public int generateResourceId() {
        return nextResourceId++;
    }

    public boolean addResource(Resource resource, User owner) {

        if (resource == null || owner == null) {
            System.out.println("Invalid resource or owner!");
            return false;
        }

        if (resources.containsKey(resource.getResourceId())) {
            System.out.println("Resource already exists!");
            return false;
        }

        if (resource.getOwnerId() != owner.getUserId()) {
            System.out.println("Invalid resource owner!");
            return false;
        }

        if (resource.getName() == null ||
                resource.getName().isBlank()) {
            System.out.println("Invalid resource name!");
            return false;
        }

        if (resource.getCategory() == null ||
                resource.getCategory().isBlank()) {
            System.out.println("Invalid category!");
            return false;
        }

        if (!resource.isValidPricing()) {
            System.out.println("Invalid borrowing type or pricing details!");
            return false;
        }

        resources.put(resource.getResourceId(), resource);
        categories.add(resource.getCategory());

        System.out.println("Resource uploaded successfully!");
        return true;
    }


    public void viewResources() {

        if (resources.isEmpty()) {
            System.out.println("No resources available.");
            return;
        }

        for (Resource resource : resources.values()) {

            System.out.println(
                    "ID: " + resource.getResourceId()
                            + " | Name: " + resource.getName()
                            + " | Category: " + resource.getCategory()
                            + " | Owner ID: " + resource.getOwnerId()
                            + " | Available: " + resource.isAvailable()
                            + " | Type: " + resource.getBorrowingType()
                            + " | Price/Day: Rs. " + resource.getPricePerDay()
                            + " | Deposit: Rs. " + resource.getSecurityDeposit()
            );
        }
    }




    public Resource getResourceById(int id) {
        return resources.get(id);
    }


    public List<Resource> getResourcesByOwner(int ownerId) {

        List<Resource> result = new ArrayList<>();

        for (Resource resource : resources.values()) {
            if (resource.getOwnerId() == ownerId) {
                result.add(resource);
            }
        }

        return result;
    }


    public List<Resource> searchResource(String keyword) {

        List<Resource> result = new ArrayList<>();

        if (keyword == null) {
            return result;
        }

        keyword = keyword.trim().toLowerCase();

        if (keyword.isEmpty()) {
            return result;
        }

        for (Resource resource : resources.values()) {

            String name = resource.getName() == null
                    ? "" : resource.getName().toLowerCase();

            String category = resource.getCategory() == null
                    ? "" : resource.getCategory().toLowerCase();

            String description = resource.getDescription() == null
                    ? "" : resource.getDescription().toLowerCase();

            if (name.contains(keyword)
                    || category.contains(keyword)
                    || description.contains(keyword)) {

                result.add(resource);
            }
        }

        return result;
    }


    public boolean updateResource(int id, String name,
                                  String category, String description) {

        Resource resource = resources.get(id);

        if (resource == null) {
            System.out.println("Resource not found!");
            return false;
        }

        if (name == null || name.isBlank()) {
            System.out.println("Resource name cannot be empty!");
            return false;
        }

        if (category == null || category.isBlank()) {
            System.out.println("Resource category cannot be empty!");
            return false;
        }

        if (description == null || description.isBlank()) {
            System.out.println("Resource description cannot be empty!");
            return false;
        }

        resource.setName(name);
        resource.setCategory(category);
        resource.setDescription(description);

        refreshCategories();

        System.out.println("Resource updated successfully!");
        return true;
    }


    public boolean removeResource(int id) {

        Resource removed = resources.remove(id);

        if (removed == null) {
            System.out.println("Resource not found!");
            return false;
        }

        refreshCategories();

        System.out.println("Resource removed successfully!");
        return true;
    }


    private void refreshCategories() {

        categories.clear();

        for (Resource resource : resources.values()) {
            categories.add(resource.getCategory());
        }
    }


    public void viewCategories() {
        System.out.println("Categories: " + categories);
    }


    public List<Resource> sortResourcesByName() {

        List<Resource> sortedResources =
                new ArrayList<>(resources.values());

        Collections.sort(sortedResources);

        return sortedResources;
    }


    public List<Resource> sortResourcesByCategory() {

        List<Resource> sortedResources =
                new ArrayList<>(resources.values());

        sortedResources.sort(
                Comparator.comparing(
                        Resource::getCategory,
                        String.CASE_INSENSITIVE_ORDER
                )
        );

        return sortedResources;
    }


    public List<Resource> sortResourcesByNameDescending() {

        List<Resource> sortedResources =
                new ArrayList<>(resources.values());

        sortedResources.sort(
                Comparator.comparing(
                        Resource::getName,
                        String.CASE_INSENSITIVE_ORDER
                ).reversed()
        );

        return sortedResources;
    }


    public List<Resource> sortResourcesById() {

        List<Resource> sortedResources =
                new ArrayList<>(resources.values());

        sortedResources.sort(
                Comparator.comparingInt(Resource::getResourceId)
        );

        return sortedResources;
    }


    public List<Resource> getAvailableResources() {

        List<Resource> availableResources = new ArrayList<>();

        for (Resource resource : resources.values()) {

            if (resource.isAvailable()) {
                availableResources.add(resource);
            }
        }

        return availableResources;
    }


    public Collection<Resource> getAllResources() {

        return new ArrayList<>(resources.values());
    }
}
