//Ishita - 18/11/26
package model;

public class Resource {
    private int resourceId;
    private String name;
    private String category;
    private String description;
    private int ownerId;
    private boolean available;
    public Resource(int resourceId, String name, String category,
                    String description, int ownerId, boolean available) {

        this.resourceId = resourceId;
        this.name = name;
        this.category = category;
        this.description = description;
        this.ownerId = ownerId;
        this.available = available;
    }
    public int getResourceId() {
        return resourceId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public int getOwnerId() {
        return ownerId;
    }


    public boolean isAvailable() {
        return available;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
