package model;

// Abstraction: Waste is abstract because a general "Waste" record needs a real category.
public abstract class Waste {
    // Encapsulation: fields are private and accessed through getters/setters.
    private int id;
    private String description;
    private double quantity;
    private String location;
    private String status;
    private String collectedBy;
    private String createdBy;
    private String collectedAt;
    private String invalidReason;
    private boolean completionRequested;
    private String completionRequestStatus;

    public Waste() {
    }

    public Waste(int id, String description, double quantity, String location, String status) {
        this.id = id;
        this.description = description;
        this.quantity = quantity;
        this.location = location;
        this.status = status;
    }

    // Polymorphism: each child class gives its own category.
    public abstract String getWasteCategory();

    public String getCollectedBy() { return collectedBy; }
    public void setCollectedBy(String collectedBy) { this.collectedBy = collectedBy; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getCollectedAt() { return collectedAt; }
    public void setCollectedAt(String collectedAt) { this.collectedAt = collectedAt; }
    public String getInvalidReason() { return invalidReason; }
    public void setInvalidReason(String invalidReason) { this.invalidReason = invalidReason; }
    public boolean isCompletionRequested() { return completionRequested; }
    public void setCompletionRequested(boolean completionRequested) { this.completionRequested = completionRequested; }
    public String getCompletionRequestStatus() { return completionRequestStatus; }
    public void setCompletionRequestStatus(String completionRequestStatus) { this.completionRequestStatus = completionRequestStatus; }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
