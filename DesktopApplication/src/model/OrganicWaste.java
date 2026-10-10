package model;

// Inheritance: OrganicWaste extends the abstract Waste class.
public class OrganicWaste extends Waste {
    public OrganicWaste() {
    }

    public OrganicWaste(int id, String description, double quantity, String location, String status) {
        super(id, description, quantity, location, status);
    }

    @Override
    public String getWasteCategory() {
        return "Organic";
    }
}
