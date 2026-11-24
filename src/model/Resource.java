//Ishita - 18/11/26
package model;

public class Resource implements Comparable<Resource> {
    private int resourceId;
    private String name;
    private String category;
    private String description;
    private int ownerId;
    private boolean available;
    private String borrowingType;
    private double pricePerDay;
    private double securityDeposit;

    public Resource(int resourceId, String name, String category,
                    String description, int ownerId, boolean available) {

        this.resourceId = resourceId;
        this.name = name;
        this.category = category;
        this.description = description;
        this.ownerId = ownerId;
        this.available = available;
        this.borrowingType = "FREE";
        this.pricePerDay = 0;
        this.securityDeposit = 0;
    }

    public Resource(int resourceId, String name, String category,
                    String description, int ownerId, boolean available,
                    String borrowingType, double pricePerDay,
                    double securityDeposit) {

        this.resourceId = resourceId;
        this.name = name;
        this.category = category;
        this.description = description;
        this.ownerId = ownerId;
        this.available = available;
        this.borrowingType = borrowingType;
        this.pricePerDay = pricePerDay;
        this.securityDeposit = securityDeposit;
    }
    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
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

    public String getBorrowingType() {

        return borrowingType;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public double getSecurityDeposit() {
        return securityDeposit;
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

    public void setBorrowingType(String borrowingType) {
        this.borrowingType = borrowingType;
    }

    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public void setSecurityDeposit(double securityDeposit) {
        this.securityDeposit = securityDeposit;
    }


    public boolean isValidPricing() {

        if (borrowingType == null ||
                !Double.isFinite(pricePerDay) ||
                !Double.isFinite(securityDeposit) ||
                pricePerDay < 0 ||
                securityDeposit < 0) {
            return false;
        }

        if ("FREE".equalsIgnoreCase(borrowingType.trim())) {
            return pricePerDay == 0 && securityDeposit == 0;
        }

        if ("PAID".equalsIgnoreCase(borrowingType.trim())) {
            return pricePerDay > 0;
        }

        return false;
    }


    @Override
    public int compareTo(Resource other) {
        return this.name.compareToIgnoreCase(other.name);
    }
}
