package model;

// Inheritance: HazardousWaste extends the abstract Waste class.
public class HazardousWaste extends Waste {
    public HazardousWaste() {
    }

    public HazardousWaste(int id, String description, double quantity, String location, String status) {
        super(id, description, quantity, location, status);
    }

    @Override
    public String getWasteCategory() {
        return "Hazardous";
    }
}
